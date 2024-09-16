package com.terraformersmc.mod_menu.util.mod.neoforge;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.util.VersionUtil;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadge;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;

public class NeoforgeMod implements Mod {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeMod");

	protected final ModContainer container;
	protected final IModInfo modInfo;

	protected final ModMenuData modMenuData;

	protected final Set<Badge> badges;
	protected final Set<String> badgeNames = new HashSet<>();

	protected final Map<String, String> links = new HashMap<>();

	protected final List<String> contributors = new ArrayList<>();
	protected final List<String> authors = new ArrayList<>();

	protected boolean defaultIconWarning = true;
	protected boolean childHasUpdate = false;

	protected String sources;
	protected String issueTrackerUrl;
	protected String website;

	public NeoforgeMod(ModContainer modContainer) {
		this.container = modContainer;
		this.modInfo = modContainer.getModInfo();

		String id = modInfo.getModId();

		issueTrackerUrl = modInfo.getConfig().<String>getConfigElement("issueTrackerURL").orElse(null);
		website = modInfo.getConfig().<String>getConfigElement("displayURL").orElse(null);

		/* Load modern mod menu custom value data */
		Optional<String> parentId = Optional.empty();
		ModMenuData.DummyParentData parentData = null;

		Optional<ModFileInfo> ownFile = Optional.ofNullable((ModFileInfo) container.getModInfo().getOwningFile());
		Optional<Map<String, Object>> modMenuValue = ownFile.flatMap(mfi -> mfi.getConfigElement("modproperties", "modmenu"));

		if (modMenuValue.isPresent()) {
			Map<String, Object> modMenuMap = modMenuValue.get();

			Optional<Map<String, Object>> parentValues = ownFile.flatMap(mfi -> mfi.getConfigElement("modproperties", "modmenu_parent"));
			if (parentValues.isPresent() && !parentValues.get().isEmpty()) {
				Set<String> parentBadges = new HashSet<>();

				if (modMenuMap.get("badges") instanceof ArrayList<?> list)
					parentBadges.addAll((List<String>) list);

				try {
					parentId = Optional.of((String) parentValues.get().get("id"));
					parentData = new ModMenuData.DummyParentData(
							parentId.orElseThrow(() -> new RuntimeException("Parent object lacks an id")),
							Optional.of((String) parentValues.get().get("name")),
							Optional.of(parentValues.get().get("description") + "\n" + modInfo.getConfig().getConfigElement("credits").orElse("")),
							Optional.of((String) parentValues.get().get("icon")),
							parentBadges
					);
					if (parentId.orElse("").equals(id)) {
						parentId = Optional.empty();
						parentData = null;
						throw new RuntimeException("Mod declared itself as its own parent");
					}
				} catch (Throwable t) {
					LOGGER.error("Error loading parent data from mod: " + id, t);
				}
			}

			if (modMenuMap.get("badges") instanceof ArrayList<?> list) badgeNames.addAll((List<String>) list);

			if (modMenuMap.get("links") instanceof ArrayList<?> list) list.forEach(string -> {
				String[] strings = string.toString().split("=");
				links.put(strings[0], strings[1]);
			});

			if (modMenuMap.get("contributors") instanceof ArrayList<?> list) contributors.addAll((List<String>) list);

			this.sources = (String) modMenuMap.getOrDefault("sources", "");
		}

		for (String string : modInfo.getConfig().getConfigElement("authors").orElse("").toString().split(", ")) {
			if (string.contains(",")) authors.addAll(Arrays.stream(string.split(",")).toList());

			authors.add(string);
		}

		this.modMenuData = new ModMenuData(parentId, parentData, id);

		/* Hardcode parents and badges for Fabric API & kotlin api */
		if (id.startsWith("fabric")) {
			modMenuData.fillParentIfEmpty("fabric-api");
			modMenuData.getBadges().add(ModBadge.LIBRARY);
		}

		/* Hardcode parents and badges for connector-extras */
		if (id.startsWith("connectorextras") || id.startsWith("modmenu")) {
			if (ModList.get().isLoaded("connectormod")) {
				modMenuData.fillParentIfEmpty("connectormod");
			}

			modMenuData.getBadges().add(ModBadge.LIBRARY);
		}

		/* Add additional badges */
		this.badges = modMenuData.getBadges();
	/*	if (this.modInfo.getEnvironment() == ModEnvironment.CLIENT) { not sure how to check that
			badges.add(Badge.CLIENT);
		}*/
		if ("java".equals(id)) {
			badges.add(ModBadge.LIBRARY);
		}
		if ("minecraft".equals(getId())) {
			badges.add(ModBadge.DEFAULT_BADGES.get("minecraft"));
		}
	}

	public Optional<ModContainer> getContainer() {
		return Optional.ofNullable(container);
	}

