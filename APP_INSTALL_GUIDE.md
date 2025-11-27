# App Installeren op Android Emulator

## Probleem
De app staat niet in de Android simulator.

## Oplossing: App Installeren via Android Studio

### Stap 1: Start Android Emulator
1. Open **Android Studio**
2. Klik op **Device Manager** (rechtsboven, of Tools → Device Manager)
3. Klik op **▶️ Play** naast een emulator om deze te starten
   - Als er geen emulator is: klik op **Create Device** en maak er een aan
4. Wacht tot de emulator volledig is opgestart (zie het Android homescreen)

### Stap 2: Run de App
1. Zorg dat je project is geopend in Android Studio
2. Klik op de **▶️ Run** knop (groene play button) in de toolbar
   - Of druk op `Shift + F10` (Windows/Linux) of `Ctrl + R` (macOS)
3. Selecteer de emulator in het dropdown menu
4. Klik op **OK**

### Stap 3: Wacht op Installatie
- Android Studio zal nu:
  1. De app builden (als dat nog niet is gebeurd)
  2. De APK installeren op de emulator
  3. De app automatisch starten

### Stap 4: App Vinden op Emulator
Na installatie zou je de app moeten zien:
- **Automatisch**: De app start automatisch na installatie
- **Handmatig**: Zoek in de app drawer naar **"Raptor"** of het app icoon

## Alternatief: Via Terminal (als emulator draait)

Als de emulator al draait, kun je ook via terminal installeren:

```bash
cd /Users/sara/AndroidStudioProjects/Raptor
./gradlew installDebug
```

## Troubleshooting

### Emulator start niet
- Controleer of **Android Virtual Device (AVD)** Manager een emulator heeft
- Maak een nieuwe emulator aan als er geen is
- Zorg voor voldoende RAM (minimaal 4GB beschikbaar)

### App start niet
- Controleer **Logcat** in Android Studio voor errors
- Zorg dat de emulator volledig is opgestart voordat je de app installeert
- Probeer de emulator opnieuw te starten

### App is geïnstalleerd maar niet zichtbaar
- Check de app drawer (swipe up op het homescreen)
- Zoek naar "Raptor" in de app lijst
- Controleer of de app niet is verborgen

## App Naam en Icoon
- **App naam**: "Raptor" (zoals geconfigureerd in `strings.xml`)
- **Launcher Activity**: `SplashActivity` (startscherm)

## Verificatie
Na installatie zou je moeten zien:
1. ✅ App icoon in app drawer
2. ✅ App start met splash screen
3. ✅ Login/Register scherm verschijnt

