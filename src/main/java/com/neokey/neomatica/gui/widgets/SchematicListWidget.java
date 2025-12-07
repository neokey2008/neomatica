package com.neokey.neomatica.gui.widgets;

import com.neokey.neomatica.network.OnlineRepository.SchematicInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3i;

import java.util.function.Consumer;

/**
 * Widget de lista de schematics
 */
public class SchematicListWidget extends AlwaysSelectedEntryListWidget<SchematicListWidget.SchematicEntry> {
    
    private Consumer<SchematicInfo> selectionCallback;
    
    public SchematicListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, itemHeight);
        this.setRenderHeader(false, 0);
    }
    
    /**
     * Agrega un schematic a la lista
     */
    public void addEntry(SchematicInfo schematicInfo) {
        this.addEntry(new SchematicEntry(schematicInfo, this));
    }
    
    /**
     * Limpia todas las entradas de la lista
     */
    public void clearAllEntries() {
        this.clearEntries();
    }
    
    /**
     * Establece el callback cuando se selecciona un schematic
     */
    public void setSchematicSelectedCallback(Consumer<SchematicInfo> callback) {
        this.selectionCallback = callback;
    }
    
    /**
     * Notifica la selección de un schematic
     */
    protected void onSchematicSelected(SchematicInfo schematicInfo) {
        if (selectionCallback != null) {
            selectionCallback.accept(schematicInfo);
        }
    }
    
    @Override
    public int getRowWidth() {
        return this.width - 20;
    }
    
    @Override
    protected int getScrollbarPositionX() {
        return this.width - 6;
    }
    
    /**
     * Entrada individual de schematic en la lista
     */
    public class SchematicEntry extends Entry<SchematicEntry> {
        
        private final SchematicInfo schematicInfo;
        private final SchematicListWidget parent;
        private final MinecraftClient client;
        
        public SchematicEntry(SchematicInfo schematicInfo, SchematicListWidget parent) {
            this.schematicInfo = schematicInfo;
            this.parent = parent;
            this.client = MinecraftClient.getInstance();
        }
        
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, 
                          int mouseX, int mouseY, boolean hovered, float tickDelta) {
            
            // Fondo si está seleccionado o hover
            if (hovered || parent.getSelectedOrNull() == this) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0x80FFFFFF);
            }
            
            // Renderizar nombre del schematic
            context.drawText(
                client.textRenderer,
                schematicInfo.getName(),
                x + 5,
                y + 2,
                0xFFFFFF,
                false
            );
            
            // Renderizar autor
            String author = "por " + schematicInfo.getAuthor();
            context.drawText(
                client.textRenderer,
                author,
                x + 5,
                y + 13,
                0xAAAAAA,
                false
            );
            
            // Renderizar dimensiones si están disponibles
            Vec3i size = schematicInfo.getSize();
            if (size != null) {
                String dimensions = String.format("%dx%dx%d", size.getX(), size.getY(), size.getZ());
                int dimensionsWidth = client.textRenderer.getWidth(dimensions);
                context.drawText(
                    client.textRenderer,
                    dimensions,
                    x + entryWidth - dimensionsWidth - 5,
                    y + 7,
                    0x55FF55,
                    false
                );
            }
            
            // Renderizar categoría
            String category = schematicInfo.getCategory();
            if (category != null && !category.isEmpty()) {
                context.drawText(
                    client.textRenderer,
                    "[" + category + "]",
                    x + entryWidth - 100,
                    y + 2,
                    0xFFAA00,
                    false
                );
            }
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                parent.setSelected(this);
                parent.onSchematicSelected(schematicInfo);
                return true;
            }
            return false;
        }
        
        @Override
        public Text getNarration() {
            return Text.literal(schematicInfo.getName());
        }
        
        public SchematicInfo getSchematicInfo() {
            return schematicInfo;
        }
    }
}