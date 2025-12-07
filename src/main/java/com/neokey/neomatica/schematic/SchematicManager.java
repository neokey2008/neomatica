package com.neokey.neomatica.schematic;

import com.neokey.neomatica.Neomatica;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestor principal de schematics
 * Maneja la carga, guardado y administración de schematics
 */
public class SchematicManager {
    
    private final Map<String, LoadedSchematic> loadedSchematics;
    private final SchematicLoader loader;
    private final SchematicExporter exporter;
    private final SchematicConverter converter;
    
    private LoadedSchematic activeSchematic;
    private String schematicsDirectory;
    
    public SchematicManager() {
        this.loadedSchematics = new ConcurrentHashMap<>();
        this.loader = new SchematicLoader();
        this.exporter = new SchematicExporter();
        this.converter = new SchematicConverter();
        this.schematicsDirectory = "schematics";
        
        // Crear directorio de schematics si no existe
        createSchematicsDirectory();
    }
    
    /**
     * Crea el directorio de schematics
     */
    private void createSchematicsDirectory() {
        File dir = new File(schematicsDirectory);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                Neomatica.LOGGER.info("Directorio de schematics creado: {}", schematicsDirectory);
            }
        }
    }
    
    /**
     * Carga un schematic desde un archivo
     */
    public LoadedSchematic loadSchematic(File file) {
        try {
            LoadedSchematic schematic = loader.load(file);
            
            if (schematic != null) {
                String id = UUID.randomUUID().toString();
                schematic.setId(id);
                loadedSchematics.put(id, schematic);
                
                Neomatica.LOGGER.info("Schematic cargado: {} ({})", file.getName(), id);
                return schematic;
            }
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al cargar schematic: {}", file.getName(), e);
        }
        
        return null;
    }
    
    /**
     * Carga un schematic desde una ruta
     */
    public LoadedSchematic loadSchematic(String path) {
        return loadSchematic(new File(path));
    }
    
    /**
     * Descarga e importa un schematic desde URL
     */
    public LoadedSchematic importFromUrl(String url, String name) {
        try {
            File downloadedFile = new File(schematicsDirectory + "/downloaded", name);
            // Aquí se implementaría la descarga
            // Por ahora retorna null, se implementará en SchematicDownloader
            
            return loadSchematic(downloadedFile);
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al importar schematic desde URL: {}", url, e);
            return null;
        }
    }
    
    /**
     * Exporta un schematic a archivo
     */
    public boolean exportSchematic(LoadedSchematic schematic, File outputFile) {
        try {
            return exporter.export(schematic, outputFile);
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al exportar schematic: {}", outputFile.getName(), e);
            return false;
        }
    }
    
    /**
     * Convierte un schematic a formato Litematica
     */
    public boolean convertToLitematica(File inputFile, File outputFile) {
        try {
            return converter.convertToLitematica(inputFile, outputFile);
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al convertir a Litematica", e);
            return false;
        }
    }
    
    /**
     * Crea un schematic desde una selección de área
     */
    public LoadedSchematic createFromSelection(BlockPos pos1, BlockPos pos2, String name) {
        try {
            LoadedSchematic schematic = new LoadedSchematic(name);
            String id = UUID.randomUUID().toString();
            schematic.setId(id);
            
            // Calcular dimensiones
            Vec3i size = calculateSize(pos1, pos2);
            schematic.setSize(size);
            schematic.setOrigin(getMinPosition(pos1, pos2));
            
            // Registrar el schematic
            addSchematic(id, schematic);
            
            Neomatica.LOGGER.info("Schematic creado desde selección: {}", name);
            return schematic;
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al crear schematic desde selección", e);
            return null;
        }
    }
    
    /**
     * Obtiene un schematic cargado por ID
     */
    public LoadedSchematic getSchematic(String id) {
        return loadedSchematics.get(id);
    }
    
    /**
     * Agrega un schematic al gestor con un ID específico
     */
    public void addSchematic(String id, LoadedSchematic schematic) {
        schematic.setId(id);
        loadedSchematics.put(id, schematic);
        Neomatica.LOGGER.debug("Schematic agregado al gestor: {} (ID: {})", schematic.getName(), id);
    }
    
    /**
     * Obtiene todos los schematics cargados
     */
    public Collection<LoadedSchematic> getAllSchematics() {
        return loadedSchematics.values();
    }
    
    /**
     * Elimina un schematic de la memoria
     */
    public void removeSchematic(String id) {
        LoadedSchematic removed = loadedSchematics.remove(id);
        if (removed != null) {
            if (removed == activeSchematic) {
                activeSchematic = null;
            }
            Neomatica.LOGGER.info("Schematic eliminado: {}", removed.getName());
        }
    }
    
    /**
     * Establece el schematic activo
     */
    public void setActiveSchematic(String id) {
        LoadedSchematic schematic = loadedSchematics.get(id);
        if (schematic != null) {
            this.activeSchematic = schematic;
            Neomatica.LOGGER.info("Schematic activo: {}", schematic.getName());
        }
    }
    
    /**
     * Obtiene el schematic activo
     */
    public LoadedSchematic getActiveSchematic() {
        return activeSchematic;
    }
    
    /**
     * Lista todos los archivos de schematics en el directorio
     */
    public List<File> listSchematicFiles() {
        List<File> files = new ArrayList<>();
        File dir = new File(schematicsDirectory);
        
        if (dir.exists() && dir.isDirectory()) {
            File[] fileArray = dir.listFiles((d, name) -> 
                name.endsWith(".litematic") || 
                name.endsWith(".schem") || 
                name.endsWith(".schematic")
            );
            
            if (fileArray != null) {
                files.addAll(Arrays.asList(fileArray));
            }
        }
        
        return files;
    }
    
    /**
     * Calcula el tamaño entre dos posiciones
     */
    private Vec3i calculateSize(BlockPos pos1, BlockPos pos2) {
        int sizeX = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int sizeY = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int sizeZ = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        return new Vec3i(sizeX, sizeY, sizeZ);
    }
    
    /**
     * Obtiene la posición mínima entre dos posiciones
     */
    private BlockPos getMinPosition(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ())
        );
    }
    
    /**
     * Limpia todos los schematics cargados
     */
    public void clearAll() {
        loadedSchematics.clear();
        activeSchematic = null;
        Neomatica.LOGGER.info("Todos los schematics han sido limpiados");
    }
    
    /**
     * Obtiene el directorio de schematics
     */
    public String getSchematicsDirectory() {
        return schematicsDirectory;
    }
    
    /**
     * Establece el directorio de schematics
     */
    public void setSchematicsDirectory(String directory) {
        this.schematicsDirectory = directory;
        createSchematicsDirectory();
    }
    
    /**
     * Clase interna que representa un schematic cargado
     */
    public static class LoadedSchematic {
        private String id;
        private String name;
        private Vec3i size;
        private BlockPos origin;
        private BlockPos placement;
        private boolean visible = true;
        private float opacity = 1.0f;
        private Map<BlockPos, SchematicBlock> blocks;
        
        public LoadedSchematic(String name) {
            this.name = name;
            this.blocks = new HashMap<>();
            this.placement = BlockPos.ORIGIN;
        }
        
        // Getters y Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Vec3i getSize() { return size; }
        public void setSize(Vec3i size) { this.size = size; }
        
        public BlockPos getOrigin() { return origin; }
        public void setOrigin(BlockPos origin) { this.origin = origin; }
        
        public BlockPos getPlacement() { return placement; }
        public void setPlacement(BlockPos placement) { this.placement = placement; }
        
        public boolean isVisible() { return visible; }
        public void setVisible(boolean visible) { this.visible = visible; }
        
        public float getOpacity() { return opacity; }
        public void setOpacity(float opacity) { this.opacity = Math.max(0.0f, Math.min(1.0f, opacity)); }
        
        public Map<BlockPos, SchematicBlock> getBlocks() { return blocks; }
        public void setBlocks(Map<BlockPos, SchematicBlock> blocks) { this.blocks = blocks; }
        
        public void addBlock(BlockPos pos, SchematicBlock block) {
            blocks.put(pos, block);
        }
        
        public SchematicBlock getBlock(BlockPos pos) {
            return blocks.get(pos);
        }
    }
    
    /**
     * Representa un bloque dentro de un schematic
     */
    public static class SchematicBlock {
        private String blockId;
        private Map<String, String> properties;
        
        public SchematicBlock(String blockId) {
            this.blockId = blockId;
            this.properties = new HashMap<>();
        }
        
        public String getBlockId() { return blockId; }
        public void setBlockId(String blockId) { this.blockId = blockId; }
        
        public Map<String, String> getProperties() { return properties; }
        public void setProperties(Map<String, String> properties) { this.properties = properties; }
        
        public void setProperty(String key, String value) {
            properties.put(key, value);
        }
        
        public String getProperty(String key) {
            return properties.get(key);
        }
    }
}