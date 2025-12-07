package com.neokey.neomatica.tools;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Herramienta de selección de área
 */
public class SelectionTool {
    
    private final ToolManager toolManager;
    private final MinecraftClient client;
    
    private BlockPos hoveredPos;
    private boolean isSelecting;
    
    public SelectionTool(ToolManager toolManager) {
        this.toolManager = toolManager;
        this.client = MinecraftClient.getInstance();
    }
    
    /**
     * Actualiza la herramienta cada tick
     */
    public void tick() {
        if (client.player == null || client.world == null) {
            return;
        }
        
        // Obtener el bloque que el jugador está mirando
        updateHoveredBlock();
    }
    
    /**
     * Actualiza el bloque sobre el que está el cursor
     */
    private void updateHoveredBlock() {
        HitResult hitResult = client.crosshairTarget;
        
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hitResult;
            hoveredPos = blockHit.getBlockPos();
        } else {
            hoveredPos = null;
        }
    }
    
    /**
     * Selecciona la primera posición
     */
    public void selectPosition1() {
        if (hoveredPos != null) {
            toolManager.setPosition1();
        } else if (client.player != null) {
            // Usar posición del jugador si no hay bloque apuntado
            BlockPos playerPos = client.player.getBlockPos();
            toolManager.setPosition1();
        }
    }
    
    /**
     * Selecciona la segunda posición
     */
    public void selectPosition2() {
        if (hoveredPos != null) {
            toolManager.setPosition2();
        } else if (client.player != null) {
            // Usar posición del jugador si no hay bloque apuntado
            BlockPos playerPos = client.player.getBlockPos();
            toolManager.setPosition2();
        }
    }
    
    /**
     * Selecciona automáticamente un área basada en un punto
     */
    public void autoSelect(BlockPos center, int radius) {
        BlockPos pos1 = center.add(-radius, -radius, -radius);
        BlockPos pos2 = center.add(radius, radius, radius);
        
        // Establecer las posiciones en el tool manager
        // (Esto requeriría métodos públicos en ToolManager)
    }
    
    /**
     * Expande la selección en una dirección
     */
    public void expandSelection(int x, int y, int z) {
        BlockPos pos1 = toolManager.getPosition1();
        BlockPos pos2 = toolManager.getPosition2();
        
        if (pos1 == null || pos2 == null) {
            return;
        }
        
        // Expandir en las direcciones especificadas
        int minX = Math.min(pos1.getX(), pos2.getX()) - (x < 0 ? Math.abs(x) : 0);
        int minY = Math.min(pos1.getY(), pos2.getY()) - (y < 0 ? Math.abs(y) : 0);
        int minZ = Math.min(pos1.getZ(), pos2.getZ()) - (z < 0 ? Math.abs(z) : 0);
        
        int maxX = Math.max(pos1.getX(), pos2.getX()) + (x > 0 ? x : 0);
        int maxY = Math.max(pos1.getY(), pos2.getY()) + (y > 0 ? y : 0);
        int maxZ = Math.max(pos1.getZ(), pos2.getZ()) + (z > 0 ? z : 0);
        
        // Actualizar posiciones (esto requeriría setters públicos)
        // toolManager.setPosition1Direct(new BlockPos(minX, minY, minZ));
        // toolManager.setPosition2Direct(new BlockPos(maxX, maxY, maxZ));
    }
    
    /**
     * Contrae la selección
     */
    public void contractSelection(int x, int y, int z) {
        expandSelection(-x, -y, -z);
    }
    
    /**
     * Obtiene el bloque sobre el que está el cursor
     */
    public BlockPos getHoveredBlock() {
        return hoveredPos;
    }
    
    /**
     * Verifica si está seleccionando
     */
    public boolean isSelecting() {
        return isSelecting;
    }
    
    /**
     * Establece el estado de selección
     */
    public void setSelecting(boolean selecting) {
        this.isSelecting = selecting;
    }
    
    /**
     * Calcula el volumen de la selección actual
     */
    public int getSelectionVolume() {
        BlockPos pos1 = toolManager.getPosition1();
        BlockPos pos2 = toolManager.getPosition2();
        
        if (pos1 == null || pos2 == null) {
            return 0;
        }
        
        int sizeX = Math.abs(pos2.getX() - pos1.getX()) + 1;
        int sizeY = Math.abs(pos2.getY() - pos1.getY()) + 1;
        int sizeZ = Math.abs(pos2.getZ() - pos1.getZ()) + 1;
        
        return sizeX * sizeY * sizeZ;
    }
    
    /**
     * Obtiene el centro de la selección
     */
    public BlockPos getSelectionCenter() {
        BlockPos pos1 = toolManager.getPosition1();
        BlockPos pos2 = toolManager.getPosition2();
        
        if (pos1 == null || pos2 == null) {
            return null;
        }
        
        int centerX = (pos1.getX() + pos2.getX()) / 2;
        int centerY = (pos1.getY() + pos2.getY()) / 2;
        int centerZ = (pos1.getZ() + pos2.getZ()) / 2;
        
        return new BlockPos(centerX, centerY, centerZ);
    }
    
    /**
     * Verifica si un bloque está dentro de la selección
     */
    public boolean isBlockInSelection(BlockPos pos) {
        BlockPos pos1 = toolManager.getPosition1();
        BlockPos pos2 = toolManager.getPosition2();
        
        if (pos1 == null || pos2 == null) {
            return false;
        }
        
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        return pos.getX() >= minX && pos.getX() <= maxX &&
               pos.getY() >= minY && pos.getY() <= maxY &&
               pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }
}