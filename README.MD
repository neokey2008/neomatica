# Neomatica

**Gestión avanzada de schematics para Minecraft 1.21.8 (Fabric)**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8-green.svg)
![Fabric](https://img.shields.io/badge/Fabric-0.16.5-orange.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## 🎯 Características

- 🔍 **Búsqueda de Schematics Online** - Busca y descarga schematics directamente desde internet
- 🎨 **Preview 3D en Tiempo Real** - Visualiza schematics en 3D antes de colocarlos
- 🛠️ **Herramientas Completas** - Seleccionar, copiar, pegar, mover, rotar y voltear
- 📊 **Contador de Bloques** - Ve exactamente qué y cuántos bloques necesitas
- 📐 **Guía por Capas** - Construye capa por capa con facilidad
- 🔄 **Compatibilidad con Litematica** - Importa y exporta archivos .litematic
- 🌐 **Interfaz en Español** - Completamente traducido
- 💻 **Client-Side** - Funciona en servidores sin necesidad de instalación

## 📦 Instalación

1. Descarga e instala [Fabric Loader](https://fabricmc.net/use/)
2. Descarga [Fabric API](https://modrinth.com/mod/fabric-api)
3. Descarga Neomatica
4. Coloca ambos archivos .jar en la carpeta `mods/`
5. ¡Inicia Minecraft!

## 🎮 Uso Básico

### Atajos de Teclado (Configurables)

- **M** - Abrir menú principal
- **J** - Acceso rápido a herramientas
- **H** - Alternar visibilidad de schematics

### Herramientas Disponibles

- **Seleccionar Área** - Define un área con dos puntos
- **Copiar** - Copia el área seleccionada
- **Pegar** - Coloca un schematic
- **Mover** - Reposiciona schematics
- **Rotar** - Rota en múltiples ejes
- **Voltear** - Espejo en X, Y o Z
- **Eliminar** - Borra bloques del área

## 🔧 Compilación

### Requisitos

- Java 21 o superior
- Gradle 8.5 (incluido con Gradle Wrapper)

### Comandos

```bash
# Compilar el mod
./gradlew build

# Ejecutar cliente de desarrollo
./gradlew runClient

# Generar sources
./gradlew genSources

# Limpiar build
./gradlew clean
```

El archivo JAR compilado estará en: `build/libs/neomatica-1.0.0.jar`

## 📁 Estructura del Proyecto

```
neomatica/
├── src/main/java/com/neokey/neomatica/
│   ├── config/          # Configuración
│   ├── schematic/       # Gestión de schematics
│   ├── network/         # Descarga online
│   ├── gui/             # Interfaces gráficas
│   ├── tools/           # Herramientas de edición
│   ├── render/          # Renderizado
│   ├── util/            # Utilidades
│   └── integration/     # Integración con otros mods
├── src/main/resources/
│   ├── fabric.mod.json
│   ├── assets/neomatica/
│   │   └── lang/es_es.json
│   └── data/neomatica/
│       └── repositories.json
└── build.gradle
```

## 🔌 Integración con Otros Mods

### Litematica
Neomatica es compatible con archivos .litematic. Puedes:
- Importar tus schematics de Litematica
- Exportar a formato Litematica
- Usar ambos mods simultáneamente

### Mod Menu
Acceso directo desde el menú de mods para abrir la interfaz de Neomatica.

## 🐛 Reportar Bugs

Si encuentras algún problema, por favor [abre un issue](https://github.com/neokey/neomatica/issues) con:
- Versión de Minecraft
- Versión del mod
- Descripción detallada del problema
- Logs (si es posible)

## 📝 Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

## 👨‍💻 Autor

**NEOKEY** - [GitHub](https://github.com/neokey)

## 🙏 Agradecimientos

- [Fabric](https://fabricmc.net/) - Framework de modding
- [Litematica](https://www.curseforge.com/minecraft/mc-mods/litematica) - Inspiración y compatibilidad
- La comunidad de Minecraft modding

---

**¿Te gusta Neomatica?** ⭐ ¡Dale una estrella al repositorio!