package com.neokey.neomatica.gui.widgets;

import com.neokey.neomatica.tools.ToolManager.Tool;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Botón especializado para herramientas
 */
public class ToolButtonWidget extends ButtonWidget {
    
    private final Tool tool;
    private boolean isActive = false;
    private boolean isEnabled = true;
    
    public ToolButtonWidget(int x, int y, int width, int height, Tool tool, PressAction onPress) {
        super(x, y, width, height, Text.literal(tool.getDisplayName()), onPress, DEFAULT_NARRATION_SUPPLIER);
        this.tool = tool;
    }
    
    /**
     * Establece si esta herramienta está activa
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    /**
     * Verifica si esta herramienta está activa
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Establece si esta herramienta está habilitada
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        this.active = enabled;
    }
    
    /**
     * Obtiene la herramienta asociada
     */
    public Tool getTool() {
        return tool;
    }
    
    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        // Color del botón basado en estado
        int backgroundColor;
        int textColor;
        
        if (isActive) {
            // Verde si está activa
            backgroundColor = 0xFF00AA00;
            textColor = 0xFFFFFF;
        } else if (!isEnabled || !active) {
            // Gris si está deshabilitada
            backgroundColor = 0xFF333333;
            textColor = 0xFF666666;
        } else if (isMouseOver(mouseX, mouseY)) {
            // Azul claro si hay hover
            backgroundColor = 0xFF5599FF;
            textColor = 0xFFFFFF;
        } else {
            // Gris oscuro normal
            backgroundColor = 0xFF555555;
            textColor = 0xFFFFFF;
        }
        
        // Renderizar fondo del botón
        context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), backgroundColor);
        
        // Renderizar borde
        int borderColor = isActive ? 0xFF00FF00 : 0xFF888888;
        context.drawBorder(getX(), getY(), getWidth(), getHeight(), borderColor);
        
        // Renderizar icono de la herramienta (emoji/símbolo)
        String icon = getToolIcon(tool);
        int iconWidth = this.getWidth() > 60 ? 20 : this.getWidth() - 10;
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            icon,
            getX() + iconWidth / 2,
            getY() + (getHeight() - 8) / 2,
            0xFFFFFF
        );
        
        // Renderizar texto del botón (si hay espacio)
        if (this.getWidth() > 60) {
            context.drawText(
                this.textRenderer,
                this.getMessage(),
                getX() + 25,
                getY() + (getHeight() - 8) / 2,
                textColor,
                false
            );
        }
        
        // Renderizar tooltip si hay hover
        if (isMouseOver(mouseX, mouseY) && tool.getDescription() != null) {
            // El tooltip se renderiza en la pantalla padre
        }
    }
    
    /**
     * Obtiene el icono visual para cada herramienta
     */
    private String getToolIcon(Tool tool) {
        return switch (tool) {
            case SELECT -> "⬚"; // Cuadro de selección
            case COPY -> "📋"; // Portapapeles
            case PASTE -> "📄"; // Pegar
            case MOVE -> "↔"; // Flechas de movimiento
            case DELETE -> "🗑"; // Papelera
            case ROTATE -> "↻"; // Rotación
            case FLIP -> "⇄"; // Flip
            case FILL -> "🎨"; // Relleno
        };
    }
    
    @Override
    protected boolean clicked(double mouseX, double mouseY) {
        return this.active && this.visible && isEnabled && 
               mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && 
               mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height);
    }
    
    /**
     * Verifica si el mouse está sobre el botón
     */
    private boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= (double)this.getX() && mouseY >= (double)this.getY() && 
               mouseX < (double)(this.getX() + this.width) && mouseY < (double)(this.getY() + this.height);
    }
}