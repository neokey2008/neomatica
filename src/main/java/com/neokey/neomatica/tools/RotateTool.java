package com.neokey.neomatica.tools;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/**
 * Herramienta para rotar schematics
 */
public class RotateTool {
    
    private final ToolManager toolManager;
    
    public RotateTool(ToolManager toolManager) {
        this.toolManager = toolManager;
    }
    
    /**
     * Rota un schematic 90 grados en sentido horario (eje Y)
     */
    public LoadedSchematic rotateClockwise(LoadedSchematic schematic) {
        return rotate(schematic, 90);
    }
    
    /**
     * Rota un schematic 90 grados en sentido antihorario (eje Y)
     */
    public LoadedSchematic rotateCounterClockwise(LoadedSchematic schematic) {
        return rotate(schematic, -90);
    }
    
    /**
     * Rota un schematic 180 grados (eje Y)
     */
    public LoadedSchematic rotate180(LoadedSchematic schematic) {
        return rotate(schematic, 180);
    }
    
    /**
     * Rota un schematic un número específico de grados (eje Y)
     */
    public LoadedSchematic rotate(LoadedSchematic schematic, int degrees) {
        if (schematic == null) {
            return null;
        }
        
        // Normalizar grados a 0, 90, 180, 270
        degrees = ((degrees % 360) + 360) % 360;
        
        if (degrees == 0) {
            return schematic;
        }
        
        try {
            LoadedSchematic rotated = new LoadedSchematic(schematic.getName() + "_rot" + degrees);
            
            Vec3i oldSize = schematic.getSize();
            Vec3i newSize;
            
            // Calcular nuevo tamaño según rotación
            if (degrees == 90 || degrees == 270) {
                newSize = new Vec3i(oldSize.getZ(), oldSize.getY(), oldSize.getX());
            } else {
                newSize = oldSize;
            }
            
            rotated.setSize(newSize);
            rotated.setOrigin(schematic.getOrigin());
            rotated.setPlacement(schematic.getPlacement());
            
            // Rotar cada bloque
            for (var entry : schematic.getBlocks().entrySet()) {
                BlockPos oldPos = entry.getKey();
                SchematicBlock block = entry.getValue();
                
                BlockPos newPos = rotatePosition(oldPos, oldSize, degrees);
                
                SchematicBlock rotatedBlock = new SchematicBlock(block.getBlockId());
                rotatedBlock.setProperties(rotateBlockProperties(block, degrees));
                
                rotated.addBlock(newPos, rotatedBlock);
            }
            
            Neomatica.LOGGER.info("Schematic rotado {} grados", degrees);
            return rotated;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al rotar schematic", e);
            return schematic;
        }
    }
    
