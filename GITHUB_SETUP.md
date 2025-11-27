# GitHub Setup Instructies

De Android app is klaar om naar GitHub te pushen. Volg deze stappen:

## Stap 1: Maak een GitHub Repository aan

1. Ga naar [GitHub.com](https://github.com) en log in
2. Klik op het **+** icoon rechtsboven → **New repository**
3. Vul in:
   - **Repository name**: `Raptor-Android` (of een andere naam)
   - **Description**: "Android versie van de Raptor bezorgservice app"
   - **Visibility**: Kies Public of Private
   - **NIET** "Initialize with README" aanvinken (we hebben al een README)
4. Klik op **Create repository**

## Stap 2: Push naar GitHub

Open Terminal en voer deze commando's uit:

```bash
cd /Users/sara/AndroidStudioProjects/Raptor

# Voeg de GitHub remote toe (vervang USERNAME en REPO_NAME)
git remote add origin https://github.com/USERNAME/REPO_NAME.git

# Push naar GitHub
git branch -M main
git push -u origin main
```

**Vervang:**
- `USERNAME` met je GitHub gebruikersnaam
- `REPO_NAME` met de naam van je repository (bijv. `Raptor-Android`)

## Stap 3: Authenticatie

Als GitHub om authenticatie vraagt:
- Gebruik een **Personal Access Token** (niet je wachtwoord)
- Of gebruik **GitHub CLI** (`gh auth login`)

## Alternatief: Via Android Studio

1. Open Android Studio
2. VCS → Git → Push
3. Klik op "Define remote"
4. Voer je GitHub repository URL in
5. Klik op Push

## Downloaden

Nadat je gepusht hebt, kan iedereen de app downloaden via:

```bash
git clone https://github.com/USERNAME/REPO_NAME.git
```

Of download als ZIP via de GitHub website (groene "Code" knop → "Download ZIP")

