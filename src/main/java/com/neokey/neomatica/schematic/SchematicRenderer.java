package com.neokey.neomatica.schematic;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;

import java.util.Map;

/**
 * Renderizador de schematics en el mundo
 */
public class SchematicRenderer {
    
    private final MinecraftClient client;
    private boolean enabled = true;
    private RenderMode renderMode = RenderMode.TRANSLUCENT;
    
    public SchematicRenderer() {
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Renderiza un schematic en el mundo
     */
    public void render(LoadedSchematic schematic, MatrixStack matrices, float tickDelta) {
        if (!enabled || schematic == null || !schematic.isVisible()) {
            return;
        }
        
        if (client.player == null || client.world == null) {
            return;
        }
        
        try {
            matrices.push();
            
            // Obtener posición del jugador para calcular offset
            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            BlockPos placement = schematic.getPlacement();
            
            // Trasladar a la posición de colocación
            matrices.translate(
                placement.getX() - cameraPos.x,
                placement.getY() - cameraPos.y,
                placement.getZ() - cameraPos.z
            );
            
            // Renderizar bloques
            renderBlocks(schematic, matrices, tickDelta);
            
            matrices.pop();
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al renderizar schematic", e);
        }
    }
    
    /**
     * Renderiza los bloques del schematic
     */
    private void renderBlocks(LoadedSchematic schematic, MatrixStack matrices, float tickDelta) {
        Map<BlockPos, SchematicBlock> blocks = schematic.getBlocks();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        // Configurar renderizado
        setupRenderState();
        
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        
        for (Map.Entry<BlockPos, SchematicBlock> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();
            SchematicBlock schematicBlock = entry.getValue();
            
            renderBlock(pos, schematicBlock, matrices, buffer, schematic.getOpacity());
        }
        
        tessellator.draw();
        restoreRenderState();
    }
    
    /**
     * Renderiza un bloque individual
     */
    private void renderBlock(BlockPos pos, SchematicBlock schematicBlock, 
                            MatrixStack matrices, BufferBuilder buffer, float opacity) {
        try {
            // Obtener el block state de Minecraft
            Identifier blockId = Identifier.tryParse(schematicBlock.getBlockId());
            if (blockId == null) {
                return;
            }
            
            Block block = Registries.BLOCK.get(blockId);
            BlockState state = block.getDefaultState();
            
            // Obtener color del bloque
            int color = getBlockColor(state);
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;
            float a = opacity;
            
            // Renderizar las caras del bloque
            renderBlockBox(pos, matrices, buffer, r, g, b, a);
            
        } catch (Exception e) {
            // Ignorar errores de bloques individuales
        }
    }
    
    /**
     * Renderiza una caja de bloque
     */
    private void renderBlockBox(BlockPos pos, MatrixStack matrices, BufferBuilder buffer,
                                float r, float g, float b, float a) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        
        float x1 = pos.getX();
        float y1 = pos.getY();
        float z1 = pos.getZ();
        float x2 = x1 + 1.0f;
        float y2 = y1 + 1.0f;
        float z2 = z1 + 1.0f;
        
        // Ajustar ligeramente para evitar z-fighting
        float offset = 0.001f;
        x1 += offset;
        y1 += offset;
        z1 += offset;
        x2 -= offset;
        y2 -= offset;
        z2 -= offset;
        
        // Cara inferior (Y-)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();
        
        // Cara superior (Y+)
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
        
        // Cara norte (Z-)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();
        
        // Cara sur (Z+)
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
        
        // Cara oeste (X-)
        buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).next();
        
        // Cara este (X+)
        buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).next();
        buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).next();
    }
    
    /**
     * Configura el estado de renderizado
     */
    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    }
    
    /**
     * Restaura el estado de renderizado
     */
    private void restoreRenderState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    
    /**
     * Obtiene el color de un bloque
     */
    private int getBlockColor(BlockState state) {
        // Colores básicos para diferentes tipos de bloques
        String blockName = Registries.BLOCK.getId(state.getBlock()).getPath();
        
        // Madera
        if (blockName.contains("planks") || blockName.contains("log")) {
            return 0x8B4513; // Marrón
        }
        // Piedra
        if (blockName.contains("stone") || blockName.contains("cobblestone")) {
            return 0x808080; // Gris
        }
        // Tierra
        if (blockName.contains("dirt") || blockName.contains("grass_block")) {
            return 0x8B4513; // Marrón tierra
        }
        // Cristal
        if (blockName.contains("glass")) {
            return 0x87CEEB; // Azul claro
        }
        // Lana
        if (blockName.contains("wool")) {
            return getWoolColor(blockName);
        }
        // Ladrillos
        if (blockName.contains("brick")) {
            return 0xB22222; // Rojo ladrillo
        }
        // Oro
        if (blockName.contains("gold")) {
            return 0xFFD700; // Dorado
        }
        // Hierro
        if (blockName.contains("iron")) {
            return 0xC0C0C0; // Plateado
        }
        // Diamante
        if (blockName.contains("diamond")) {
            return 0x00FFFF; // Cian
        }
        // Esmeralda
        if (blockName.contains("emerald")) {
            return 0x00FF00; // Verde
        }
        
        // Color por defecto
        return 0xFFFFFF; // Blanco
    }
    
    /**
     * Obtiene el color específico de lana
     */
    private int getWoolColor(String blockName) {
        if (blockName.contains("white")) return 0xFFFFFF;
        if (blockName.contains("orange")) return 0xFF8C00;
        if (blockName.contains("magenta")) return 0xFF00FF;
        if (blockName.contains("light_blue")) return 0x87CEEB;
        if (blockName.contains("yellow")) return 0xFFFF00;
        if (blockName.contains("lime")) return 0x00FF00;
        if (blockName.contains("pink")) return 0xFFC0CB;
        if (blockName.contains("gray")) return 0x808080;
        if (blockName.contains("light_gray")) return 0xD3D3D3;
        if (blockName.contains("cyan")) return 0x00FFFF;
        if (blockName.contains("purple")) return 0x800080;
        if (blockName.contains("blue")) return 0x0000FF;
        if (blockName.contains("brown")) return 0x8B4513;
        if (blockName.contains("green")) return 0x008000;
        if (blockName.contains("red")) return 0xFF0000;
        if (blockName.contains("black")) return 0x000000;
        return 0xFFFFFF; // Blanco por defecto
    }
    
    /**
     * Verifica si un schematic es visible desde la posición de la cámara
     */
    public boolean isSchematicVisible(LoadedSchematic schematic, double renderDistance) {
        if (client.player == null) {
            return false;
        }
        
        Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
        BlockPos placement = schematic.getPlacement();
        
        double distance = cameraPos.distanceTo(new Vec3d(
            placement.getX(), 
            placement.getY(), 
            placement.getZ()
        ));
        
        return distance <= renderDistance;
    }
    
    // Getters y Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public RenderMode getRenderMode() {
        return renderMode;
    }
    
    public void setRenderMode(RenderMode renderMode) {
        this.renderMode = renderMode;
    }
    
    /**
     * Modos de renderizado
     */
    public enum RenderMode {
        SOLID("Sólido"),
        TRANSLUCENT("Translúcido"),
        WIREFRAME("Esqueleto"),
        GHOST("Fantasma");
        
        private final String displayName;
        
        RenderMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}