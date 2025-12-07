package com.neokey.neomatica.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.lwjgl.glfw.GLFW;

import com.neokey.neomatica.gui.NeomaticaScreen;
import com.neokey.neomatica.gui.ToolsScreen;
import com.neokey.neomatica.tools.ToolManager;

/**
 * Gestión de teclas de acceso rápido para Neomatica
 */
public class KeyBindings {
    
    // Categoría para los keybinds
    private static final String CATEGORY = "key.categories.neomatica";
    
    // Keybinds principales
    private KeyBinding openMenuKey;
    private KeyBinding quickToolsKey;
    private KeyBinding selectPos1Key;
    private KeyBinding selectPos2Key;
    private KeyBinding copyKey;
    private KeyBinding pasteKey;
    private KeyBinding toggleVisibilityKey;
    private KeyBinding rotateClockwiseKey;
    private KeyBinding rotateCounterKey;
    
    /**
     * Registra todos los keybinds
     */
    public void register() {
        // Abrir menú principal (M por defecto)
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.open_menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            CATEGORY
        ));
        
        // Acceso rápido a herramientas (J por defecto)
        quickToolsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.quick_tools",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            CATEGORY
        ));
        
        // Seleccionar posición 1 (Sin tecla por defecto)
        selectPos1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.select_pos1",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
        ));
        
        // Seleccionar posición 2 (Sin tecla por defecto)
        selectPos2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.select_pos2",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
        ));
        
        // Copiar área (Sin tecla por defecto)
        copyKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.copy",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
        ));
        
        // Pegar schematic (Sin tecla por defecto)
        pasteKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.paste",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
        ));
        
        // Alternar visibilidad (H por defecto)
        toggleVisibilityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.toggle_visibility",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            CATEGORY
        ));
        
        // Rotar en sentido horario (Sin tecla por defecto)
        rotateClockwiseKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.rotate_clockwise",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
        ));
        
        // Rotar en sentido antihorario (Sin tecla por defecto)
        rotateCounterKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "neomatica.keybind.rotate_counter",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
        ));
    }
    
    /**
     * Procesa las teclas presionadas
     */
    public void process(MinecraftClient client) {
        // Abrir menú principal
        if (openMenuKey.wasPressed()) {
            client.setScreen(new NeomaticaScreen(client.currentScreen));
        }
        
        // Abrir herramientas rápidas
        if (quickToolsKey.wasPressed()) {
            client.setScreen(new ToolsScreen(client.currentScreen));
        }
        
        // Obtener ToolManager del cliente
        ToolManager toolManager = NeomaticaClient.getInstance().getToolManager();
        if (toolManager == null) return;
        
        // Seleccionar posición 1
        if (selectPos1Key.wasPressed()) {
            toolManager.setPosition1();
        }
        
        // Seleccionar posición 2
        if (selectPos2Key.wasPressed()) {
            toolManager.setPosition2();
        }
        
        // Copiar área
        if (copyKey.wasPressed()) {
            toolManager.copyArea();
        }
        
        // Pegar schematic
        if (pasteKey.wasPressed()) {
            toolManager.pasteSchematic();
        }
        
        // Alternar visibilidad
        if (toggleVisibilityKey.wasPressed()) {
            toolManager.toggleVisibility();
        }
        
        // Rotar en sentido horario
        if (rotateClockwiseKey.wasPressed()) {
            toolManager.rotateSchematicClockwise();
        }
        
        // Rotar en sentido antihorario
        if (rotateCounterKey.wasPressed()) {
            toolManager.rotateSchematicCounterClockwise();
        }
    }
    
    // Getters
    public KeyBinding getOpenMenuKey() { return openMenuKey; }
    public KeyBinding getQuickToolsKey() { return quickToolsKey; }
    public KeyBinding getSelectPos1Key() { return selectPos1Key; }
    public KeyBinding getSelectPos2Key() { return selectPos2Key; }
    public KeyBinding getCopyKey() { return copyKey; }
    public KeyBinding getPasteKey() { return pasteKey; }
    public KeyBinding getToggleVisibilityKey() { return toggleVisibilityKey; }
    public KeyBinding getRotateClockwiseKey() { return rotateClockwiseKey; }
    public KeyBinding getRotateCounterKey() { return rotateCounterKey; }
}