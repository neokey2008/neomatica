package com.neokey.neomatica.render;

import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Map;

/**
 * Renderizador 3D de previews de schematics
 * Usado principalmente para la GUI de preview
 */
public class Preview3DRenderer {
    
    private float rotationX = 30.0f;
    private float rotationY = 45.0f;
    private float zoom = 1.0f;
    private boolean enableLighting = true;
    
    public Preview3DRenderer() {
    }
    
    /**
     * Renderiza un preview 3D del schematic
     */
    public void render(LoadedSchematic schematic, MatrixStack matrices, 
                      int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
        if (schematic == null) {
            return;
        }
        
        Vec3i size = schematic.getSize();
        if (size == null) {
            return;
        }
        
        // Calcular centro y escala
        float centerX = size.getX() / 2.0f;
        float centerY = size.getY() / 2.0f;
        float centerZ = size.getZ() / 2.0f;
        
        float maxDimension = Math.max(size.getX(), Math.max(size.getY(), size.getZ()));
        float scale = (Math.min(viewportWidth, viewportHeight) / maxDimension) * zoom * 0.8f;
        
        // Configurar renderizado
        setupRenderState();
        
        matrices.push();
        
        // Trasladar al centro del viewport
        matrices.translate(
            viewportX + viewportWidth / 2.0f,
            viewportY + viewportHeight / 2.0f,
            0
        );
        
        // Aplicar rotación
        Quaternionf rotation = new Quaternionf()
            .rotateY((float) Math.toRadians(rotationY))
            .rotateX((float) Math.toRadians(rotationX));
        matrices.multiply(rotation);
        
        // Aplicar escala
        matrices.scale(scale, -scale, scale);
        
        // Trasladar al centro del schematic
        matrices.translate(-centerX, -centerY, -centerZ);
        
        // Renderizar bloques
        renderBlocks(schematic, matrices);
        
        matrices.pop();
        
        restoreRenderState();
    }
    
    /**
     * Renderiza los bloques del schematic
     */
    private void renderBlocks(LoadedSchematic schematic, MatrixStack matrices) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin();
        
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        Map<BlockPos, SchematicBlock> blocks = schematic.getBlocks();
        
        for (Map.Entry<BlockPos, SchematicBlock> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            SchematicBlock block = entry.getValue();
            
            int color = getBlockColor(block.getBlockId());
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = 0.9f;
            
            renderBlockCube(buffer, matrices.peek().getPositionMatrix(), pos, r, g, b, a);
        }
        
