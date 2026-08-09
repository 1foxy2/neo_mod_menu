package com.terraformersmc.modmenu.util.mod.neoforge;

import com.google.common.collect.Lists;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.ModMenuDisplayInfo;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.javafmlmod.AutomaticEventSubscriber;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;
import net.neoforged.neoforgespi.locating.IModFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.util.*;

public class NeoforgeMod implements Mod {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeMod");

	protected final ModContainer container;
	protected final IModInfo modInfo;

	protected final ModMenuData modMenuData;

	protected final Set<ModBadge> badges;
	protected final LinkedHashSet<String> badgeNames = new LinkedHashSet<>();

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

		ModFileInfo modFileInfo = (ModFileInfo) modInfo.getOwningFile();

		issueTrackerUrl = modInfo.getConfig().<String>getConfigElement("issueTrackerURL").orElse(null);
		if (issueTrackerUrl == null)
			issueTrackerUrl = modFileInfo.getConfig().<String>getConfigElement("issueTrackerURL").orElse(null);
		website = modInfo.getConfig().<String>getConfigElement("displayURL").orElse(null);

		/* Load modern mod menu custom value data */
		Optional<String> parentId = Optional.empty();
		ModMenuData.DummyParentData parentData = null;

		Optional<Map<String, Object>> modMenuValue = modFileInfo.getConfigElement("modproperties", "modmenu");

		if (modMenuValue.isPresent()) {
			Map<String, Object> modMenuMap = modMenuValue.get();

			Optional<Map<String, Object>> parentValues = modFileInfo.getConfigElement("modproperties", "modmenu_parent");
			if (parentValues.isPresent() && !parentValues.get().isEmpty()) {
				Set<String> parentBadges = new LinkedHashSet<>();

				if (parentValues.get().get("badges") instanceof ArrayList<?> list)
					parentBadges.addAll((List<String>) list);

				try {
					parentId = Optional.of((String) parentValues.get().get("id"));
					parentData = new ModMenuData.DummyParentData(
							parentId.orElseThrow(() -> new RuntimeException("Parent object lacks an id")),
							Optional.of((String) parentValues.get().get("name")),
							Optional.of((String) parentValues.get().get("description")),
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

			if (modMenuMap.get("badges") instanceof ArrayList<?> list)
				badgeNames.addAll((List<String>) list);

			if (modMenuMap.get("links") instanceof ArrayList<?> list) list.forEach(string -> {
				String[] strings = string.toString().split("=");
				links.put(strings[0], strings[1]);
			});

			if (modMenuMap.get("contributors") instanceof ArrayList<?> list) contributors.addAll((List<String>) list);

			this.sources = (String) modMenuMap.getOrDefault("sources", "");
		}

		for (String string : modInfo.getConfig().getConfigElement("authors").orElse("").toString().split(",\\s")) {
			if (string.isEmpty()) {
				continue;
			}

			authors.add(string);
		}

		this.modMenuData = new ModMenuData(parentId, parentData, id);

		/* Hardcode parents and badges for Fabric API & kotlin api */
		if (id.startsWith("fabric")) {
			if (!id.equals("fabric_api")) {
				modMenuData.fillParentIfEmpty("fabric_api");
			}
			badgeNames.add("library");
		}

		/* Hardcode parents and badges for connector-extras */
		if (id.startsWith("connectorextras") || id.equals("modmenu")) {
			modMenuData.fillParentIfEmpty("connector");

			badgeNames.add("library");
		}

		/* Add additional badges */
		this.badges = modMenuData.getBadges();
		IModFile parent = modFileInfo.getFile().getDiscoveryAttributes().parent();
		if (parent != null && parent.getType() != IModFile.Type.LIBRARY) {
			badgeNames.add("library");
		}
		boolean isClientSide = false;

		for (ModFileScanData.AnnotationData data : modFileInfo.getFile().getScanResult().getAnnotatedBy(net.neoforged.fml.common.Mod.class, ElementType.TYPE).toList()) {
			var dist = AutomaticEventSubscriber.getSides(data.annotationData().get("dist"));
			if (!dist.contains(Dist.DEDICATED_SERVER))
				isClientSide = true;
			else {
				isClientSide = false;
				break;
			}
		}


		if ("minecraft".equals(id))
			badgeNames.add("minecraft");
		else {
			if (ModMenu.HAS_SINYTRA)
					badgeNames.addFirst("sinytra_neoforge");

			if (isClientSide)
				badgeNames.add("client");
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
	public @NotNull String getIconPath(boolean isSmall) {
		if ("minecraft".equals(getId())) {
			return ModMenu.NAMESPACE + ":minecraft_icon.png";
		}

		String firstIcon;
		String secondIcon;
		if (isSmall) {
			firstIcon = "iconFile";
			secondIcon = "bannerFile";
		} else {
			firstIcon = "bannerFile";
			secondIcon = "iconFile";
		}

		return container.getModInfo().getConfig().getConfigElement(firstIcon)
				.or(() -> container.getModInfo().getOwningFile().getConfig().getConfigElement(firstIcon))
				.or(() -> container.getModInfo().getConfig().getConfigElement(secondIcon))
				.or(() -> container.getModInfo().getOwningFile().getConfig().getConfigElement(secondIcon))
				.or(() -> container.getModInfo().getLogoFile())
				.or(() -> container.getModInfo().getOwningFile().getConfig().getConfigElement("logoFile"))
				.orElse(null) instanceof String iconFile ? iconFile : getId() + ":icon.png";
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
	public @NotNull Map<String, Optional<String>> getLicense() {
		if ("minecraft".equals(getId())) {
			return Map.of("Minecraft EULA", Optional.of("https://www.minecraft.net/eula"));
		}
		return Map.of(modInfo.getOwningFile().getLicense(), modInfo.getOwningFile().getConfig().getConfigElement("licenseURL"));
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
	public String getForgeCredits() {
		return modInfo.getConfig().<String>getConfigElement("credits").orElse("");
	}

	@Override
	public ModDisplayInfo getDisplayInfo() {
		return container.getCustomExtension(ModDisplayInfo.class)
				.orElseGet(() -> new ModMenuDisplayInfo(this));
	}
}
