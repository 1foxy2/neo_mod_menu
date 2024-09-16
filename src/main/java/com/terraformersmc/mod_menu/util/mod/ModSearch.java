package com.terraformersmc.mod_menu.util.mod;

import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Tuple;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ModSearch {

	public static boolean validSearchQuery(String query) {
		return query != null && !query.isEmpty();
	}

	public static List<Mod> search(ModsScreen screen, String query, List<Mod> candidates) {
		if (!validSearchQuery(query)) {
			return candidates;
		}
		return candidates.stream()
			.map(modContainer -> new Tuple<>(modContainer,
				passesFilters(screen, modContainer, query.toLowerCase(Locale.ROOT))
			))
			.filter(pair -> pair.getB() > 0)
			.sorted((a, b) -> b.getB() - a.getB())
			.map(Tuple::getA)
			.collect(Collectors.toList());
	}

	private static int passesFilters(ModsScreen screen, Mod mod, String query) {
		String modId = mod.getId();
		String modName = mod.getName();
		String modTranslatedName = mod.getTranslatedName();
		String modDescription = mod.getDescription();
		String modSummary = mod.getSummary();

		String library = I18n.get("mod_menu.searchTerms.library");
		String sinytra = I18n.get("mod_menu.searchTerms.sinytra");
		String modpack = I18n.get("mod_menu.searchTerms.modpack");
		String deprecated = I18n.get("mod_menu.searchTerms.deprecated");
		String clientside = I18n.get("mod_menu.searchTerms.clientside");
		String configurable = I18n.get("mod_menu.searchTerms.configurable");
		String hasUpdate = I18n.get("mod_menu.searchTerms.hasUpdate");

		// Libraries are currently hidden, ignore them entirely
		if (mod.isHidden() || !ModMenu.getConfig().SHOW_LIBRARIES.get() && mod.getBadges().contains(ModBadge.LIBRARY)) {
			return 0;
		}

		// Some basic search, could do with something more advanced but this will do for now
		if (modName.toLowerCase(Locale.ROOT).contains(query) // Search default mod name
			|| modTranslatedName.toLowerCase(Locale.ROOT).contains(query) // Search localized mod name
			|| modId.toLowerCase(Locale.ROOT).contains(query) // Search mod ID
		) {
			return query.length() >= 3 ? 2 : 1;
		}

		if (modDescription.toLowerCase(Locale.ROOT).contains(query) // Search default mod description
			|| modSummary.toLowerCase(Locale.ROOT).contains(query) // Search mod summary
			|| authorMatches(mod, query) // Search via author
			|| library.contains(query) && mod.getBadges().contains(ModBadge.LIBRARY) // Search for lib mods
			|| sinytra.contains(query) && mod.getBadges()
			.contains(ModBadge.DEFAULT_BADGES.get("sinytra_fabric")) // Search for sinytra mods
			|| modpack.contains(query) && mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("modpack")) // Search for modpack mods
			|| deprecated.contains(query) && mod.getBadges()
			.contains(ModBadge.DEFAULT_BADGES.get("deprecated")) // Search for deprecated mods
			|| clientside.contains(query) && mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("client")) // Search for clientside mods
			|| configurable.contains(query) && screen.getModHasConfigScreen()
			.getOrDefault(modId, false) // Search for mods that can be configured
			// Search for mods that have updates
		) {
			return 1;
		}

		// Allow parent to pass filter if a child passes
		if (ModMenu.PARENT_MAP.keySet().contains(mod)) {
			for (Mod child : ModMenu.PARENT_MAP.get(mod)) {
				int result = passesFilters(screen, child, query);

				if (result > 0) {
					return result;
				}
			}
		}
		return 0;
	}

	private static boolean authorMatches(Mod mod, String query) {
		return mod.getAuthors()
			.stream()
			.map(s -> s.toLowerCase(Locale.ROOT))
			.anyMatch(s -> s.contains(query.toLowerCase(Locale.ROOT)));
	}

}
