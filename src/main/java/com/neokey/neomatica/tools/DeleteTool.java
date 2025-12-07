package com.neokey.neomatica.tools;

import com.neokey.neomatica.Neomatica;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Herramienta para eliminar bloques
 * Nota: En client-side, esto es principalmente para visualización
 */
public class DeleteTool {
    
    private final ToolManager toolManager;
    private final MinecraftClient client;
    
    public DeleteTool(ToolManager toolManager) {
        this.toolManager = toolManager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Elimina bloques en un área
     * En client-side, esto marca el área para eliminación visual
     */
    public boolean delete(BlockPos pos1, BlockPos pos2) {
        if (client.world == null) {
            return false;
        }
        
        try {
            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            
            int deletedCount = 0;
            
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        
                        if (!client.world.getBlockState(pos).isAir()) {
                            // En client-side, solo contamos
                            deletedCount++;
                        }
                    }
                }
            }
            
            Neomatica.LOGGER.info("Área marcada para eliminación: {} bloques", deletedCount);
            return true;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al eliminar área", e);
            return false;
        }
    }
    
    /**
     * Elimina solo bloques específicos
     */
    public boolean deleteFiltered(BlockPos pos1, BlockPos pos2, String... blockIds) {
        if (client.world == null) {
            return false;
        }
        
        try {
            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            
            int deletedCount = 0;
            
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        String blockId = client.world.getBlockState(pos).getBlock().toString();
                        
                        for (String filter : blockIds) {
                            if (blockId.contains(filter)) {
                                deletedCount++;
                                break;
                            }
                        }
                    }
                }
            }
            
            Neomatica.LOGGER.info("Bloques filtrados marcados: {}", deletedCount);
            return true;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al eliminar bloques filtrados", e);
            return false;
        }
    }
    
    /**
     * Elimina solo aire (rellena el área)
     */
    public boolean deleteAir(BlockPos pos1, BlockPos pos2, String fillBlock) {
        if (client.world == null) {
            return false;
        }
        
        try {
            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            
            int filledCount = 0;
            
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        
                        // Solo rellenar si es aire
                        if (client.world.getBlockState(pos).isAir()) {
                            filledCount++;
                        }
                    }
                }
            }
            
            Neomatica.LOGGER.info("Aire marcado para relleno: {} bloques con {}", filledCount, fillBlock);
            return true;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al rellenar aire", e);
            return false;
        }
    }
    
    /**
     * Elimina la capa superior
     */
    public boolean deleteTopLayer(BlockPos pos1, BlockPos pos2) {
        int maxY = Math.max(pos1.getY(), pos2.getY());
        BlockPos layerPos1 = new BlockPos(pos1.getX(), maxY, pos1.getZ());
        BlockPos layerPos2 = new BlockPos(pos2.getX(), maxY, pos2.getZ());
        
        return delete(layerPos1, layerPos2);
    }
    
    /**
     * Elimina la capa inferior
     */
    public boolean deleteBottomLayer(BlockPos pos1, BlockPos pos2) {
        int minY = Math.min(pos1.getY(), pos2.getY());
        BlockPos layerPos1 = new BlockPos(pos1.getX(), minY, pos1.getZ());
        BlockPos layerPos2 = new BlockPos(pos2.getX(), minY, pos2.getZ());
        
        return delete(layerPos1, layerPos2);
    }
    
    /**
     * Elimina bordes del área
     */
    public boolean deleteEdges(BlockPos pos1, BlockPos pos2) {
        if (client.world == null) {
            return false;
        }
        
        try {
            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            
            int edgeCount = 0;
            
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        // Verificar si está en el borde
                        boolean isEdge = x == minX || x == maxX ||
                                       y == minY || y == maxY ||
                                       z == minZ || z == maxZ;
                        
                        if (isEdge) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (!client.world.getBlockState(pos).isAir()) {
                                edgeCount++;
                            }
                        }
                    }
                }
            }
            
            Neomatica.LOGGER.info("Bordes marcados para eliminación: {} bloques", edgeCount);
            return true;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al eliminar bordes", e);
            return false;
        }
    }
    
    /**
     * Elimina el interior dejando los bordes
     */
    public boolean hollowArea(BlockPos pos1, BlockPos pos2) {
        if (client.world == null) {
            return false;
        }
        
        try {
            int minX = Math.min(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());
            
            int hollowedCount = 0;
            
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        // Verificar si NO está en el borde
                        boolean isInterior = x > minX && x < maxX &&
                                           y > minY && y < maxY &&
                                           z > minZ && z < maxZ;
                        
                        if (isInterior) {
                            BlockPos pos = new BlockPos(x, y, z);
                            if (!client.world.getBlockState(pos).isAir()) {
                                hollowedCount++;
                            }
                        }
                    }
                }
            }
            
            Neomatica.LOGGER.info("Interior marcado para vaciado: {} bloques", hollowedCount);
            return true;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al vaciar área", e);
            return false;
        }
    }
    
    /**
     * Cuenta bloques en un área
     */
    public int countBlocks(BlockPos pos1, BlockPos pos2) {
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
    
    /**
     * Verifica si se puede eliminar en un área
     */
    public boolean canDelete(BlockPos pos1, BlockPos pos2) {
        if (client.world == null) {
            return false;
        }
        
        // Verificar límites del mundo
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        
        return client.world.isInBuildLimit(new BlockPos(0, minY, 0)) &&
               client.world.isInBuildLimit(new BlockPos(0, maxY, 0));
    }
    
    /**
     * Obtiene el volumen del área a eliminar
     */
    public int getVolume(BlockPos pos1, BlockPos pos2) {
        int sizeX = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int sizeY = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int sizeZ = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        
        return sizeX * sizeY * sizeZ;
    }
}