package com.neokey.neomatica.gui;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.gui.widgets.SchematicListWidget;
import com.neokey.neomatica.network.OnlineRepository;
import com.neokey.neomatica.network.OnlineRepository.SchematicInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla para buscar y descargar schematics online
 */
public class SchematicBrowserScreen extends Screen {
    
    private final Screen parent;
    private TextFieldWidget searchField;
    private SchematicListWidget schematicList;
    private ButtonWidget searchButton;
    private ButtonWidget previewButton;
    private ButtonWidget downloadButton;
    private ButtonWidget categoryButton;
    
    private List<SchematicInfo> searchResults;
    private SchematicInfo selectedSchematic;
    private String currentCategory = "all";
    private boolean isSearching = false;
    
    private static final int SEARCH_BAR_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 80;
    private static final int BUTTON_HEIGHT = 20;
    
    public SchematicBrowserScreen(Screen parent) {
        super(Text.translatable("neomatica.browser.title"));
        this.parent = parent;
        this.searchResults = new ArrayList<>();
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Campo de búsqueda
        searchField = new TextFieldWidget(
            this.textRenderer,
            this.width / 2 - 150,
            20,
            300,
            SEARCH_BAR_HEIGHT,
            Text.translatable("neomatica.browser.search")
        );
        searchField.setMaxLength(128);
        searchField.setPlaceholder(Text.translatable("neomatica.browser.search"));
        searchField.setChangedListener(text -> {
            // Búsqueda en tiempo real (opcional)
        });
        this.addSelectableChild(searchField);
        
        // Botón de búsqueda
        searchButton = this.addDrawableChild(ButtonWidget.builder(
            Text.literal("🔍"),
            button -> performSearch())
            .dimensions(this.width / 2 + 155, 20, 30, SEARCH_BAR_HEIGHT)
            .build()
        );
        
        // Botón de categoría
        categoryButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.browser.filter." + currentCategory),
            button -> cycleCategory())
            .dimensions(20, 20, 100, SEARCH_BAR_HEIGHT)
            .build()
        );
        
        // Lista de schematics
        schematicList = new SchematicListWidget(
            this.client,
            this.width,
            this.height - 110,
            50,
            this.height - 60,
            25
        );
        schematicList.setSchematicSelectedCallback(this::onSchematicSelected);
        this.addSelectableChild(schematicList);
        
        // Botones inferiores
        int bottomY = this.height - 30;
        int centerX = this.width / 2;
        
        // Botón: Vista Previa
        previewButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.browser.preview"),
            button -> openPreview())
            .dimensions(centerX - BUTTON_WIDTH * 2 - 10, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        previewButton.active = false;
        
        // Botón: Descargar
        downloadButton = this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.browser.download"),
            button -> downloadSchematic())
            .dimensions(centerX - BUTTON_WIDTH - 5, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        downloadButton.active = false;
        
        // Botón: Importar a Litematica
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.browser.import"),
            button -> importToLitematica())
            .dimensions(centerX + 5, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        ).active = false;
        
        // Botón: Atrás
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.BACK,
            button -> this.close())
            .dimensions(centerX + BUTTON_WIDTH + 10, bottomY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Cargar resultados iniciales
        loadInitialResults();
    }
    
    /**
     * Carga resultados iniciales (populares/recientes)
     */
    private void loadInitialResults() {
        new Thread(() -> {
            try {
                OnlineRepository repository = Neomatica.getInstance().getOnlineRepository();
                List<SchematicInfo> results = repository.getPopularSchematics(currentCategory, 50);
                
                if (this.client != null) {
                    this.client.execute(() -> {
                        searchResults = results;
                        updateSchematicList();
                    });
                }
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error al cargar schematics iniciales", e);
            }
        }).start();
    }
    
    /**
     * Realiza una búsqueda
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        
        if (query.isEmpty()) {
            loadInitialResults();
            return;
        }
        
        isSearching = true;
        searchButton.active = false;
        
        new Thread(() -> {
            try {
                OnlineRepository repository = Neomatica.getInstance().getOnlineRepository();
                List<SchematicInfo> results = repository.searchSchematics(query, currentCategory, 100);
                
                if (this.client != null) {
                    this.client.execute(() -> {
                        searchResults = results;
                        updateSchematicList();
                        isSearching = false;
                        searchButton.active = true;
                    });
                }
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error al buscar schematics", e);
                if (this.client != null) {
                    this.client.execute(() -> {
                        isSearching = false;
                        searchButton.active = true;
                    });
                }
            }
        }).start();
    }
    
    /**
     * Cambia la categoría de filtrado
     */
    private void cycleCategory() {
        String[] categories = {"all", "buildings", "redstone", "organic", "decoration"};
        
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(currentCategory)) {
                currentCategory = categories[(i + 1) % categories.length];
                break;
            }
        }
        
        categoryButton.setMessage(Text.translatable("neomatica.browser.filter." + currentCategory));
        performSearch();
    }
    
    /**
     * Actualiza la lista de schematics
     */
    private void updateSchematicList() {
        schematicList.clearAllEntries();
        
        for (SchematicInfo info : searchResults) {
            schematicList.addEntry(info);
        }
    }
    
    /**
     * Callback cuando se selecciona un schematic
     */
    private void onSchematicSelected(SchematicInfo schematic) {
        selectedSchematic = schematic;
        previewButton.active = true;
        downloadButton.active = true;
    }
    
    /**
     * Abre la vista previa del schematic
     */
    private void openPreview() {
        if (selectedSchematic != null && this.client != null) {
            this.client.setScreen(new SchematicPreviewScreen(this, selectedSchematic));
        }
    }
    
    /**
     * Descarga el schematic seleccionado
     */
    private void downloadSchematic() {
        if (selectedSchematic == null) return;
        
        downloadButton.active = false;
        downloadButton.setMessage(Text.literal("Descargando..."));
        
        new Thread(() -> {
            try {
                OnlineRepository repository = Neomatica.getInstance().getOnlineRepository();
                boolean success = repository.downloadSchematic(selectedSchematic);
                
                if (this.client != null) {
                    this.client.execute(() -> {
                        if (success) {
                            if (this.client.player != null) {
                                this.client.player.sendMessage(
                                    Text.translatable("neomatica.message.downloaded"),
                                    false
                                );
                            }
                        }
                        downloadButton.active = true;
                        downloadButton.setMessage(Text.translatable("neomatica.browser.download"));
                    });
                }
            } catch (Exception e) {
                Neomatica.LOGGER.error("Error al descargar schematic", e);
                if (this.client != null) {
                    this.client.execute(() -> {
                        downloadButton.active = true;
                        downloadButton.setMessage(Text.translatable("neomatica.browser.download"));
                    });
                }
            }
        }).start();
    }
    
    /**
     * Importa el schematic a Litematica
     */
    private void importToLitematica() {
        if (selectedSchematic == null) return;
        
        // TODO: Implementar integración con Litematica
        if (this.client != null && this.client.player != null) {
            this.client.player.sendMessage(
                Text.literal("Función de importación a Litematica en desarrollo"),
                false
            );
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Renderizar fondo
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Renderizar lista de schematics
        schematicList.render(context, mouseX, mouseY, delta);
        
        // Renderizar campo de búsqueda
        searchField.render(context, mouseX, mouseY, delta);
        
        // Renderizar título
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            5,
            0xFFFFFF
        );
        
        // Renderizar contador de resultados
        if (!searchResults.isEmpty()) {
            String count = searchResults.size() + " resultados";
            context.drawTextWithShadow(
                this.textRenderer,
                count,
                this.width - this.textRenderer.getWidth(count) - 5,
                5,
                0x808080
            );
        }
        
        // Indicador de búsqueda en progreso
        if (isSearching) {
            String searching = "Buscando...";
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                searching,
                this.width / 2,
                this.height / 2,
                0xFFFFFF
            );
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void tick() {
        super.tick();
        searchField.tick();
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