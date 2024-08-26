package com.terraformersmc.mod_menu.util;

import com.terraformersmc.mod_menu.ModMenu;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ModMenuScreenTexts {
	public static final Component TITLE = Component.translatable("mod_menu.title");

	public static final String LIBRARIES = ModMenu.MOD_ID + ".configuration.show_libraries";
	public static final String SORTING = ModMenu.MOD_ID + ".configuration.sorting";

	private ModMenuScreenTexts() {
	}

	public static Component getLibrariesComponent() {
		return CommonComponents.optionNameValue(Component.translatable(LIBRARIES),
				Component.translatable(LIBRARIES + "." + ModMenu.getConfig().SHOW_LIBRARIES.get().toString().toLowerCase()));
	}

	public static Component getSortingComponent() {
		return CommonComponents.optionNameValue(Component.translatable(SORTING),
				Component.translatable(SORTING + "." + ModMenu.getConfig().SORTING.get().toString().toLowerCase()));
	}
}
