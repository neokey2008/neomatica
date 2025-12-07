package com.neokey.neomatica.gui;

import com.neokey.neomatica.client.NeomaticaClient;
import com.neokey.neomatica.tools.ToolManager;
import com.neokey.neomatica.tools.ToolManager.Tool;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Pantalla de herramientas de Neomatica
 */
public class ToolsScreen extends Screen {
    
    private final Screen parent;
    private ToolManager toolManager;
    
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;
    
    public ToolsScreen(Screen parent) {
        super(Text.translatable("neomatica.tools.title"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        toolManager = NeomaticaClient.getInstance().getToolManager();
        
        int centerX = this.width / 2;
        int startY = 60;
        
        // Herramienta: Seleccionar Área
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.select"),
            button -> {
                toolManager.setActiveTool(Tool.SELECT);
                updateButtonStates();
                closeAndReturnToGame();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Copiar Área
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.copy"),
            button -> {
                toolManager.copyArea();
                closeAndReturnToGame();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Pegar Schematic
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.paste"),
            button -> {
                toolManager.setActiveTool(Tool.PASTE);
                updateButtonStates();
                closeAndReturnToGame();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Mover Schematic
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.move"),
            button -> {
                toolManager.setActiveTool(Tool.MOVE);
                updateButtonStates();
                closeAndReturnToGame();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Borrar Bloques
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.delete"),
            button -> {
                toolManager.setActiveTool(Tool.DELETE);
                updateButtonStates();
                closeAndReturnToGame();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 4, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Quitar Schematic
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.remove"),
            button -> {
                toolManager.removeActiveSchematic();
                closeAndReturnToGame();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 5, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Alternar visibilidad
        ButtonWidget toggleVisibilityButton = this.addDrawableChild(ButtonWidget.builder(
            getVisibilityButtonText(),
            button -> {
                toolManager.toggleVisibility();
                button.setMessage(getVisibilityButtonText());
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 6, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Exportar Schematic
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.export"),
            button -> {
                toolManager.exportSchematic();
                closeAndReturnToGame();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 7, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Rotar Schematic (Horario)
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.rotate_clockwise"),
            button -> {
                toolManager.rotateSchematicClockwise();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 8, BUTTON_WIDTH / 2 - 2, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Rotar Schematic (Antihorario)
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.rotate_counter"),
            button -> {
                toolManager.rotateSchematicCounterClockwise();
            })
            .dimensions(centerX + 2, startY + BUTTON_SPACING * 8, BUTTON_WIDTH / 2 - 2, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Voltear X
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.flip_x"),
            button -> {
                toolManager.flipSchematicX();
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 9, BUTTON_WIDTH / 3 - 2, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Voltear Y
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.flip_y"),
            button -> {
                toolManager.flipSchematicY();
            })
            .dimensions(centerX - BUTTON_WIDTH / 6, startY + BUTTON_SPACING * 9, BUTTON_WIDTH / 3 - 2, BUTTON_HEIGHT)
            .build()
        );
        
        // Herramienta: Voltear Z
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.tools.flip_z"),
            button -> {
                toolManager.flipSchematicZ();
            })
            .dimensions(centerX + BUTTON_WIDTH / 6 + 2, startY + BUTTON_SPACING * 9, BUTTON_WIDTH / 3 - 2, BUTTON_HEIGHT)
            .build()
        );
        
        // Botón: Atrás
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.BACK,
            button -> this.close())
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 11, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
    }
    
    /**
     * Obtiene el texto del botón de visibilidad según el estado actual
     */
    private Text getVisibilityButtonText() {
        boolean visible = toolManager.isSchematicVisible();
        return Text.translatable(visible ? "neomatica.tools.hide" : "neomatica.tools.show");
    }
    
    /**
     * Actualiza el estado de los botones
     */
    private void updateButtonStates() {
        // Actualizar visual de botones según herramienta activa
    }
    
    /**
     * Cierra el menú y vuelve al juego
     */
    private void closeAndReturnToGame() {
        if (this.client != null) {
            this.client.setScreen(null);
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Renderizar fondo
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Renderizar título
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            20,
            0xFFFFFF
        );
        
        // Renderizar herramienta activa
        Tool activeTool = toolManager.getActiveTool();
        if (activeTool != null) {
            String activeText = "Herramienta activa: " + activeTool.getDisplayName();
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                activeText,
                this.width / 2,
                40,
                0x00FF00
            );
        }
        
        // Renderizar información de selección
        if (toolManager.hasSelection()) {
            String selectionInfo = "Selección activa";
            context.drawTextWithShadow(
                this.textRenderer,
                selectionInfo,
                10,
                this.height - 30,
                0xFFFF00
            );
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}