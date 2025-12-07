package com.neokey.neomatica.schematic;

import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import java.util.*;

/**
 * Contador de bloques en schematics
 * Calcula la cantidad de cada tipo de bloque necesario
 */
public class BlockCounter {
    
    /**
     * Cuenta todos los bloques en un schematic
     */
    public BlockCount countBlocks(LoadedSchematic schematic) {
        if (schematic == null) {
            return new BlockCount();
        }
        
        Map<String, Integer> counts = new HashMap<>();
        int totalBlocks = 0;
        
        for (SchematicBlock block : schematic.getBlocks().values()) {
            String blockId = block.getBlockId();
            
            // Ignorar aire
            if (blockId.equals("minecraft:air")) {
                continue;
            }
            
            counts.put(blockId, counts.getOrDefault(blockId, 0) + 1);
            totalBlocks++;
        }
        
        return new BlockCount(counts, totalBlocks);
    }
    
    /**
     * Obtiene una lista ordenada de bloques por cantidad
     */
    public List<BlockCountEntry> getSortedBlocks(LoadedSchematic schematic) {
        BlockCount count = countBlocks(schematic);
        List<BlockCountEntry> entries = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : count.getBlockCounts().entrySet()) {
            entries.add(new BlockCountEntry(entry.getKey(), entry.getValue()));
        }
        
        // Ordenar por cantidad (mayor a menor)
        entries.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        
        return entries;
    }
    
    /**
     * Obtiene una lista ordenada de bloques por nombre
     */
    public List<BlockCountEntry> getSortedBlocksByName(LoadedSchematic schematic) {
        BlockCount count = countBlocks(schematic);
        List<BlockCountEntry> entries = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : count.getBlockCounts().entrySet()) {
            entries.add(new BlockCountEntry(entry.getKey(), entry.getValue()));
        }
        
        // Ordenar alfabéticamente
        entries.sort(Comparator.comparing(BlockCountEntry::getBlockId));
        
        return entries;
    }
    
    /**
     * Calcula el número de stacks necesarios para cada bloque
     */
    public Map<String, StackInfo> calculateStacks(LoadedSchematic schematic) {
        BlockCount count = countBlocks(schematic);
        Map<String, StackInfo> stacks = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : count.getBlockCounts().entrySet()) {
            String blockId = entry.getKey();
            int totalCount = entry.getValue();
            
            // La mayoría de bloques tienen stack de 64
            int stackSize = getStackSize(blockId);
            int fullStacks = totalCount / stackSize;
            int remainder = totalCount % stackSize;
            
            stacks.put(blockId, new StackInfo(fullStacks, remainder, stackSize));
        }
        
        return stacks;
    }
    
    /**
     * Obtiene el tamaño de stack para un bloque
     */
    private int getStackSize(String blockId) {
        // Bloques con stack de 16
        if (blockId.contains("sign") || 
            blockId.contains("bucket") || 
            blockId.contains("ender_pearl") ||
            blockId.contains("snowball") ||
            blockId.contains("egg")) {
            return 16;
        }
        
        // Bloques con stack de 1
        if (blockId.contains("sword") || 
            blockId.contains("pickaxe") || 
            blockId.contains("axe") ||
            blockId.contains("shovel") ||
            blockId.contains("hoe") ||
            blockId.contains("armor")) {
            return 1;
        }
        
        // Por defecto 64
        return 64;
    }
    
    /**
     * Formatea un nombre de bloque para display
     */
    public String formatBlockName(String blockId) {
        // Remover namespace de minecraft
        String name = blockId.replace("minecraft:", "");
        
        // Reemplazar guiones bajos con espacios
        name = name.replace("_", " ");
        
        // Capitalizar cada palabra
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    formatted.append(word.substring(1).toLowerCase());
                }
                formatted.append(" ");
            }
        }
        
        return formatted.toString().trim();
    }
    
    /**
     * Clase que representa el conteo de bloques
     */
    public static class BlockCount {
        private final Map<String, Integer> blockCounts;
        private final int totalBlocks;
        
        public BlockCount() {
            this(new HashMap<>(), 0);
        }
        
        public BlockCount(Map<String, Integer> blockCounts, int totalBlocks) {
            this.blockCounts = blockCounts;
            this.totalBlocks = totalBlocks;
        }
        
        public Map<String, Integer> getBlockCounts() {
            return blockCounts;
        }
        
        public int getTotalBlocks() {
            return totalBlocks;
        }
        
        public int getUniqueBlockTypes() {
            return blockCounts.size();
        }
        
        public int getCountForBlock(String blockId) {
            return blockCounts.getOrDefault(blockId, 0);
        }
    }
    
    /**
     * Entrada individual de conteo de bloques
     */
    public static class BlockCountEntry {
        private final String blockId;
        private final int count;
        
        public BlockCountEntry(String blockId, int count) {
            this.blockId = blockId;
            this.count = count;
        }
        
        public String getBlockId() {
            return blockId;
        }
        
        public int getCount() {
            return count;
        }
    }
    
    /**
     * Información sobre stacks de bloques
     */
    public static class StackInfo {
        private final int fullStacks;
        private final int remainder;
        private final int stackSize;
        
        public StackInfo(int fullStacks, int remainder, int stackSize) {
            this.fullStacks = fullStacks;
            this.remainder = remainder;
            this.stackSize = stackSize;
        }
        
        public int getFullStacks() {
            return fullStacks;
        }
        
        public int getRemainder() {
            return remainder;
        }
        
        public int getStackSize() {
            return stackSize;
        }
        
        public int getTotalBlocks() {
            return fullStacks * stackSize + remainder;
        }
        
        public String toDisplayString() {
            if (remainder == 0) {
                return fullStacks + " stacks";
            } else if (fullStacks == 0) {
                return remainder + " bloques";
            } else {
                return fullStacks + " stacks + " + remainder;
            }
        }
    }
}