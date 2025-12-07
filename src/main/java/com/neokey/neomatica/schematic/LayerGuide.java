package com.neokey.neomatica.schematic;

import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.*;

/**
 * Guía de construcción por capas
 * Permite ver y navegar el schematic capa por capa
 */
public class LayerGuide {
    
    private LoadedSchematic schematic;
    private int currentLayer;
    private LayerAxis axis;
    private boolean showCurrentOnly;
    private boolean hideBelow;
    
    public LayerGuide(LoadedSchematic schematic) {
        this.schematic = schematic;
        this.currentLayer = 0;
        this.axis = LayerAxis.Y;
        this.showCurrentOnly = false;
        this.hideBelow = false;
    }
    
    /**
     * Obtiene el número total de capas según el eje actual
     */
    public int getTotalLayers() {
        if (schematic == null || schematic.getSize() == null) {
            return 0;
        }
        
        Vec3i size = schematic.getSize();
        return switch (axis) {
            case X -> size.getX();
            case Y -> size.getY();
            case Z -> size.getZ();
        };
    }
    
    /**
     * Avanza a la siguiente capa
     */
    public void nextLayer() {
        if (currentLayer < getTotalLayers() - 1) {
            currentLayer++;
        }
    }
    
    /**
     * Retrocede a la capa anterior
     */
    public void previousLayer() {
        if (currentLayer > 0) {
            currentLayer--;
        }
    }
    
    /**
     * Va a una capa específica
     */
    public void goToLayer(int layer) {
        if (layer >= 0 && layer < getTotalLayers()) {
            currentLayer = layer;
        }
    }
    
    /**
     * Obtiene la capa actual
     */
    public int getCurrentLayer() {
        return currentLayer;
    }
    
    /**
     * Obtiene todos los bloques de la capa actual
     */
    public Map<BlockPos, SchematicBlock> getCurrentLayerBlocks() {
        return getLayerBlocks(currentLayer);
    }
    
    /**
     * Obtiene todos los bloques de una capa específica
     */
    public Map<BlockPos, SchematicBlock> getLayerBlocks(int layer) {
        Map<BlockPos, SchematicBlock> layerBlocks = new HashMap<>();
        
        if (schematic == null) {
            return layerBlocks;
        }
        
        for (Map.Entry<BlockPos, SchematicBlock> entry : schematic.getBlocks().entrySet()) {
            BlockPos pos = entry.getKey();
            
            int layerCoord = switch (axis) {
                case X -> pos.getX();
                case Y -> pos.getY();
                case Z -> pos.getZ();
            };
            
            if (layerCoord == layer) {
                layerBlocks.put(pos, entry.getValue());
            }
        }
        
        return layerBlocks;
    }
    
    /**
     * Obtiene los bloques visibles según la configuración actual
     */
    public Map<BlockPos, SchematicBlock> getVisibleBlocks() {
        if (schematic == null) {
            return new HashMap<>();
        }
        
        if (showCurrentOnly) {
            // Solo mostrar la capa actual
            return getCurrentLayerBlocks();
        } else if (hideBelow) {
            // Mostrar desde la capa actual hacia arriba/adelante
            return getBlocksFromLayer(currentLayer, true);
        } else {
            // Mostrar todas las capas
            return schematic.getBlocks();
        }
    }
    
    /**
     * Obtiene bloques desde una capa específica
     */
    public Map<BlockPos, SchematicBlock> getBlocksFromLayer(int startLayer, boolean above) {
        Map<BlockPos, SchematicBlock> blocks = new HashMap<>();
        
        if (schematic == null) {
            return blocks;
        }
        
        for (Map.Entry<BlockPos, SchematicBlock> entry : schematic.getBlocks().entrySet()) {
            BlockPos pos = entry.getKey();
            
            int layerCoord = switch (axis) {
                case X -> pos.getX();
                case Y -> pos.getY();
                case Z -> pos.getZ();
            };
            
            if (above ? layerCoord >= startLayer : layerCoord <= startLayer) {
                blocks.put(pos, entry.getValue());
            }
        }
        
        return blocks;
    }
    
    /**
     * Cuenta los bloques en la capa actual
     */
    public int getCurrentLayerBlockCount() {
        return getCurrentLayerBlocks().size();
    }
    
