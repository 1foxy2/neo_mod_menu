package com.terraformersmc.mod_menu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.mod_menu.config.ModMenuConfig;
import com.terraformersmc.mod_menu.config.ModMenuConfigScreen;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import com.terraformersmc.mod_menu.util.EnumToLowerCaseJsonConverter;
import com.terraformersmc.mod_menu.util.ModMenuScreenTexts;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.fabric.FabricMod;
import com.terraformersmc.mod_menu.util.mod.java.JavaDummyMod;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeDummyParentMod;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.*;

@net.neoforged.fml.common.Mod(ModMenu.MOD_ID)
public class ModMenu {
	public static final String MOD_ID = "mod_menu";
	public static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu");
	public static final Gson GSON;
	public static final Gson GSON_MINIFIED;
	public static final Pair<ModMenuConfig, ModConfigSpec> CONFIG;

	static {
		GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Enum.class,
				new EnumToLowerCaseJsonConverter()
			)
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
		GSON = builder.setPrettyPrinting().create();
		GSON_MINIFIED = builder.create();

		CONFIG = new ModConfigSpec.Builder()
				.configure(ModMenuConfig::new);
	}

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	public static final Map<String, IConfigScreenFactory> configScreenFactories = new HashMap<>();

	private static int cachedDisplayedModCount = -1;
	public static final boolean HAS_SINYTRA = ModList.get().isLoaded("connector");
	public static final boolean TEXT_PLACEHOLDER_COMPAT = ModList.get().isLoaded("placeholder_api");

	public static Screen getConfigScreen(ModContainer c, Screen menuScreen) {
		configScreenFactories.putIfAbsent("minecraft", (modContainer, screen) -> new OptionsScreen(screen, Minecraft.getInstance().options));

		if (ModMenu.getConfig().HIDDEN_CONFIGS.get().contains(c.getModId()) || "java".equals(c.getModId())) {
			return null;
		}

		IConfigScreenFactory factory = configScreenFactories.get(c.getModId());
		if (factory != null) {
            return factory.createScreen(c, menuScreen);
        }

		Optional<IConfigScreenFactory> factoryOptional = IConfigScreenFactory.getForMod(c.getModInfo());

		factoryOptional.ifPresent(f -> configScreenFactories.put(c.getModId(), f));

        return factoryOptional.map(iConfigScreenFactory -> iConfigScreenFactory.createScreen(c, menuScreen)).orElse(null);
    }

	public ModMenu(IEventBus bus, ModContainer container) {
		bus.addListener(this::onClientSetup);
		NeoForge.EVENT_BUS.addListener(this::onClientTick);

		container.registerConfig(ModConfig.Type.CLIENT, CONFIG.getValue());
		container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) ->
				new ConfigurationScreen(container, screen, ModMenuConfigScreen::new));

		// Fill mods map
		for (ModContainer modContainer : ModList.get().getSortedMods()) {
			Mod mod;

			if (HAS_SINYTRA && ModsScreen.isFabricMod(modContainer.getModInfo().getOwningFile().getFile().getFilePath())) {
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

	public void onClientSetup(FMLClientSetupEvent event) {
		ModList.get().getMods().forEach(info -> IConfigScreenFactory.getForMod(info).ifPresent(
				factory -> configScreenFactories.put(info.getModId(), factory)));
		createBadges();
		addBadges();
	}

	public void onClientTick(ClientTickEvent.Post event) {
	//	LOGGER.warn(String.valueOf(Minecraft.getInstance().screen));

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
		Map<ResourceLocation, IoSupplier<InputStream>> map = new HashMap<>();
		Minecraft.getInstance().getResourceManager().listPacks().forEach(packResources ->
				packResources.listResources(PackType.CLIENT_RESOURCES, MOD_ID, "badge", map::putIfAbsent));
		map.forEach((key, value) -> {
            try {
				LOGGER.warn(GsonHelper.parse(new InputStreamReader(value.get())).get("name").getAsString());
            } catch (IOException e) {
                LOGGER.warn("incorrect badge json from {} {}", key, e.getMessage());
            }
        });
		LOGGER.warn(map.toString());
	}

	public static void addBadges() {
		Set<Mod> allMods = new HashSet<>();
		allMods.addAll(ROOT_MODS.values());
		allMods.addAll(MODS.values());
		allMods.forEach(Mod::reCalculateLibraries);
	}
}
