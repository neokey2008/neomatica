package com.neokey.neomatica.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * Utilidades matemáticas para Neomatica
 */
public class MathUtil {
    
    /**
     * Calcula la distancia entre dos BlockPos
     */
    public static double distance(BlockPos pos1, BlockPos pos2) {
        double dx = pos2.getX() - pos1.getX();
        double dy = pos2.getY() - pos1.getY();
        double dz = pos2.getZ() - pos1.getZ();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    
    /**
     * Calcula la distancia Manhattan entre dos BlockPos
     */
    public static int manhattanDistance(BlockPos pos1, BlockPos pos2) {
        return Math.abs(pos2.getX() - pos1.getX()) + 
               Math.abs(pos2.getY() - pos1.getY()) + 
               Math.abs(pos2.getZ() - pos1.getZ());
    }
    
    /**
     * Calcula el punto medio entre dos BlockPos
     */
    public static BlockPos midpoint(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(
            (pos1.getX() + pos2.getX()) / 2,
            (pos1.getY() + pos2.getY()) / 2,
            (pos1.getZ() + pos2.getZ()) / 2
        );
    }
    
    /**
     * Limita un valor entre un mínimo y máximo
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Limita un valor float entre un mínimo y máximo
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Limita un valor double entre un mínimo y máximo
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Interpola linealmente entre dos valores
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp(t, 0.0f, 1.0f);
    }
    
    /**
     * Interpola linealmente entre dos valores double
     */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * clamp(t, 0.0, 1.0);
    }
    
    /**
     * Calcula el volumen entre dos posiciones
     */
    public static int volume(BlockPos pos1, BlockPos pos2) {
        int dx = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int dy = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int dz = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        return dx * dy * dz;
    }
    
    /**
     * Calcula el volumen de un Vec3i
     */
    public static int volume(Vec3i size) {
        return size.getX() * size.getY() * size.getZ();
    }
    
    /**
     * Obtiene la posición mínima entre dos BlockPos
     */
    public static BlockPos min(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(
            Math.min(pos1.getX(), pos2.getX()),
            Math.min(pos1.getY(), pos2.getY()),
            Math.min(pos1.getZ(), pos2.getZ())
        );
    }
    
    /**
     * Obtiene la posición máxima entre dos BlockPos
     */
    public static BlockPos max(BlockPos pos1, BlockPos pos2) {
        return new BlockPos(
            Math.max(pos1.getX(), pos2.getX()),
            Math.max(pos1.getY(), pos2.getY()),
            Math.max(pos1.getZ(), pos2.getZ())
        );
    }
    
    /**
     * Normaliza un ángulo a [0, 360)
     */
    public static float normalizeAngle(float angle) {
        angle = angle % 360.0f;
        if (angle < 0) {
            angle += 360.0f;
        }
        return angle;
    }
    
    /**
     * Convierte grados a radianes
     */
    public static float toRadians(float degrees) {
        return (float) Math.toRadians(degrees);
    }
    
    /**
     * Convierte radianes a grados
     */
    public static float toDegrees(float radians) {
        return (float) Math.toDegrees(radians);
    }
    
    /**
     * Calcula el área de una superficie 2D
     */
    public static int area(int width, int height) {
        return width * height;
    }
    
