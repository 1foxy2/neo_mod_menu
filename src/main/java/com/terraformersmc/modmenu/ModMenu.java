package com.terraformersmc.modmenu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.EnumToLowerCaseJsonConverter;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeDummyParentMod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@net.neoforged.fml.common.Mod(ModMenu.MOD_ID)
public class ModMenu {
	public static final String MOD_ID = "modmenu";
	public static final String GITHUB_REF = "TerraformersMC/ModMenu";
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
	public static final Gson GSON;
	public static final Gson GSON_MINIFIED;

	static {
		GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Enum.class,
				new EnumToLowerCaseJsonConverter()
			)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		GSON = builder.setPrettyPrinting().create();
		GSON_MINIFIED = builder.create();
	}

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	private static final Map<String, IConfigScreenFactory> configScreenFactories = new HashMap<>();

	private static int cachedDisplayedModCount = -1;
	public static final boolean RUNNING_QUILT = false;
	public static final boolean DEV_ENVIRONMENT = !FMLEnvironment.production;
	public static final boolean TEXT_PLACEHOLDER_COMPAT = ModList.get().isLoaded("placeholder_api");

	public static Screen getConfigScreen(String modid, Screen menuScreen) {
		configScreenFactories.putIfAbsent("minecraft", (modContainer, screen) -> new OptionsScreen(screen, Minecraft.getInstance().options));

		if (ModMenuConfig.hidden_configs.contains(modid)) {
			return null;
		}
		IConfigScreenFactory factory = configScreenFactories.get(modid);
		if (factory != null) {
			return factory.createScreen(ModList.get().getModContainerById(modid).get(), menuScreen);
		}
		return null;
	}

	public ModMenu(IEventBus event, ModContainer container) {
		//ModMenuConfigManager.initializeConfig();
		Set<String> modpackMods = new HashSet<>();
		//Map<String, UpdateChecker> updateCheckers = new HashMap<>();
		//Map<String, UpdateChecker> providedUpdateCheckers = new HashMap<>();

		event.addListener(this::onClientSetup);

		container.registerConfig(ModConfig.Type.CLIENT, ModMenuConfig.SPEC);
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		// Ignore deprecations, they're from Quilt Loader being in the dev env
		/*FabricLoader.getInstance().getEntrypointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
			//noinspection deprecation
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				ModMenuApi api = entrypoint.getEntrypoint();
				configScreenFactories.put(modId, api.getModConfigScreenFactory());
				apiImplementations.add(api);
				updateCheckers.put(modId, api.getUpdateChecker());
				providedUpdateCheckers.putAll(api.getProvidedUpdateCheckers());
				api.attachModpackBadges(modpackMods::add);
			} catch (Throwable e) {
				LOGGER.error("Mod {} provides a broken implementation of ModMenuApi", modId, e);
			}
		});*/

		// Fill mods map
		for (ModContainer modContainer : ModList.get().getSortedMods()) {
			Mod mod;

			if (RUNNING_QUILT) {
				//mod = new QuiltMod(modContainer, modpackMods);
			} else {
				mod = new NeoforgeMod(modContainer, modpackMods);
				//mod = new FabricMod(modContainer, modpackMods);
			}

			/*var updateChecker = updateCheckers.get(mod.getId());

			if (updateChecker == null) {
				updateChecker = providedUpdateCheckers.get(mod.getId());
			}*/

			MODS.put(mod.getId(), mod);
			//mod.setUpdateChecker(updateChecker);
		}

		Map<String, Mod> dummyParents = new HashMap<>();

		// Initialize parent map
		for (Mod mod : MODS.values()) {
			String parentId = mod.getParent();
			if (parentId != null) {
				Mod parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
				if (parent == null) {
					if (mod instanceof NeoforgeMod) {
						parent = new NeoforgeDummyParentMod((NeoforgeMod) mod, parentId);
						dummyParents.put(parentId, parent);
					}
				}
				PARENT_MAP.put(parent, mod);
			} else {
				ROOT_MODS.put(mod.getId(), mod);
			}
		}
		MODS.putAll(dummyParents);
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
	}

	public void onClientSetup(FMLClientSetupEvent event) {
		ModList.get().getMods().forEach(info -> IConfigScreenFactory.getForMod(info).ifPresent(
				factory -> configScreenFactories.put(info.getModId(), factory)));
	}

	public static boolean areModUpdatesAvailable() {
		/*if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
			return false;
		}

		for (Mod mod : MODS.values()) {
			if (mod.isHidden()) {
				continue;
			}

			if (!ModMenuConfig.show_libraries && mod.getBadges().contains(Mod.Badge.LIBRARY)) {
				continue;
			}

			if (mod.hasUpdate() || mod.getChildHasUpdate()) {
				return true; // At least one currently visible mod has an update
			}
		}*/

		return false;
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			boolean includeChildren = ModMenuConfig.count_children;
			boolean includeLibraries = ModMenuConfig.count_libraries;
			boolean includeHidden = ModMenuConfig.count_hidden_mods;

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
		var titleStyle = ModMenuConfig.mods_button_style;
		var gameMenuStyle = ModMenuConfig.game_menu_button_style;
		var isIcon = title ?
			titleStyle == ModMenuConfig.TitleMenuButtonStyle.ICON :
			gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.ICON;
		var isShort = title ?
			titleStyle == ModMenuConfig.TitleMenuButtonStyle.SHRINK :
			gameMenuStyle == ModMenuConfig.GameMenuButtonStyle.REPLACE;
		MutableComponent modsText = ModMenuScreenTexts.TITLE.copy();
		if (ModMenuConfig.mod_count_location.isOnModsButton() && !isIcon) {
			String count = ModMenu.getDisplayedModCount();
			if (isShort) {
				modsText.append(Component.literal(" ")).append(Component.translatable("modmenu.loaded.short", count));
			} else {
				String specificKey = "modmenu.loaded." + count;
				String key = I18n.exists(specificKey) ? specificKey : "modmenu.loaded";
				if (ModMenuConfig.easter_eggs && I18n.exists(specificKey + ".secret")) {
					key = specificKey + ".secret";
				}
				modsText.append(Component.literal(" ")).append(Component.translatable(key, count));
			}
		}
		return modsText;
	}
}
