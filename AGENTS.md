# FidelyBar — Convetions

## Versioning
Semantic Versioning (`X.Y.Z`), baseline `1.0.0`.

- `versionName` = `X.Y.Z`
- `versionCode` = `X * 10000 + Y * 100 + Z` (es. `1.2.3` → `10203`)

Bump ON EVERY COMMIT that touches app code, sized by the change:

| Change | Bump |
|---|---|
| Bugfix, polish, minor UI testi, dipendenze, docs | patch: `Z + 1` |
| Nuova funzionalità / significativa UX | minor: `Y + 1, Z = 0` |
| Redesign, breaking change, rilascio Play Store | major: `X + 1, Y = 0, Z = 0` |

Non fare commit senza aver prima aggiornato `versionCode`/`versionName`
in `app/build.gradle.kts` (il versionCode deve sempre aumentare).

## Comandi
- Build debug: `.\gradlew.bat :app:assembleDebug`
- Compila solo Kotlin: `.\gradlew.bat :app:compileDebugKotlin`
- Commit convention: Conventional Commits, messaggi in italiano.

## App
- Wallet carte fedeltà offline-first. Niente Room: persistenza JSON in
  `fidelybar_cards.json` via `CardFileStore` + `StateFlow` nel ViewModel.
- Navigazione in-schermata (nessun NavHost): overlay full-screen per dettaglio
  carta, editor e impostazioni via `FullScreenHost`. Vedi `ui/FidelyBarApp.kt`.
- Kotlin 2.4.20, AGP 9.4.0, Compose BOM 2026.09.00, coil3 (SingletonImageLoader.Factory).
- Il file `fidelybar_cards.json` è escluso dal backup cloud (privacy): mai riabilitarlo
  rimuovendo gli `<exclude>` in `res/xml/backup_rules.xml` e `data_extraction_rules.xml`.