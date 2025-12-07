package com.neokey.neomatica.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * Utilidades para traducciones y localización
 */
public class TranslationUtil {
    
    private static final String MOD_PREFIX = "neomatica.";
    
    /**
     * Traduce una clave de traducción
     */
    public static String translate(String key, Object... args) {
        return I18n.translate(key, args);
    }
    
    /**
     * Traduce una clave de traducción con el prefijo del mod
     */
    public static String translateMod(String key, Object... args) {
        return translate(MOD_PREFIX + key, args);
    }
    
    /**
     * Obtiene un Text traducido
     */
    public static MutableText getText(String key, Object... args) {
        return Text.translatable(key, args);
    }
    
    /**
     * Obtiene un Text traducido con el prefijo del mod
     */
    public static MutableText getModText(String key, Object... args) {
        return Text.translatable(MOD_PREFIX + key, args);
    }
    
    /**
     * Obtiene un Text literal (sin traducción)
     */
    public static MutableText literal(String text) {
        return Text.literal(text);
    }
    
    /**
     * Verifica si existe una traducción para una clave
     */
    public static boolean hasTranslation(String key) {
        return I18n.hasTranslation(key);
    }
    
    /**
     * Verifica si existe una traducción del mod
     */
    public static boolean hasModTranslation(String key) {
        return hasTranslation(MOD_PREFIX + key);
    }
    
    /**
     * Traduce una clave o retorna un valor por defecto
     */
    public static String translateOrDefault(String key, String defaultValue) {
        return hasTranslation(key) ? translate(key) : defaultValue;
    }
    
    /**
     * Formatea un número con separadores de miles
     */
    public static String formatNumber(int number) {
        return String.format("%,d", number);
    }
    
    /**
     * Formatea un número float con decimales
     */
    public static String formatFloat(float number, int decimals) {
        return String.format("%." + decimals + "f", number);
    }
    
    /**
     * Formatea un porcentaje
     */
    public static String formatPercentage(float value, float max) {
        float percentage = (value / max) * 100.0f;
        return String.format("%.1f%%", percentage);
    }
    
    /**
     * Formatea un tiempo en formato legible
     */
    public static String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
    
    /**
     * Formatea un tamaño de archivo
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Formatea coordenadas
     */
    public static String formatCoordinates(int x, int y, int z) {
        return String.format("X: %d, Y: %d, Z: %d", x, y, z);
    }
    
    /**
     * Formatea dimensiones
     */
    public static String formatDimensions(int width, int height, int length) {
        return String.format("%dx%dx%d", width, height, length);
    }
    
    /**
     * Capitaliza la primera letra de una string
     */
    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1).toLowerCase();
    }
    
    /**
     * Capitaliza cada palabra de una string
     */
    public static String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(capitalize(word)).append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Reemplaza guiones bajos con espacios y capitaliza
     */
    public static String formatIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }
        
        // Remover namespace si existe
        String name = identifier.contains(":") ? 
            identifier.substring(identifier.indexOf(":") + 1) : identifier;
        
        // Reemplazar guiones bajos con espacios
        name = name.replace("_", " ");
        
        // Capitalizar
        return capitalizeWords(name);
    }
    
    /**
     * Obtiene el lenguaje actual del juego
     */
    public static String getCurrentLanguage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.getLanguageManager() != null) {
            return client.getLanguageManager().getLanguage();
        }
        return "en_us";
    }
    
    /**
     * Verifica si el lenguaje actual es español
     */
    public static boolean isSpanish() {
        String lang = getCurrentLanguage();
        return lang.startsWith("es_");
    }
    
    /**
     * Obtiene un mensaje de error traducido
     */
    public static MutableText getErrorText(String errorKey) {
        return getModText("error." + errorKey);
    }
    
    /**
     * Obtiene un mensaje de éxito traducido
     */
    public static MutableText getSuccessText(String successKey) {
        return getModText("success." + successKey);
    }
    
    /**
     * Obtiene un mensaje de advertencia traducido
     */
    public static MutableText getWarningText(String warningKey) {
        return getModText("warning." + warningKey);
    }
    
    /**
     * Obtiene un mensaje de información traducido
     */
    public static MutableText getInfoText(String infoKey) {
        return getModText("info." + infoKey);
    }
    
    /**
     * Formatea una lista de items
     */
    public static String formatList(String[] items, String separator) {
        if (items == null || items.length == 0) {
            return "";
        }
        return String.join(separator, items);
    }
    
    /**
     * Formatea una lista de items con separador por defecto
     */
    public static String formatList(String[] items) {
        return formatList(items, ", ");
    }
    
    /**
     * Trunca un texto largo y agrega puntos suspensivos
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Formatea un boolean como Sí/No
     */
    public static String formatBoolean(boolean value) {
        return value ? translate("neomatica.button.yes") : translate("neomatica.button.no");
    }
    
    /**
     * Formatea un estado de activación
     */
    public static String formatEnabled(boolean enabled) {
        return enabled ? translate("neomatica.status.enabled") : translate("neomatica.status.disabled");
    }
    
    /**
     * Obtiene un mensaje con color
     */
    public static MutableText getColoredText(String text, int color) {
        return Text.literal(text).styled(style -> style.withColor(color));
    }
    
    /**
     * Obtiene texto en negrita
     */
    public static MutableText getBoldText(String key, Object... args) {
        return getText(key, args).styled(style -> style.withBold(true));
    }
    
    /**
     * Obtiene texto en cursiva
     */
    public static MutableText getItalicText(String key, Object... args) {
        return getText(key, args).styled(style -> style.withItalic(true));
    }
    
    /**
     * Obtiene texto subrayado
     */
    public static MutableText getUnderlinedText(String key, Object... args) {
        return getText(key, args).styled(style -> style.withUnderline(true));
    }
}