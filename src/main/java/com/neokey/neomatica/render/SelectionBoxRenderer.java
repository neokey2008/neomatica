package com.neokey.neomatica.render;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.tools.ToolManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import org.joml.Matrix4f;

/**
 * Renderizador de cajas de selección
 */
public class SelectionBoxRenderer {
    
    private final MinecraftClient client;
    
    private boolean enabled = true;
    private float lineWidth = 2.0f;
    
    // Colores
    private static final float[] COLOR_POS1 = {1.0f, 0.0f, 0.0f, 0.8f}; // Rojo
    private static final float[] COLOR_POS2 = {0.0f, 0.0f, 1.0f, 0.8f}; // Azul
    private static final float[] COLOR_BOX = {0.0f, 1.0f, 0.0f, 0.4f};  // Verde
    private static final float[] COLOR_EDGES = {1.0f, 1.0f, 0.0f, 0.6f}; // Amarillo
    
    public SelectionBoxRenderer() {
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Renderiza las cajas de selección
     */
    public void render(WorldRenderContext context) {
        if (!enabled || client.player == null) {
            return;
        }
        
        try {
            // Obtener el ToolManager desde el cliente
            // Nota: Esto requiere que guardes una referencia al ToolManager en algún lugar accesible
            // Por ahora, usaremos null check
            ToolManager toolManager = getToolManager();
            
            if (toolManager == null) {
                return;
            }
            
            BlockPos pos1 = toolManager.getPosition1();
            BlockPos pos2 = toolManager.getPosition2();
            
            MatrixStack matrices = context.matrixStack();
            Vec3d cameraPos = context.camera().getPos();
            
            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
            
            // Configurar renderizado
            setupRenderState();
            
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin();
            
            // Renderizar posición 1
            if (pos1 != null) {
                renderPosition(matrices, buffer, pos1, COLOR_POS1);
            }
            
            // Renderizar posición 2
            if (pos2 != null) {
                renderPosition(matrices, buffer, pos2, COLOR_POS2);
            }
            
            // Renderizar caja de selección
            if (pos1 != null && pos2 != null) {
                renderSelectionBox(matrices, buffer, pos1, pos2);
            }
            
            // Restaurar estado
            restoreRenderState();
            
            matrices.pop();
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al renderizar caja de selección", e);
        }
    }
    
    /**
     * Obtiene el ToolManager - debe ser implementado según tu arquitectura
     */
    private ToolManager getToolManager() {
        // Esta es una implementación temporal
        // Necesitarás almacenar el ToolManager en un lugar accesible
        // Por ejemplo, en una clase singleton o en el cliente
        return null; // TODO: Implementar acceso al ToolManager
    }
    
    /**
     * Renderiza un marcador de posición
     */
    private void renderPosition(MatrixStack matrices, BufferBuilder buffer, BlockPos pos, float[] color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();
        float size = 0.05f;
        
        // Cruz en X
        addLine(buffer, matrix, x - size, y + 0.5f, z + 0.5f, x + 1 + size, y + 0.5f, z + 0.5f, color);
        
        // Cruz en Y
        addLine(buffer, matrix, x + 0.5f, y - size, z + 0.5f, x + 0.5f, y + 1 + size, z + 0.5f, color);
        
        // Cruz en Z
        addLine(buffer, matrix, x + 0.5f, y + 0.5f, z - size, x + 0.5f, y + 0.5f, z + 1 + size, color);
        
        Tessellator.getInstance().draw();
        
        // Renderizar cubo pequeño en el centro
        renderSmallCube(matrices, buffer, pos, color);
    }
    
    /**
     * Renderiza un cubo pequeño
     */
    private void renderSmallCube(MatrixStack matrices, BufferBuilder buffer, BlockPos pos, float[] color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        
        float x = pos.getX() + 0.4f;
        float y = pos.getY() + 0.4f;
        float z = pos.getZ() + 0.4f;
        float size = 0.2f;
        
        // Aristas del cubo
        drawCubeEdges(buffer, matrix, x, y, z, size, color);
        
        Tessellator.getInstance().draw();
    }
    
    /**
     * Renderiza la caja de selección completa
     */
    private void renderSelectionBox(MatrixStack matrices, BufferBuilder buffer, BlockPos pos1, BlockPos pos2) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        
        int maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
        int maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
        int maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;
        
        // Renderizar aristas
        buffer.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        drawBoxEdges(buffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, COLOR_EDGES);
        Tessellator.getInstance().draw();
        
        // Renderizar caras translúcidas
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        drawBoxFaces(buffer, matrix, minX, minY, minZ, maxX, maxY, maxZ, COLOR_BOX);
        Tessellator.getInstance().draw();
    }
    
