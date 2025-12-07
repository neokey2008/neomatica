package com.neokey.neomatica.gui;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.gui.widgets.Preview3DWidget;
import com.neokey.neomatica.network.OnlineRepository.SchematicInfo;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Pantalla de vista previa 3D de schematics
 */
public class SchematicPreviewScreen extends Screen {
    
    private final Screen parent;
    private final SchematicInfo schematicInfo;
    private Preview3DWidget preview3D;
    private LoadedSchematic loadedSchematic;
    
    private ButtonWidget rotateLeftButton;
    private ButtonWidget rotateRightButton;
    private ButtonWidget zoomInButton;
    private ButtonWidget zoomOutButton;
    private ButtonWidget blockListButton;
    private ButtonWidget layerGuideButton;
    private ButtonWidget downloadButton;
    private ButtonWidget importButton;
    
    private boolean isLoading = true;
    private String loadingMessage = "Cargando preview...";
    
    private static final int BUTTON_SIZE = 30;
    private static final int BUTTON_SPACING = 35;
    
    public SchematicPreviewScreen(Screen parent, SchematicInfo schematicInfo) {
        super(Text.translatable("neomatica.preview.title"));
        this.parent = parent;
        this.schematicInfo = schematicInfo;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Widget de preview 3D
        int previewWidth = this.width - 220;
        int previewHeight = this.height - 100;
        
        preview3D = new Preview3DWidget(
            10,
            50,
            previewWidth,
            previewHeight,
            this.client
        );
        this.addDrawableChild(preview3D);
        
        // Panel derecho con controles
        int rightPanelX = this.width - 200;
        int controlsY = 60;
        
        // Controles de rotación
        rotateLeftButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal("◀"),
            button -> preview3D.rotateLeft())
            .dimensions(rightPanelX, controlsY, BUTTON_SIZE, BUTTON_SIZE)
            .build()
        );
        
        rotateRightButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal("▶"),
            button -> preview3D.rotateRight())
            .dimensions(rightPanelX + BUTTON_SPACING, controlsY, BUTTON_SIZE, BUTTON_SIZE)
            .build()
        );
        
        // Controles de zoom
        controlsY += 40;
        zoomInButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal("+"),
            button -> preview3D.zoomIn())
            .dimensions(rightPanelX, controlsY, BUTTON_SIZE, BUTTON_SIZE)
            .build()
        );
        
        zoomOutButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal("-"),
            button -> preview3D.zoomOut())
            .dimensions(rightPanelX + BUTTON_SPACING, controlsY, BUTTON_SIZE, BUTTON_SIZE)
            .build()
        );
        
        // Botones de información
        controlsY += 60;
        blockListButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.preview.blocks"),
            button -> openBlockList())
            .dimensions(rightPanelX - 10, controlsY, 120, 20)
            .build()
        );
        
        controlsY += 25;
        layerGuideButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.preview.layers"),
            button -> openLayerGuide())
            .dimensions(rightPanelX - 10, controlsY, 120, 20)
            .build()
        );
        
        // Botones de acción
        int bottomY = this.height - 30;
        int centerX = this.width / 2;
        
        downloadButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.preview.download"),
            button -> downloadSchematic())
            .dimensions(centerX - 105, bottomY, 100, 20)
            .build()
        );
        
        importButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.preview.import"),
            button -> importSchematic())
            .dimensions(centerX + 5, bottomY, 100, 20)
            .build()
        );
        
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.BACK,
            button -> this.close())
            .dimensions(10, bottomY, 80, 20)
            .build()
        );
        
        // Desactivar botones mientras carga
        setControlsEnabled(false);
        
        // Cargar preview del schematic
        loadSchematicPreview();
    }
    
    /**
     * Carga el preview del schematic
     */
    private void loadSchematicPreview() {
        new Thread(() -> {
            try {
                // Descargar preview temporal
                // TODO: Implementar descarga de preview ligero
                Thread.sleep(1000); // Simulación
                
                // Por ahora, simular carga exitosa
                if (this.client != null) {
                    this.client.execute(() -> {
                        isLoading = false;
                        setControlsEnabled(true);
                    });
                }
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error al cargar preview", e);
                if (this.client != null) {
                    this.client.execute(() -> {
                        isLoading = false;
                        loadingMessage = "Error al cargar preview";
                    });
                }
            }
        }).start();
    }
    
    /**
     * Habilita o deshabilita los controles
     */
    private void setControlsEnabled(boolean enabled) {
        rotateLeftButton.active = enabled;
        rotateRightButton.active = enabled;
        zoomInButton.active = enabled;
        zoomOutButton.active = enabled;
        blockListButton.active = enabled;
        layerGuideButton.active = enabled;
    }
    
    /**
     * Abre la lista de bloques necesarios
     */
    private void openBlockList() {
        if (loadedSchematic != null && this.client != null) {
            this.client.setScreen(new BlockListScreen(this, loadedSchematic));
        }
    }
    
    /**
     * Abre la guía por capas
     */
    private void openLayerGuide() {
        if (loadedSchematic != null && this.client != null) {
            this.client.setScreen(new LayerGuideScreen(this, loadedSchematic));
        }
    }
    
    /**
     * Descarga el schematic
     */
    private void downloadSchematic() {
        downloadButton.active = false;
        downloadButton.setMessage(Text.literal("Descargando..."));
        
        new Thread(() -> {
            try {
                boolean success = Neomatica.getInstance()
                    .getOnlineRepository()
                    .downloadSchematic(schematicInfo);
                
                if (this.client != null) {
                    this.client.execute(() -> {
                        if (success && this.client.player != null) {
                            this.client.player.sendMessage(
                                Text.translatable("neomatica.message.downloaded"),
                                false
                            );
                        }
                        downloadButton.active = true;
                        downloadButton.setMessage(Text.translatable("neomatica.preview.download"));
                    });
                }
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error al descargar", e);
                if (this.client != null) {
                    this.client.execute(() -> {
                        downloadButton.active = true;
                        downloadButton.setMessage(Text.translatable("neomatica.preview.download"));
                    });
                }
            }
        }).start();
    }
    
    /**
     * Importa el schematic a Litematica
     */
    private void importSchematic() {
        // TODO: Implementar integración con Litematica
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(
                Text.translatable("neomatica.message.imported"),
                false
            );
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Renderizar fondo
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Renderizar título
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            schematicInfo.getName(),
            this.width / 2,
            10,
            0xFFFFFF
        );
        
        // Renderizar información del schematic
        int infoY = 30;
        String author = "Autor: " + schematicInfo.getAuthor();
        context.drawTextWithShadow(
            this.textRenderer,
            author,
            10,
            infoY,
            0xAAAAAA
        );
        
        // Panel derecho - Título de controles
        int rightPanelX = this.width - 200;
        context.drawTextWithShadow(
            this.textRenderer,
            "Controles:",
            rightPanelX - 10,
            50,
            0xFFFFFF
        );
        
        // Dimensiones
        if (schematicInfo.getSize() != null) {
            String size = String.format("Tamaño: %dx%dx%d",
                schematicInfo.getSize().getX(),
                schematicInfo.getSize().getY(),
                schematicInfo.getSize().getZ()
            );
            context.drawTextWithShadow(
                this.textRenderer,
                size,
                rightPanelX - 10,
                140,
                0xAAAAAA
            );
        }
        
        // Indicador de carga
        if (isLoading) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                loadingMessage,
                (this.width - 220) / 2,
                this.height / 2,
                0xFFFFFF
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