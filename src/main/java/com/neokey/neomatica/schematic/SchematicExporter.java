package com.neokey.neomatica.schematic;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * Exportador de schematics a diferentes formatos
 */
public class SchematicExporter {
    
    /**
     * Exporta un schematic a archivo
     */
    public boolean export(LoadedSchematic schematic, File outputFile) {
        String fileName = outputFile.getName().toLowerCase();
        
        try {
            if (fileName.endsWith(".litematic")) {
                return exportToLitematic(schematic, outputFile);
            } else if (fileName.endsWith(".schem")) {
                return exportToSpongeSchematic(schematic, outputFile);
            } else if (fileName.endsWith(".schematic")) {
                return exportToWorldEditSchematic(schematic, outputFile);
            } else {
                // Por defecto exportar como litematic
                File litematicFile = new File(outputFile.getParent(), 
                    outputFile.getName() + ".litematic");
                return exportToLitematic(schematic, litematicFile);
            }
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al exportar schematic", e);
            return false;
        }
    }
    
    /**
     * Exporta a formato Litematica
     */
    private boolean exportToLitematic(LoadedSchematic schematic, File outputFile) throws IOException {
        NbtCompound root = new NbtCompound();
        
        // Metadata
        NbtCompound metadata = new NbtCompound();
        metadata.putInt("Version", 6); // Versión de Litematica
        metadata.putString("Name", schematic.getName());
        metadata.putString("Author", "Neomatica");
        metadata.putLong("TimeCreated", System.currentTimeMillis());
        metadata.putLong("TimeModified", System.currentTimeMillis());
        metadata.putString("Description", "Exportado con Neomatica");
        root.put("Metadata", metadata);
        
        // Regions
        NbtCompound regions = new NbtCompound();
        NbtCompound region = new NbtCompound();
        
        // Tamaño
        Vec3i size = schematic.getSize();
        NbtCompound sizeNbt = new NbtCompound();
        sizeNbt.putInt("x", size.getX());
        sizeNbt.putInt("y", size.getY());
        sizeNbt.putInt("z", size.getZ());
        region.put("Size", sizeNbt);
        
        // Posición
        BlockPos origin = schematic.getOrigin();
        if (origin != null) {
            NbtCompound posNbt = new NbtCompound();
            posNbt.putInt("x", origin.getX());
            posNbt.putInt("y", origin.getY());
            posNbt.putInt("z", origin.getZ());
            region.put("Position", posNbt);
        }
        
        // Crear paleta de bloques
        Map<String, Integer> palette = new HashMap<>();
        NbtCompound paletteNbt = new NbtCompound();
        int paletteIndex = 0;
        
        // Agregar aire a la paleta primero
        NbtCompound airState = new NbtCompound();
        airState.putString("Name", "minecraft:air");
        paletteNbt.put(String.valueOf(paletteIndex), airState);
        palette.put("minecraft:air", paletteIndex++);
        
        // Agregar todos los bloques únicos a la paleta
        for (SchematicBlock block : schematic.getBlocks().values()) {
            String blockId = block.getBlockId();
            if (!palette.containsKey(blockId)) {
                NbtCompound blockState = new NbtCompound();
                blockState.putString("Name", blockId);
                
                // Agregar propiedades si las hay
                if (!block.getProperties().isEmpty()) {
                    NbtCompound props = new NbtCompound();
                    for (Map.Entry<String, String> entry : block.getProperties().entrySet()) {
                        props.putString(entry.getKey(), entry.getValue());
                    }
                    blockState.put("Properties", props);
                }
                
                paletteNbt.put(String.valueOf(paletteIndex), blockState);
                palette.put(blockId, paletteIndex++);
            }
        }
        
        region.put("BlockStatePalette", paletteNbt);
        
        // Crear array de estados de bloques
        int totalBlocks = size.getX() * size.getY() * size.getZ();
        int bitsPerBlock = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(palette.size() - 1));
        int longsNeeded = (totalBlocks * bitsPerBlock + 63) / 64;
        long[] blockStates = new long[longsNeeded];
        
        int index = 0;
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    SchematicBlock block = schematic.getBlock(pos);
                    
                    int paletteId = 0; // aire por defecto
                    if (block != null) {
                        paletteId = palette.getOrDefault(block.getBlockId(), 0);
                    }
                    
