# FidelyBar

Portafoglio digitale delle carte fedeltà: le tue tessere dei supermercati e
dei negozi, sempre con te, anche offline. Un tap e sei alla cassa.

## Caratteristiche

- Gestisci le tue carte fedeltà in modalità offline-first
- Codici a barre e QR per un utilizzo rapido alla cassa
- Catalogo dei principali locali d'Italia (supermercati, abbigliamento, ecc.)
- Carta "Personalizzata" per qualsiasi ente non presente nel catalogo
- Personalizzazione completa di colori, gradazioni e intensità delle card e del tema
- Backup criptati con password, export/import in formato `.fid`
- Cifratura locale dei dati (AES-256-GCM)

## Privacy

FidelyBar è pensato **esclusivamente** per carte fedeltà.

Non inserire codici personali, IBAN, numeri di carte di credito o prepagate
o altri dati sensibili. Lo sviluppatore non si assume alcuna responsabilità
per l'uso improprio dell'applicazione.

I dati sono salvati solo in locale sul dispositivo, cifrati, ed esclusi dai
backup cloud automatici.

## Requisiti di build

- Kotlin 2.4.20
- AGP 9.4.0
- Compose BOM 2026.09.00
- Min SDK 28, Target SDK 37

## Build

```
.\gradlew.bat :app:assembleDebug
```

L'APK di debug sarà in `app/build/outputs/apk/debug/`.

## Licenza

Tutti i diritti riservati. Vedi il file [LICENSE](LICENSE) per i dettagli.