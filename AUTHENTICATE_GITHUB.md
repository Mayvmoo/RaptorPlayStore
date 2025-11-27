# GitHub Authenticatie voor Push

De repository bestaat al: **https://github.com/Mayvmoo/RaptorPlayStore**

Om de code te pushen, moet je authenticeren. Kies één van deze opties:

## Optie 1: Personal Access Token (Aanbevolen)

1. **Maak een Personal Access Token:**
   - Ga naar: https://github.com/settings/tokens
   - Klik "Generate new token" → "Generate new token (classic)"
   - Geef een naam: "Raptor Android Push"
   - Selecteer scope: **repo** (alle repo rechten)
   - Klik "Generate token"
   - **Kopieer de token** (je ziet hem maar één keer!)

2. **Push met token:**
   ```bash
   cd /Users/sara/AndroidStudioProjects/Raptor
   git push -u origin main
   ```
   - Wanneer om username gevraagd wordt: `Mayvmoo`
   - Wanneer om password gevraagd wordt: **plak je Personal Access Token** (niet je wachtwoord!)

## Optie 2: Via Android Studio

1. Open Android Studio
2. VCS → Git → Push
3. Klik op "Define remote" als dat nodig is
4. URL: `https://github.com/Mayvmoo/RaptorPlayStore.git`
5. Authenticeer met je GitHub credentials

## Optie 3: GitHub Desktop

1. Installeer GitHub Desktop
2. File → Add Local Repository
3. Selecteer `/Users/sara/AndroidStudioProjects/Raptor`
4. Klik "Publish repository"

## Na succesvol pushen

De app is dan beschikbaar op:
**https://github.com/Mayvmoo/RaptorPlayStore**

Downloaden:
```bash
git clone https://github.com/Mayvmoo/RaptorPlayStore.git
```

