package com.neokey.neomatica.render;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

/**
 * Renderizador de schematics en el mundo
 */
public class SchematicWorldRenderer {
    
    private final MinecraftClient client;
    private final SchematicManager schematicManager;
    private final SchematicRenderer schematicRenderer;
    
    private boolean enabled = true;
    private double renderDistance = 256.0;
    
    public SchematicWorldRenderer() {
        this.client = MinecraftClient.getInstance();
        this.schematicManager = Neomatica.getInstance().getSchematicManager();
        this.schematicRenderer = new SchematicRenderer();
    }
    
    /**
     * Renderiza todos los schematics cargados en el mundo
     */
    public void render(WorldRenderContext context) {
        if (!enabled || client.player == null) {
            return;
        }
        
        try {
            MatrixStack matrices = context.matrixStack();
            float tickDelta = context.tickCounter().getLastFrameDuration();
            
            // Configurar estado de renderizado
            setupRenderState();
            
            // Renderizar cada schematic cargado
            for (LoadedSchematic schematic : schematicManager.getAllSchematics()) {
                if (schematic == null || !schematic.isVisible()) {
                    continue;
                }
                
                // Verificar distancia de renderizado
                if (!isInRenderDistance(schematic)) {
                    continue;
                }
                
                // Renderizar el schematic
                schematicRenderer.render(schematic, matrices, tickDelta);
            }
            
            // Restaurar estado de renderizado
            restoreRenderState();
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al renderizar schematics en el mundo", e);
        }
    }
    
    /**
     * Configura el estado de renderizado
     */
    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }
    
    /**
     * Restaura el estado de renderizado
     */
    private void restoreRenderState() {
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }
    
    /**
     * Verifica si un schematic está dentro de la distancia de renderizado
     */
    private boolean isInRenderDistance(LoadedSchematic schematic) {
        if (client.player == null) {
            return false;
        }
        
        return schematicRenderer.isSchematicVisible(schematic, renderDistance);
    }
    
    /**
     * Renderiza un schematic específico
     */
    public void renderSchematic(LoadedSchematic schematic, MatrixStack matrices, float tickDelta) {
        if (schematic == null || !schematic.isVisible()) {
            return;
        }
        
        schematicRenderer.render(schematic, matrices, tickDelta);
    }
    
    /**
     * Actualiza el renderizador cada tick
     */
    public void tick() {
        // Actualizar lógica si es necesario
    }
    
    // Getters y Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public double getRenderDistance() {
        return renderDistance;
    }
    
    public void setRenderDistance(double renderDistance) {
        this.renderDistance = Math.max(16.0, Math.min(512.0, renderDistance));
    }
    
    public SchematicRenderer getSchematicRenderer() {
        return schematicRenderer;
    }
}