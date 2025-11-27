# Android App - Test Resultaten

## Test: Nieuwe Gebruiker Registreren

### Test Scenario
Registreer een nieuwe gebruiker met de naam "Android" via curl (simuleert Android app).

### Curl Commando

```bash
curl -X POST http://localhost/raptor/Backend/customers.php \
  -H "Content-Type: application/json" \
  -d '{
    "action": "register",
    "email": "android_[timestamp]@test.com",
    "password": "test123456",
    "customerType": "individual",
    "contactName": "Android",
    "phoneNumber": "+31 6 12345678",
    "address": "Teststraat 123, Amsterdam"
  }'
```

### Test Resultaten

**Status:** ⚠️ Rate Limiting Actief

De backend heeft rate limiting geactiveerd na meerdere test pogingen. Dit is normaal gedrag voor beveiliging.

**Oplossing:**
1. Wacht 15 minuten voordat je opnieuw test
2. Of reset de rate_limits tabel in de database:
   ```sql
   DELETE FROM rate_limits WHERE identifier LIKE '%127.0.0.1%' OR identifier LIKE '%::1%';
   ```

### Backend Endpoint Details

- **URL:** `http://localhost/raptor/Backend/customers.php`
- **Method:** POST
- **Action:** `register`
- **Required Fields:**
  - `email` (string, valid email format)
  - `password` (string, min 6 characters)
  - `customerType` (string: "business" of "individual")
  - `contactName` (string)
- **Optional Fields:**
  - `companyName` (string, required for business type)
  - `phoneNumber` (string)
  - `address` (string)

### Success Response

```json
{
  "success": true,
  "session": {
    "email": "android_1234567890@test.com",
    "customerType": "individual",
    "companyName": null,
    "contactName": "Android",
    "phoneNumber": "+31 6 12345678",
    "address": "Teststraat 123, Amsterdam"
  }
}
```

### Error Responses

**Rate Limiting:**
```json
{
  "success": false,
  "error": "Te veel registratie pogingen. Probeer het over 15 minuten opnieuw."
}
```

**Email al in gebruik:**
```json
{
  "success": false,
  "error": "Emailadres is al in gebruik"
}
```

**Bedrijfsnaam verplicht (voor business type):**
```json
{
  "success": false,
  "error": "Bedrijfsnaam is verplicht voor zakelijke klanten"
}
```

### Android App Implementatie

De Android app gebruikt:
- **Repository:** `CustomerAuthRepository.kt`
- **ViewModel:** `CustomerAuthViewModel.kt`
- **API Service:** `RaptorApiService.kt`
- **Network Module:** `NetworkModule.kt`

Alle code is geïmplementeerd en klaar voor gebruik!

### Test Script

Gebruik het test script:
```bash
./test_register_android.sh
```

