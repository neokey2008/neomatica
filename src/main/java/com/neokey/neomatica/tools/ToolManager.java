package com.neokey.neomatica.tools;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.io.File;

/**
 * Gestor central de herramientas de Neomatica
 */
public class ToolManager {
    
    private final MinecraftClient client;
    private final SchematicManager schematicManager;
    
    private Tool activeTool;
    private SelectionTool selectionTool;
    private CopyTool copyTool;
    private PasteTool pasteTool;
    private MoveTool moveTool;
    private DeleteTool deleteTool;
    private RotateTool rotateTool;
    private FlipTool flipTool;
    
    private BlockPos position1;
    private BlockPos position2;
    private LoadedSchematic clipboard;
    
    public ToolManager() {
        this.client = MinecraftClient.getInstance();
        this.schematicManager = Neomatica.getInstance().getSchematicManager();
        
        // Inicializar herramientas
        this.selectionTool = new SelectionTool(this);
        this.copyTool = new CopyTool(this);
        this.pasteTool = new PasteTool(this);
        this.moveTool = new MoveTool(this);
        this.deleteTool = new DeleteTool(this);
        this.rotateTool = new RotateTool(this);
        this.flipTool = new FlipTool(this);
        
        this.activeTool = Tool.SELECT;
    }
    
    /**
     * Actualiza las herramientas cada tick
     */
    public void tick(MinecraftClient client) {
        if (activeTool == null || client.player == null) {
            return;
        }
        
        // Actualizar herramienta activa
        switch (activeTool) {
            case SELECT -> selectionTool.tick();
            case PASTE -> pasteTool.tick();
            case MOVE -> moveTool.tick();
        }
    }
    
