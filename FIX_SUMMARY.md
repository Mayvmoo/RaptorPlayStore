# Fix Summary - Order Creation Issue

## Probleem
POST requests naar `/orders.php` gaven de error "Order ID is verplicht" terwijl bij het aanmaken van een order geen orderId hoort te zijn.

## Oorzaak
1. **Oude versie in XAMPP**: De bestand in `/Applications/XAMPP/htdocs/raptor/Backend/orders.php` was ouder dan de versie in `/Users/sara/Desktop/Raptor/Raptor/Backend/orders.php`
2. **Routing logica fout**: De routing checkte op `!$hasOrderId` om CREATE requests te detecteren, maar de iOS app stuurt WEL een orderId mee (lokaal gegenereerd)

## Oplossing
1. **Bestand gesynchroniseerd**: Nieuwste versie gekopieerd naar XAMPP
2. **Routing logica aangepast**: CREATE requests worden nu gedetecteerd op basis van create fields (senderName, senderAddress, destinationAddress), ongeacht of er een orderId is
3. **handleCreate accepteert orderId**: De handleCreate functie accepteert een orderId als die wordt meegegeven, maar genereert een nieuwe als die te lang is of ontbreekt

## Test Resultaten
✅ Order aanmaken werkt nu correct
✅ Test suite: Order aangemaakt met orderId: 251124-J2SQH3

## Belangrijke wijzigingen
- Routing logica aangepast: `if ($hasCreateFields && !$hasOrderId)` → `if ($hasCreateFields)`
- Bestand gesynchroniseerd tussen Desktop en XAMPP directories
- handleCreate accepteert nu orderId van iOS app (zoals bedoeld)