	@Override
	public @NotNull String getId() {
		return modInfo.getModId();
	}

	@Override
	public @NotNull String getName() {
		return modInfo.getDisplayName();
	}

	@Override
	public @NotNull Tuple<DynamicTexture, Dimension> getIcon(NeoforgeIconHandler iconHandler, int i, boolean isSmall) {
		String iconSourceId = getId();
		String iconPath = modInfo.getLogoFile().orElse("assets/" + getId() + "/icon.png");

		if (isSmall) iconPath = iconPath.replace(".png", "_small.png");
		if ("minecraft".equals(getId())) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/minecraft_icon.png";
		} else if ("forge".equals(getId()) && isSmall && iconPath.contains("neoforge")) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/neoforge.png";
		}

		final String finalIconSourceId = iconSourceId;
		ModContainer iconSource = ModList.get()
				.getModContainerById(iconSourceId)
			.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Neoforge mod with id " + finalIconSourceId));
		Tuple<DynamicTexture, Dimension> icon = iconHandler.createIcon(iconSource, iconPath);
		if (icon == null && !isSmall) {
			if (defaultIconWarning) {
				LOGGER.warn("Warning! Mod {} has a broken icon, loading default icon", modInfo.getModId());
				defaultIconWarning = false;
			}
			return iconHandler.createIcon(
				ModList.get()
						.getModContainerById(ModMenu.MOD_ID)
					.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Neoforge mod with id " + ModMenu.MOD_ID)),
				"assets/" + ModMenu.MOD_ID + "/unknown_icon.png"
			);
		}
		return icon;
	}

	@Override
	public @NotNull String getDescription() {
		return modInfo.getDescription();
	}

	@Override
	public @NotNull String getTranslatedDescription() {

        return Mod.super.getTranslatedDescription();
	}

	@Override
	public @NotNull String getVersion() {
		return modInfo.getVersion().toString();
	}

	public @NotNull String getPrefixedVersion() {
		return VersionUtil.getPrefixedVersion(getVersion());
	}

	@Override
	public @NotNull List<String> getAuthors() {
		if (authors.isEmpty()) {
			if ("minecraft".equals(getId())) {
				return Lists.newArrayList("Mojang Studios");
			}
		}
		return authors;
	}

	@Override
	public @NotNull Map<String, Collection<String>> getContributors() {
		Map<String, Collection<String>> contributors = new LinkedHashMap<>();

		for (String contributor : this.contributors) {
			contributors.put(contributor, List.of("Contributor"));
		}

		return contributors;
	}

	@Override
	public @NotNull SortedMap<String, Set<String>> getCredits() {
		SortedMap<String, Set<String>> credits = new TreeMap<>();

		var authors = this.getAuthors();
		var contributors = this.getContributors();

		for (var author : authors) {
			contributors.put(author, List.of("Author"));
		}

		for (var contributor : contributors.entrySet()) {
			for (var role : contributor.getValue()) {
				credits.computeIfAbsent(role, key -> new LinkedHashSet<>());
				credits.get(role).add(contributor.getKey());
			}
		}

		return credits;
	}

	@Override
	public @NotNull Set<ModBadge> getBadges() {
		return badges;
	}

	@Override
	public @Nullable String getWebsite() {
		if ("minecraft".equals(getId())) {
			return "https://www.minecraft.net/";
		}

		return website;
	}

	@Override
	public @Nullable String getIssueTracker() {
		if ("minecraft".equals(getId())) {
			return "https://aka.ms/snapshotbugs?ref=game";
		}

		return issueTrackerUrl;
	}

	@Override
	public @Nullable String getSource() {
		return this.sources;
	}

	@Override
	public @Nullable String getParent() {
		return modMenuData.getParent().orElse(null);
	}

	@Override
	public @NotNull Set<String> getLicense() {
		if ("minecraft".equals(getId())) {
			return Sets.newHashSet("Minecraft EULA");
		}
		return Sets.newHashSet(modInfo.getOwningFile().getLicense());
	}

	@Override
	public @NotNull Map<String, String> getLinks() {
		return links;
	}

	@Override
	public boolean isReal() {
		return true;
	}

	public ModMenuData getModMenuData() {
		return modMenuData;
	}

	@Override
	public boolean getChildHasUpdate() {
		return childHasUpdate;
	}

	@Override
	public void setChildHasUpdate() {
		this.childHasUpdate = true;
	}

	@Override
	public boolean isHidden() {
		return ModMenu.getConfig().HIDDEN_MODS.get().contains(this.getId());
	}

	@Override
	public void reCalculateBadge() {
		Set<String> badgelist = ModMenu.getConfig().mod_badges.get(this.getId());
		if (badgelist != null)
			badgeNames.addAll(badgelist);

		this.modMenuData.getBadges().addAll(ModBadge.convert(badgeNames, this.getId()));
	}
}
