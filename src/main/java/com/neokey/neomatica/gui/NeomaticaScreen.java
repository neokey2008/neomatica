package com.neokey.neomatica.gui;

import com.neokey.neomatica.Neomatica;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

/**
 * Pantalla principal de Neomatica
 * Menú central para acceder a todas las funciones
 */
public class NeomaticaScreen extends Screen {
    
    private final Screen parent;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 24;
    
    public NeomaticaScreen(Screen parent) {
        super(Text.translatable("neomatica.menu.main"));
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int startY = this.height / 2 - 80;
        
        // Botón: Buscar Schematics Online
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.menu.browser"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new SchematicBrowserScreen(this));
                }
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Botón: Schematics Locales
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.menu.local"),
            button -> {
                if (this.client != null) {
                    // Abrir pantalla de schematics locales
                    // TODO: Crear LocalSchematicsScreen
                }
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Botón: Herramientas
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.menu.tools"),
            button -> {
                if (this.client != null) {
                    this.client.setScreen(new ToolsScreen(this));
                }
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 2, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Botón: Configuración
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("neomatica.menu.settings"),
            button -> {
                if (this.client != null) {
                    // Abrir pantalla de configuración
                    // TODO: Crear ConfigScreen
                }
            })
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 3, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
        
        // Botón: Cerrar
        this.addDrawableChild(ButtonWidget.builder(
            ScreenTexts.DONE,
            button -> this.close())
            .dimensions(centerX - BUTTON_WIDTH / 2, startY + BUTTON_SPACING * 5, BUTTON_WIDTH, BUTTON_HEIGHT)
            .build()
        );
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Renderizar fondo oscuro
        this.renderBackground(context, mouseX, mouseY, delta);
        
        // Renderizar título
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            20,
            0xFFFFFF
        );
        
        // Renderizar versión
        String version = "Neomatica v" + Neomatica.VERSION;
        context.drawTextWithShadow(
            this.textRenderer,
            version,
            this.width - this.textRenderer.getWidth(version) - 5,
            this.height - 15,
            0x808080
        );
        
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
        return false; // No pausar el juego
    }
}