package com.terraformersmc.mod_menu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.*;
import com.terraformersmc.mod_menu.config.ModMenuConfig;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import com.terraformersmc.mod_menu.util.EnumToLowerCaseJsonConverter;
import com.terraformersmc.mod_menu.util.ModMenuScreenTexts;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadge;
import com.terraformersmc.mod_menu.util.mod.fabric.FabricMod;
import com.terraformersmc.mod_menu.util.mod.java.JavaDummyMod;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeDummyParentMod;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiFunction;

@net.minecraftforge.fml.common.Mod(ModMenu.MOD_ID)
public class ModMenu {
	public static final String MOD_ID = "mod_menu";
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
	public static final Gson GSON;
	public static final Gson GSON_MINIFIED;
	public static final Pair<ModMenuConfig, ForgeConfigSpec> CONFIG;

	static {
		GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Enum.class,
				new EnumToLowerCaseJsonConverter()
			)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		GSON = builder.setPrettyPrinting().create();
		GSON_MINIFIED = builder.create();

		CONFIG = new ForgeConfigSpec.Builder()
				.configure(ModMenuConfig::new);
	}

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	public static final Map<String, BiFunction<Minecraft, Screen, Screen>> configScreenFactories = new HashMap<>();

	private static int cachedDisplayedModCount = -1;
	public static final boolean HAS_SINYTRA = ModList.get().isLoaded("connectormod");

	public static Screen getConfigScreen(ModContainer c, Screen menuScreen) {
		configScreenFactories.putIfAbsent("minecraft", (minecraft, screen) -> new OptionsScreen(screen, Minecraft.getInstance().options));

		if (ModMenu.getConfig().HIDDEN_CONFIGS.get().contains(c.getModId()) || "java".equals(c.getModId())) {
			return null;
		}

		BiFunction<Minecraft, Screen, Screen> factory = configScreenFactories.get(c.getModId());
		if (factory != null) {
            return factory.apply(Minecraft.getInstance(), menuScreen);
        }

		Optional<BiFunction<Minecraft, Screen, Screen>> factoryOptional = ConfigScreenHandler.getScreenFactoryFor(c.getModInfo());

		factoryOptional.ifPresent(f -> configScreenFactories.put(c.getModId(), f));

        return factoryOptional.map(f -> f.apply(Minecraft.getInstance(), menuScreen)).orElse(null);
    }

	public ModMenu() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::onClientSetup);

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG.getValue());
		//container.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, (modContainer, screen) ->
		//		new ConfigurationScreen(container, screen, ModMenuConfigScreen::new));

		// Fill mods map
		ModList.get().forEachModContainer((s, modContainer) -> {
			Mod mod;

			if (HAS_SINYTRA && ModsScreen.isFabricMod(modContainer.getModInfo().getOwningFile().getFile().getFilePath())) {
				mod = new FabricMod(modContainer.getModId());
			} else {
				mod = new NeoforgeMod(modContainer);
			}

			MODS.put(mod.getId(), mod);
		});

		Map<String, Mod> dummyParents = new HashMap<>();

		// Initialize parent map
		HashSet<String> modParentSet = new HashSet<>();
		for (Mod mod : MODS.values()) {
			String parentId = mod.getParent();
			if (parentId == null) {
				ROOT_MODS.put(mod.getId(), mod);
				continue;
			}

			Mod parent;
			modParentSet.clear();
			while (true) {
				parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
				if (parent == null) {
					if (mod instanceof NeoforgeMod) {
						parent = new NeoforgeDummyParentMod(mod, parentId);
					}
					if (mod instanceof FabricMod) {
						parent = new NeoforgeDummyParentMod(mod, parentId);
					}
					dummyParents.put(parentId, parent);
				}

				parentId = parent != null ? parent.getParent() : null;
				if (parentId == null) {
					// It will most likely end here in the first iteration
					break;
				}

				if (modParentSet.contains(parentId)) {
					LOGGER.warn("Mods contain each other as parents: {}", modParentSet);
					parent = null;
					break;
				}
				modParentSet.add(parentId);
			}

			if (parent == null) {
				ROOT_MODS.put(mod.getId(), mod);
				continue;
			}
			PARENT_MAP.put(parent, mod);
		}

		Mod java = new JavaDummyMod();
		MODS.put("java", java);
		ROOT_MODS.put("java", java);
		MODS.putAll(dummyParents);
	}

	public void onClientSetup(FMLClientSetupEvent event) {
		ModList.get().getMods().forEach(info -> ConfigScreenHandler.getScreenFactoryFor(info).ifPresent(
				factory -> configScreenFactories.put(info.getModId(), factory)));
		getConfig().onLoad();
		createBadges();
		addBadges();
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
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
				boolean isLibrary = mod.getBadges().contains(ModBadge.LIBRARY);
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
				modsText.append(Component.literal(" ")).append(Component.translatable("mod_menu.loaded.short", count));
			} else {
				String specificKey = "mod_menu.loaded." + count;
				String key = I18n.exists(specificKey) ? specificKey : "mod_menu.loaded";
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

	public static void createBadges() {
		ModBadge.CUSTOM_BADGES.clear();
		Minecraft.getInstance().getResourceManager().listPacks().forEach(packResources ->
				packResources.listResources(PackType.CLIENT_RESOURCES, MOD_ID, "badge", (key, value) -> {
					try {
						JsonObject jsonObject = GsonHelper.parse(new InputStreamReader(value.get()));
						JsonArray fillColor = jsonObject.getAsJsonArray("fill_color");
						JsonArray outlineColor = jsonObject.getAsJsonArray("outline_color");
						String id = key.getPath().replace("badge/", "").replace(".json", "");
						ModBadge badge = new ModBadge(jsonObject.get("name").getAsString(),
								new Color(outlineColor.get(0).getAsInt(), outlineColor.get(1).getAsInt(), outlineColor.get(2).getAsInt()).getRGB(),
								new Color(fillColor.get(0).getAsInt(), fillColor.get(1).getAsInt(), fillColor.get(2).getAsInt()).getRGB());

						ModBadge.CUSTOM_BADGES.put(id, badge);
					} catch (Exception e) {
						LOGGER.warn("incorrect badge json from {} {}", key, e.getMessage());
					}}));
	}

	public static void addBadges() {
		Set<Mod> allMods = new HashSet<>();
		allMods.addAll(ROOT_MODS.values());
		allMods.addAll(MODS.values());
		allMods.forEach(Mod::reCalculateBadge);
	}
}
