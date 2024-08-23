package com.terraformersmc.modmenu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigScreen;
import com.terraformersmc.modmenu.util.EnumToLowerCaseJsonConverter;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import com.terraformersmc.modmenu.util.mod.java.JavaDummyMod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeDummyParentMod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.*;

@net.neoforged.fml.common.Mod(ModMenu.MOD_ID)
public class ModMenu {
	public static final String MOD_ID = "modmenu";
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
	public static final Gson GSON;
	public static final Gson GSON_MINIFIED;
	public static final Pair<ModMenuConfig, ModConfigSpec> CONFIG;

	public static final Component LIBRARIES = Component.translatable(MOD_ID + ".configuration.show_libraries");
	public static final Component SHOWN_LIBRARIES = Component.translatable( MOD_ID + ".configuration.show_libraries.true");
	public static final Component HIDDEN_LIBRARIES = Component.translatable(MOD_ID + ".configuration.show_libraries.false");
	public static final Component SORTING = Component.translatable(MOD_ID + ".configuration.sorting");
	public static final Component ASCENDING = Component.translatable(MOD_ID + ".configuration.sorting.ascending");
	public static final Component DESCENDING = Component.translatable(MOD_ID + ".configuration.sorting.descending");

	static {
		GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Enum.class,
				new EnumToLowerCaseJsonConverter()
			)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		GSON = builder.setPrettyPrinting().create();
		GSON_MINIFIED = builder.create();

		CONFIG = new ModConfigSpec.Builder()
				.configure(ModMenuConfig::new);
		// Store pair values in some constant field
	}

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	private static final Map<String, IConfigScreenFactory> configScreenFactories = new HashMap<>();

	private static int cachedDisplayedModCount = -1;
	public static final boolean HAS_SINYTRA = ModList.get().isLoaded("connector");
	public static final boolean TEXT_PLACEHOLDER_COMPAT = ModList.get().isLoaded("placeholder_api");

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		configScreenFactories.putIfAbsent("minecraft", (modContainer, screen) -> new OptionsScreen(screen, Minecraft.getInstance().options));

		if (ModMenu.getConfig().HIDDEN_CONFIGS.get().contains(modid)) {
			return null;
		}
		IConfigScreenFactory factory = configScreenFactories.get(modid);
		if (factory != null) {
			return factory.createScreen(ModList.get().getModContainerById(modid).get(), menuScreen);
		}
		return null;
	}

	public ModMenu(IEventBus event, ModContainer container) {
		event.addListener(this::onClientSetup);
		container.registerConfig(ModConfig.Type.CLIENT, CONFIG.getValue());
		container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) ->
				new ConfigurationScreen(container, screen, ModMenuConfigScreen::new));

		// Fill mods map
		for (ModContainer modContainer : ModList.get().getSortedMods()) {
			Mod mod;

			if (HAS_SINYTRA && FabricMod.isFabricMod(modContainer.getModId())) {
				mod = new FabricMod(modContainer.getModId());
			} else {
				mod = new NeoforgeMod(modContainer);
			}

			MODS.put(mod.getId(), mod);
		}

		Map<String, Mod> dummyParents = new HashMap<>();

		// Initialize parent map
		for (Mod mod : MODS.values()) {
			String parentId = mod.getParent();
			if (parentId != null) {
				Mod parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
				if (parent == null) {
					if (mod instanceof NeoforgeMod) {
						parent = new NeoforgeDummyParentMod(mod, parentId);
					}
					if (mod instanceof FabricMod) {
						parent = new NeoforgeDummyParentMod(mod, parentId);
					}
					dummyParents.put(parentId, parent);
				}
				PARENT_MAP.put(parent, mod);
			} else {
				ROOT_MODS.put(mod.getId(), mod);
			}
		}

		Mod java = new JavaDummyMod();
		MODS.put("java", java);
		ROOT_MODS.put("java", java);
		MODS.putAll(dummyParents);
		ROOT_MODS.putAll(dummyParents);
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
	}

	public static Component getLibrariesComponent() {
		return CommonComponents.optionNameValue(LIBRARIES, ModMenu.getConfig().SHOW_LIBRARIES.get() ? SHOWN_LIBRARIES : HIDDEN_LIBRARIES);
	}

	public static Component getSortingComponent() {
		return CommonComponents.optionNameValue(SORTING, ModMenu.getConfig().SORTING.get() == ModMenuConfig.Sorting.ASCENDING ? ASCENDING : DESCENDING);
	}

	public void onClientSetup(FMLClientSetupEvent event) {
		ModList.get().getMods().forEach(info -> IConfigScreenFactory.getForMod(info).ifPresent(
				factory -> configScreenFactories.put(info.getModId(), factory)));
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			boolean includeChildren = ModMenu.getConfig().COUNT_CHILDREN.get();
			boolean includeLibraries = ModMenu.getConfig().COUNT_LIBRARIES.get();
			boolean includeHidden = ModMenu.getConfig().COUNT_HIDDEN_MODS.get();

			// listen, if you have >= 2^32 mods then that's on you
			cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter(mod -> {
				boolean isChild = mod.getParent() != null;
				if (!includeChildren && isChild) {
					return false;
				}
				boolean isLibrary = mod.getBadges().contains(Mod.Badge.LIBRARY);
				if (!includeLibraries && isLibrary) {
					return false;
				}
				return includeHidden || !mod.isHidden();
			}).count());
		}
		return NumberFormat.getInstance().format(cachedDisplayedModCount);
	}

	public static Component createModsButtonText(boolean title) {
		var titleStyle = ModMenu.getConfig().MODS_BUTTON_STYLE.get();
		var gameMenuStyle = ModMenu.getConfig().GAME_MENU_BUTTON_STYLE.get();
		var isIcon = title ?
			titleStyle == ModMenuConfig.TitleMenuButtonStyle.ICON :
			gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.ICON;
		var isShort = title ?
			titleStyle == ModMenuConfig.TitleMenuButtonStyle.SHRINK :
			gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.REPLACE;
		MutableComponent modsText = ModMenuScreenTexts.TITLE.copy();
		if (ModMenu.getConfig().MOD_COUNT_LOCATION.get().isOnModsButton() && !isIcon) {
			String count = ModMenu.getDisplayedModCount();
			if (isShort) {
				modsText.append(Component.literal(" ")).append(Component.translatable("modmenu.loaded.short", count));
			} else {
				String specificKey = "modmenu.loaded." + count;
				String key = I18n.exists(specificKey) ? specificKey : "modmenu.loaded";
				if (ModMenu.getConfig().EASTER_EGGS.get() && I18n.exists(specificKey + ".secret")) {
					key = specificKey + ".secret";
				}
				modsText.append(Component.literal(" ")).append(Component.translatable(key, count));
			}
		}
		return modsText;
	}

	public static ModMenuConfig getConfig() {
		return CONFIG.getLeft();
	}
}
