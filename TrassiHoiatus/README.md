# Trassihoiatus 0.1 – Androidi prototüüp

Androidi natiivrakendus metsamasina juhi hoiatamiseks lubatud liikumiskoridorist väljumise eel. Projekt sisaldab lähtekoodi, mitte valmis APK-d.

## Funktsioonid
- QGISist eksporditud **EPSG:4326 GeoJSON** Polygon/MultiPolygon import (FeatureCollection toetatud).
- Lubatud koridori piirini kauguse arvutamine lokaalses meetrilises lähenduses (sisepool positiivne, väljaspool negatiivne).
- Androidi **foreground location service** jälgib GPS-i ka lukustatud ekraanil, kuni süsteem teenust lubab.
- 5 m piirivöönd, kolm järjestikust mõõtmist enne piirihoiatust, 15 s hoiatuspaus.
- GPS-i raporteeritud horisontaalse täpsuse >12 m korral asukohahoiatus peatatakse ja antakse eraldi hoiatus.
- Helisignaal ja süsteemiteavitus.
- Ei vaja kaardi kuvamiseks internetti; rakenduses pole veel kaardivaadet.

## QGIS ettevalmistus
1. Loo 10 m laiuse trassi polügoonile 10 m puhver (kui ±10 m tähendab mõlemalt poolt trassi serva); vajadusel `Dissolve`.
2. Ekspordi tulemus **GeoJSON** formaadis, koordinaatsüsteem **EPSG:4326**.
3. Impordi fail rakenduse nupuga.

## Ehitamine
Ava kaust Android Studios (JDK 17, Android SDK 35). Gradle Sync ja **Build > Build APK(s)**. Projektil pole Gradle wrapperit; Android Studio saab kasutada paigaldatud Gradle'it või wrapperi luua.

## Piirangud ja riskid
- **Mitte kasutada ainsa meetmena looduskaitse piirangute järgimise tagamiseks.** Ei ole välitingimustes valideeritud.
- Täpsuslävend 12 m on ainult Androidi hinnang; metsas võivad esineda suuremad süstemaatilised vead.
- Kolm järjestikust mõõtmist ei ole statistiline GPS-silumine. Hoiatus võib hilineda, eriti kiiresti liikuval masinal.
- Teenuse taaskäivitus pärast telefoni restarti puudub; käivitada käsitsi iga tööpäeva alguses.
- Asukoht põhineb telefonil, mitte masina gabariitidel ega noole ulatusel.
- Androidi aku säästurežiimid võivad teenuse peatada. Vajalik kontroll igal telefonimudelil.
- GPS-signaali täieliku kadumise ajal ei teki automaatselt aegumishoiatust; see tuleb järgmises versioonis lisada.
- Importida tohib ainult usaldusväärseid, mõistliku suurusega GeoJSON-faile.
- Puuduvad kaardivaade, keskne andmeuuendus, logimine, töökorraldus ja automaattestid.

## Järgmised sammud
Ehitada APK, testida Android-seadmel, lisada GPS-signaali aegumise hoiatus ja diagnostikalogid, seejärel katsetada 3 masinaga ning mõõta valehäireid ja hilinemisi.
