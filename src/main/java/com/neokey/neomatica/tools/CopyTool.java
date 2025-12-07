package com.neokey.neomatica.tools;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * Herramienta para copiar áreas
 */
public class CopyTool {
    
    private final ToolManager toolManager;
    private final MinecraftClient client;
    
    public CopyTool(ToolManager toolManager) {
        this.toolManager = toolManager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Copia el área seleccionada
     */
    public LoadedSchematic copy(BlockPos pos1, BlockPos pos2) {
        if (client.world == null) {
            return null;
        }
        
        try {
            // Calcular dimensiones
            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            
            int sizeX = maxX - minX + 1;
            int sizeY = maxY - minY + 1;
            int sizeZ = maxZ - minZ + 1;
            
            // Crear schematic
            String name = "Copy_" + System.currentTimeMillis();
            LoadedSchematic schematic = new LoadedSchematic(name);
            schematic.setSize(new Vec3i(sizeX, sizeY, sizeZ));
            schematic.setOrigin(new BlockPos(minX, minY, minZ));
            
            // Copiar bloques
            int blockCount = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos worldPos = new BlockPos(x, y, z);
                        BlockState state = client.world.getBlockState(worldPos);
                        
                        // Ignorar aire
                        if (state.isAir()) {
                            continue;
                        }
                        
                        // Posición relativa en el schematic
                        BlockPos relativePos = new BlockPos(
                            x - minX,
                            y - minY,
                            z - minZ
                        );
                        
                        // Crear bloque del schematic
                        String blockId = Registries.BLOCK.getId(state.getBlock()).toString();
                        SchematicBlock schematicBlock = new SchematicBlock(blockId);
                        
                        // Copiar propiedades del bloque
                        state.getEntries().forEach((property, value) -> {
                            schematicBlock.setProperty(
                                property.getName(),
                                value.toString()
                            );
                        });
                        
                        schematic.addBlock(relativePos, schematicBlock);
                        blockCount++;
                    }
                }
            }
            
            Neomatica.LOGGER.info("Área copiada: {} bloques", blockCount);
            return schematic;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al copiar área", e);
            return null;
        }
    }
    
    /**
     * Copia solo bloques específicos del área
     */
    public LoadedSchematic copyFiltered(BlockPos pos1, BlockPos pos2, String... blockIds) {
        if (client.world == null) {
            return null;
        }
        
        LoadedSchematic fullCopy = copy(pos1, pos2);
        if (fullCopy == null) {
            return null;
        }
        
        // Filtrar bloques
        LoadedSchematic filtered = new LoadedSchematic(fullCopy.getName() + "_filtered");
        filtered.setSize(fullCopy.getSize());
        filtered.setOrigin(fullCopy.getOrigin());
        
        for (var entry : fullCopy.getBlocks().entrySet()) {
            String blockId = entry.getValue().getBlockId();
            
            for (String filter : blockIds) {
                if (blockId.contains(filter)) {
                    filtered.addBlock(entry.getKey(), entry.getValue());
                    break;
                }
            }
        }
        
        return filtered;
    }
    
    /**
     * Copia un área con un offset
     */
    public LoadedSchematic copyWithOffset(BlockPos pos1, BlockPos pos2, int offsetX, int offsetY, int offsetZ) {
        BlockPos newPos1 = pos1.add(offsetX, offsetY, offsetZ);
        BlockPos newPos2 = pos2.add(offsetX, offsetY, offsetZ);
        
        return copy(newPos1, newPos2);
    }
    
    /**
     * Copia solo la capa superior del área
     */
    public LoadedSchematic copyTopLayer(BlockPos pos1, BlockPos pos2) {
        int maxY = Math.max(pos1.getY(), pos2.getY());
        BlockPos layerPos1 = new BlockPos(pos1.getX(), maxY, pos1.getZ());
        BlockPos layerPos2 = new BlockPos(pos2.getX(), maxY, pos2.getZ());
        
        return copy(layerPos1, layerPos2);
    }
    
    /**
     * Copia múltiples áreas y las combina
     */
    public LoadedSchematic copyMultipleAreas(BlockPos[]... areas) {
        if (areas.length == 0) {
            return null;
        }
        
        // Crear schematic combinado
        LoadedSchematic combined = new LoadedSchematic("Combined_" + System.currentTimeMillis());
        
        // Encontrar dimensiones totales
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        
        for (BlockPos[] area : areas) {
            if (area.length != 2) continue;
            
            minX = Math.min(minX, Math.min(area[0].getX(), area[1].getX()));
            minY = Math.min(minY, Math.min(area[0].getY(), area[1].getY()));
            minZ = Math.min(minZ, Math.min(area[0].getZ(), area[1].getZ()));
            
            maxX = Math.max(maxX, Math.max(area[0].getX(), area[1].getX()));
            maxY = Math.max(maxY, Math.max(area[0].getY(), area[1].getY()));
            maxZ = Math.max(maxZ, Math.max(area[0].getZ(), area[1].getZ()));
        }
        
        combined.setSize(new Vec3i(
            maxX - minX + 1,
            maxY - minY + 1,
            maxZ - minZ + 1
        ));
        combined.setOrigin(new BlockPos(minX, minY, minZ));
        
        // Copiar cada área
        for (BlockPos[] area : areas) {
            if (area.length != 2) continue;
            
            LoadedSchematic areaCopy = copy(area[0], area[1]);
            if (areaCopy == null) continue;
            
            // Agregar bloques al schematic combinado
            for (var entry : areaCopy.getBlocks().entrySet()) {
                BlockPos relativePos = entry.getKey();
                BlockPos absolutePos = areaCopy.getOrigin().add(relativePos);
                BlockPos combinedPos = new BlockPos(
                    absolutePos.getX() - minX,
                    absolutePos.getY() - minY,
                    absolutePos.getZ() - minZ
                );
                
                combined.addBlock(combinedPos, entry.getValue());
            }
        }
        
        return combined;
    }
    
    /**
     * Obtiene el número de bloques en un área
     */
    public int countBlocksInArea(BlockPos pos1, BlockPos pos2) {
        if (client.world == null) {
            return 0;
        }
        
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        int count = 0;
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!client.world.getBlockState(pos).isAir()) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
}