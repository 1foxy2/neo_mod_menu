package com.terraformersmc.mod_menu.util;

import com.terraformersmc.mod_menu.ModMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class ModMenuScreenTexts {
	public static final Component CONFIGURE = Component.translatable("modmenu.configure");
	public static final Component DROP_CONFIRM = Component.translatable("modmenu.dropConfirm");
	public static final Component DROP_INFO_LINE_1 = Component.translatable("modmenu.dropInfo.line1");
	public static final Component DROP_INFO_LINE_2 = Component.translatable("modmenu.dropInfo.line2");
	public static final Component DROP_SUCCESSFUL_LINE_1 = Component.translatable("modmenu.dropSuccessful.line1");
	public static final Component DROP_SUCCESSFUL_LINE_2 = Component.translatable("modmenu.dropSuccessful.line2");
	public static final Component ISSUES = Component.translatable("modmenu.issues");
	public static final Component MODS_FOLDER = Component.translatable("modmenu.modsFolder");
	public static final Component SEARCH = Component.translatable("modmenu.search");
	public static final Component TITLE = Component.translatable("modmenu.title");
	public static final Component TOGGLE_FILTER_OPTIONS = Component.translatable("modmenu.toggleFilterOptions");
	public static final Component WEBSITE = Component.translatable("modmenu.website");

	public static final String LIBRARIES = "option.modmenu.show_libraries";
	public static final String SORTING = "option.modmenu.sorting";

	private ModMenuScreenTexts() {
	}

	public static Component modIdTooltip(String modId) {
		return Component.translatable("modmenu.modIdToolTip", modId);
	}

	public static Component configureError(String modId, Throwable e) {
		return Component.translatable("modmenu.configure.error", modId, modId)
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
