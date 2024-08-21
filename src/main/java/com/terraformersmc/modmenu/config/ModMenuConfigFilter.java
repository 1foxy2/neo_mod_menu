package com.terraformersmc.modmenu.config;

import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.jetbrains.annotations.Nullable;

public class ModMenuConfigFilter implements ConfigurationScreen.ConfigurationSectionScreen.Filter {
    @Nullable
    @Override
    public ConfigurationScreen.ConfigurationSectionScreen.Element filterEntry(ConfigurationScreen.ConfigurationSectionScreen.Context context, String key, ConfigurationScreen.ConfigurationSectionScreen.Element original) {
        return original;
    }
}