    /**
     * Dibuja las aristas de una caja
     */
    private void drawBoxEdges(BufferBuilder buffer, Matrix4f matrix, 
                              float x1, float y1, float z1, 
                              float x2, float y2, float z2, 
                              float[] color) {
        // Aristas inferiores
        addLine(buffer, matrix, x1, y1, z1, x2, y1, z1, color);
        addLine(buffer, matrix, x2, y1, z1, x2, y1, z2, color);
        addLine(buffer, matrix, x2, y1, z2, x1, y1, z2, color);
        addLine(buffer, matrix, x1, y1, z2, x1, y1, z1, color);
        
        // Aristas verticales
        addLine(buffer, matrix, x1, y1, z1, x1, y2, z1, color);
        addLine(buffer, matrix, x2, y1, z1, x2, y2, z1, color);
        addLine(buffer, matrix, x2, y1, z2, x2, y2, z2, color);
        addLine(buffer, matrix, x1, y1, z2, x1, y2, z2, color);
        
        // Aristas superiores
        addLine(buffer, matrix, x1, y2, z1, x2, y2, z1, color);
        addLine(buffer, matrix, x2, y2, z1, x2, y2, z2, color);
        addLine(buffer, matrix, x2, y2, z2, x1, y2, z2, color);
        addLine(buffer, matrix, x1, y2, z2, x1, y2, z1, color);
    }
    
    /**
     * Dibuja las caras de una caja
     */
    private void drawBoxFaces(BufferBuilder buffer, Matrix4f matrix,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float[] color) {
        float r = color[0], g = color[1], b = color[2], a = color[3];
        
        // Cara inferior (Y-)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a);
        
        // Cara superior (Y+)
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a);
        
        // Cara norte (Z-)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a);
        
        // Cara sur (Z+)
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);
        
        // Cara oeste (X-)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a);
        
        // Cara este (X+)
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a);
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a);
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a);
    }
    
    /**
     * Dibuja las aristas de un cubo pequeño
     */
    private void drawCubeEdges(BufferBuilder buffer, Matrix4f matrix,
                              float x, float y, float z, float size,
                              float[] color) {
        float x2 = x + size;
        float y2 = y + size;
        float z2 = z + size;
        
        // Aristas inferiores
        addLine(buffer, matrix, x, y, z, x2, y, z, color);
        addLine(buffer, matrix, x2, y, z, x2, y, z2, color);
        addLine(buffer, matrix, x2, y, z2, x, y, z2, color);
        addLine(buffer, matrix, x, y, z2, x, y, z, color);
        
        // Aristas verticales
        addLine(buffer, matrix, x, y, z, x, y2, z, color);
        addLine(buffer, matrix, x2, y, z, x2, y2, z, color);
        addLine(buffer, matrix, x2, y, z2, x2, y2, z2, color);
        addLine(buffer, matrix, x, y, z2, x, y2, z2, color);
        
        // Aristas superiores
        addLine(buffer, matrix, x, y2, z, x2, y2, z, color);
        addLine(buffer, matrix, x2, y2, z, x2, y2, z2, color);
        addLine(buffer, matrix, x2, y2, z2, x, y2, z2, color);
        addLine(buffer, matrix, x, y2, z2, x, y2, z, color);
    }
    
    /**
     * Agrega una línea al buffer
     */
    private void addLine(BufferBuilder buffer, Matrix4f matrix,
                        float x1, float y1, float z1,
                        float x2, float y2, float z2,
                        float[] color) {
        buffer.vertex(matrix, x1, y1, z1).color(color[0], color[1], color[2], color[3]);
        buffer.vertex(matrix, x2, y2, z2).color(color[0], color[1], color[2], color[3]);
    }
    
    /**
     * Configura el estado de renderizado
     */
    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(lineWidth);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
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
    
    public float getLineWidth() {
        return lineWidth;
    }
    
    public void setLineWidth(float lineWidth) {
        this.lineWidth = Math.max(1.0f, Math.min(5.0f, lineWidth));
    }
}