        tessellator.draw();
    }
    
    /**
     * Renderiza un cubo de bloque
     */
    private void renderBlockCube(BufferBuilder buffer, Matrix4f matrix, BlockPos pos,
                                 float r, float g, float b, float a) {
        float x1 = pos.getX();
        float y1 = pos.getY();
        float z1 = pos.getZ();
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;
        
        // Cara superior (Y+) - más brillante
        float topR = Math.min(r * 1.2f, 1.0f);
        float topG = Math.min(g * 1.2f, 1.0f);
        float topB = Math.min(b * 1.2f, 1.0f);
        
        buffer.vertex(matrix, x1, y2, z1).color(topR, topG, topB, a);
        buffer.vertex(matrix, x1, y2, z2).color(topR, topG, topB, a);
        buffer.vertex(matrix, x2, y2, z2).color(topR, topG, topB, a);
        buffer.vertex(matrix, x2, y2, z1).color(topR, topG, topB, a);
        
        // Cara frontal (Z+) - normal
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);
        
        // Cara derecha (X+) - más oscura
        float sideR = r * 0.8f;
        float sideG = g * 0.8f;
        float sideB = b * 0.8f;
        
        buffer.vertex(matrix, x2, y1, z2).color(sideR, sideG, sideB, a);
        buffer.vertex(matrix, x2, y1, z1).color(sideR, sideG, sideB, a);
        buffer.vertex(matrix, x2, y2, z1).color(sideR, sideG, sideB, a);
        buffer.vertex(matrix, x2, y2, z2).color(sideR, sideG, sideB, a);
        
        // Cara trasera (Z-) - más oscura
        buffer.vertex(matrix, x2, y1, z1).color(sideR, sideG, sideB, a);
        buffer.vertex(matrix, x1, y1, z1).color(sideR, sideG, sideB, a);
        buffer.vertex(matrix, x1, y2, z1).color(sideR, sideG, sideB, a);
        buffer.vertex(matrix, x2, y2, z1).color(sideR, sideG, sideB, a);
        
        // Cara izquierda (X-) - normal
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a);
        
        // Cara inferior (Y-) - más oscura
        float bottomR = r * 0.6f;
        float bottomG = g * 0.6f;
        float bottomB = b * 0.6f;
        
        buffer.vertex(matrix, x1, y1, z1).color(bottomR, bottomG, bottomB, a);
        buffer.vertex(matrix, x2, y1, z1).color(bottomR, bottomG, bottomB, a);
        buffer.vertex(matrix, x2, y1, z2).color(bottomR, bottomG, bottomB, a);
        buffer.vertex(matrix, x1, y1, z2).color(bottomR, bottomG, bottomB, a);
    }
    
    /**
     * Obtiene el color de un bloque
     */
    private int getBlockColor(String blockId) {
        // Colores básicos para diferentes tipos de bloques
        String name = blockId.toLowerCase();
        
        if (name.contains("stone")) return 0x808080;
        if (name.contains("wood") || name.contains("planks") || name.contains("log")) return 0x8B4513;
        if (name.contains("dirt")) return 0x654321;
        if (name.contains("grass")) return 0x7CFC00;
        if (name.contains("sand")) return 0xF4A460;
        if (name.contains("glass")) return 0x87CEEB;
        if (name.contains("brick")) return 0xB22222;
        if (name.contains("gold")) return 0xFFD700;
        if (name.contains("iron")) return 0xC0C0C0;
        if (name.contains("diamond")) return 0x00FFFF;
        if (name.contains("emerald")) return 0x00FF00;
        if (name.contains("redstone")) return 0xFF0000;
        if (name.contains("lapis")) return 0x1E90FF;
        if (name.contains("coal")) return 0x222222;
        if (name.contains("obsidian")) return 0x1A0033;
        if (name.contains("netherrack")) return 0x8B0000;
        if (name.contains("glowstone")) return 0xFFFF99;
        if (name.contains("quartz")) return 0xF5F5F5;
        if (name.contains("concrete")) return 0xAAAAAA;
        
        // Lanas de colores
        if (name.contains("white_wool")) return 0xFFFFFF;
        if (name.contains("orange_wool")) return 0xFF8C00;
        if (name.contains("magenta_wool")) return 0xFF00FF;
        if (name.contains("light_blue_wool")) return 0x87CEEB;
        if (name.contains("yellow_wool")) return 0xFFFF00;
        if (name.contains("lime_wool")) return 0x00FF00;
        if (name.contains("pink_wool")) return 0xFFC0CB;
        if (name.contains("gray_wool")) return 0x808080;
        if (name.contains("cyan_wool")) return 0x00FFFF;
        if (name.contains("purple_wool")) return 0x800080;
        if (name.contains("blue_wool")) return 0x0000FF;
        if (name.contains("brown_wool")) return 0x8B4513;
        if (name.contains("green_wool")) return 0x008000;
        if (name.contains("red_wool")) return 0xFF0000;
        if (name.contains("black_wool")) return 0x000000;
        
        // Color por defecto
        return 0xCCCCCC;
    }
    
    /**
     * Configura el estado de renderizado
     */
    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
    }
    
    /**
     * Restaura el estado de renderizado
     */
    private void restoreRenderState() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
    
    // Getters y Setters
    
    public float getRotationX() {
        return rotationX;
    }
    
    public void setRotationX(float rotationX) {
        this.rotationX = rotationX;
    }
    
    public float getRotationY() {
        return rotationY;
    }
    
    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }
    
    public float getZoom() {
        return zoom;
    }
    
    public void setZoom(float zoom) {
        this.zoom = Math.max(0.1f, Math.min(5.0f, zoom));
    }
    
    public void rotateLeft() {
        rotationY -= 45.0f;
    }
    
    public void rotateRight() {
        rotationY += 45.0f;
    }
    
    public void zoomIn() {
        zoom = Math.min(zoom + 0.1f, 5.0f);
    }
    
    public void zoomOut() {
        zoom = Math.max(zoom - 0.1f, 0.1f);
    }
}