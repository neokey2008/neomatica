package com.neokey.neomatica.tools;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Herramienta para pegar schematics
 */
public class PasteTool {
    
    private final ToolManager toolManager;
    private final MinecraftClient client;
    
    private LoadedSchematic previewSchematic;
    private BlockPos previewPosition;
    
    public PasteTool(ToolManager toolManager) {
        this.toolManager = toolManager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Actualiza la herramienta cada tick
     */
    public void tick() {
        // Actualizar posición del preview si es necesario
        if (previewSchematic != null && client.player != null) {
            previewPosition = client.player.getBlockPos();
        }
    }
    
    /**
     * Pega un schematic en una posición
     * Nota: En modo client-side, esto solo coloca el schematic como overlay visual
     */
    public boolean paste(LoadedSchematic schematic, BlockPos position) {
        if (schematic == null || position == null) {
            return false;
        }
        
        try {
            // En modo client-side, establecer el schematic como activo para visualización
            schematic.setPlacement(position);
            schematic.setVisible(true);
            
            Neomatica.getInstance().getSchematicManager().setActiveSchematic(schematic.getId());
            
            Neomatica.LOGGER.info("Schematic colocado en: {}", position);
            return true;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al pegar schematic", e);
            return false;
        }
    }
    
    /**
     * Pega un schematic con opciones
     */
    public boolean pasteWithOptions(LoadedSchematic schematic, BlockPos position, PasteOptions options) {
        if (schematic == null || position == null) {
            return false;
        }
        
        try {
            // Clonar el schematic para aplicar opciones
            LoadedSchematic modifiedSchematic = cloneSchematic(schematic);
            
            // Aplicar rotación si está configurada
            if (options.rotation != 0) {
                modifiedSchematic = rotateSchematic(modifiedSchematic, options.rotation);
            }
            
            // Aplicar flip si está configurado
            if (options.flipX || options.flipY || options.flipZ) {
                modifiedSchematic = flipSchematic(modifiedSchematic, options.flipX, options.flipY, options.flipZ);
            }
            
            // Aplicar offset
            BlockPos finalPosition = position.add(options.offsetX, options.offsetY, options.offsetZ);
            
            return paste(modifiedSchematic, finalPosition);
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al pegar con opciones", e);
            return false;
        }
    }
    
    /**
     * Pega solo bloques de aire (para crear huecos)
     */
    public boolean pasteAirOnly(LoadedSchematic schematic, BlockPos position) {
        // En client-side, esto es principalmente visual
        return paste(schematic, position);
    }
    
    /**
     * Pega sin reemplazar bloques existentes
     */
    public boolean pasteNonDestructive(LoadedSchematic schematic, BlockPos position) {
        // TODO: Implementar lógica para no reemplazar bloques existentes
        return paste(schematic, position);
    }
    
    /**
     * Establece el schematic de preview
     */
    public void setPreview(LoadedSchematic schematic, BlockPos position) {
        this.previewSchematic = schematic;
        this.previewPosition = position;
        
        if (schematic != null) {
            schematic.setPlacement(position);
            schematic.setOpacity(0.5f); // Semi-transparente para preview
        }
    }
    
    /**
     * Limpia el preview
     */
    public void clearPreview() {
        this.previewSchematic = null;
        this.previewPosition = null;
    }
    
    /**
     * Confirma el preview (pega el schematic)
     */
    public boolean confirmPreview() {
        if (previewSchematic == null || previewPosition == null) {
            return false;
        }
        
        boolean success = paste(previewSchematic, previewPosition);
        
        if (success) {
            clearPreview();
        }
        
        return success;
    }
    
    /**
     * Clona un schematic
     */
    private LoadedSchematic cloneSchematic(LoadedSchematic original) {
        LoadedSchematic clone = new LoadedSchematic(original.getName() + "_clone");
        clone.setSize(original.getSize());
        clone.setOrigin(original.getOrigin());
        
        for (var entry : original.getBlocks().entrySet()) {
            SchematicBlock originalBlock = entry.getValue();
            SchematicBlock clonedBlock = new SchematicBlock(originalBlock.getBlockId());
            clonedBlock.setProperties(originalBlock.getProperties());
            
            clone.addBlock(entry.getKey(), clonedBlock);
        }
        
        return clone;
    }
    
    /**
     * Rota un schematic
     */
    private LoadedSchematic rotateSchematic(LoadedSchematic schematic, int degrees) {
        // TODO: Implementar rotación real
        // Por ahora retorna el schematic sin modificar
        Neomatica.LOGGER.warn("Rotación de schematics no implementada aún");
        return schematic;
    }
    
    /**
     * Voltea un schematic
     */
    private LoadedSchematic flipSchematic(LoadedSchematic schematic, boolean flipX, boolean flipY, boolean flipZ) {
        // TODO: Implementar flip real
        Neomatica.LOGGER.warn("Flip de schematics no implementado aún");
        return schematic;
    }
    
    /**
     * Valida si un bloque puede ser colocado
     */
    private boolean canPlaceBlock(BlockPos pos, String blockId) {
        if (client.world == null) {
            return false;
        }
        
        // Verificar límites del mundo
        if (!client.world.isInBuildLimit(pos)) {
            return false;
        }
        
        // Verificar si el bloque existe
        Identifier id = Identifier.tryParse(blockId);
        if (id == null) {
            return false;
        }
        
        Block block = Registries.BLOCK.get(id);
        return block != null;
    }
    
    // Getters
    
    public LoadedSchematic getPreviewSchematic() {
        return previewSchematic;
    }
    
    public BlockPos getPreviewPosition() {
        return previewPosition;
    }
    
    public boolean hasPreview() {
        return previewSchematic != null;
    }
    
    /**
     * Opciones de pegado
     */
    public static class PasteOptions {
        public int rotation = 0; // 0, 90, 180, 270
        public boolean flipX = false;
        public boolean flipY = false;
        public boolean flipZ = false;
        public int offsetX = 0;
        public int offsetY = 0;
        public int offsetZ = 0;
        public boolean replaceAir = false;
        public boolean replaceBlocks = true;
        
        public PasteOptions() {
        }
        
        public static PasteOptions defaults() {
            return new PasteOptions();
        }
        
        public PasteOptions withRotation(int degrees) {
            this.rotation = degrees;
            return this;
        }
        
        public PasteOptions withFlip(boolean x, boolean y, boolean z) {
            this.flipX = x;
            this.flipY = y;
            this.flipZ = z;
            return this;
        }
        
        public PasteOptions withOffset(int x, int y, int z) {
            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
            return this;
        }
        
        public PasteOptions nonDestructive() {
            this.replaceBlocks = false;
            return this;
        }
    }
}