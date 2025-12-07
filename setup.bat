@echo off
echo Creando estructura del proyecto Neomatica...

REM Crear directorios principales
mkdir gradle\wrapper
mkdir src\main\java\com\neokey\neomatica\config
mkdir src\main\java\com\neokey\neomatica\schematic
mkdir src\main\java\com\neokey\neomatica\network
mkdir src\main\java\com\neokey\neomatica\gui\widgets
mkdir src\main\java\com\neokey\neomatica\tools
mkdir src\main\java\com\neokey\neomatica\render
mkdir src\main\java\com\neokey\neomatica\util
mkdir src\main\java\com\neokey\neomatica\integration
mkdir src\main\resources\assets\neomatica\lang
mkdir src\main\resources\assets\neomatica\textures\gui
mkdir src\main\resources\assets\neomatica\textures\misc
mkdir src\main\resources\data\neomatica
mkdir src\client\java\com\neokey\neomatica\client

REM Crear archivos Java principales
type nul > src\main\java\com\neokey\neomatica\Neomatica.java

REM Config
type nul > src\main\java\com\neokey\neomatica\config\NeomaticaConfig.java
type nul > src\main\java\com\neokey\neomatica\config\ConfigHandler.java

REM Schematic
type nul > src\main\java\com\neokey\neomatica\schematic\SchematicManager.java
type nul > src\main\java\com\neokey\neomatica\schematic\SchematicRenderer.java
type nul > src\main\java\com\neokey\neomatica\schematic\SchematicLoader.java
type nul > src\main\java\com\neokey\neomatica\schematic\SchematicExporter.java
type nul > src\main\java\com\neokey\neomatica\schematic\SchematicConverter.java
type nul > src\main\java\com\neokey\neomatica\schematic\BlockCounter.java
type nul > src\main\java\com\neokey\neomatica\schematic\LayerGuide.java

REM Network
type nul > src\main\java\com\neokey\neomatica\network\SchematicDownloader.java
type nul > src\main\java\com\neokey\neomatica\network\OnlineRepository.java
type nul > src\main\java\com\neokey\neomatica\network\APIClient.java

REM GUI
type nul > src\main\java\com\neokey\neomatica\gui\NeomaticaScreen.java
type nul > src\main\java\com\neokey\neomatica\gui\SchematicBrowserScreen.java
type nul > src\main\java\com\neokey\neomatica\gui\SchematicPreviewScreen.java
type nul > src\main\java\com\neokey\neomatica\gui\ToolsScreen.java
type nul > src\main\java\com\neokey\neomatica\gui\BlockListScreen.java
type nul > src\main\java\com\neokey\neomatica\gui\LayerGuideScreen.java

REM GUI Widgets
type nul > src\main\java\com\neokey\neomatica\gui\widgets\SchematicListWidget.java
type nul > src\main\java\com\neokey\neomatica\gui\widgets\Preview3DWidget.java
type nul > src\main\java\com\neokey\neomatica\gui\widgets\ToolButtonWidget.java
type nul > src\main\java\com\neokey\neomatica\gui\widgets\BlockCountWidget.java

REM Tools
type nul > src\main\java\com\neokey\neomatica\tools\SelectionTool.java
type nul > src\main\java\com\neokey\neomatica\tools\CopyTool.java
type nul > src\main\java\com\neokey\neomatica\tools\PasteTool.java
type nul > src\main\java\com\neokey\neomatica\tools\MoveTool.java
type nul > src\main\java\com\neokey\neomatica\tools\DeleteTool.java
type nul > src\main\java\com\neokey\neomatica\tools\ToolManager.java

REM Render
type nul > src\main\java\com\neokey\neomatica\render\SchematicWorldRenderer.java
type nul > src\main\java\com\neokey\neomatica\render\SelectionBoxRenderer.java
type nul > src\main\java\com\neokey\neomatica\render\Preview3DRenderer.java
type nul > src\main\java\com\neokey\neomatica\render\LayerRenderer.java

REM Util
type nul > src\main\java\com\neokey\neomatica\util\FileUtil.java
type nul > src\main\java\com\neokey\neomatica\util\NBTUtil.java
type nul > src\main\java\com\neokey\neomatica\util\TranslationUtil.java
type nul > src\main\java\com\neokey\neomatica\util\KeybindUtil.java
type nul > src\main\java\com\neokey\neomatica\util\MathUtil.java

REM Integration
type nul > src\main\java\com\neokey\neomatica\integration\LitematicaIntegration.java
type nul > src\main\java\com\neokey\neomatica\integration\ModMenuIntegration.java

REM Client
type nul > src\client\java\com\neokey\neomatica\client\NeomaticaClient.java
type nul > src\client\java\com\neokey\neomatica\client\KeyBindings.java

REM Resources
type nul > src\main\resources\fabric.mod.json
type nul > src\main\resources\neomatica.mixins.json
type nul > src\main\resources\assets\neomatica\lang\es_es.json
type nul > src\main\resources\data\neomatica\repositories.json

REM Archivos de configuración del proyecto
type nul > build.gradle
type nul > gradle.properties
type nul > settings.gradle
type nul > LICENSE

REM Crear archivo .gitignore
(
echo # Gradle
echo .gradle/
echo build/
echo out/
echo classes/
echo.
echo # Eclipse
echo *.launch
echo .settings/
echo .metadata
echo .classpath
echo .project
echo.
echo # IntelliJ IDEA
echo .idea/
echo *.iml
echo *.ipr
echo *.iws
echo.
echo # Visual Studio Code
echo .vscode/
echo.
echo # MacOS
echo .DS_Store
echo.
echo # Fabric
echo run/
echo.
echo # Logs
echo logs/
echo *.log
) > .gitignore

echo Estructura del proyecto creada exitosamente!
echo Ahora puedes ejecutar: gradlew.bat build
pause