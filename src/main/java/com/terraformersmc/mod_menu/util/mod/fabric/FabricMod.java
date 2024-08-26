package com.terraformersmc.mod_menu.util.mod.fabric;

import com.google.common.collect.Sets;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.util.VersionUtil;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeIconHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FabricMod implements Mod {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | FabricMod");

	protected final ModContainer container;
	protected final net.minecraftforge.fml.ModContainer forgeContainer;
	protected final ModMetadata metadata;

	protected final ModMenuData modMenuData;

	protected final Set<Badge> badges;

	protected final Map<String, String> links = new HashMap<>();

	protected boolean defaultIconWarning = true;

	protected boolean allowsUpdateChecks = true;

	protected boolean childHasUpdate = false;

	protected boolean wasInLibraries = false;

	public FabricMod(String modId) {
		this.container = FabricLoader.getInstance().getModContainer(modId).get();
		this.forgeContainer = ModList.get().getModContainerById(modId).get();
		this.metadata = container.getMetadata();

		String id = metadata.getId();

		/* Load modern mod menu custom value data */
		Optional<String> parentId = Optional.empty();
		ModMenuData.DummyParentData parentData = null;
		Set<String> badgeNames = new HashSet<>();
		CustomValue modMenuValue = metadata.getCustomValue("modmenu");
		if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
			CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();
			CustomValue parentCv = modMenuObject.get("parent");
			if (parentCv != null) {
				if (parentCv.getType() == CustomValue.CvType.STRING) {
					parentId = Optional.of(parentCv.getAsString());
				} else if (parentCv.getType() == CustomValue.CvType.OBJECT) {
					try {
						CustomValue.CvObject parentObj = parentCv.getAsObject();
						parentId = CustomValueUtil.getString("id", parentObj);
						parentData = new ModMenuData.DummyParentData(
							parentId.orElseThrow(() -> new RuntimeException("Parent object lacks an id")),
							CustomValueUtil.getString("name", parentObj),
							CustomValueUtil.getString("description", parentObj),
							CustomValueUtil.getString("icon", parentObj),
							CustomValueUtil.getStringSet("badges", parentObj).orElse(new HashSet<>())
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
			}
			badgeNames.addAll(CustomValueUtil.getStringSet("badges", modMenuObject).orElse(new HashSet<>()));
			CustomValueUtil.getStringMap("links", modMenuObject).orElse(new HashMap<>()).forEach((key, vakue) -> {
				if (key.startsWith("modmenu")) key = key.replace("modmenu", "mod_menu");
				links.put(key, vakue);
			});
			allowsUpdateChecks = CustomValueUtil.getBoolean("update_checker", modMenuObject).orElse(true);
		}
		this.modMenuData = new ModMenuData(badgeNames, parentId, parentData, id);

		/* Hardcode parents and badges for Fabric API & Fabric Loader */
		if (id.startsWith("fabric") && (id.equals("fabricloader") || metadata.getProvides()
			.contains("fabricloader") || id.equals("fabric") || id.equals("fabric_api") || metadata.getProvides()
			.contains("fabric") || metadata.getProvides()
			.contains("fabric_api") || id.equals("fabric_language_kotlin"))) {
			modMenuData.getBadges().add(Badge.LIBRARY);
		}

		/* Hardcode parents and badges for Kotlin */
		if (id.startsWith("org_jetbrains_kotlin")) {
			modMenuData.fillParentIfEmpty("fabric_language_kotlin");
			modMenuData.getBadges().add(Badge.LIBRARY);
		}

		/* Add additional badges */
		this.badges = modMenuData.getBadges();
		if (this.metadata.getEnvironment() == ModEnvironment.CLIENT) {
			badges.add(Badge.CLIENT);
		}

		badges.add(Badge.SINYTRA_FABRIC);
	}

	public Optional<net.minecraftforge.fml.ModContainer> getContainer() {
		return Optional.of(forgeContainer);
	}

	@Override
	public @NotNull String getId() {
		return metadata.getId();
	}

	@Override
	public @NotNull String getName() {
		return metadata.getName();
	}

	@Override
	public @NotNull Tuple<DynamicTexture, Dimension> getIcon(NeoforgeIconHandler iconHandler, int i, boolean isSmall) {
		String iconSourceId = getId();
		String iconPath = metadata.getIconPath(i).orElse("assets/" + getId() + "/icon.png");

		final String finalIconSourceId = iconSourceId;
		if (isSmall) iconPath = iconPath.replace(".png", "_small.png");
		net.minecraftforge.fml.ModContainer iconSource = ModList.get()
				.getModContainerById(iconSourceId)
			.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + finalIconSourceId));
		Tuple<DynamicTexture, Dimension> icon = iconHandler.createIcon(iconSource, iconPath);
		if (icon == null && !isSmall) {
			if (defaultIconWarning) {
				LOGGER.warn("Warning! Mod {} has a broken icon, loading default icon", metadata.getId());
				defaultIconWarning = false;
			}
			return iconHandler.createIcon(
				ModList.get()
						.getModContainerById(ModMenu.MOD_ID)
					.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + ModMenu.MOD_ID)),
				"assets/" + ModMenu.MOD_ID + "/unknown_icon.png"
			);
		}
		return icon;
	}

	@Override
	public @NotNull String getDescription() {
		return metadata.getDescription();
	}

	@Override
	public @NotNull String getTranslatedDescription() {
        return Mod.super.getTranslatedDescription();
	}

	@Override
	public @NotNull String getVersion() {
		return metadata.getVersion().getFriendlyString();
	}

	public @NotNull String getPrefixedVersion() {
		return VersionUtil.getPrefixedVersion(getVersion());
	}

	@Override
	public @NotNull List<String> getAuthors() {

        return metadata.getAuthors().stream().map(Person::getName).collect(Collectors.toList());
	}

	@Override
	public @NotNull Map<String, Collection<String>> getContributors() {
		Map<String, Collection<String>> contributors = new LinkedHashMap<>();

		for (var contributor : this.metadata.getContributors()) {
			contributors.put(contributor.getName(), List.of("Contributor"));
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
	public @NotNull Set<Badge> getBadges() {
		return badges;
	}

	@Override
	public @Nullable String getWebsite() {
		return metadata.getContact().get("homepage").orElse(null);
	}

	@Override
	public @Nullable String getIssueTracker() {
		return metadata.getContact().get("issues").orElse(null);
	}

	@Override
	public @Nullable String getSource() {
		return metadata.getContact().get("sources").orElse(null);
	}

	@Override
	public @Nullable String getParent() {
		return modMenuData.getParent().orElse(null);
	}

	@Override
	public @NotNull Set<String> getLicense() {
		return Sets.newHashSet(metadata.getLicense());
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
	public void reCalculateLibraries() {
		boolean isInLibraries = ModMenu.getConfig().LIBRARY_LIST.get().contains(getId());
		if (!getModMenuData().getBadges().contains(Badge.LIBRARY) && isInLibraries && !wasInLibraries) {
			this.modMenuData.addLibraryBadge(true);
			wasInLibraries = true;
		} else if (!isInLibraries && wasInLibraries) {
			this.modMenuData.getBadges().remove(Badge.LIBRARY);
			wasInLibraries = false;
		}
	}
}
