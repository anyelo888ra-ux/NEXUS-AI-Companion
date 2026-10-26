# Run this from the project root after creating an EMPTY GitHub repository.
# Replace the URL below with your new repository URL.
$repo = Read-Host "Paste the HTTPS URL of your empty GitHub repository"
if ([string]::IsNullOrWhiteSpace($repo)) { throw "Repository URL is required." }
git init
git add .
git commit -m "Initial NEXUS.AI Companion scaffold"
git branch -M main
git remote add origin $repo
git push -u origin main
