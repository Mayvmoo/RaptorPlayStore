# Gradle Network Fix

## Probleem
Gradle kan `dl.google.com` niet bereiken met de error:
```
Unknown host 'dl.google.com: nodename nor servname provided, or not known'
```

## Oplossingen Toegepast

### 1. Network Timeout Verhoogd
- `gradle.properties`: HTTP/HTTPS timeouts verhoogd naar 60 seconden
- `gradle-wrapper.properties`: Network timeout verhoogd naar 60 seconden

### 2. Alternatieve Oplossingen

#### Optie A: Retry de Build
Probeer de build opnieuw - het kan een tijdelijk netwerkprobleem zijn.

#### Optie B: Offline Mode (als alle dependencies al gedownload zijn)
In Android Studio:
1. File → Settings → Build, Execution, Deployment → Gradle
2. Vink "Offline work" aan
3. Sync project

#### Optie C: Gradle Daemon Herstarten
```bash
cd /Users/sara/AndroidStudioProjects/Raptor
./gradlew --stop
./gradlew build
```

#### Optie D: Proxy Instellingen (als je achter een proxy zit)
Voeg toe aan `gradle.properties`:
```properties
systemProp.http.proxyHost=your.proxy.host
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=your.proxy.host
systemProp.https.proxyPort=8080
```

#### Optie E: DNS Cache Leegmaken (macOS)
```bash
sudo dscacheutil -flushcache
sudo killall -HUP mDNSResponder
```

## Test
Na de wijzigingen, probeer opnieuw te builden in Android Studio.

