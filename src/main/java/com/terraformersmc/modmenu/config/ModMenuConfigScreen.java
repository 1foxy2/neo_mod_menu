package com.terraformersmc.modmenu.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ModMenuConfigScreen extends ConfigurationScreen.ConfigurationSectionScreen {
    public ModMenuConfigScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }

    @Override
    protected void onChanged(String key) {
        super.onChanged(key);
    }
}
