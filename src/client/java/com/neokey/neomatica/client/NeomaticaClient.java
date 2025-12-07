package com.neokey.neomatica.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.render.SchematicWorldRenderer;
import com.neokey.neomatica.render.SelectionBoxRenderer;
import com.neokey.neomatica.tools.ToolManager;
import com.neokey.neomatica.integration.LitematicaIntegration;

/**
 * Inicialización del lado del cliente para Neomatica
 */
public class NeomaticaClient implements ClientModInitializer {
    
    private static NeomaticaClient instance;
    private KeyBindings keyBindings;
    private SchematicWorldRenderer worldRenderer;
    private SelectionBoxRenderer selectionRenderer;
    private ToolManager toolManager;
    
    @Override
    public void onInitializeClient() {
        instance = this;
        
        Neomatica.LOGGER.info("Inicializando cliente de Neomatica");
        
        // Registrar keybinds
        keyBindings = new KeyBindings();
        keyBindings.register();
        
        // Inicializar renderizadores
        worldRenderer = new SchematicWorldRenderer();
        selectionRenderer = new SelectionBoxRenderer();
        
        // Inicializar gestor de herramientas
        toolManager = new ToolManager();
        
        // Registrar eventos de renderizado
        registerRenderEvents();
        
        // Registrar eventos de tick
        registerTickEvents();
        
        // Intentar integración con Litematica
        tryLitematicaIntegration();
        
        Neomatica.LOGGER.info("Cliente de Neomatica inicializado");
    }
    
    /**
     * Registra eventos de renderizado
     */
    private void registerRenderEvents() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            // Renderizar schematics cargados
            worldRenderer.render(context);
            
            // Renderizar caja de selección
            selectionRenderer.render(context);
        });
    }
    
    /**
     * Registra eventos de tick del cliente
     */
    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Procesar keybinds
            keyBindings.process(client);
            
            // Actualizar herramientas activas
            toolManager.tick(client);
        });
    }
    
    /**
     * Intenta cargar la integración con Litematica si está presente
     */
    private void tryLitematicaIntegration() {
        try {
            LitematicaIntegration.initialize();
            Neomatica.LOGGER.info("Integración con Litematica habilitada");
        } catch (Exception e) {
            Neomatica.LOGGER.info("Litematica no detectado, funcionando en modo standalone");
        }
    }
    
    /**
     * Obtiene la instancia singleton del cliente
     */
    public static NeomaticaClient getInstance() {
        return instance;
    }
    
    /**
     * Obtiene el renderizador de mundo
     */
    public SchematicWorldRenderer getWorldRenderer() {
        return worldRenderer;
    }
    
    /**
     * Obtiene el renderizador de selección
     */
    public SelectionBoxRenderer getSelectionRenderer() {
        return selectionRenderer;
    }
    
    /**
     * Obtiene el gestor de herramientas
     */
    public ToolManager getToolManager() {
        return toolManager;
    }
    
    /**
     * Obtiene los keybinds
     */
    public KeyBindings getKeyBindings() {
        return keyBindings;
    }
}