                    setBlockState(blockStates, index, paletteId, bitsPerBlock);
                    index++;
                }
            }
        }
        
        region.putLongArray("BlockStates", blockStates);
        
        // Agregar región al NBT
        regions.put(schematic.getName(), region);
        root.put("Regions", regions);
        
        // Guardar archivo comprimido
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             GZIPOutputStream gzip = new GZIPOutputStream(fos)) {
            NbtIo.writeCompressed(root, fos);
        }
        
        Neomatica.LOGGER.info("Schematic exportado a Litematica: {}", outputFile.getName());
        return true;
    }
    
    /**
     * Exporta a formato Sponge Schematic (.schem)
     */
    private boolean exportToSpongeSchematic(LoadedSchematic schematic, File outputFile) throws IOException {
        NbtCompound root = new NbtCompound();
        
        // Versión
        root.putInt("Version", 2);
        root.putInt("DataVersion", 2975); // Versión de datos de Minecraft 1.18+
        
        // Dimensiones
        Vec3i size = schematic.getSize();
        root.putShort("Width", (short) size.getX());
        root.putShort("Height", (short) size.getY());
        root.putShort("Length", (short) size.getZ());
        
        // Offset
        BlockPos origin = schematic.getOrigin();
        if (origin != null) {
            root.putIntArray("Offset", new int[]{origin.getX(), origin.getY(), origin.getZ()});
        }
        
        // Metadata
        NbtCompound metadata = new NbtCompound();
        metadata.putString("Name", schematic.getName());
        metadata.putString("Author", "Neomatica");
        metadata.putLong("Date", System.currentTimeMillis());
        root.put("Metadata", metadata);
        
        // Crear paleta
        Map<String, Integer> palette = new HashMap<>();
        NbtCompound paletteNbt = new NbtCompound();
        int paletteIndex = 0;
        
        palette.put("minecraft:air", paletteIndex);
        paletteNbt.putInt("minecraft:air", paletteIndex++);
        
        for (SchematicBlock block : schematic.getBlocks().values()) {
            String blockId = block.getBlockId();
            if (!palette.containsKey(blockId)) {
                palette.put(blockId, paletteIndex);
                paletteNbt.putInt(blockId, paletteIndex++);
            }
        }
        
        root.put("Palette", paletteNbt);
        
        // Crear datos de bloques
        int totalBlocks = size.getX() * size.getY() * size.getZ();
        byte[] blockData = new byte[totalBlocks];
        
        int index = 0;
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    SchematicBlock block = schematic.getBlock(pos);
                    
                    int paletteId = 0;
                    if (block != null) {
                        paletteId = palette.getOrDefault(block.getBlockId(), 0);
                    }
                    
                    blockData[index++] = (byte) paletteId;
                }
            }
        }
        
        root.putByteArray("BlockData", blockData);
        
        // Guardar archivo comprimido
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            NbtIo.writeCompressed(root, fos);
        }
        
        Neomatica.LOGGER.info("Schematic exportado a Sponge: {}", outputFile.getName());
        return true;
    }
    
    /**
     * Exporta a formato WorldEdit legacy (.schematic)
     */
    private boolean exportToWorldEditSchematic(LoadedSchematic schematic, File outputFile) throws IOException {
        NbtCompound root = new NbtCompound();
        
        Vec3i size = schematic.getSize();
        root.putShort("Width", (short) size.getX());
        root.putShort("Height", (short) size.getY());
        root.putShort("Length", (short) size.getZ());
        
        root.putString("Materials", "Alpha");
        
        // Crear datos de bloques (formato legacy simplificado)
        int totalBlocks = size.getX() * size.getY() * size.getZ();
        byte[] blocks = new byte[totalBlocks];
        byte[] data = new byte[totalBlocks];
        
        int index = 0;
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    SchematicBlock block = schematic.getBlock(pos);
                    
                    // Conversión simplificada a IDs legacy
                    if (block != null) {
                        blocks[index] = (byte) convertToLegacyId(block.getBlockId());
                        data[index] = 0;
                    } else {
                        blocks[index] = 0; // aire
                        data[index] = 0;
                    }
                    
                    index++;
                }
            }
        }
        
        root.putByteArray("Blocks", blocks);
        root.putByteArray("Data", data);
        
        // TileEntities y Entities vacíos
        root.put("TileEntities", new NbtList());
        root.put("Entities", new NbtList());
        
        // Guardar archivo comprimido
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            NbtIo.writeCompressed(root, fos);
        }
        
        Neomatica.LOGGER.info("Schematic exportado a WorldEdit: {}", outputFile.getName());
        return true;
    }
    
    /**
     * Establece un estado de bloque en el array de longs
     */
    private void setBlockState(long[] data, int index, int value, int bitsPerBlock) {
        int startBit = index * bitsPerBlock;
        int longIndex = startBit / 64;
        int bitOffset = startBit % 64;
        
        if (longIndex >= data.length) return;
        
        long mask = (1L << bitsPerBlock) - 1;
        data[longIndex] = (data[longIndex] & ~(mask << bitOffset)) | ((long) value << bitOffset);
        
        if (bitOffset + bitsPerBlock > 64 && longIndex + 1 < data.length) {
            int overflow = (bitOffset + bitsPerBlock) - 64;
            data[longIndex + 1] = (data[longIndex + 1] & ~((1L << overflow) - 1)) | (value >> (bitsPerBlock - overflow));
        }
    }
    
    /**
     * Convierte nombre de bloque moderno a ID legacy
     */
    private int convertToLegacyId(String blockId) {
        // Conversión simplificada
        return switch (blockId) {
            case "minecraft:stone" -> 1;
            case "minecraft:grass_block" -> 2;
            case "minecraft:dirt" -> 3;
            case "minecraft:cobblestone" -> 4;
            case "minecraft:oak_planks" -> 5;
            // ... agregar más conversiones
            default -> 1; // stone por defecto
        };
    }
}