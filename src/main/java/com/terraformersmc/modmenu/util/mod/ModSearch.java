package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Tuple;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ModSearch {

	public static boolean validSearchQuery(String query) {
		return query != null && !query.isEmpty();
	}

	public static List<Mod> search(ModsScreen screen, String query, List<Mod> candidates) {
		if (!validSearchQuery(query)) {
			return candidates.stream().filter(child -> !child.isHidden() &&
					(ModMenu.getConfig().SHOW_LIBRARIES.get()
							|| !child.getBadges().contains(ModBadge.LIBRARY))).toList();
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

		boolean hasCustomBadge = false;
		for (Map.Entry<String, ModBadge> badgeEntry : ModBadge.CUSTOM_BADGES.entrySet()) {
			String searchTerms = badgeEntry.getValue().getComponent().getString();

			if (I18n.exists("modmenu.searchTerms." + badgeEntry.getKey()))
				searchTerms = I18n.get("modmenu.searchTerms." + badgeEntry.getKey());

			if (searchTerms.contains(query) && mod.getBadges().contains(badgeEntry.getValue())) {
				hasCustomBadge = true;
				break;
			}
		}

		String library = I18n.get("modmenu.searchTerms.library");
		String sinytra = I18n.get("modmenu.searchTerms.sinytra");
		String modpack = I18n.get("modmenu.searchTerms.modpack");
		String deprecated = I18n.get("modmenu.searchTerms.deprecated");
		String clientside = I18n.get("modmenu.searchTerms.clientside");
		String neoforge = I18n.get("modmenu.searchTerms.neoforge");
		String configurable = I18n.get("modmenu.searchTerms.configurable");

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
			|| sinytra.contains(query) && mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("sinytra_fabric")) // Search for sinytra mods
			|| modpack.contains(query) && mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("modpack")) // Search for modpack mods
			|| deprecated.contains(query) && mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("deprecated")) // Search for deprecated mods
				|| deprecated.contains(query) && mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("deprecated")) // Search for deprecated mods
			|| clientside.contains(query) && mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("client")) // Search for clientside mods
				|| neoforge.contains(query) && mod.getBadges().contains(ModBadge.DEFAULT_BADGES.get("sinytra_neoforge")) // Search for neoforge mods
				|| hasCustomBadge
			|| configurable.contains(query) && screen.getModHasConfigScreen(mod.getContainer())// Search for mods that can be configured
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
