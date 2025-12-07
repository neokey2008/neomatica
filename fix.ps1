# Script para corregir la API de renderizado en Neomatica
# Ejecutar desde la raíz del proyecto: .\fix-rendering-api.ps1

Write-Host "Corrigiendo API de renderizado para Minecraft 1.21.8..." -ForegroundColor Green

# Función para reemplazar texto en archivos
function Replace-InFile {
    param(
        [string]$Path,
        [string]$OldText,
        [string]$NewText
    )
    
    if (Test-Path $Path) {
        $content = Get-Content $Path -Raw -Encoding UTF8
        $escapedOld = [regex]::Escape($OldText)
        if ($content -match $escapedOld) {
            $content = $content -replace $escapedOld, $NewText
            Set-Content $Path -Value $content -Encoding UTF8 -NoNewline
            Write-Host "  OK Actualizado: $Path" -ForegroundColor Yellow
        }
    }
}

# Lista de archivos a corregir
$renderFiles = @(
    "src\main\java\com\neokey\neomatica\render\SchematicWorldRenderer.java",
    "src\main\java\com\neokey\neomatica\render\SelectionBoxRenderer.java",
    "src\main\java\com\neokey\neomatica\render\LayerRenderer.java",
    "src\main\java\com\neokey\neomatica\schematic\SchematicRenderer.java",
    "src\main\java\com\neokey\neomatica\render\Preview3DRenderer.java"
)

Write-Host ""
Write-Host "1. Corrigiendo llamadas a vertex..." -ForegroundColor Cyan

foreach ($file in $renderFiles) {
    Replace-InFile -Path $file -OldText '.next();' -NewText ';'
}

Write-Host ""
Write-Host "2. Corrigiendo GameRenderer..." -ForegroundColor Cyan

foreach ($file in $renderFiles) {
    Replace-InFile -Path $file -OldText 'GameRenderer::getPositionColorProgram' -NewText 'GameRenderer::getPositionColorShader'
}

Write-Host ""
Write-Host "3. Corrigiendo Tessellator..." -ForegroundColor Cyan

foreach ($file in $renderFiles) {
    Replace-InFile -Path $file -OldText 'tessellator.getBuffer()' -NewText 'tessellator.begin()'
    Replace-InFile -Path $file -OldText '.getBuffer()' -NewText '.begin()'
}

Write-Host ""
Write-Host "4. Corrigiendo tickCounter..." -ForegroundColor Cyan

$tickFile = "src\main\java\com\neokey\neomatica\render\SchematicWorldRenderer.java"
Replace-InFile -Path $tickFile -OldText 'context.tickCounter().getTickDelta(false)' -NewText 'context.tickCounter().getLastFrameDuration()'

Write-Host ""
Write-Host "Correccion de API completada!" -ForegroundColor Green
Write-Host ""
Write-Host "Ahora ejecuta: .\gradlew clean build" -ForegroundColor Cyan