package com.terraformersmc.mod_menu.config;

import com.terraformersmc.mod_menu.ModMenu;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ModMenuConfigListScreen<T> extends ConfigurationScreen.ConfigurationListScreen<T> {
    public ModMenuConfigListScreen(Context context, String key, Component title, ModConfigSpec.ListValueSpec spec, ModConfigSpec.ConfigValue valueList) {
        super(context, key, title, spec, valueList);
    }

    @Override
    public ConfigurationScreen.ConfigurationSectionScreen rebuild() {
        return super.rebuild();
    }

    @Override
    public void onClose() {
        super.onClose();
        ModMenu.addBadges();
    }
}
