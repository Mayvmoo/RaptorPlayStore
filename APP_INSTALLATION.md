# App Installatie op Android Emulator

## Status
✅ **App is succesvol geïnstalleerd!**
- Package name: `com.example.raptor`
- Geïnstalleerd op: Pixel_9a(AVD) - 16

## App Vinden op Emulator

### Methode 1: Via App Drawer
1. Op de emulator, swipe omhoog vanaf de onderkant (of klik op het app drawer icoon)
2. Zoek naar **"Raptor"** in de lijst met apps
3. Klik op de app om te starten

### Methode 2: Via Zoeken
1. Op de emulator, swipe omhoog om het app drawer te openen
2. Gebruik de zoekbalk bovenaan
3. Typ **"Raptor"** of **"raptor"**
4. Klik op de app in de zoekresultaten

### Methode 3: Via Android Studio
1. In Android Studio, klik op de **Run** knop (groene play icoon)
2. Of: **Run** → **Run 'app'**
3. De app wordt automatisch gestart op de emulator

### Methode 4: Via Terminal (als adb beschikbaar is)
```bash
# Start de app
adb shell am start -n com.example.raptor/.ui.SplashActivity

# Of start de main activity
adb shell am start -n com.example.raptor/.MainActivity
```

## Als de App Niet Zichtbaar Is

### Oplossing 1: Emulator Herstarten
1. Sluit de emulator
2. Start de emulator opnieuw vanuit Android Studio
3. Installeer de app opnieuw: **Run** → **Run 'app'**

### Oplossing 2: App Opnieuw Installeren
In Android Studio:
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. **Run** → **Run 'app'**

### Oplossing 3: App Drawer Resetten
Op de emulator:
1. Ga naar **Settings** → **Apps**
2. Zoek naar **"Raptor"**
3. Als de app daar staat, is het geïnstalleerd
4. Ga terug naar home screen en open app drawer opnieuw

## App Info
- **Package**: com.example.raptor
- **Main Activity**: SplashActivity (startscherm)
- **Launcher Activity**: MainActivity (login/register)

## Troubleshooting

### App Crasht bij Start
- Controleer logcat in Android Studio voor error messages
- Zorg dat de backend server draait (XAMPP)
- Controleer network configuratie (10.0.2.2 voor emulator)

### App Verschijnt Niet
- Controleer of emulator draait: **Tools** → **Device Manager**
- Installeer opnieuw via **Run** → **Run 'app'**
- Check of app in Settings → Apps staat

