# Offline Mode Uitschakelen - Stap voor Stap

## Probleem
Gradle kan `kotlin-build-tools-impl:2.0.21` niet vinden omdat offline mode aan staat.

## Oplossing: Offline Mode Uitschakelen in Android Studio

### Stap 1: Open Gradle Settings
1. In Android Studio: **File** → **Settings** (of **Android Studio** → **Preferences** op macOS)
   - Sneltoets: `Cmd + ,` (macOS) of `Ctrl + Alt + S` (Windows/Linux)

### Stap 2: Navigeer naar Gradle
1. In het linker menu: **Build, Execution, Deployment**
2. Klik op **Gradle**

### Stap 3: Schakel Offline Mode Uit
1. Zoek naar de optie **"Offline work"** (of **"Offline mode"**)
2. **Zet het vinkje UIT** (uncheck)
3. Klik op **Apply**
4. Klik op **OK**

### Stap 4: Sync Project
1. Klik op **"Sync Project with Gradle Files"** (elephant icoon in de toolbar)
   - Of: **File** → **Sync Project with Gradle Files**
2. Wacht tot Gradle sync klaar is (zie progress bar onderaan)

### Stap 5: Build Opnieuw
1. **Build** → **Rebuild Project**
2. Of: **Build** → **Make Project**

## Alternatief: Via Gradle Tool Window
1. Open **Gradle** tool window (rechts in Android Studio)
2. Klik op het **⚙️ Settings** icoon bovenaan
3. Zet **"Offline mode"** uit
4. Sync project opnieuw

## Verificatie
Na het uitschakelen van offline mode, zou Gradle automatisch de ontbrekende dependencies moeten downloaden, inclusief:
- `kotlin-build-tools-impl:2.0.21`
- Andere ontbrekende dependencies

## Als het nog steeds niet werkt
1. **File** → **Invalidate Caches / Restart** → **Invalidate and Restart**
2. Wacht tot Android Studio opnieuw opstart
3. Sync project opnieuw

