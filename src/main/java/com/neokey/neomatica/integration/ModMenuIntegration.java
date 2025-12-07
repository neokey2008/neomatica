package com.neokey.neomatica.integration;

import com.neokey.neomatica.gui.NeomaticaScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Integración con Mod Menu
 */
public class ModMenuIntegration implements ModMenuApi {
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new NeomaticaScreen(parent);
    }
}