# run-friend-windows.ps1
# Windows PowerShell script to run Friend app with JavaFX on the module-path
#
# Usage:
#   1. Download JavaFX SDK 21.0.2 from https://openjfx.io/
#   2. Extract it (e.g., to C:\javafx-sdk-21)
#   3. Edit the $javafxPath variable below to point to the lib folder
#   4. Run: powershell -ExecutionPolicy Bypass -File run-friend-windows.ps1

# ============= EDIT THIS PATH =============
# Set this to the path of your JavaFX SDK lib directory
$javafxPath = "C:\javafx-sdk-21.0.9\lib"
# ==========================================

# Check if the JAR exists
$jarFile = "$PSScriptRoot\target\Friend-1.0-SNAPSHOT-shaded.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "ERROR: JAR file not found at $jarFile" -ForegroundColor Red
    Write-Host "Please run 'mvn -DskipTests package' first."
    exit 1
}

# Check if JavaFX SDK path exists
if (-not (Test-Path $javafxPath)) {
    Write-Host "ERROR: JavaFX SDK path not found: $javafxPath" -ForegroundColor Red
    Write-Host ""
    Write-Host "To fix this:" -ForegroundColor Yellow
    Write-Host "1. Download JavaFX SDK 21.0.2 from https://openjfx.io/"
    Write-Host "2. Extract it to a location (e.g., C:\javafx-sdk-21)"
    Write-Host "3. Edit this script and set `$javafxPath to the lib folder path"
    Write-Host ""
    exit 1
}

Write-Host "Starting Friend app with JavaFX..." -ForegroundColor Green
Write-Host "Using JavaFX from: $javafxPath"
Write-Host ""

# Run the app with JavaFX on the module-path
java `
    --module-path "$javafxPath" `
    --add-modules javafx.controls,javafx.fxml,javafx.web,javafx.swing `
    -jar "$jarFile"
