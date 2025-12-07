package com.neokey.neomatica.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.neokey.neomatica.Neomatica;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Gestor de configuración para cargar y guardar opciones
 */
public class ConfigHandler {
    
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "neomatica.json";
    
    private final Gson gson;
    private final File configFile;
    private NeomaticaConfig config;
    
    public ConfigHandler() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        
        // Crear directorio de configuración si no existe
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        this.configFile = new File(configDir, CONFIG_FILE);
        this.config = new NeomaticaConfig();
    }
    
    /**
     * Carga la configuración desde el archivo
     */
    public void load() {
        if (!configFile.exists()) {
            Neomatica.LOGGER.info("Archivo de configuración no encontrado, creando uno nuevo");
            save();
            return;
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            config = gson.fromJson(reader, NeomaticaConfig.class);
            
            if (config == null) {
                config = new NeomaticaConfig();
                Neomatica.LOGGER.warn("Configuración corrupta, usando valores por defecto");
                save();
            } else {
                Neomatica.LOGGER.info("Configuración cargada exitosamente");
            }
            
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al cargar la configuración", e);
            config = new NeomaticaConfig();
        }
    }
    
    /**
     * Guarda la configuración actual al archivo
     */
    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(config, writer);
            Neomatica.LOGGER.info("Configuración guardada exitosamente");
            
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al guardar la configuración", e);
        }
    }
    
    /**
     * Obtiene la configuración actual
     */
    public NeomaticaConfig getConfig() {
        return config;
    }
    
    /**
     * Establece una nueva configuración
     */
    public void setConfig(NeomaticaConfig config) {
        this.config = config;
    }
    
    /**
     * Resetea la configuración a valores por defecto
     */
    public void reset() {
        config = new NeomaticaConfig();
        save();
        Neomatica.LOGGER.info("Configuración reseteada a valores por defecto");
    }
}