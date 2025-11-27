# Push naar GitHub - Raptor-Playstore

## Optie 1: Via GitHub Website (Aanbevolen)

1. **Maak repository aan:**
   - Ga naar https://github.com/new
   - Repository name: `Raptor-Playstore`
   - Description: "Android versie van de Raptor bezorgservice app"
   - Kies **Public** of **Private**
   - **NIET** "Initialize with README" aanvinken
   - Klik **Create repository**

2. **Push code:**
   ```bash
   cd /Users/sara/AndroidStudioProjects/Raptor
   git remote remove origin 2>/dev/null
   git remote add origin https://github.com/Mayvmoo/Raptor-Playstore.git
   git branch -M main
   git push -u origin main
   ```

## Optie 2: Via GitHub CLI (als geïnstalleerd)

```bash
cd /Users/sara/AndroidStudioProjects/Raptor
gh repo create Raptor-Playstore --public --source=. --remote=origin --push
```

## Optie 3: Via Personal Access Token

Als je een GitHub Personal Access Token hebt:

```bash
cd /Users/sara/AndroidStudioProjects/Raptor

# Maak repository aan
curl -X POST \
  -H "Authorization: token YOUR_TOKEN_HERE" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/user/repos \
  -d '{"name":"Raptor-Playstore","description":"Android versie van de Raptor bezorgservice app","private":false}'

# Push code
git remote remove origin 2>/dev/null
git remote add origin https://github.com/Mayvmoo/Raptor-Playstore.git
git branch -M main
git push -u origin main
```

## Na het pushen

De app is dan beschikbaar op:
**https://github.com/Mayvmoo/Raptor-Playstore**

Je kunt de code downloaden via:
```bash
git clone https://github.com/Mayvmoo/Raptor-Playstore.git
```

Of download als ZIP via de GitHub website.

