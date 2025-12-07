package com.neokey.neomatica.gui.widgets;

import com.neokey.neomatica.schematic.BlockCounter.BlockCountEntry;
import com.neokey.neomatica.schematic.BlockCounter.StackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.text.Text;

/**
 * Widget de lista para mostrar conteo de bloques
 */
public class BlockCountWidget extends AlwaysSelectedEntryListWidget<BlockCountWidget.BlockCountEntryWidget> {
    
    private final TextRenderer textRenderer;
    
    public BlockCountWidget(MinecraftClient client, int width, int height, int top, int bottom, TextRenderer textRenderer) {
        super(client, width, height, top, 22);
        this.textRenderer = textRenderer;
        this.setRenderHeader(false, 0);
    }
    
    /**
     * Agrega una entrada de bloque
     */
    public void addEntry(BlockCountEntry entry, StackInfo stackInfo) {
        this.addEntry(new BlockCountEntryWidget(entry, stackInfo, this));
    }
    
    /**
     * Limpia todas las entradas de la lista
     */
    public void clearAllEntries() {
        this.clearEntries();
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
     * Entrada individual de conteo de bloques
     */
    public class BlockCountEntryWidget extends Entry<BlockCountEntryWidget> {
        
        private final BlockCountEntry blockEntry;
        private final StackInfo stackInfo;
        private final BlockCountWidget parent;
        private final MinecraftClient client;
        
        public BlockCountEntryWidget(BlockCountEntry blockEntry, StackInfo stackInfo, BlockCountWidget parent) {
            this.blockEntry = blockEntry;
            this.stackInfo = stackInfo;
            this.parent = parent;
            this.client = MinecraftClient.getInstance();
        }
        
        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, 
                          int mouseX, int mouseY, boolean hovered, float tickDelta) {
            
            // Fondo alternado para mejor legibilidad
            if (index % 2 == 0) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0x20FFFFFF);
            }
            
            // Fondo si hay hover
            if (hovered) {
                context.fill(x, y, x + entryWidth, y + entryHeight, 0x40FFFFFF);
            }
            
            // Renderizar nombre del bloque (formateado)
            String blockName = formatBlockName(blockEntry.getBlockId());
            context.drawText(
                textRenderer,
                blockName,
                x + 5,
                y + 2,
                0xFFFFFF,
                false
            );
            
            // Renderizar cantidad
            String countText = Text.translatable(
                "neomatica.blocks.amount",
                blockEntry.getCount()
            ).getString();
            
            int countWidth = textRenderer.getWidth(countText);
            context.drawText(
                textRenderer,
                countText,
                x + entryWidth - countWidth - 5,
                y + 2,
                0x55FF55,
                false
            );
            
            // Renderizar información de stacks
            if (stackInfo != null) {
                String stackText = stackInfo.toDisplayString();
                context.drawText(
                    textRenderer,
                    stackText,
                    x + 5,
                    y + 13,
                    0xAAAAAA,
                    false
                );
            }
        }
        
        /**
         * Formatea el nombre de un bloque
         */
        private String formatBlockName(String blockId) {
            // Remover namespace
            String name = blockId.replace("minecraft:", "");
            
            // Reemplazar guiones bajos con espacios
            name = name.replace("_", " ");
            
            // Capitalizar cada palabra
            String[] words = name.split(" ");
            StringBuilder formatted = new StringBuilder();
            
            for (String word : words) {
                if (!word.isEmpty()) {
                    formatted.append(Character.toUpperCase(word.charAt(0)));
                    if (word.length() > 1) {
                        formatted.append(word.substring(1).toLowerCase());
                    }
                    formatted.append(" ");
                }
            }
            
            return formatted.toString().trim();
        }
        
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false; // No seleccionable
        }
        
        @Override
        public Text getNarration() {
            return Text.literal(formatBlockName(blockEntry.getBlockId()) + ": " + blockEntry.getCount());
        }
    }
}