    /**
     * Establece la herramienta activa
     */
    public void setActiveTool(Tool tool) {
        this.activeTool = tool;
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("Herramienta activada: " + tool.getDisplayName()),
                true
            );
        }
    }
    
    /**
     * Obtiene la herramienta activa
     */
    public Tool getActiveTool() {
        return activeTool;
    }
    
    /**
     * Establece la primera posición de selección
     */
    public void setPosition1() {
        if (client.player == null) return;
        
        BlockPos pos = client.player.getBlockPos();
        position1 = pos;
        
        client.player.sendMessage(
            Text.translatable("neomatica.selection.pos1"),
            true
        );
        
        Neomatica.LOGGER.info("Posición 1 establecida: {}", pos);
    }
    
    /**
     * Establece la primera posición directamente
     */
    public void setPosition1Direct(BlockPos pos) {
        position1 = pos;
    }
    
    /**
     * Establece la segunda posición de selección
     */
    public void setPosition2() {
        if (client.player == null) return;
        
        BlockPos pos = client.player.getBlockPos();
        position2 = pos;
        
        client.player.sendMessage(
            Text.translatable("neomatica.selection.pos2"),
            true
        );
        
        if (position1 != null) {
            showSelectionInfo();
        }
        
        Neomatica.LOGGER.info("Posición 2 establecida: {}", pos);
    }
    
    /**
     * Establece la segunda posición directamente
     */
    public void setPosition2Direct(BlockPos pos) {
        position2 = pos;
        if (position1 != null) {
            showSelectionInfo();
        }
    }
    
    /**
     * Muestra información de la selección
     */
    private void showSelectionInfo() {
        if (position1 == null || position2 == null) return;
        
        int sizeX = Math.abs(position2.getX() - position1.getX()) + 1;
        int sizeY = Math.abs(position2.getY() - position1.getY()) + 1;
        int sizeZ = Math.abs(position2.getZ() - position1.getZ()) + 1;
        
        if (client.player != null) {
            client.player.sendMessage(
                Text.translatable("neomatica.selection.size", sizeX, sizeY, sizeZ),
                false
            );
        }
    }
    
    /**
     * Copia el área seleccionada
     */
    public void copyArea() {
        if (!hasSelection()) {
            sendMessage("neomatica.message.error", "No hay área seleccionada");
            return;
        }
        
        clipboard = copyTool.copy(position1, position2);
        
        if (clipboard != null) {
            sendMessage("neomatica.message.copied");
        } else {
            sendMessage("neomatica.message.error", "Error al copiar área");
        }
    }
    
    /**
     * Pega el schematic del clipboard
     */
    public void pasteSchematic() {
        if (clipboard == null) {
            sendMessage("neomatica.message.error", "No hay nada en el portapapeles");
            return;
        }
        
        if (client.player == null) return;
        
        BlockPos pos = client.player.getBlockPos();
        boolean success = pasteTool.paste(clipboard, pos);
        
        if (success) {
            sendMessage("neomatica.message.pasted");
        } else {
            sendMessage("neomatica.message.error", "Error al pegar schematic");
        }
    }
    
    /**
     * Mueve el schematic activo
     */
    public void moveSchematic(int x, int y, int z) {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        moveTool.move(active, x, y, z);
        sendMessage("neomatica.message.moved");
    }
    
    /**
     * Elimina bloques en el área seleccionada
     */
    public void deleteArea() {
        if (!hasSelection()) {
            sendMessage("neomatica.message.error", "No hay área seleccionada");
            return;
        }
        
        boolean success = deleteTool.delete(position1, position2);
        
        if (success) {
            sendMessage("neomatica.message.deleted");
        } else {
            sendMessage("neomatica.message.error", "Error al eliminar bloques");
        }
    }
    
    /**
     * Quita el schematic activo
     */
    public void removeActiveSchematic() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        schematicManager.removeSchematic(active.getId());
        sendMessage("Schematic removido");
    }
    
    /**
     * Alterna la visibilidad del schematic activo
     */
    public void toggleVisibility() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        active.setVisible(!active.isVisible());
        
        String message = active.isVisible() ? "Schematic visible" : "Schematic oculto";
        sendMessage(message);
    }
    
    /**
     * Exporta el schematic activo
     */
    public void exportSchematic() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        String fileName = active.getName() + "_" + System.currentTimeMillis() + ".litematic";
        File outputFile = new File("schematics/exports", fileName);
        
        boolean success = schematicManager.exportSchematic(active, outputFile);
        
        if (success) {
            sendMessage("neomatica.message.exported", outputFile.getPath());
        } else {
            sendMessage("neomatica.message.error", "Error al exportar");
        }
    }
    
    /**
     * Rota el schematic activo 90 grados en sentido horario
     */
    public void rotateSchematicClockwise() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        LoadedSchematic rotated = rotateTool.rotateClockwise(active);
        
        if (rotated != null) {
            schematicManager.removeSchematic(active.getId());
            String id = java.util.UUID.randomUUID().toString();
            rotated.setId(id);
            schematicManager.addSchematic(id, rotated);
            schematicManager.setActiveSchematic(id);
            
            sendMessage("Schematic rotado 90° horario");
        }
    }
    
    /**
     * Rota el schematic activo 90 grados en sentido antihorario
     */
    public void rotateSchematicCounterClockwise() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        LoadedSchematic rotated = rotateTool.rotateCounterClockwise(active);
        
        if (rotated != null) {
            schematicManager.removeSchematic(active.getId());
            String id = java.util.UUID.randomUUID().toString();
            rotated.setId(id);
            schematicManager.addSchematic(id, rotated);
            schematicManager.setActiveSchematic(id);
            
            sendMessage("Schematic rotado 90° antihorario");
        }
    }
    
    /**
     * Voltea el schematic activo en el eje X
     */
    public void flipSchematicX() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        LoadedSchematic flipped = flipTool.flipX(active);
        
        if (flipped != null) {
            schematicManager.removeSchematic(active.getId());
            String id = java.util.UUID.randomUUID().toString();
            flipped.setId(id);
            schematicManager.addSchematic(id, flipped);
            schematicManager.setActiveSchematic(id);
            
            sendMessage("Schematic volteado en eje X");
        }
    }
    
    /**
     * Voltea el schematic activo en el eje Y
     */
    public void flipSchematicY() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        LoadedSchematic flipped = flipTool.flipY(active);
        
        if (flipped != null) {
            schematicManager.removeSchematic(active.getId());
            String id = java.util.UUID.randomUUID().toString();
            flipped.setId(id);
            schematicManager.addSchematic(id, flipped);
            schematicManager.setActiveSchematic(id);
            
            sendMessage("Schematic volteado en eje Y");
        }
    }
    
    /**
     * Voltea el schematic activo en el eje Z
     */
    public void flipSchematicZ() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        
        if (active == null) {
            sendMessage("neomatica.message.error", "No hay schematic activo");
            return;
        }
        
        LoadedSchematic flipped = flipTool.flipZ(active);
        
        if (flipped != null) {
            schematicManager.removeSchematic(active.getId());
            String id = java.util.UUID.randomUUID().toString();
            flipped.setId(id);
            schematicManager.addSchematic(id, flipped);
            schematicManager.setActiveSchematic(id);
            
            sendMessage("Schematic volteado en eje Z");
        }
    }
    
    /**
     * Limpia la selección
     */
    public void clearSelection() {
        position1 = null;
        position2 = null;
        sendMessage("neomatica.selection.clear");
    }
    
    /**
     * Verifica si hay una selección válida
     */
    public boolean hasSelection() {
        return position1 != null && position2 != null;
    }
    
    /**
     * Verifica si hay un schematic visible
     */
    public boolean isSchematicVisible() {
        LoadedSchematic active = schematicManager.getActiveSchematic();
        return active != null && active.isVisible();
    }
    
    /**
     * Envía un mensaje al jugador
     */
    private void sendMessage(String key, Object... args) {
        if (client.player != null) {
            client.player.sendMessage(Text.translatable(key, args), false);
        }
    }
    
    // Getters
    
    public BlockPos getPosition1() {
        return position1;
    }
    
    public BlockPos getPosition2() {
        return position2;
    }
    
    public LoadedSchematic getClipboard() {
        return clipboard;
    }
    
    public SelectionTool getSelectionTool() {
        return selectionTool;
    }
    
    public CopyTool getCopyTool() {
        return copyTool;
    }
    
    public PasteTool getPasteTool() {
        return pasteTool;
    }
    
    public MoveTool getMoveTool() {
        return moveTool;
    }
    
    public DeleteTool getDeleteTool() {
        return deleteTool;
    }
    
    public RotateTool getRotateTool() {
        return rotateTool;
    }
    
    public FlipTool getFlipTool() {
        return flipTool;
    }
    
    /**
     * Enum de herramientas disponibles
     */
    public enum Tool {
        SELECT("Seleccionar", "Selecciona un área con dos puntos"),
        COPY("Copiar", "Copia el área seleccionada"),
        PASTE("Pegar", "Pega el schematic del portapapeles"),
        MOVE("Mover", "Mueve el schematic activo"),
        DELETE("Borrar", "Elimina bloques del área seleccionada"),
        ROTATE("Rotar", "Rota el schematic activo"),
        FLIP("Voltear", "Voltea el schematic activo"),
        FILL("Rellenar", "Rellena el área con un bloque");
        
        private final String displayName;
        private final String description;
        
        Tool(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}