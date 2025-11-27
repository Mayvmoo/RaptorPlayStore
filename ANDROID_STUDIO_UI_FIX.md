# Android Studio "System UI is not responding" Fix

## Probleem
Android Studio geeft de fout: "System UI is not responding"

## Oplossingen

### Oplossing 1: Android Studio Herstarten (Snelste)
1. Force quit Android Studio (Cmd+Q op macOS)
2. Herstart Android Studio
3. Open het project opnieuw

### Oplossing 2: Cache Invalideren
1. In Android Studio: File → Invalidate Caches / Restart
2. Selecteer: "Invalidate and Restart"
3. Wacht tot Android Studio opnieuw opstart

### Oplossing 3: Gradle Daemon Stoppen
Open Terminal en voer uit:
```bash
cd /Users/sara/AndroidStudioProjects/Raptor
./gradlew --stop
```

### Oplossing 4: Memory Settings Verhogen
1. Help → Edit Custom VM Options
2. Voeg toe of pas aan:
```
-Xms512m
-Xmx4096m
-XX:ReservedCodeCacheSize=1024m
```
3. Herstart Android Studio

### Oplossing 5: Android Studio Instellingen Resetten
Als niets werkt:
1. Sluit Android Studio
2. Verwijder cache directories:
```bash
rm -rf ~/Library/Caches/Google/AndroidStudio*
rm -rf ~/Library/Application\ Support/Google/AndroidStudio*/caches
```
3. Herstart Android Studio

### Oplossing 6: Project Opnieuw Importeren
Als het probleem specifiek bij dit project is:
1. Sluit Android Studio
2. Verwijder `.idea` folder in project:
```bash
cd /Users/sara/AndroidStudioProjects/Raptor
rm -rf .idea
```
3. Open project opnieuw in Android Studio

## Preventie
- Regelmatig Android Studio herstarten
- Cache invalideren na grote wijzigingen
- Genoeg RAM beschikbaar houden (minimaal 8GB)

