package com.neokey.neomatica.tools;

import com.neokey.neomatica.Neomatica;
import com.neokey.neomatica.schematic.SchematicManager.LoadedSchematic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Herramienta para mover schematics
 */
public class MoveTool {
    
    private final ToolManager toolManager;
    private final MinecraftClient client;
    
    private boolean isMoving;
    private BlockPos moveStartPos;
    
    public MoveTool(ToolManager toolManager) {
        this.toolManager = toolManager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Actualiza la herramienta cada tick
     */
    public void tick() {
        // Actualizar lógica de movimiento si es necesario
    }
    
    /**
     * Mueve un schematic por un offset
     */
    public void move(LoadedSchematic schematic, int x, int y, int z) {
        if (schematic == null) {
            return;
        }
        
        BlockPos currentPos = schematic.getPlacement();
        BlockPos newPos = currentPos.add(x, y, z);
        
        schematic.setPlacement(newPos);
        
        Neomatica.LOGGER.info("Schematic movido a: {}", newPos);
    }
    
    /**
     * Mueve un schematic a una posición específica
     */
    public void moveTo(LoadedSchematic schematic, BlockPos position) {
        if (schematic == null || position == null) {
            return;
        }
        
        schematic.setPlacement(position);
        Neomatica.LOGGER.info("Schematic movido a: {}", position);
    }
    
    /**
     * Mueve un schematic hacia arriba
     */
    public void moveUp(LoadedSchematic schematic, int blocks) {
        move(schematic, 0, blocks, 0);
    }
    
    /**
     * Mueve un schematic hacia abajo
     */
    public void moveDown(LoadedSchematic schematic, int blocks) {
        move(schematic, 0, -blocks, 0);
    }
    
    /**
     * Mueve un schematic hacia el norte
     */
    public void moveNorth(LoadedSchematic schematic, int blocks) {
        move(schematic, 0, 0, -blocks);
    }
    
    /**
     * Mueve un schematic hacia el sur
     */
    public void moveSouth(LoadedSchematic schematic, int blocks) {
        move(schematic, 0, 0, blocks);
    }
    
    /**
     * Mueve un schematic hacia el este
     */
    public void moveEast(LoadedSchematic schematic, int blocks) {
        move(schematic, blocks, 0, 0);
    }
    
    /**
     * Mueve un schematic hacia el oeste
     */
    public void moveWest(LoadedSchematic schematic, int blocks) {
        move(schematic, -blocks, 0, 0);
    }
    
    /**
     * Inicia el modo de movimiento interactivo
     */
    public void startInteractiveMove(LoadedSchematic schematic) {
        if (schematic == null) {
            return;
        }
        
        isMoving = true;
        moveStartPos = schematic.getPlacement();
    }
    
    /**
     * Finaliza el modo de movimiento interactivo
     */
    public void finishInteractiveMove() {
        isMoving = false;
        moveStartPos = null;
    }
    
    /**
     * Cancela el movimiento y vuelve a la posición inicial
     */
    public void cancelMove(LoadedSchematic schematic) {
        if (schematic != null && moveStartPos != null) {
            schematic.setPlacement(moveStartPos);
        }
        
        isMoving = false;
        moveStartPos = null;
    }
    
    /**
     * Mueve el schematic hacia donde mira el jugador
     */
    public void moveToLookPosition(LoadedSchematic schematic, int distance) {
        if (client.player == null || schematic == null) {
            return;
        }
        
        // Calcular posición basada en la dirección de mirada
        BlockPos playerPos = client.player.getBlockPos();
        BlockPos targetPos = playerPos.offset(client.player.getHorizontalFacing(), distance);
        
        moveTo(schematic, targetPos);
    }
    
    /**
     * Centra el schematic en la posición del jugador
     */
    public void centerOnPlayer(LoadedSchematic schematic) {
        if (client.player == null || schematic == null) {
            return;
        }
        
        BlockPos playerPos = client.player.getBlockPos();
        
        // Ajustar para centrar el schematic
        int offsetX = schematic.getSize().getX() / 2;
        int offsetZ = schematic.getSize().getZ() / 2;
        
        BlockPos centeredPos = playerPos.add(-offsetX, 0, -offsetZ);
        moveTo(schematic, centeredPos);
    }
    
    /**
     * Alinea el schematic a la cuadrícula
     */
    public void alignToGrid(LoadedSchematic schematic, int gridSize) {
        if (schematic == null || gridSize <= 0) {
            return;
        }
        
        BlockPos currentPos = schematic.getPlacement();
        
        int alignedX = (currentPos.getX() / gridSize) * gridSize;
        int alignedY = (currentPos.getY() / gridSize) * gridSize;
        int alignedZ = (currentPos.getZ() / gridSize) * gridSize;
        
        BlockPos alignedPos = new BlockPos(alignedX, alignedY, alignedZ);
        moveTo(schematic, alignedPos);
    }
    
    /**
     * Nudge (movimiento pequeño) en una dirección
     */
    public void nudge(LoadedSchematic schematic, int x, int y, int z) {
        move(schematic, x, y, z);
    }
    
    /**
     * Obtiene la distancia de movimiento desde el inicio
     */
    public int getMoveDistance(LoadedSchematic schematic) {
        if (schematic == null || moveStartPos == null) {
            return 0;
        }
        
        BlockPos currentPos = schematic.getPlacement();
        
        int dx = Math.abs(currentPos.getX() - moveStartPos.getX());
        int dy = Math.abs(currentPos.getY() - moveStartPos.getY());
        int dz = Math.abs(currentPos.getZ() - moveStartPos.getZ());
        
        return dx + dy + dz;
    }
    
    // Getters
    
    public boolean isMoving() {
        return isMoving;
    }
    
    public BlockPos getMoveStartPos() {
        return moveStartPos;
    }
}