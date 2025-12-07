package com.neokey.neomatica.render;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.LayerGuide;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;

import java.util.Map;

/**
 * Renderizador de capas para guía de construcción
 */
public class LayerRenderer {
    
    private final MinecraftClient client;
    
    private boolean enabled = true;
    private boolean showIndicators = true;
    private float layerOpacity = 0.7f;
    
    // Colores
    private static final float[] COLOR_CURRENT_LAYER = {0.0f, 1.0f, 0.0f, 0.6f}; // Verde
    private static final float[] COLOR_NEXT_LAYER = {1.0f, 1.0f, 0.0f, 0.4f};    // Amarillo
    private static final float[] COLOR_PREV_LAYER = {0.5f, 0.5f, 0.5f, 0.3f};    // Gris
    
    public LayerRenderer() {
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Renderiza la guía de capas
     */
    public void render(WorldRenderContext context, LayerGuide layerGuide) {
        if (!enabled || layerGuide == null || client.player == null) {
            return;
        }
        
        LoadedSchematic schematic = layerGuide.getSchematic();
        if (schematic == null || !schematic.isVisible()) {
            return;
        }
        
        try {
            MatrixStack matrices = context.matrixStack();
            Vec3d cameraPos = context.camera().getPos();
            
            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            
            // Configurar renderizado
            setupRenderState();
            
            // Renderizar capa actual
            renderLayer(matrices, layerGuide, layerGuide.getCurrentLayer(), COLOR_CURRENT_LAYER);
            
            // Renderizar indicadores si está habilitado
            if (showIndicators) {
                renderLayerIndicators(matrices, layerGuide);
            }
            
            // Restaurar estado
            restoreRenderState();
            
            matrices.pop();
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al renderizar capas", e);
        }
    }
    
    /**
     * Renderiza una capa específica
     */
    private void renderLayer(MatrixStack matrices, LayerGuide layerGuide, int layerNumber, float[] color) {
        LoadedSchematic schematic = layerGuide.getSchematic();
        Map<BlockPos, SchematicBlock> layerBlocks = layerGuide.getLayerBlocks(layerNumber);
        
        if (layerBlocks.isEmpty()) {
            return;
        }
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        BlockPos placement = schematic.getPlacement();
        
        for (Map.Entry<BlockPos, SchematicBlock> entry : layerBlocks.entrySet()) {
            BlockPos relativePos = entry.getKey();
            BlockPos worldPos = placement.add(relativePos);
            
            renderBlockHighlight(buffer, matrices.peek().getPositionMatrix(), worldPos, color);
        }
        
        tessellator.draw();
    }
    
    /**
     * Renderiza el resaltado de un bloque
     */
    private void renderBlockHighlight(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, float[] color) {
        float x1 = pos.getX();
        float y1 = pos.getY();
        float z1 = pos.getZ();
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;
        
        float r = color[0];
        float g = color[1];
        float b = color[2];
        float a = color[3] * layerOpacity;
        
        // Solo renderizar cara superior para la guía
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
    }
    
    /**
     * Renderiza indicadores de capa
     */
    private void renderLayerIndicators(MatrixStack matrices, LayerGuide layerGuide) {
        LoadedSchematic schematic = layerGuide.getSchematic();
        BlockPos placement = schematic.getPlacement();
        
        int currentLayer = layerGuide.getCurrentLayer();
        int totalLayers = layerGuide.getTotalLayers();
        
        // Calcular posición del indicador
        int indicatorY = switch (layerGuide.getAxis()) {
            case Y -> placement.getY() + currentLayer;
            case X -> placement.getY();
            case Z -> placement.getY();
        };
        
        // Renderizar línea indicadora
        renderLayerLine(matrices, placement, indicatorY, schematic.getSize());
    }
    
    /**
     * Renderiza una línea indicadora de capa
     */
    private void renderLayerLine(MatrixStack matrices, BlockPos placement, int y, net.minecraft.util.math.Vec3i size) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        float x1 = placement.getX() - 1;
        float x2 = placement.getX() + size.getX() + 1;
        float z1 = placement.getZ() - 1;
        float z2 = placement.getZ() + size.getZ() + 1;
        float yf = y + 0.5f;
        
        float[] color = COLOR_CURRENT_LAYER;
        
        // Líneas del perímetro
        addLine(buffer, matrix, x1, yf, z1, x2, yf, z1, color);
        addLine(buffer, matrix, x2, yf, z1, x2, yf, z2, color);
        addLine(buffer, matrix, x2, yf, z2, x1, yf, z2, color);
        addLine(buffer, matrix, x1, yf, z2, x1, yf, z1, color);
        
        tessellator.draw();
    }
    
    /**
     * Agrega una línea al buffer
     */
    private void addLine(BufferBuilder buffer, Matrix4f matrix,
                        float x1, float y1, float z1,
                        float x2, float y2, float z2,
                        float[] color) {
        buffer.vertex(matrix, x1, y1, z1).color(color[0], color[1], color[2], color[3]).next();
        buffer.vertex(matrix, x2, y2, z2).color(color[0], color[1], color[2], color[3]).next();
    }
    
    /**
     * Renderiza todas las capas con diferentes opacidades
     */
    public void renderAllLayers(WorldRenderContext context, LayerGuide layerGuide) {
        if (!enabled || layerGuide == null) {
            return;
        }
        
        int currentLayer = layerGuide.getCurrentLayer();
        int totalLayers = layerGuide.getTotalLayers();
        
        MatrixStack matrices = context.matrixStack();
        
        matrices.push();
        
        setupRenderState();
        
        // Renderizar capas anteriores con baja opacidad
        for (int i = 0; i < currentLayer; i++) {
            renderLayer(matrices, layerGuide, i, COLOR_PREV_LAYER);
        }
        
        // Renderizar capa actual
        renderLayer(matrices, layerGuide, currentLayer, COLOR_CURRENT_LAYER);
        
        // Renderizar siguiente capa con opacidad media
        if (currentLayer + 1 < totalLayers) {
            renderLayer(matrices, layerGuide, currentLayer + 1, COLOR_NEXT_LAYER);
        }
        
        restoreRenderState();
        
        matrices.pop();
    }
    
    /**
     * Configura el estado de renderizado
     */
    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(2.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    }
    
    /**
     * Restaura el estado de renderizado
     */
    private void restoreRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }
    
    // Getters y Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isShowIndicators() {
        return showIndicators;
    }
    
    public void setShowIndicators(boolean showIndicators) {
        this.showIndicators = showIndicators;
    }
    
    public float getLayerOpacity() {
        return layerOpacity;
    }
    
    public void setLayerOpacity(float layerOpacity) {
        this.layerOpacity = Math.max(0.0f, Math.min(1.0f, layerOpacity));
    }
}