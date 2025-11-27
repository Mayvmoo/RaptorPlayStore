#!/bin/bash

# Script om GitHub repository aan te maken en code te pushen
# Vereist: GitHub Personal Access Token met 'repo' scope

REPO_NAME="Raptor-Playstore"
GITHUB_USER="Mayvmoo"
GITHUB_TOKEN=""  # Vul hier je Personal Access Token in

# Als je een token hebt, kun je deze gebruiken:
if [ -n "$GITHUB_TOKEN" ]; then
    echo "Repository aanmaken op GitHub..."
    curl -X POST \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        https://api.github.com/user/repos \
        -d "{\"name\":\"$REPO_NAME\",\"description\":\"Android versie van de Raptor bezorgservice app\",\"private\":false}"
    
    echo "Repository aangemaakt!"
fi

# Push naar GitHub
cd /Users/sara/AndroidStudioProjects/Raptor
git remote remove origin 2>/dev/null
git remote add origin https://github.com/$GITHUB_USER/$REPO_NAME.git
git branch -M main
git push -u origin main

echo "✅ Code gepusht naar https://github.com/$GITHUB_USER/$REPO_NAME"