    /**
     * Obtiene estadísticas de la capa actual
     */
    public LayerStats getCurrentLayerStats() {
        Map<BlockPos, SchematicBlock> layerBlocks = getCurrentLayerBlocks();
        Map<String, Integer> blockCounts = new HashMap<>();
        
        for (SchematicBlock block : layerBlocks.values()) {
            String blockId = block.getBlockId();
            blockCounts.put(blockId, blockCounts.getOrDefault(blockId, 0) + 1);
        }
        
        return new LayerStats(currentLayer, layerBlocks.size(), blockCounts);
    }
    
    /**
     * Obtiene todas las capas con sus estadísticas
     */
    public List<LayerStats> getAllLayersStats() {
        List<LayerStats> stats = new ArrayList<>();
        int totalLayers = getTotalLayers();
        
        for (int i = 0; i < totalLayers; i++) {
            Map<BlockPos, SchematicBlock> layerBlocks = getLayerBlocks(i);
            Map<String, Integer> blockCounts = new HashMap<>();
            
            for (SchematicBlock block : layerBlocks.values()) {
                String blockId = block.getBlockId();
                blockCounts.put(blockId, blockCounts.getOrDefault(blockId, 0) + 1);
            }
            
            stats.add(new LayerStats(i, layerBlocks.size(), blockCounts));
        }
        
        return stats;
    }
    
    /**
     * Resetea a la primera capa
     */
    public void reset() {
        currentLayer = 0;
        showCurrentOnly = false;
        hideBelow = false;
    }
    
    /**
     * Va a la última capa
     */
    public void goToLastLayer() {
        currentLayer = Math.max(0, getTotalLayers() - 1);
    }
    
    /**
     * Verifica si estamos en la primera capa
     */
    public boolean isFirstLayer() {
        return currentLayer == 0;
    }
    
    /**
     * Verifica si estamos en la última capa
     */
    public boolean isLastLayer() {
        return currentLayer >= getTotalLayers() - 1;
    }
    
    /**
     * Obtiene el progreso como porcentaje
     */
    public float getProgress() {
        int total = getTotalLayers();
        if (total == 0) return 0;
        return (float) (currentLayer + 1) / total * 100;
    }
    
    // Getters y Setters
    
    public LoadedSchematic getSchematic() {
        return schematic;
    }
    
    public void setSchematic(LoadedSchematic schematic) {
        this.schematic = schematic;
        this.currentLayer = 0;
    }
    
    public LayerAxis getAxis() {
        return axis;
    }
    
    public void setAxis(LayerAxis axis) {
        this.axis = axis;
        this.currentLayer = 0; // Resetear al cambiar eje
    }
    
    public boolean isShowCurrentOnly() {
        return showCurrentOnly;
    }
    
    public void setShowCurrentOnly(boolean showCurrentOnly) {
        this.showCurrentOnly = showCurrentOnly;
    }
    
    public boolean isHideBelow() {
        return hideBelow;
    }
    
    public void setHideBelow(boolean hideBelow) {
        this.hideBelow = hideBelow;
    }
    
    /**
     * Enum para los ejes de capas
     */
    public enum LayerAxis {
        X("X (Este-Oeste)"),
        Y("Y (Arriba-Abajo)"),
        Z("Z (Norte-Sur)");
        
        private final String displayName;
        
        LayerAxis(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public LayerAxis next() {
            return switch (this) {
                case X -> Y;
                case Y -> Z;
                case Z -> X;
            };
        }
        
        public LayerAxis previous() {
            return switch (this) {
                case X -> Z;
                case Y -> X;
                case Z -> Y;
            };
        }
    }
    
    /**
     * Estadísticas de una capa
     */
    public static class LayerStats {
        private final int layerNumber;
        private final int totalBlocks;
        private final Map<String, Integer> blockCounts;
        
        public LayerStats(int layerNumber, int totalBlocks, Map<String, Integer> blockCounts) {
            this.layerNumber = layerNumber;
            this.totalBlocks = totalBlocks;
            this.blockCounts = blockCounts;
        }
        
        public int getLayerNumber() {
            return layerNumber;
        }
        
        public int getTotalBlocks() {
            return totalBlocks;
        }
        
        public Map<String, Integer> getBlockCounts() {
            return blockCounts;
        }
        
        public int getUniqueBlockTypes() {
            return blockCounts.size();
        }
        
        public boolean isEmpty() {
            return totalBlocks == 0;
        }
        
        public String getMostCommonBlock() {
            if (blockCounts.isEmpty()) {
                return "Ninguno";
            }
            
            return blockCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Ninguno");
        }
    }
}