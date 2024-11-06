package com.terraformersmc.modmenu.util.mod.fabric;

import com.google.common.collect.Sets;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sinytra.connector.ConnectorEarlyLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FabricMod implements Mod {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | FabricMod");

	protected final ModContainer container;
	protected final net.neoforged.fml.ModContainer forgeContainer;
	protected final ModMetadata metadata;

	protected final ModMenuData modMenuData;

	protected final Set<ModBadge> badges;
	protected final Set<String> badgeNames = new LinkedHashSet<>();

	protected final Map<String, String> links = new HashMap<>();

	protected boolean defaultIconWarning = true;

	protected boolean allowsUpdateChecks = true;

	protected boolean childHasUpdate = false;

	public FabricMod(String modId) {
		this.container = FabricLoader.getInstance().getModContainer(modId).get();
		this.forgeContainer = ModList.get().getModContainerById(modId).get();
		this.metadata = container.getMetadata();

		String id = metadata.getId();

		/* Load modern mod menu custom value data */
		Optional<String> parentId = Optional.empty();
		ModMenuData.DummyParentData parentData = null;
		CustomValue modMenuValue = metadata.getCustomValue("modmenu");
		if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
			CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();
			CustomValue parentCv = modMenuObject.get("parent");
			if (parentCv != null) {
				if (parentCv.getType() == CustomValue.CvType.STRING) {
					parentId = Optional.of(parentCv.getAsString().replace("-", "_"));
				} else if (parentCv.getType() == CustomValue.CvType.OBJECT) {
					try {
						CustomValue.CvObject parentObj = parentCv.getAsObject();
						parentId = Optional.of(CustomValueUtil.getString("id", parentObj)
								.orElseThrow(() -> new RuntimeException("Parent object lacks an id"))
								.replace("-", "_"));
						parentData = new ModMenuData.DummyParentData(
							parentId.get(),
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
				if (key.startsWith("mod_menu")) key = key.replace("mod_menu", "modmenu");
				links.put(key, vakue);
			});
			allowsUpdateChecks = CustomValueUtil.getBoolean("update_checker", modMenuObject).orElse(true);
		}
		this.modMenuData = new ModMenuData(parentId, parentData, id);

		/* Hardcode parents and badges for Kotlin */
		if (getId().equals("fabric_language_kotlin")) {
			badgeNames.add("library");
		}

		if (getId().startsWith("org_jetbrains_kotlin")) {
			modMenuData.fillParentIfEmpty("fabric_language_kotlin");
			badgeNames.add("library");
		}

		/* Add additional badges */
		this.badges = modMenuData.getBadges();
		if (this.metadata.getEnvironment() == ModEnvironment.CLIENT) {
			badgeNames.add("client");
		}

		if (ConnectorEarlyLoader.isConnectorMod(getId()))
			badgeNames.add("sinytra_fabric");
		else badgeNames.add("sinytra_neoforge");
	}

	public Optional<net.neoforged.fml.ModContainer> getContainer() {
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

		String iconResourceId = iconSourceId  + (isSmall ? "_small" : "");
		if (NeoforgeIconHandler.modResourceIconCache.containsKey(iconResourceId))
			return NeoforgeIconHandler.modResourceIconCache.get(iconResourceId);

		String iconPath = metadata.getIconPath(i).orElse("assets/" + getId() + "/icon.png");

		final String finalIconSourceId = iconSourceId;
		if (isSmall) iconPath = iconPath.replace(".png", "_small.png");
		net.neoforged.fml.ModContainer iconSource = ModList.get()
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
				"assets/" + ModMenu.NAMESPACE + "/unknown_icon.png"
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
	public @NotNull Set<ModBadge> getBadges() {
		return badges;
	}

	@Override
	@NotNull
	public Set<String> getBadgeNames() {
		return badgeNames;
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
}
