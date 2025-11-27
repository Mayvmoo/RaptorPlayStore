# Raptor Android App

Android versie van de Raptor bezorgservice app. Deze app is gebouwd met Kotlin en volgt dezelfde UI/UX als de iOS versie.

## Features

- ✅ Splash Screen met animaties
- ✅ Login & Registratie (multi-step form)
- ✅ Customer Dashboard met Google Maps
- ✅ Hamburger Menu (Navigation Drawer)
- ✅ Order History
- ✅ Profile Management
- ✅ Settings (Login/Security, Privacy, Help, About)
- ✅ Real-time Driver Tracking op kaart
- ✅ Order Flashcard voor geaccepteerde orders
- ✅ Route visualisatie (gouden lijn tussen driver en bestemming)
- ✅ Quick Order met Partners
- ✅ Schedule Picker voor geplande orders
- ✅ Payment View

## Technologie

- **Language**: Kotlin
- **UI**: XML Layouts + Material Design
- **Maps**: Google Maps API
- **Networking**: Retrofit + OkHttp
- **Architecture**: MVVM pattern
- **Coroutines**: Voor async operaties

## Setup

1. Clone deze repository
2. Open in Android Studio
3. Voeg je Google Maps API key toe aan `AndroidManifest.xml`
4. Configureer de backend URL in `NetworkModule.kt`
5. Build en run

## Backend

De app verbindt met een XAMPP PHP backend. Zorg dat de backend draait op:
- Emulator: `http://10.0.2.2/raptor/Backend/`
- Fysiek device: `http://[jouw-ip]/raptor/Backend/`

## Structuur

```
app/src/main/
├── java/com/example/raptor/
│   ├── models/          # Data models
│   ├── network/         # API service & Retrofit
│   ├── repositories/    # Data repositories
│   ├── ui/              # Activities & Fragments
│   │   ├── auth/        # Login & Register
│   │   ├── dashboard/   # Main dashboard
│   │   ├── orders/      # Order management
│   │   ├── profile/     # Profile & settings
│   │   └── settings/    # Settings screens
│   └── viewmodels/      # ViewModels
└── res/
    ├── layout/          # XML layouts
    ├── drawable/       # Icons & shapes
    └── values/         # Colors, strings, etc.
```

## Kleuren Thema

- **Primary Blue**: `#3366CC`
- **Gold**: `#D9A621`
- **Light Gold**: `#FFD700`

## License

Copyright © 2025 Raptor

