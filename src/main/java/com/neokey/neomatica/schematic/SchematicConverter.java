package com.neokey.neomatica.schematic;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;

import java.io.File;

/**
 * Conversor entre diferentes formatos de schematics
 */
public class SchematicConverter {
    
    private final SchematicLoader loader;
    private final SchematicExporter exporter;
    
    public SchematicConverter() {
        this.loader = new SchematicLoader();
        this.exporter = new SchematicExporter();
    }
    
    /**
     * Convierte un schematic a formato Litematica
     */
    public boolean convertToLitematica(File inputFile, File outputFile) {
        try {
            // Cargar schematic original
            LoadedSchematic schematic = loader.load(inputFile);
            
            if (schematic == null) {
                Neomatica.LOGGER.error("No se pudo cargar el schematic para convertir: {}", inputFile.getName());
                return false;
            }
            
            // Asegurar extensión .litematic
            if (!outputFile.getName().endsWith(".litematic")) {
                outputFile = new File(outputFile.getParent(), outputFile.getName() + ".litematic");
            }
            
            // Exportar a Litematica
            boolean success = exporter.export(schematic, outputFile);
            
            if (success) {
                Neomatica.LOGGER.info("Conversión exitosa: {} -> {}", 
                    inputFile.getName(), outputFile.getName());
            }
            
            return success;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al convertir a Litematica", e);
            return false;
        }
    }
    
    /**
     * Convierte un schematic a formato Sponge (.schem)
     */
    public boolean convertToSponge(File inputFile, File outputFile) {
        try {
            LoadedSchematic schematic = loader.load(inputFile);
            
            if (schematic == null) {
                return false;
            }
            
            if (!outputFile.getName().endsWith(".schem")) {
                outputFile = new File(outputFile.getParent(), outputFile.getName() + ".schem");
            }
            
            boolean success = exporter.export(schematic, outputFile);
            
            if (success) {
                Neomatica.LOGGER.info("Conversión exitosa a Sponge: {}", outputFile.getName());
            }
            
            return success;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al convertir a Sponge", e);
            return false;
        }
    }
    
    /**
     * Convierte un schematic a formato WorldEdit legacy (.schematic)
     */
    public boolean convertToWorldEdit(File inputFile, File outputFile) {
        try {
            LoadedSchematic schematic = loader.load(inputFile);
            
            if (schematic == null) {
                return false;
            }
            
            if (!outputFile.getName().endsWith(".schematic")) {
                outputFile = new File(outputFile.getParent(), outputFile.getName() + ".schematic");
            }
            
            boolean success = exporter.export(schematic, outputFile);
            
            if (success) {
                Neomatica.LOGGER.info("Conversión exitosa a WorldEdit: {}", outputFile.getName());
            }
            
            return success;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al convertir a WorldEdit", e);
            return false;
        }
    }
    
    /**
     * Convierte un schematic al formato especificado
     */
    public boolean convert(File inputFile, File outputFile, SchematicFormat targetFormat) {
        return switch (targetFormat) {
            case LITEMATIC -> convertToLitematica(inputFile, outputFile);
            case SPONGE -> convertToSponge(inputFile, outputFile);
            case WORLDEDIT -> convertToWorldEdit(inputFile, outputFile);
        };
    }
    
    /**
     * Detecta el formato de un archivo schematic
     */
    public SchematicFormat detectFormat(File file) {
        String name = file.getName().toLowerCase();
        
        if (name.endsWith(".litematic")) {
            return SchematicFormat.LITEMATIC;
        } else if (name.endsWith(".schem")) {
            return SchematicFormat.SPONGE;
        } else if (name.endsWith(".schematic")) {
            return SchematicFormat.WORLDEDIT;
        }
        
        return SchematicFormat.UNKNOWN;
    }
    
    /**
     * Enum de formatos de schematics soportados
     */
    public enum SchematicFormat {
        LITEMATIC("Litematica", ".litematic"),
        SPONGE("Sponge Schematic", ".schem"),
        WORLDEDIT("WorldEdit Legacy", ".schematic"),
        UNKNOWN("Desconocido", "");
        
        private final String displayName;
        private final String extension;
        
        SchematicFormat(String displayName, String extension) {
            this.displayName = displayName;
            this.extension = extension;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getExtension() {
            return extension;
        }
    }
}