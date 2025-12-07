package com.neokey.neomatica;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neokey.neomatica.config.ConfigHandler;
import com.neokey.neomatica.schematic.SchematicManager;
import com.neokey.neomatica.network.OnlineRepository;

/**
 * Neomatica - Gestión avanzada de schematics para Minecraft
 * 
 * @author NEOKEY
 * @version 1.0.0
 */
public class Neomatica implements ModInitializer {
    public static final String MOD_ID = "neomatica";
    public static final String MOD_NAME = "Neomatica";
    public static final String VERSION = "1.0.0";
    
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    
    private static Neomatica instance;
    private ConfigHandler configHandler;
    private SchematicManager schematicManager;
    private OnlineRepository onlineRepository;
    
    @Override
    public void onInitialize() {
        instance = this;
        
        LOGGER.info("Inicializando {} v{}", MOD_NAME, VERSION);
        
        // Inicializar componentes principales
        initializeConfig();
        initializeManagers();
        
        LOGGER.info("{} inicializado correctamente", MOD_NAME);
    }
    
    /**
     * Inicializa el sistema de configuración
     */
    private void initializeConfig() {
        try {
            configHandler = new ConfigHandler();
            configHandler.load();
            LOGGER.info("Configuración cargada");
        } catch (Exception e) {
            LOGGER.error("Error al cargar la configuración", e);
        }
    }
    
    /**
     * Inicializa los gestores principales del mod
     */
    private void initializeManagers() {
        try {
            // Gestor de schematics
            schematicManager = new SchematicManager();
            LOGGER.info("SchematicManager inicializado");
            
            // Repositorio online
            onlineRepository = new OnlineRepository();
            LOGGER.info("OnlineRepository inicializado");
            
        } catch (Exception e) {
            LOGGER.error("Error al inicializar gestores", e);
        }
    }
    
    /**
     * Obtiene la instancia singleton del mod
     */
    public static Neomatica getInstance() {
        return instance;
    }
    
    /**
     * Obtiene el gestor de configuración
     */
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }
    
    /**
     * Obtiene el gestor de schematics
     */
    public SchematicManager getSchematicManager() {
        return schematicManager;
    }
    
    /**
     * Obtiene el repositorio online
     */
    public OnlineRepository getOnlineRepository() {
        return onlineRepository;
    }
}