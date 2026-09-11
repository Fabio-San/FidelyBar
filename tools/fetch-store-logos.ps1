# FidelyBar — fetch-store-logos.ps1
#
# Scarica i loghi ufficiali delle catene presenti nella lista Wikipedia
# "List of supermarket chains in Italy" e li salva come risorse bundle
# in app/src/main/res/drawable/logo_<storeId>.<ext>.
#
# Fonte: https://en.wikipedia.org/wiki/List_of_supermarket_chains_in_Italy (CC BY-SA 4.0)
# Licenza dei file immagine: vedere la pagina file su Wikimedia Commons.
# Attribuzione in-app richiesta (schermata Credit).
#
# Uso:  pwsh tools/fetch-store-logos.ps1

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$catalogPath = Join-Path $root 'app/src/main/java/com/card/fidelybar/data/StoreCatalog.kt'
$outputDir = Join-Path $root 'app/src/main/res/drawable'
$userAgent = 'FidelyBar/1.3.0 build-tool (contact: dev@example.invalid)'
$page = 'List_of_supermarket_chains_in_Italy'

# --- 1. Leggi StoreCatalog.kt: id -> name --------------------------------
$catalogSrc = Get-Content -Raw -LiteralPath $catalogPath
$pairs = [regex]::Matches($catalogSrc, 'StorePreset\("([^"]+)",\s*"([^"]+)"')
$catalog = @{}
foreach ($m in $pairs) {
    $id = $m.Groups[1].Value
    $name = $m.Groups[2].Value
    $catalog[$name] = $id
}

function Normalize([string]$s) {
    if (-not $s) { return '' }
    $s = $s.ToLowerInvariant()
    $s = $s.Normalize([System.Text.NormalizationForm]::FormD)
    $sb = [System.Text.StringBuilder]::new()
    foreach ($ch in $s.ToCharArray()) {
        if ([System.Globalization.CharUnicodeInfo]::GetUnicodeCategory($ch) -ne [System.Globalization.UnicodeCategory]::NonSpacingMark) {
            [void]$sb.Append($ch)
        }
    }
    $s = $sb.ToString()
    $s = $s -replace '''', '' -replace '[^a-z0-9 ]', ' ' -replace '\s+', ' ' -replace '^\s+|\s+$', ''
    return $s
}

$normalizedToId = @{}
foreach ($name in $catalog.Keys) {
    $normalizedToId[(Normalize $name)] = $catalog[$name]
}
# alias per nomi diversi tra lista Wikipedia e catalogo
$aliases = @{
    'lidl'        = 'lidl'
    'aldil'       = 'lidl'
}
$found = @{}
$unmatched = [System.Collections.Generic.List[string]]::new()
$notInCatalog = [System.Collections.Generic.List[string]]::new()

# --- 2. Fetch HTML della lista --------------------------------------------
$apiUrl = "https://en.wikipedia.org/w/api.php?action=parse&page=$page&prop=text&format=json&formatversion=2"
$html = (Invoke-RestMethod -Uri $apiUrl -Headers @{ 'User-Agent' = $userAgent }).parse.text

$rows = [regex]::Matches($html, '<tr>.*?</tr>', [System.Text.RegularExpressions.RegexOptions]::Singleline)
foreach ($row in $rows) {
    $cells = [regex]::Matches($row.Value, '<td>.*?</td>', [System.Text.RegularExpressions.RegexOptions]::Singleline)
    if ($cells.Count -lt 2) { continue }

    # logo: primo <img> con src verso upload.wikimedia.org
    $imgMatch = [regex]::Match($cells[0].Value, '<img[^>]+src="([^"]+)"')
    # nome: seconda cella, tutto tag rimosso
    $nameHtml = [regex]::Replace($cells[1].Value, '<[^>]+>', ' ')
    $name = [System.Net.WebUtility]::HtmlDecode($nameHtml) -replace '\s+', ' ' | ForEach-Object { $_.Trim() }
    if (-not $name) { continue }

    if (-not $imgMatch.Success) {
        $unmatched.Add("$name (nessun logo su Wikipedia)")
        continue
    }
    $imgUrl = $imgMatch.Groups[1].Value -replace '^//', 'https://' -replace '\?.*$', ''

    $norm = Normalize $name
    $storeId = $null
    if ($normalizedToId.ContainsKey($norm)) { $storeId = $normalizedToId[$norm] }
    elseif ($aliases.ContainsKey($norm)) { $storeId = $aliases[$norm] }

    if ($storeId) {
        $found[$imgUrl] = "$storeId|$name"
    } else {
        $notInCatalog.Add("$name | $norm")
    }
}

Write-Host "=== Match ($($found.Count)) ==="
$found.GetEnumerator() | Sort-Object Value | ForEach-Object { Write-Host "  $($_.Value)" }

Write-Host ""
Write-Host "=== Nella lista Wikipedia ma NON nel catalogo ($($notInCatalog.Count)) ==="
$notInCatalog | ForEach-Object { Write-Host "  $_" }
Write-Host ""
Write-Host "=== Nella lista senza logo ($($unmatched.Count)) ==="
$unmatched | ForEach-Object { Write-Host "  $_" }

# --- 3. Download thumbnail 512px (fallback 320, 120) -----------------------
Write-Host ""
$ok = 0; $fail = 0
foreach ($imgUrl in $found.Keys) {
    $parts = $found[$imgUrl].Split('|')
    $id = $parts[0]; $name = $parts[1]

    $base = [regex]::Match($imgUrl, '/[^/]+$').Value.TrimStart('/')
    if ($base -match '^\d+px-(.+)$') { $fileBase = $Matches[1] } else { $fileBase = $base }
    $ext = ($fileBase -split '\.')[-1] -replace '[^a-z0-9]', ''
    if ($ext -notin @('png','jpg','jpeg','gif','webp','svg')) { $ext = 'png' }
    $target = Join-Path $outputDir "logo_$id.$ext"

    $url512 = [regex]::Replace($imgUrl, '/\d+px-(?=[^/]+$)', '/512px-')
    $url320 = [regex]::Replace($imgUrl, '/\d+px-(?=[^/]+$)', '/320px-')

    $saved = $false
    foreach ($url in @($url512, $url320, $imgUrl)) {
        try {
            Invoke-WebRequest -Uri $url -OutFile $target -Headers @{
                'User-Agent' = $userAgent
                'Accept' = 'image/*'
            } -TimeoutSec 30
            $saved = $true
            break
        } catch { continue }
    }
    if ($saved) {
        $kb = [math]::Round((Get-Item $target).Length / 1KB, 1)
        Write-Host "  ok   $name -> logo_$id.$ext (${kb} KB)"
        $ok++
    } else {
        Write-Host "  FAIL $name (download non riuscito)"
        $fail++
    }
}

Write-Host ""
Write-Host "Fatto: $ok loghi scaricati, $fail errori."
if (-not $ok) { exit 1 }