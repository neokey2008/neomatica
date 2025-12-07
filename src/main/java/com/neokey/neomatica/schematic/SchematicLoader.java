package com.neokey.neomatica.schematic;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;
import com.neokey.neomatica.schematic.SchematicManager.SchematicBlock;
import com.neokey.neomatica.util.NBTUtil;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Cargador de archivos schematic
 * Soporta múltiples formatos: .litematic, .schem, .schematic
 */
public class SchematicLoader {
    
    /**
     * Carga un schematic desde un archivo
     */
    public LoadedSchematic load(File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("El archivo no existe: " + file.getPath());
        }
        
        String fileName = file.getName().toLowerCase();
        
        if (fileName.endsWith(".litematic")) {
            return loadLitematic(file);
        } else if (fileName.endsWith(".schem")) {
            return loadSpongeSchematic(file);
        } else if (fileName.endsWith(".schematic")) {
            return loadWorldEditSchematic(file);
        } else {
            throw new IOException("Formato de archivo no soportado: " + fileName);
        }
    }
    
    /**
     * Carga un archivo .litematic
     */
    private LoadedSchematic loadLitematic(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             GZIPInputStream gzip = new GZIPInputStream(fis)) {
            
            NbtCompound nbt = NbtIo.readCompressed(fis, NbtSizeTracker.ofUnlimitedBytes());
            
            if (!nbt.contains("Metadata") || !nbt.contains("Regions")) {
                throw new IOException("Archivo litematic inválido");
            }
            
            NbtCompound metadata = nbt.getCompound("Metadata").orElse(new NbtCompound());
            String name = metadata.getString("Name").orElse(file.getName());
            
            LoadedSchematic schematic = new LoadedSchematic(name.isEmpty() ? file.getName() : name);
            
            // Leer regiones
            NbtCompound regions = nbt.getCompound("Regions").orElse(new NbtCompound());
            
            for (String regionName : regions.getKeys()) {
                NbtCompound region = regions.getCompound(regionName).orElse(new NbtCompound());
                loadLitematicRegion(schematic, region);
            }
            
            Neomatica.LOGGER.info("Litematic cargado: {}", name);
            return schematic;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al cargar litematic", e);
            throw new IOException("Error al cargar litematic: " + e.getMessage());
        }
    }
    
    /**
     * Carga una región de un archivo litematic
     */
    private void loadLitematicRegion(LoadedSchematic schematic, NbtCompound region) {
        if (!region.contains("Size") || !region.contains("BlockStates")) {
            return;
        }
        
        // Leer tamaño
        NbtCompound sizeNbt = region.getCompound("Size").orElse(new NbtCompound());
        Vec3i size = new Vec3i(
            sizeNbt.getInt("x").orElse(0),
            sizeNbt.getInt("y").orElse(0),
            sizeNbt.getInt("z").orElse(0)
        );
        schematic.setSize(size);
        
        // Leer posición
        if (region.contains("Position")) {
            NbtCompound posNbt = region.getCompound("Position").orElse(new NbtCompound());
            BlockPos origin = new BlockPos(
                posNbt.getInt("x").orElse(0),
                posNbt.getInt("y").orElse(0),
                posNbt.getInt("z").orElse(0)
            );
            schematic.setOrigin(origin);
        }
        
        // Leer paleta de bloques
        NbtCompound palette = region.getCompound("BlockStatePalette").orElse(new NbtCompound());
        String[] paletteArray = new String[palette.getSize()];
        
        for (String key : palette.getKeys()) {
            try {
                int index = Integer.parseInt(key);
                NbtCompound blockState = palette.getCompound(key).orElse(new NbtCompound());
                paletteArray[index] = blockState.getString("Name").orElse("minecraft:air");
            } catch (NumberFormatException e) {
                // Ignorar claves inválidas
            }
        }
        
        // Leer estados de bloques
        long[] blockStates = region.getLongArray("BlockStates").orElse(new long[0]);
        int bitsPerBlock = Math.max(2, Integer.SIZE - Integer.numberOfLeadingZeros(paletteArray.length - 1));
        
        int index = 0;
        for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int x = 0; x < size.getX(); x++) {
                    int paletteId = extractPaletteId(blockStates, index, bitsPerBlock);
                    
                    if (paletteId < paletteArray.length && !paletteArray[paletteId].equals("minecraft:air")) {
                        BlockPos pos = new BlockPos(x, y, z);
                        SchematicBlock block = new SchematicBlock(paletteArray[paletteId]);
                        schematic.addBlock(pos, block);
                    }
                    
                    index++;
                }
            }
        }
    }
    
    /**
     * Carga un archivo .schem (Sponge Schematic)
     */
    private LoadedSchematic loadSpongeSchematic(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            NbtCompound nbt = NbtIo.readCompressed(fis, NbtSizeTracker.ofUnlimitedBytes());
            
            // Verificar versión
            int version = nbt.getInt("Version").orElse(0);
            if (version < 1 || version > 3) {
                throw new IOException("Versión de Sponge Schematic no soportada: " + version);
            }
            
            String name = file.getName().replace(".schem", "");
            LoadedSchematic schematic = new LoadedSchematic(name);
            
            // Leer dimensiones
            short width = nbt.getShort("Width").orElse((short)0);
            short height = nbt.getShort("Height").orElse((short)0);
            short length = nbt.getShort("Length").orElse((short)0);
            schematic.setSize(new Vec3i(width, height, length));
            
            // Leer offset si existe
            if (nbt.contains("Offset")) {
                int[] offset = nbt.getIntArray("Offset").orElse(new int[0]);
                if (offset.length == 3) {
                    schematic.setOrigin(new BlockPos(offset[0], offset[1], offset[2]));
                }
            }
            
            // Leer paleta
            NbtCompound palette = nbt.getCompound("Palette").orElse(new NbtCompound());
            String[] paletteArray = new String[palette.getSize()];
            
            for (String key : palette.getKeys()) {
                int index = palette.getInt(key).orElse(0);
                if (index < paletteArray.length) {
                    paletteArray[index] = key;
                }
            }
            
            // Leer datos de bloques
            byte[] blockData = nbt.getByteArray("BlockData").orElse(new byte[0]);
            
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        if (index < blockData.length) {
                            int paletteId = blockData[index] & 0xFF;
                            
                            if (paletteId < paletteArray.length && !paletteArray[paletteId].equals("minecraft:air")) {
                                BlockPos pos = new BlockPos(x, y, z);
                                SchematicBlock block = new SchematicBlock(paletteArray[paletteId]);
                                schematic.addBlock(pos, block);
                            }
                        }
                        index++;
                    }
                }
            }
            
            Neomatica.LOGGER.info("Sponge Schematic cargado: {}", name);
            return schematic;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al cargar Sponge Schematic", e);
            throw new IOException("Error al cargar Sponge Schematic: " + e.getMessage());
        }
    }
    
    /**
     * Carga un archivo .schematic (WorldEdit legacy)
     */
    private LoadedSchematic loadWorldEditSchematic(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            NbtCompound nbt = NbtIo.readCompressed(fis, NbtSizeTracker.ofUnlimitedBytes());
            
            String name = file.getName().replace(".schematic", "");
            LoadedSchematic schematic = new LoadedSchematic(name);
            
            // Leer dimensiones
            short width = nbt.getShort("Width").orElse((short)0);
            short height = nbt.getShort("Height").orElse((short)0);
            short length = nbt.getShort("Length").orElse((short)0);
            schematic.setSize(new Vec3i(width, height, length));
            
            // Leer datos de bloques (formato legacy)
            byte[] blocks = nbt.getByteArray("Blocks").orElse(new byte[0]);
            byte[] data = nbt.getByteArray("Data").orElse(new byte[0]);
            
            int index = 0;
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    for (int x = 0; x < width; x++) {
                        if (index < blocks.length) {
                            int blockId = blocks[index] & 0xFF;
                            int blockData = (index < data.length) ? (data[index] & 0xFF) : 0;
                            
                            if (blockId != 0) { // 0 = air
                                BlockPos pos = new BlockPos(x, y, z);
                                // Convertir ID legacy a nombre moderno
                                String blockName = convertLegacyBlockId(blockId, blockData);
                                SchematicBlock block = new SchematicBlock(blockName);
                                schematic.addBlock(pos, block);
                            }
                        }
                        index++;
                    }
                }
            }
            
            Neomatica.LOGGER.info("WorldEdit Schematic cargado: {}", name);
            return schematic;
            
        } catch (Exception e) {
            Neomatica.LOGGER.error("Error al cargar WorldEdit Schematic", e);
            throw new IOException("Error al cargar WorldEdit Schematic: " + e.getMessage());
        }
    }
    
    /**
     * Extrae el ID de paleta de un array de longs
     */
    private int extractPaletteId(long[] data, int index, int bitsPerBlock) {
        if (data.length == 0) return 0;
        
        int longIndex = (index * bitsPerBlock) / 64;
        int startBit = (index * bitsPerBlock) % 64;
        
        if (longIndex >= data.length) {
            return 0;
        }
        
        long value = data[longIndex] >>> startBit;
        
        if (startBit + bitsPerBlock > 64 && longIndex + 1 < data.length) {
            value |= data[longIndex + 1] << (64 - startBit);
        }
        
        return (int) (value & ((1L << bitsPerBlock) - 1));
    }
    
    /**
     * Convierte un ID de bloque legacy a nombre moderno
     */
    private String convertLegacyBlockId(int id, int data) {
        // Conversión básica de IDs legacy a nombres modernos
        return switch (id) {
            case 1 -> "minecraft:stone";
            case 2 -> "minecraft:grass_block";
            case 3 -> "minecraft:dirt";
            case 4 -> "minecraft:cobblestone";
            case 5 -> "minecraft:oak_planks";
            default -> "minecraft:stone";
        };
    }
}