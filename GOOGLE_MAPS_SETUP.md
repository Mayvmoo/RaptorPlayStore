# Google Maps Setup - Android Raptor App

## ✅ API Key Toegevoegd

De Google Maps API key is toegevoegd aan `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSyDHeGBqSsjBYOrEEQydgXxAIlGeqpR4Km8" />
```

## 📍 Maps Functionaliteit

De app gebruikt Google Maps voor:
- **Driver Locaties** - Real-time locaties van bezorgers
- **Route Tracking** - Route van bezorger naar bestemming
- **Order Tracking** - Volg je order op de kaart

## 🔧 Vereisten

1. **Google Maps API Key** - ✅ Toegevoegd
2. **Maps SDK** - ✅ Dependency toegevoegd (play-services-maps 19.0.0)
3. **Location Permissions** - ✅ Toegevoegd aan manifest
4. **Location Services** - ✅ Dependency toegevoegd (play-services-location 21.0.1)

## 🗺️ Gebruik

De Maps functionaliteit is beschikbaar via:
- **OrderDetailActivity** - "Bekijk op Kaart" button (wanneer order assigned is)
- Toont driver locatie en route naar bestemming
- Updates elke 15 seconden

## ⚠️ Belangrijk

Zorg ervoor dat de Google Maps API key de juiste restricties heeft:
- **Android apps** toegestaan
- **Maps SDK for Android** API ingeschakeld
- **Geocoding API** ingeschakeld (voor adres naar coördinaten)

## 🧪 Testen

1. Open een order die assigned is aan een bezorger
2. Klik op "Bekijk op Kaart"
3. De kaart zou moeten laden met driver locatie en route

