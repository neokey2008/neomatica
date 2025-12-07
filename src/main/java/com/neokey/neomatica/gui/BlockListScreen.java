package com.neokey.neomatica.gui;

import com.neokey.neomatica.gui.widgets.BlockCountWidget;
import com.neokey.neomatica.schematic.BlockCounter;
import com.neokey.neomatica.schematic.BlockCounter.BlockCountEntry;
import com.neokey.neomatica.schematic.BlockCounter.StackInfo;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

/**
 * Pantalla que muestra la lista de bloques necesarios
 */
public class BlockListScreen extends Screen {
    
    private final Screen parent;
    private final LoadedSchematic schematic;
    private BlockCountWidget blockList;
    private BlockCounter blockCounter;
    
    private List<BlockCountEntry> sortedBlocks;
    private Map<String, StackInfo> stackInfo;
    private BlockCounter.BlockCount blockCount;
    
    private boolean sortByQuantity = true;
    
    public BlockListScreen(Screen parent, LoadedSchematic schematic) {
        super(Text.translatable("neomatica.blocks.title"));
        this.parent = parent;
        this.schematic = schematic;
        this.blockCounter = new BlockCounter();
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Calcular estadísticas
        calculateStats();
        
        // Lista de bloques
        blockList = new BlockCountWidget(
            this.client,
            this.width - 40,
            this.height - 120,
            60,
            this.height - 60,
            this.textRenderer
        );
        updateBlockList();
        this.addSelectableChild(blockList);
        
        // Botón: Ordenar
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal(sortByQuantity ? "Ordenar por Nombre" : "Ordenar por Cantidad"),
            button -> {
                sortByQuantity = !sortByQuantity;
                button.setMessage(Text.literal(sortByQuantity ? "Ordenar por Nombre" : "Ordenar por Cantidad"));
                updateBlockList();
            })
            .dimensions(this.width / 2 - 100, this.height - 50, 200, 20)
            .build()
        );
        
        // Botón: Atrás
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.BACK,
            button -> this.close())
            .dimensions(this.width / 2 - 50, this.height - 25, 100, 20)
            .build()
        );
    }
    
    /**
     * Calcula las estadísticas de bloques
     */
    private void calculateStats() {
        blockCount = blockCounter.countBlocks(schematic);
        
        if (sortByQuantity) {
            sortedBlocks = blockCounter.getSortedBlocks(schematic);
        } else {
            sortedBlocks = blockCounter.getSortedBlocksByName(schematic);
        }
        
        stackInfo = blockCounter.calculateStacks(schematic);
    }
    
    /**
     * Actualiza la lista de bloques
     */
    private void updateBlockList() {
        calculateStats();
        blockList.clearAllEntries();
        
        for (BlockCountEntry entry : sortedBlocks) {
            StackInfo stack = stackInfo.get(entry.getBlockId());
            blockList.addEntry(entry, stack);
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
        
        // Renderizar estadísticas generales
        int statsY = 40;
        
        String totalBlocks = Text.translatable(
            "neomatica.blocks.total",
            blockCount.getTotalBlocks()
        ).getString();
        context.drawTextWithShadow(
            this.textRenderer,
            totalBlocks,
            20,
            statsY,
            0xFFFFFF
        );
        
        String uniqueTypes = Text.translatable(
            "neomatica.blocks.unique",
            blockCount.getUniqueBlockTypes()
        ).getString();
        context.drawTextWithShadow(
            this.textRenderer,
            uniqueTypes,
            this.width - 20 - this.textRenderer.getWidth(uniqueTypes),
            statsY,
            0xFFFFFF
        );
        
        // Renderizar lista
        blockList.render(context, mouseX, mouseY, delta);
        
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