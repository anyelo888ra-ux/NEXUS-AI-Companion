#!/usr/bin/env sh
set -eu
printf "Paste the HTTPS URL of your empty GitHub repository: "
read repo
[ -n "$repo" ] || { echo "Repository URL is required."; exit 1; }
git init
git add .
git commit -m "Initial NEXUS.AI Companion scaffold"
git branch -M main
git remote add origin "$repo"
git push -u origin main