    /**
     * Verifica si un punto está dentro de un rectángulo
     */
    public static boolean isInBounds(int x, int y, int minX, int minY, int maxX, int maxY) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }
    
    /**
     * Verifica si un BlockPos está dentro de un área
     */
    public static boolean isInBounds(BlockPos pos, BlockPos min, BlockPos max) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
               pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
               pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }
    
    /**
     * Redondea un valor al múltiplo más cercano
     */
    public static int roundToMultiple(int value, int multiple) {
        if (multiple == 0) return value;
        return Math.round((float) value / multiple) * multiple;
    }
    
    /**
     * Redondea hacia abajo al múltiplo más cercano
     */
    public static int floorToMultiple(int value, int multiple) {
        if (multiple == 0) return value;
        return (value / multiple) * multiple;
    }
    
    /**
     * Redondea hacia arriba al múltiplo más cercano
     */
    public static int ceilToMultiple(int value, int multiple) {
        if (multiple == 0) return value;
        return ((value + multiple - 1) / multiple) * multiple;
    }
    
    /**
     * Calcula el porcentaje de un valor
     */
    public static float percentage(float value, float max) {
        if (max == 0) return 0;
        return (value / max) * 100.0f;
    }
    
    /**
     * Calcula el porcentaje como valor entre 0 y 1
     */
    public static float percentageNormalized(float value, float max) {
        if (max == 0) return 0;
        return clamp(value / max, 0.0f, 1.0f);
    }
    
    /**
     * Verifica si dos valores float son aproximadamente iguales
     */
    public static boolean approximately(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }
    
    /**
     * Verifica si dos valores float son aproximadamente iguales (epsilon = 0.0001)
     */
    public static boolean approximately(float a, float b) {
        return approximately(a, b, 0.0001f);
    }
    
    /**
     * Calcula la diferencia absoluta entre dos valores
     */
    public static int abs(int value) {
        return Math.abs(value);
    }
    
    /**
     * Calcula el valor absoluto de un float
     */
    public static float abs(float value) {
        return Math.abs(value);
    }
    
    /**
     * Calcula el valor absoluto de un double
     */
    public static double abs(double value) {
        return Math.abs(value);
    }
    
    /**
     * Calcula el signo de un valor
     */
    public static int sign(int value) {
        return value == 0 ? 0 : (value > 0 ? 1 : -1);
    }
    
    /**
     * Calcula el signo de un float
     */
    public static float sign(float value) {
        return value == 0 ? 0 : (value > 0 ? 1 : -1);
    }
    
    /**
     * Obtiene el mayor de tres valores
     */
    public static int max(int a, int b, int c) {
        return Math.max(Math.max(a, b), c);
    }
    
    /**
     * Obtiene el menor de tres valores
     */
    public static int min(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }
    
    /**
     * Calcula la potencia de 2 más cercana
     */
    public static int nextPowerOfTwo(int value) {
        int power = 1;
        while (power < value) {
            power *= 2;
        }
        return power;
    }
    
    /**
     * Verifica si un número es potencia de 2
     */
    public static boolean isPowerOfTwo(int value) {
        return value > 0 && (value & (value - 1)) == 0;
    }
    
    /**
     * Convierte Vec3d a BlockPos
     */
    public static BlockPos toBlockPos(Vec3d vec) {
        return new BlockPos(
            (int) Math.floor(vec.x),
            (int) Math.floor(vec.y),
            (int) Math.floor(vec.z)
        );
    }
    
    /**
     * Convierte BlockPos a Vec3d (centro del bloque)
     */
    public static Vec3d toVec3d(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
    
    /**
     * Calcula la distancia euclidiana en 2D
     */
    public static double distance2D(double x1, double z1, double x2, double z2) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dz * dz);
    }
    
    /**
     * Calcula el promedio de un array de enteros
     */
    public static float average(int... values) {
        if (values.length == 0) return 0;
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return (float) sum / values.length;
    }
    
    /**
     * Calcula el promedio de un array de floats
     */
    public static float average(float... values) {
        if (values.length == 0) return 0;
        float sum = 0;
        for (float value : values) {
            sum += value;
        }
        return sum / values.length;
    }
    
    /**
     * Mapea un valor de un rango a otro
     */
    public static float map(float value, float inMin, float inMax, float outMin, float outMax) {
        return outMin + (outMax - outMin) * ((value - inMin) / (inMax - inMin));
    }
    
    /**
     * Suaviza un valor usando smoothstep
     */
    public static float smoothstep(float edge0, float edge1, float x) {
        float t = clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }
}