    /**
     * Rota una posición según los grados especificados
     */
    private BlockPos rotatePosition(BlockPos pos, Vec3i size, int degrees) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        
        return switch (degrees) {
            case 90 -> new BlockPos(size.getZ() - 1 - z, y, x);
            case 180 -> new BlockPos(size.getX() - 1 - x, y, size.getZ() - 1 - z);
            case 270 -> new BlockPos(z, y, size.getX() - 1 - x);
            default -> pos;
        };
    }
    
    /**
     * Rota las propiedades de un bloque (como facing)
     */
    private java.util.Map<String, String> rotateBlockProperties(SchematicBlock block, int degrees) {
        java.util.Map<String, String> rotatedProps = new java.util.HashMap<>(block.getProperties());
        
        // Rotar la propiedad 'facing' si existe
        if (rotatedProps.containsKey("facing")) {
            String facing = rotatedProps.get("facing");
            String rotatedFacing = rotateFacing(facing, degrees);
            rotatedProps.put("facing", rotatedFacing);
        }
        
        // Rotar la propiedad 'axis' si existe
        if (rotatedProps.containsKey("axis")) {
            String axis = rotatedProps.get("axis");
            String rotatedAxis = rotateAxis(axis, degrees);
            rotatedProps.put("axis", rotatedAxis);
        }
        
        return rotatedProps;
    }
    
    /**
     * Rota un valor de facing
     */
    private String rotateFacing(String facing, int degrees) {
        if (facing.equals("up") || facing.equals("down")) {
            return facing; // No rotar arriba/abajo
        }
        
        String[] horizontals = {"north", "east", "south", "west"};
        int index = java.util.Arrays.asList(horizontals).indexOf(facing);
        
        if (index == -1) {
            return facing;
        }
        
        int rotations = degrees / 90;
        int newIndex = (index + rotations + 4) % 4;
        
        return horizontals[newIndex];
    }
    
    /**
     * Rota un valor de axis
     */
    private String rotateAxis(String axis, int degrees) {
        if (axis.equals("y")) {
            return axis; // No rotar eje Y
        }
        
        if (degrees == 90 || degrees == 270) {
            return axis.equals("x") ? "z" : "x";
        }
        
        return axis;
    }
    
    /**
     * Rota alrededor del eje X (pitch)
     */
    public LoadedSchematic rotateAroundX(LoadedSchematic schematic, int degrees) {
        if (schematic == null) {
            return null;
        }
        
        degrees = ((degrees % 360) + 360) % 360;
        
        if (degrees == 0) {
            return schematic;
        }
        
        try {
            LoadedSchematic rotated = new LoadedSchematic(schematic.getName() + "_rotX" + degrees);
            
            Vec3i oldSize = schematic.getSize();
            Vec3i newSize;
            
            if (degrees == 90 || degrees == 270) {
                newSize = new Vec3i(oldSize.getX(), oldSize.getZ(), oldSize.getY());
            } else {
                newSize = oldSize;
            }
            
            rotated.setSize(newSize);
            rotated.setOrigin(schematic.getOrigin());
            
            for (var entry : schematic.getBlocks().entrySet()) {
                BlockPos oldPos = entry.getKey();
                SchematicBlock block = entry.getValue();
                
                int x = oldPos.getX();
                int y = oldPos.getY();
                int z = oldPos.getZ();
                
                BlockPos newPos = switch (degrees) {
                    case 90 -> new BlockPos(x, oldSize.getZ() - 1 - z, y);
                    case 180 -> new BlockPos(x, oldSize.getY() - 1 - y, oldSize.getZ() - 1 - z);
                    case 270 -> new BlockPos(x, z, oldSize.getY() - 1 - y);
                    default -> oldPos;
                };
                
                SchematicBlock rotatedBlock = new SchematicBlock(block.getBlockId());
                rotatedBlock.setProperties(block.getProperties());
                
                rotated.addBlock(newPos, rotatedBlock);
            }
            
            return rotated;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al rotar alrededor de X", e);
            return schematic;
        }
    }
    
    /**
     * Rota alrededor del eje Z (roll)
     */
    public LoadedSchematic rotateAroundZ(LoadedSchematic schematic, int degrees) {
        if (schematic == null) {
            return null;
        }
        
        degrees = ((degrees % 360) + 360) % 360;
        
        if (degrees == 0) {
            return schematic;
        }
        
        try {
            LoadedSchematic rotated = new LoadedSchematic(schematic.getName() + "_rotZ" + degrees);
            
            Vec3i oldSize = schematic.getSize();
            Vec3i newSize;
            
            if (degrees == 90 || degrees == 270) {
                newSize = new Vec3i(oldSize.getY(), oldSize.getX(), oldSize.getZ());
            } else {
                newSize = oldSize;
            }
            
            rotated.setSize(newSize);
            rotated.setOrigin(schematic.getOrigin());
            
            for (var entry : schematic.getBlocks().entrySet()) {
                BlockPos oldPos = entry.getKey();
                SchematicBlock block = entry.getValue();
                
                int x = oldPos.getX();
                int y = oldPos.getY();
                int z = oldPos.getZ();
                
                BlockPos newPos = switch (degrees) {
                    case 90 -> new BlockPos(oldSize.getY() - 1 - y, x, z);
                    case 180 -> new BlockPos(oldSize.getX() - 1 - x, oldSize.getY() - 1 - y, z);
                    case 270 -> new BlockPos(y, oldSize.getX() - 1 - x, z);
                    default -> oldPos;
                };
                
                SchematicBlock rotatedBlock = new SchematicBlock(block.getBlockId());
                rotatedBlock.setProperties(block.getProperties());
                
                rotated.addBlock(newPos, rotatedBlock);
            }
            
            return rotated;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al rotar alrededor de Z", e);
            return schematic;
        }
    }
}