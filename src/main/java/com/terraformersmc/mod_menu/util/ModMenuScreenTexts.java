package com.terraformersmc.mod_menu.util;

import com.terraformersmc.mod_menu.ModMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ModMenuScreenTexts {
	public static final Component CONFIGURE = Component.translatable("mod_menu.configure");
	public static final Component DROP_CONFIRM = Component.translatable("mod_menu.dropConfirm");
	public static final Component DROP_INFO_LINE_1 = Component.translatable("mod_menu.dropInfo.line1");
	public static final Component DROP_INFO_LINE_2 = Component.translatable("mod_menu.dropInfo.line2");
	public static final Component DROP_SUCCESSFUL_LINE_1 = Component.translatable("mod_menu.dropSuccessful.line1");
	public static final Component DROP_SUCCESSFUL_LINE_2 = Component.translatable("mod_menu.dropSuccessful.line2");
	public static final Component ISSUES = Component.translatable("mod_menu.issues");
	public static final Component MODS_FOLDER = Component.translatable("mod_menu.modsFolder");
	public static final Component SEARCH = Component.translatable("mod_menu.search");
	public static final Component TITLE = Component.translatable("mod_menu.title");
	public static final Component TOGGLE_FILTER_OPTIONS = Component.translatable("mod_menu.toggleFilterOptions");
	public static final Component WEBSITE = Component.translatable("mod_menu.website");

	public static final String LIBRARIES = ModMenu.MOD_ID + ".configuration.show_libraries";
	public static final String SORTING = ModMenu.MOD_ID + ".configuration.sorting";

	private ModMenuScreenTexts() {
	}

	public static Component modIdTooltip(String modId) {
		return Component.translatable("mod_menu.modIdToolTip", modId);
	}

	public static Component configureError(String modId, Throwable e) {
		return Component.translatable("mod_menu.configure.error", modId, modId)
			.append(CommonComponents.NEW_LINE)
			.append(CommonComponents.NEW_LINE)
			.append(e.toString())
			.withStyle(ChatFormatting.RED);
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
