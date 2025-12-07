package com.neokey.neomatica.integration;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Integración con Litematica
 * Usa reflexión para evitar dependencia dura
 */
public class LitematicaIntegration {
    
    private static boolean isLitematicaLoaded = false;
    private static boolean integrationInitialized = false;
    private static Class<?> litematicaClass;
    private static Class<?> schematicManagerClass;
    
    /**
     * Inicializa la integración con Litematica
     */
    public static void initialize() {
        if (integrationInitialized) {
            return;
        }
        
        try {
            // Intentar cargar clases de Litematica
            litematicaClass = Class.forName("fi.dy.masa.litematica.Litematica");
            schematicManagerClass = Class.forName("fi.dy.masa.litematica.schematic.SchematicManager");
            
            isLitematicaLoaded = true;
            integrationInitialized = true;
            
            Neomatica.LOGGER.info("Integración con Litematica habilitada");
            
        } catch (ClassNotFoundException e) {
            isLitematicaLoaded = false;
            integrationInitialized = true;
            
            Neomatica.LOGGER.info("Litematica no detectado, funcionando en modo standalone");
        } catch (Exception e) {
            isLitematicaLoaded = false;
            integrationInitialized = true;
            
            Neomatica.LOGGER.warn("Error al inicializar integración con Litematica", e);
        }
    }
    
    /**
     * Verifica si Litematica está cargado
     */
    public static boolean isLitematicaLoaded() {
        if (!integrationInitialized) {
            initialize();
        }
        return isLitematicaLoaded;
    }
    
    /**
     * Importa un schematic de Neomatica a Litematica
     */
    public static boolean importToLitematica(LoadedSchematic schematic, File file) {
        if (!isLitematicaLoaded()) {
            Neomatica.LOGGER.warn("No se puede importar: Litematica no está cargado");
            return false;
        }
        
        try {
            // Usar reflexión para llamar métodos de Litematica
            Method loadSchematicMethod = schematicManagerClass.getMethod("loadSchematic", File.class);
            Object result = loadSchematicMethod.invoke(null, file);
            
            if (result != null) {
                Neomatica.LOGGER.info("Schematic importado a Litematica: {}", file.getName());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al importar schematic a Litematica", e);
            return false;
        }
    }
    
    /**
     * Exporta desde Litematica al formato de Neomatica
     */
    public static LoadedSchematic exportFromLitematica(String schematicName) {
        if (!isLitematicaLoaded()) {
            Neomatica.LOGGER.warn("No se puede exportar: Litematica no está cargado");
            return null;
        }
        
        try {
            // TODO: Implementar exportación desde Litematica
            Neomatica.LOGGER.warn("Exportación desde Litematica no implementada aún");
            return null;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al exportar desde Litematica", e);
            return null;
        }
    }
    
    /**
     * Convierte un archivo de Neomatica a formato Litematica
     */
    public static boolean convertToLitematicaFormat(File inputFile, File outputFile) {
        if (!isLitematicaLoaded()) {
            Neomatica.LOGGER.warn("No se puede convertir: Litematica no está cargado");
            return false;
        }
        
        try {
            // La conversión ya se maneja en SchematicConverter
            // Esta es solo una interfaz conveniente
            return Neomatica.getInstance()
                .getSchematicManager()
                .convertToLitematica(inputFile, outputFile);
                
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al convertir a formato Litematica", e);
            return false;
        }
    }
    
    /**
     * Obtiene la versión de Litematica instalada
     */
    public static String getLitematicaVersion() {
        if (!isLitematicaLoaded()) {
            return "No instalado";
        }
        
        try {
            Method getVersionMethod = litematicaClass.getMethod("getVersion");
            Object version = getVersionMethod.invoke(null);
            return version != null ? version.toString() : "Desconocida";
            
        } catch (Exception e) {
            Neomatica.LOGGER.debug("No se pudo obtener versión de Litematica", e);
            return "Desconocida";
        }
    }
    
    /**
     * Obtiene el directorio de schematics de Litematica
     */
    public static File getLitematicaSchematicsDirectory() {
        if (!isLitematicaLoaded()) {
            return null;
        }
        
        try {
            Method getDirectoryMethod = schematicManagerClass.getMethod("getSchematicsDirectory");
            Object directory = getDirectoryMethod.invoke(null);
            
            if (directory instanceof File) {
                return (File) directory;
            }
            
            return null;
            
        } catch (Exception e) {
            Neomatica.LOGGER.debug("No se pudo obtener directorio de Litematica", e);
            return null;
        }
    }
    
    /**
     * Sincroniza schematics con Litematica
     */
    public static void syncWithLitematica() {
        if (!isLitematicaLoaded()) {
            return;
        }
        
        try {
            File litematicaDir = getLitematicaSchematicsDirectory();
            
            if (litematicaDir != null && litematicaDir.exists()) {
                // Listar archivos .litematic en el directorio de Litematica
                File[] files = litematicaDir.listFiles((dir, name) -> name.endsWith(".litematic"));
                
                if (files != null) {
                    Neomatica.LOGGER.info("Encontrados {} schematics de Litematica", files.length);
                    
                    // Los schematics pueden ser accedidos desde SchematicManager
                    // No es necesario copiarlos, solo listarlos
                }
            }
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al sincronizar con Litematica", e);
        }
    }
    
    /**
     * Verifica si un archivo es compatible con Litematica
     */
    public static boolean isLitematicaCompatible(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".litematic") || name.endsWith(".schem");
    }
    
    /**
     * Abre un schematic en Litematica
     */
    public static boolean openInLitematica(File schematicFile) {
        if (!isLitematicaLoaded()) {
            Neomatica.LOGGER.warn("No se puede abrir: Litematica no está cargado");
            return false;
        }
        
        if (!isLitematicaCompatible(schematicFile)) {
            Neomatica.LOGGER.warn("Archivo no compatible con Litematica: {}", schematicFile.getName());
            return false;
        }
        
        try {
            // Usar reflexión para abrir el schematic en Litematica
            Method loadMethod = schematicManagerClass.getMethod("loadSchematic", File.class);
            Object result = loadMethod.invoke(null, schematicFile);
            
            if (result != null) {
                Neomatica.LOGGER.info("Schematic abierto en Litematica: {}", schematicFile.getName());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al abrir schematic en Litematica", e);
            return false;
        }
    }
    
    /**
     * Obtiene información de compatibilidad
     */
    public static CompatibilityInfo getCompatibilityInfo() {
        return new CompatibilityInfo(
            isLitematicaLoaded(),
            getLitematicaVersion(),
            getLitematicaSchematicsDirectory()
        );
    }
    
    /**
     * Clase con información de compatibilidad
     */
    public static class CompatibilityInfo {
        private final boolean loaded;
        private final String version;
        private final File schematicsDirectory;
        
        public CompatibilityInfo(boolean loaded, String version, File schematicsDirectory) {
            this.loaded = loaded;
            this.version = version;
            this.schematicsDirectory = schematicsDirectory;
        }
        
        public boolean isLoaded() {
            return loaded;
        }
        
        public String getVersion() {
            return version;
        }
        
        public File getSchematicsDirectory() {
            return schematicsDirectory;
        }
        
        public String getStatusMessage() {
            if (!loaded) {
                return "Litematica no instalado - Funcionando en modo standalone";
            }
            return "Litematica " + version + " detectado - Integración activa";
        }
    }
}