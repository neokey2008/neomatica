package com.neokey.neomatica.tools;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.HashMap;
import java.util.Map;

/**
 * Herramienta para voltear schematics
 */
public class FlipTool {
    
    private final ToolManager toolManager;
    
    public FlipTool(ToolManager toolManager) {
        this.toolManager = toolManager;
    }
    
    /**
     * Voltea un schematic en el eje X
     */
    public LoadedSchematic flipX(LoadedSchematic schematic) {
        return flip(schematic, true, false, false);
    }
    
    /**
     * Voltea un schematic en el eje Y
     */
    public LoadedSchematic flipY(LoadedSchematic schematic) {
        return flip(schematic, false, true, false);
    }
    
    /**
     * Voltea un schematic en el eje Z
     */
    public LoadedSchematic flipZ(LoadedSchematic schematic) {
        return flip(schematic, false, false, true);
    }
    
    /**
     * Voltea un schematic en los ejes especificados
     */
    public LoadedSchematic flip(LoadedSchematic schematic, boolean flipX, boolean flipY, boolean flipZ) {
        if (schematic == null) {
            return null;
        }
        
        if (!flipX && !flipY && !flipZ) {
            return schematic; // No hay nada que voltear
        }
        
        try {
            String suffix = "";
            if (flipX) suffix += "X";
            if (flipY) suffix += "Y";
            if (flipZ) suffix += "Z";
            
            LoadedSchematic flipped = new LoadedSchematic(schematic.getName() + "_flip" + suffix);
            
            Vec3i size = schematic.getSize();
            flipped.setSize(size);
            flipped.setOrigin(schematic.getOrigin());
            flipped.setPlacement(schematic.getPlacement());
            
            // Voltear cada bloque
            for (var entry : schematic.getBlocks().entrySet()) {
                BlockPos oldPos = entry.getKey();
                SchematicBlock block = entry.getValue();
                
                int newX = flipX ? (size.getX() - 1 - oldPos.getX()) : oldPos.getX();
                int newY = flipY ? (size.getY() - 1 - oldPos.getY()) : oldPos.getY();
                int newZ = flipZ ? (size.getZ() - 1 - oldPos.getZ()) : oldPos.getZ();
                
                BlockPos newPos = new BlockPos(newX, newY, newZ);
                
                SchematicBlock flippedBlock = new SchematicBlock(block.getBlockId());
                flippedBlock.setProperties(flipBlockProperties(block, flipX, flipY, flipZ));
                
                flipped.addBlock(newPos, flippedBlock);
            }
            
            Neomatica.LOGGER.info("Schematic volteado - X:{} Y:{} Z:{}", flipX, flipY, flipZ);
            return flipped;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al voltear schematic", e);
            return schematic;
        }
    }
    
    /**
     * Voltea las propiedades de un bloque
     */
    private Map<String, String> flipBlockProperties(SchematicBlock block, boolean flipX, boolean flipY, boolean flipZ) {
        Map<String, String> flippedProps = new HashMap<>(block.getProperties());
        
        // Voltear la propiedad 'facing' si existe
        if (flippedProps.containsKey("facing")) {
            String facing = flippedProps.get("facing");
            String flippedFacing = flipFacing(facing, flipX, flipY, flipZ);
            flippedProps.put("facing", flippedFacing);
        }
        
        // Voltear la propiedad 'half' si existe (escaleras, losas)
        if (flipY && flippedProps.containsKey("half")) {
            String half = flippedProps.get("half");
            flippedProps.put("half", half.equals("top") ? "bottom" : "top");
        }
        
        // Voltear la propiedad 'type' si existe (losas)
        if (flipY && flippedProps.containsKey("type")) {
            String type = flippedProps.get("type");
            if (type.equals("top")) {
                flippedProps.put("type", "bottom");
            } else if (type.equals("bottom")) {
                flippedProps.put("type", "top");
            }
        }
        
        // Voltear la propiedad 'hinge' si existe (puertas)
        if ((flipX || flipZ) && flippedProps.containsKey("hinge")) {
            String hinge = flippedProps.get("hinge");
            flippedProps.put("hinge", hinge.equals("left") ? "right" : "left");
        }
        
        return flippedProps;
    }
    
    /**
     * Voltea un valor de facing
     */
    private String flipFacing(String facing, boolean flipX, boolean flipY, boolean flipZ) {
        if (flipY) {
            if (facing.equals("up")) return "down";
            if (facing.equals("down")) return "up";
        }
        
        if (flipX) {
            if (facing.equals("east")) return "west";
            if (facing.equals("west")) return "east";
        }
        
        if (flipZ) {
            if (facing.equals("north")) return "south";
            if (facing.equals("south")) return "north";
        }
        
        return facing;
    }
    
    /**
     * Voltea horizontal (X y Z)
     */
    public LoadedSchematic flipHorizontal(LoadedSchematic schematic) {
        return flip(schematic, true, false, true);
    }
    
    /**
     * Voltea verticalmente (Y)
     */
    public LoadedSchematic flipVertical(LoadedSchematic schematic) {
        return flip(schematic, false, true, false);
    }
    
    /**
     * Crea una imagen espejo del schematic
     */
    public LoadedSchematic mirror(LoadedSchematic schematic, MirrorAxis axis) {
        return switch (axis) {
            case X -> flipX(schematic);
            case Y -> flipY(schematic);
            case Z -> flipZ(schematic);
            case XZ -> flip(schematic, true, false, true);
        };
    }
    
    /**
     * Ejes de espejo
     */
    public enum MirrorAxis {
        X("Eje X (Este-Oeste)"),
        Y("Eje Y (Arriba-Abajo)"),
        Z("Eje Z (Norte-Sur)"),
        XZ("Plano Horizontal");
        
        private final String displayName;
        
        MirrorAxis(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}