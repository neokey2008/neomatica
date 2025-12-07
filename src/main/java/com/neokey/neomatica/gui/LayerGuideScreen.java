package com.neokey.neomatica.gui;

import com.neokey.neomatica.schematic.LayerGuide;
import com.neokey.neomatica.schematic.LayerGuide.LayerAxis;
import com.neokey.neomatica.schematic.LayerGuide.LayerStats;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Pantalla de guía por capas
 */
public class LayerGuideScreen extends Screen {
    
    private final Screen parent;
    private final LoadedSchematic schematic;
    private LayerGuide layerGuide;
    
    private ButtonWidget previousButton;
    private ButtonWidget nextButton;
    private ButtonWidget firstButton;
    private ButtonWidget lastButton;
    private ButtonWidget axisButton;
    private CheckboxWidget showCurrentOnlyCheckbox;
    private CheckboxWidget hideBelowCheckbox;
    
    private static final int PREVIEW_SIZE = 300;
    
    public LayerGuideScreen(Screen parent, LoadedSchematic schematic) {
        super(Text.translatable("neomatica.layers.title"));
        this.parent = parent;
        this.schematic = schematic;
        this.layerGuide = new LayerGuide(schematic);
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int controlsY = this.height - 80;
        
        // Controles de navegación
        previousButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.layers.previous"),
            button -> {
                layerGuide.previousLayer();
                updateButtons();
            })
            .dimensions(centerX - 120, controlsY, 55, 20)
            .build()
        );
        
        nextButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.layers.next"),
            button -> {
                layerGuide.nextLayer();
                updateButtons();
            })
            .dimensions(centerX + 65, controlsY, 55, 20)
            .build()
        );
        
        // Ir a primera/última capa
        firstButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal("<<"),
            button -> {
                layerGuide.goToLayer(0);
                updateButtons();
            })
            .dimensions(centerX - 180, controlsY, 30, 20)
            .build()
        );
        
        lastButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal(">>"),
            button -> {
                layerGuide.goToLastLayer();
                updateButtons();
            })
            .dimensions(centerX + 150, controlsY, 30, 20)
            .build()
        );
        
        // Botón de cambio de eje
        axisButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Eje: " + layerGuide.getAxis().getDisplayName()),
            button -> {
                layerGuide.setAxis(layerGuide.getAxis().next());
                button.setMessage(Text.literal("Eje: " + layerGuide.getAxis().getDisplayName()));
                updateButtons();
            })
            .dimensions(centerX - 80, controlsY - 30, 160, 20)
            .build()
        );
        
        // Checkboxes de opciones
        int checkboxY = controlsY + 30;
        
        showCurrentOnlyCheckbox = this.addDrawableChild(
            CheckboxWidget.builder(Text.literal("Solo capa actual"), this.textRenderer)
                .pos(centerX - 100, checkboxY)
                .checked(layerGuide.isShowCurrentOnly())
                .callback((checkbox, checked) -> {
                    layerGuide.setShowCurrentOnly(checked);
                    if (checked) {
                        hideBelowCheckbox.onPress();
                    }
                })
                .build()
        );
        
        hideBelowCheckbox = this.addDrawableChild(
            CheckboxWidget.builder(Text.literal("Ocultar inferiores"), this.textRenderer)
                .pos(centerX - 100, checkboxY + 20)
                .checked(layerGuide.isHideBelow())
                .callback((checkbox, checked) -> {
                    layerGuide.setHideBelow(checked);
                    if (checked) {
                        showCurrentOnlyCheckbox.onPress();
                    }
                })
                .build()
        );
        
        // Botón: Mostrar todas
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.layers.show_all"),
            button -> {
                layerGuide.setShowCurrentOnly(false);
                layerGuide.setHideBelow(false);
                showCurrentOnlyCheckbox.onPress();
                hideBelowCheckbox.onPress();
            })
            .dimensions(centerX + 20, checkboxY + 10, 100, 20)
            .build()
        );
        
        // Botón: Atrás
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.BACK,
            button -> this.close())
            .dimensions(centerX - 50, this.height - 30, 100, 20)
            .build()
        );
        
        updateButtons();
    }
    
    /**
     * Actualiza el estado de los botones
     */
    private void updateButtons() {
        previousButton.active = !layerGuide.isFirstLayer();
        nextButton.active = !layerGuide.isLastLayer();
        firstButton.active = !layerGuide.isFirstLayer();
        lastButton.active = !layerGuide.isLastLayer();
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
            10,
            0xFFFFFF
        );
        
        // Renderizar nombre del schematic
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            schematic.getName(),
            this.width / 2,
            25,
            0xAAAAAA
        );
        
        // Información de capa actual
        int infoY = 50;
        String currentLayerText = Text.translatable(
            "neomatica.layers.current",
            layerGuide.getCurrentLayer() + 1,
            layerGuide.getTotalLayers()
        ).getString();
        
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            currentLayerText,
            this.width / 2,
            infoY,
            0xFFFF00
        );
        
        // Barra de progreso
        int progressBarWidth = 300;
        int progressBarHeight = 10;
        int progressBarX = (this.width - progressBarWidth) / 2;
        int progressBarY = infoY + 20;
        
        // Fondo de la barra
        context.fill(progressBarX, progressBarY, progressBarX + progressBarWidth, progressBarY + progressBarHeight, 0xFF333333);
        
        // Progreso
        float progress = layerGuide.getProgress() / 100f;
        int progressWidth = (int) (progressBarWidth * progress);
        context.fill(progressBarX, progressBarY, progressBarX + progressWidth, progressBarY + progressBarHeight, 0xFF00FF00);
        
        // Estadísticas de la capa actual
        LayerStats stats = layerGuide.getCurrentLayerStats();
        int statsY = progressBarY + 30;
        
        String blocksInLayer = "Bloques en esta capa: " + stats.getTotalBlocks();
        context.drawTextWithShadow(
            this.textRenderer,
            blocksInLayer,
            20,
            statsY,
            0xFFFFFF
        );
        
        String uniqueBlocks = "Tipos únicos: " + stats.getUniqueBlockTypes();
        context.drawTextWithShadow(
            this.textRenderer,
            uniqueBlocks,
            20,
            statsY + 15,
            0xFFFFFF
        );
        
        if (!stats.isEmpty()) {
            String mostCommon = "Más común: " + formatBlockName(stats.getMostCommonBlock());
            context.drawTextWithShadow(
                this.textRenderer,
                mostCommon,
                20,
                statsY + 30,
                0xFFFFFF
            );
        }
        
        // Preview 2D de la capa (simplificado)
        renderLayerPreview(context, statsY + 60);
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    /**
     * Renderiza un preview 2D simplificado de la capa
     */
    private void renderLayerPreview(DrawContext context, int y) {
        int previewX = (this.width - PREVIEW_SIZE) / 2;
        
        // Fondo del preview
        context.fill(previewX, y, previewX + PREVIEW_SIZE, y + PREVIEW_SIZE, 0xFF1A1A1A);
        
        // Borde
        context.drawBorder(previewX, y, PREVIEW_SIZE, PREVIEW_SIZE, 0xFFFFFFFF);
        
        // TODO: Renderizar bloques de la capa actual como pixels
        // Por ahora solo mostramos un placeholder
        String previewText = "Preview de Capa";
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            previewText,
            this.width / 2,
            y + PREVIEW_SIZE / 2 - 5,
            0x808080
        );
    }
    
    /**
     * Formatea el nombre de un bloque
     */
    private String formatBlockName(String blockId) {
        String name = blockId.replace("minecraft:", "");
        name = name.replace("_", " ");
        
        // Capitalizar
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    formatted.append(word.substring(1));
                }
                formatted.append(" ");
            }
        }
        
        return formatted.toString().trim();
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