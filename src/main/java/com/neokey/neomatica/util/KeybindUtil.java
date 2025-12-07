package com.neokey.neomatica.util;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Utilidades para manejo de keybinds
 */
public class KeybindUtil {
    
    /**
     * Verifica si una tecla está presionada
     */
    public static boolean isKeyPressed(int keyCode) {
        long window = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
        return GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS;
    }
    
    /**
     * Verifica si Shift está presionado
     */
    public static boolean isShiftPressed() {
        return isKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
    
    /**
     * Verifica si Ctrl está presionado
     */
    public static boolean isControlPressed() {
        return isKeyPressed(GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }
    
    /**
     * Verifica si Alt está presionado
     */
    public static boolean isAltPressed() {
        return isKeyPressed(GLFW.GLFW_KEY_LEFT_ALT) || isKeyPressed(GLFW.GLFW_KEY_RIGHT_ALT);
    }
    
    /**
     * Obtiene el nombre de una tecla
     */
    public static String getKeyName(int keyCode) {
        return GLFW.glfwGetKeyName(keyCode, 0);
    }
    
    /**
     * Obtiene el código de tecla desde el nombre
     */
    public static int getKeyCode(String keyName) {
        return switch (keyName.toLowerCase()) {
            case "a" -> GLFW.GLFW_KEY_A;
            case "b" -> GLFW.GLFW_KEY_B;
            case "c" -> GLFW.GLFW_KEY_C;
            case "d" -> GLFW.GLFW_KEY_D;
            case "e" -> GLFW.GLFW_KEY_E;
            case "f" -> GLFW.GLFW_KEY_F;
            case "g" -> GLFW.GLFW_KEY_G;
            case "h" -> GLFW.GLFW_KEY_H;
            case "i" -> GLFW.GLFW_KEY_I;
            case "j" -> GLFW.GLFW_KEY_J;
            case "k" -> GLFW.GLFW_KEY_K;
            case "l" -> GLFW.GLFW_KEY_L;
            case "m" -> GLFW.GLFW_KEY_M;
            case "n" -> GLFW.GLFW_KEY_N;
            case "o" -> GLFW.GLFW_KEY_O;
            case "p" -> GLFW.GLFW_KEY_P;
            case "q" -> GLFW.GLFW_KEY_Q;
            case "r" -> GLFW.GLFW_KEY_R;
            case "s" -> GLFW.GLFW_KEY_S;
            case "t" -> GLFW.GLFW_KEY_T;
            case "u" -> GLFW.GLFW_KEY_U;
            case "v" -> GLFW.GLFW_KEY_V;
            case "w" -> GLFW.GLFW_KEY_W;
            case "x" -> GLFW.GLFW_KEY_X;
            case "y" -> GLFW.GLFW_KEY_Y;
            case "z" -> GLFW.GLFW_KEY_Z;
            case "space" -> GLFW.GLFW_KEY_SPACE;
            case "enter" -> GLFW.GLFW_KEY_ENTER;
            case "escape" -> GLFW.GLFW_KEY_ESCAPE;
            case "tab" -> GLFW.GLFW_KEY_TAB;
            default -> GLFW.GLFW_KEY_UNKNOWN;
        };
    }
    
    /**
     * Verifica si un keybind está actualmente presionado
     */
    public static boolean isKeybindPressed(KeyBinding keyBinding) {
        return keyBinding != null && keyBinding.isPressed();
    }
    
    /**
     * Verifica si un keybind fue presionado (solo una vez)
     */
    public static boolean wasKeybindPressed(KeyBinding keyBinding) {
        return keyBinding != null && keyBinding.wasPressed();
    }
    
    /**
     * Crea un InputUtil.Key desde un código de tecla
     */
    public static InputUtil.Key createKey(int keyCode) {
        return InputUtil.Type.KEYSYM.createFromCode(keyCode);
    }
    
    /**
     * Obtiene el texto de display de un keybind
     */
    public static String getKeybindDisplayText(KeyBinding keyBinding) {
        if (keyBinding == null) {
            return "Ninguno";
        }
        return keyBinding.getBoundKeyLocalizedText().getString();
    }
    
    /**
     * Verifica si hay conflictos entre keybinds
     */
    public static boolean hasConflict(KeyBinding keybind1, KeyBinding keybind2) {
        if (keybind1 == null || keybind2 == null) {
            return false;
        }
        
        return keybind1.equals(keybind2) && 
               keybind1.matchesKey(keybind2.getDefaultKey().getCode(), 0);
    }
    
    /**
     * Convierte un modificador a string legible
     */
    public static String modifierToString(boolean shift, boolean ctrl, boolean alt) {
        StringBuilder sb = new StringBuilder();
        
        if (ctrl) sb.append("Ctrl+");
        if (alt) sb.append("Alt+");
        if (shift) sb.append("Shift+");
        
        return sb.toString();
    }
    
    /**
     * Obtiene el estado actual de todos los modificadores
     */
    public static ModifierState getCurrentModifiers() {
        return new ModifierState(
            isShiftPressed(),
            isControlPressed(),
            isAltPressed()
        );
    }
    
    /**
     * Clase que representa el estado de los modificadores
     */
    public static class ModifierState {
        public final boolean shift;
        public final boolean ctrl;
        public final boolean alt;
        
        public ModifierState(boolean shift, boolean ctrl, boolean alt) {
            this.shift = shift;
            this.ctrl = ctrl;
            this.alt = alt;
        }
        
        public boolean hasAny() {
            return shift || ctrl || alt;
        }
        
        public boolean hasNone() {
            return !shift && !ctrl && !alt;
        }
        
        @Override
        public String toString() {
            if (hasNone()) return "None";
            StringBuilder sb = new StringBuilder();
            if (ctrl) sb.append("Ctrl+");
            if (alt) sb.append("Alt+");
            if (shift) sb.append("Shift+");
            return sb.toString();
        }
    }
}