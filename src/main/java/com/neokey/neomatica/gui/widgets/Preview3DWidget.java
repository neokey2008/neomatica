package com.neokey.neomatica.gui.widgets;

import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

/**
 * Widget para preview 3D de schematics
 */
public class Preview3DWidget implements Drawable, Element {
    
    private final MinecraftClient client;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    
    private LoadedSchematic schematic;
    private float rotationY = 45.0f;
    private float rotationX = 30.0f;
    private float zoom = 1.0f;
    private Vector3f offset = new Vector3f(0, 0, 0);
    
    private boolean isDragging = false;
    private double lastMouseX;
    private double lastMouseY;
    
    private static final float ROTATION_SPEED = 2.0f;
    private static final float ZOOM_SPEED = 0.1f;
    private static final float MIN_ZOOM = 0.1f;
    private static final float MAX_ZOOM = 5.0f;
    
    public Preview3DWidget(int x, int y, int width, int height, MinecraftClient client) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.client = client;
    }
    
    /**
     * Establece el schematic a previsualizar
     */
    public void setSchematic(LoadedSchematic schematic) {
        this.schematic = schematic;
        resetView();
    }
    
    /**
     * Resetea la vista a valores por defecto
     */
    public void resetView() {
        rotationY = 45.0f;
        rotationX = 30.0f;
        zoom = 1.0f;
        offset = new Vector3f(0, 0, 0);
    }
    
    /**
     * Rota la vista a la izquierda
     */
    public void rotateLeft() {
        rotationY -= 45.0f;
    }
    
    /**
     * Rota la vista a la derecha
     */
    public void rotateRight() {
        rotationY += 45.0f;
    }
    
    /**
     * Aumenta el zoom
     */
    public void zoomIn() {
        zoom = Math.min(zoom + ZOOM_SPEED, MAX_ZOOM);
    }
    
    /**
     * Disminuye el zoom
     */
    public void zoomOut() {
        zoom = Math.max(zoom - ZOOM_SPEED, MIN_ZOOM);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Renderizar fondo
        context.fill(x, y, x + width, y + height, 0xFF1A1A1A);
        context.drawBorder(x, y, width, height, 0xFFFFFFFF);
        
        if (schematic == null) {
            // Mostrar mensaje si no hay schematic
            String message = "No hay preview disponible";
            int messageWidth = client.textRenderer.getWidth(message);
            context.drawText(
                client.textRenderer,
                message,
                x + (width - messageWidth) / 2,
                y + height / 2,
                0x808080,
                false
            );
            return;
        }
        
        // Configurar viewport para el preview
        int viewportX = x + 5;
        int viewportY = y + 5;
        int viewportWidth = width - 10;
        int viewportHeight = height - 10;
        
        // Renderizar preview 3D
        renderPreview3D(context, viewportX, viewportY, viewportWidth, viewportHeight);
        
        // Renderizar controles de ayuda
        renderHelpText(context);
    }
    
    /**
     * Renderiza el preview 3D del schematic
     */
    private void renderPreview3D(DrawContext context, int viewportX, int viewportY, int viewportWidth, int viewportHeight) {
        Vec3i size = schematic.getSize();
        if (size == null) return;
        
        // Calcular centro del schematic
        float centerX = size.getX() / 2.0f;
        float centerY = size.getY() / 2.0f;
        float centerZ = size.getZ() / 2.0f;
        
        // Calcular escala para que el schematic quepa en el viewport
        float maxDimension = Math.max(size.getX(), Math.max(size.getY(), size.getZ()));
        float scale = (Math.min(viewportWidth, viewportHeight) / maxDimension) * zoom * 0.8f;
        
        // Preparar renderizado
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        
        context.getMatrices().push();
        
        // Trasladar al centro del viewport
        context.getMatrices().translate(
            viewportX + viewportWidth / 2.0f,
            viewportY + viewportHeight / 2.0f,
            0
        );
        
        // Aplicar rotación
        Quaternionf rotation = new Quaternionf()
            .rotateY((float) Math.toRadians(rotationY))
            .rotateX((float) Math.toRadians(rotationX));
        context.getMatrices().multiply(rotation);
        
        // Aplicar escala
        context.getMatrices().scale(scale, -scale, scale); // -scale en Y para invertir eje Y
        
        // Trasladar al centro del schematic
        context.getMatrices().translate(-centerX + offset.x, -centerY + offset.y, -centerZ + offset.z);
        
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        // Renderizar bloques
        Map<BlockPos, SchematicBlock> blocks = schematic.getBlocks();
        for (Map.Entry<BlockPos, SchematicBlock> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            SchematicBlock block = entry.getValue();
            
            renderBlockCube(buffer, context.getMatrices().peek().getPositionMatrix(), 
                           pos, getBlockColor(block.getBlockId()));
        }
        
        tessellator.draw();
        
        context.getMatrices().pop();
        
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    
    /**
     * Renderiza un cubo de bloque simplificado
     */
    private void renderBlockCube(BufferBuilder buffer, Matrix4f matrix, BlockPos pos, int color) {
        float x1 = pos.getX();
        float y1 = pos.getY();
        float z1 = pos.getZ();
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;
        
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = 0.8f;
        
        // Renderizar solo las caras visibles (simplificado)
        // Cara superior
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
        
        // Cara frontal
        buffer.vertex(matrix, x1, y1, z2).color(r * 0.8f, g * 0.8f, b * 0.8f, a).next();
        buffer.vertex(matrix, x2, y1, z2).color(r * 0.8f, g * 0.8f, b * 0.8f, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r * 0.8f, g * 0.8f, b * 0.8f, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r * 0.8f, g * 0.8f, b * 0.8f, a).next();
        
        // Cara derecha
        buffer.vertex(matrix, x2, y1, z2).color(r * 0.6f, g * 0.6f, b * 0.6f, a).next();
        buffer.vertex(matrix, x2, y1, z1).color(r * 0.6f, g * 0.6f, b * 0.6f, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r * 0.6f, g * 0.6f, b * 0.6f, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r * 0.6f, g * 0.6f, b * 0.6f, a).next();
    }
    
    /**
     * Obtiene un color básico para un tipo de bloque
     */
    private int getBlockColor(String blockId) {
        if (blockId.contains("stone")) return 0x808080;
        if (blockId.contains("wood") || blockId.contains("planks")) return 0x8B4513;
        if (blockId.contains("dirt")) return 0x654321;
        if (blockId.contains("grass")) return 0x7CFC00;
        if (blockId.contains("glass")) return 0x87CEEB;
        if (blockId.contains("brick")) return 0xB22222;
        if (blockId.contains("gold")) return 0xFFD700;
        if (blockId.contains("iron")) return 0xC0C0C0;
        if (blockId.contains("diamond")) return 0x00FFFF;
        if (blockId.contains("wool")) return 0xFFFFFF;
        return 0xCCCCCC; // Gris por defecto
    }
    
    /**
     * Renderiza texto de ayuda
     */
    private void renderHelpText(DrawContext context) {
        String help = "Arrastra para rotar | Scroll para zoom";
        int helpWidth = client.textRenderer.getWidth(help);
        context.drawText(
            client.textRenderer,
            help,
            x + width - helpWidth - 5,
            y + height - 15,
            0x808080,
            false
        );
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && button == 0) {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging) {
            rotationY += (float) deltaX * ROTATION_SPEED;
            rotationX += (float) deltaY * ROTATION_SPEED;
            
            // Limitar rotación X
            rotationX = Math.max(-89.0f, Math.min(89.0f, rotationX));
            
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isMouseOver(mouseX, mouseY)) {
            if (verticalAmount > 0) {
                zoomIn();
            } else {
                zoomOut();
            }
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    
    @Override
    public void setFocused(boolean focused) {
    }
    
    @Override
    public boolean isFocused() {
        return false;
    }
}