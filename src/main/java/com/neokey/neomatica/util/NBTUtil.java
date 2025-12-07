package com.neokey.neomatica.util;

import com.neokey.neomatica.Neomatica;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.io.*;
import java.util.*;

/**
 * Utilidades para manejo de NBT
 */
public class NBTUtil {
    
    /**
     * Lee un archivo NBT
     */
    public static NbtCompound readNbtFile(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            return NbtIo.readCompressed(fis);
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al leer archivo NBT: {}", file.getName(), e);
            return null;
        }
    }
    
    /**
     * Escribe un NBT a un archivo
     */
    public static boolean writeNbtFile(File file, NbtCompound nbt) {
        if (file == null || nbt == null) {
            return false;
        }
        
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (FileOutputStream fos = new FileOutputStream(file)) {
                NbtIo.writeCompressed(nbt, fos);
            }
            
            return true;
        } catch (IOException e) {
            Neomatica.LOGGER.error("Error al escribir archivo NBT: {}", file.getName(), e);
            return false;
        }
    }
    
    /**
     * Crea un NBT de BlockPos
     */
    public static NbtCompound blockPosToNbt(BlockPos pos) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
        return nbt;
    }
    
    /**
     * Crea un BlockPos desde NBT
     */
    public static BlockPos nbtToBlockPos(NbtCompound nbt) {
        if (nbt == null || !nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z")) {
            return BlockPos.ORIGIN;
        }
        
        return new BlockPos(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
    }
    
    /**
     * Crea un NBT de Vec3i
     */
    public static NbtCompound vec3iToNbt(Vec3i vec) {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("x", vec.getX());
        nbt.putInt("y", vec.getY());
        nbt.putInt("z", vec.getZ());
        return nbt;
    }
    
    /**
     * Crea un Vec3i desde NBT
     */
    public static Vec3i nbtToVec3i(NbtCompound nbt) {
        if (nbt == null || !nbt.contains("x") || !nbt.contains("y") || !nbt.contains("z")) {
            return Vec3i.ZERO;
        }
        
        return new Vec3i(
            nbt.getInt("x"),
            nbt.getInt("y"),
            nbt.getInt("z")
        );
    }
    
    /**
     * Convierte un Map a NbtCompound
     */
    public static NbtCompound mapToNbt(Map<String, String> map) {
        NbtCompound nbt = new NbtCompound();
        
        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                nbt.putString(entry.getKey(), entry.getValue());
            }
        }
        
        return nbt;
    }
    
    /**
     * Convierte un NbtCompound a Map
     */
    public static Map<String, String> nbtToMap(NbtCompound nbt) {
        Map<String, String> map = new HashMap<>();
        
        if (nbt != null) {
            for (String key : nbt.getKeys()) {
                if (nbt.get(key) instanceof NbtString) {
                    map.put(key, nbt.getString(key));
                }
            }
        }
        
        return map;
    }
    
    /**
     * Convierte una lista a NbtList
     */
    public static NbtList stringListToNbt(List<String> list) {
        NbtList nbtList = new NbtList();
        
        if (list != null) {
            for (String item : list) {
                nbtList.add(NbtString.of(item));
            }
        }
        
        return nbtList;
    }
    
    /**
     * Convierte un NbtList a lista de strings
     */
    public static List<String> nbtToStringList(NbtList nbtList) {
        List<String> list = new ArrayList<>();
        
        if (nbtList != null) {
            for (int i = 0; i < nbtList.size(); i++) {
                list.add(nbtList.getString(i));
            }
        }
        
        return list;
    }
    
    /**
     * Copia un NbtCompound
     */
    public static NbtCompound copy(NbtCompound nbt) {
        if (nbt == null) {
            return new NbtCompound();
        }
        return nbt.copy();
    }
    
    /**
     * Fusiona dos NbtCompound (el segundo sobrescribe el primero)
     */
    public static NbtCompound merge(NbtCompound base, NbtCompound override) {
        if (base == null) {
            return override != null ? override.copy() : new NbtCompound();
        }
        
        if (override == null) {
            return base.copy();
        }
        
        NbtCompound result = base.copy();
        
        for (String key : override.getKeys()) {
            result.put(key, override.get(key).copy());
        }
        
        return result;
    }
    
    /**
     * Verifica si un NBT contiene una clave
     */
    public static boolean hasKey(NbtCompound nbt, String key) {
        return nbt != null && nbt.contains(key);
    }
    
    /**
     * Obtiene un valor de forma segura
     */
    public static String getStringSafe(NbtCompound nbt, String key, String defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        return nbt.getString(key);
    }
    
    /**
     * Obtiene un int de forma segura
     */
    public static int getIntSafe(NbtCompound nbt, String key, int defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        return nbt.getInt(key);
    }
    
    /**
     * Obtiene un long de forma segura
     */
    public static long getLongSafe(NbtCompound nbt, String key, long defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        return nbt.getLong(key);
    }
    
    /**
     * Obtiene un float de forma segura
     */
    public static float getFloatSafe(NbtCompound nbt, String key, float defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        return nbt.getFloat(key);
    }
    
    /**
     * Obtiene un double de forma segura
     */
    public static double getDoubleSafe(NbtCompound nbt, String key, double defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        return nbt.getDouble(key);
    }
    
    /**
     * Obtiene un boolean de forma segura
     */
    public static boolean getBooleanSafe(NbtCompound nbt, String key, boolean defaultValue) {
        if (nbt == null || !nbt.contains(key)) {
            return defaultValue;
        }
        return nbt.getBoolean(key);
    }
    
    /**
     * Obtiene un NbtCompound de forma segura
     */
    public static NbtCompound getCompoundSafe(NbtCompound nbt, String key) {
        if (nbt == null || !nbt.contains(key)) {
            return new NbtCompound();
        }
        return nbt.getCompound(key);
    }
    
    /**
     * Obtiene un NbtList de forma segura
     */
    public static NbtList getListSafe(NbtCompound nbt, String key) {
        if (nbt == null || !nbt.contains(key)) {
            return new NbtList();
        }
        return nbt.getList(key, NbtElement.COMPOUND_TYPE);
    }
    
    /**
     * Convierte NBT a string legible
     */
    public static String toString(NbtCompound nbt) {
        if (nbt == null) {
            return "{}";
        }
        return nbt.toString();
    }
    
    /**
     * Obtiene el tamaño (número de claves) de un NBT
     */
    public static int getSize(NbtCompound nbt) {
        if (nbt == null) {
            return 0;
        }
        return nbt.getKeys().size();
    }
    
    /**
     * Verifica si un NBT está vacío
     */
    public static boolean isEmpty(NbtCompound nbt) {
        return nbt == null || nbt.isEmpty();
    }
    
    /**
     * Elimina una clave de un NBT
     */
    public static void remove(NbtCompound nbt, String key) {
        if (nbt != null && nbt.contains(key)) {
            nbt.remove(key);
        }
    }
    
    /**
     * Obtiene todas las claves de un NBT
     */
    public static Set<String> getKeys(NbtCompound nbt) {
        if (nbt == null) {
            return new HashSet<>();
        }
        return nbt.getKeys();
    }
    
    /**
     * Convierte un array de bytes a NbtByteArray
     */
    public static NbtByteArray byteArrayToNbt(byte[] bytes) {
        return new NbtByteArray(bytes);
    }
    
    /**
     * Convierte un array de ints a NbtIntArray
     */
    public static NbtIntArray intArrayToNbt(int[] ints) {
        return new NbtIntArray(ints);
    }
    
    /**
     * Convierte un array de longs a NbtLongArray
     */
    public static NbtLongArray longArrayToNbt(long[] longs) {
        return new NbtLongArray(longs);
    }
}