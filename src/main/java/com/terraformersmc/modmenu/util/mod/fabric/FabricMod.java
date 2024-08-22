package com.terraformersmc.modmenu.util.mod.fabric;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.OptionalUtil;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.*;
import net.fabricmc.loader.impl.FMLModMetadata;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FabricMod implements Mod {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | FabricMod");

	protected final ModContainer container;
	protected final ModMetadata metadata;

	protected final ModMenuData modMenuData;

	protected final Set<Badge> badges;

	protected final Map<String, String> links = new HashMap<>();

	protected boolean defaultIconWarning = true;

	protected boolean allowsUpdateChecks = true;

	protected boolean childHasUpdate = false;

	public FabricMod(String modId, Set<String> modpackMods) {
		this.container = FabricLoader.getInstance().getModContainer(modId).get();
		this.metadata = container.getMetadata();

		String id = metadata.getId();

		if ("minecraft".equals(id) || "java".equals(id)) {
			allowsUpdateChecks = false;
		}

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
			links.putAll(CustomValueUtil.getStringMap("links", modMenuObject).orElse(new HashMap<>()));
			allowsUpdateChecks = CustomValueUtil.getBoolean("update_checker", modMenuObject).orElse(true);
		}
		this.modMenuData = new ModMenuData(badgeNames, parentId, parentData, id);

		/* Hardcode parents and badges for Fabric API & Fabric Loader */
		if (id.startsWith("fabric") && metadata.containsCustomValue("fabric-api:module-lifecycle")) {
			if (FabricLoader.getInstance().isModLoaded("fabric-api") || !FabricLoader.getInstance()
				.isModLoaded("fabric")) {
				modMenuData.fillParentIfEmpty("fabric-api");
			} else {
				modMenuData.fillParentIfEmpty("fabric");
			}
			modMenuData.getBadges().add(Badge.LIBRARY);
		}
		if (id.startsWith("fabric") && (id.equals("fabricloader") || metadata.getProvides()
			.contains("fabricloader") || id.equals("fabric") || id.equals("fabric-api") || metadata.getProvides()
			.contains("fabric") || metadata.getProvides()
			.contains("fabric-api") || id.equals("fabric-language-kotlin"))) {
			modMenuData.getBadges().add(Badge.LIBRARY);
		}

		/* Add additional badges */
		this.badges = modMenuData.getBadges();
		if (this.metadata.getEnvironment() == ModEnvironment.CLIENT) {
			badges.add(Badge.CLIENT);
		}
		if (OptionalUtil.isPresentAndTrue(CustomValueUtil.getBoolean(
			"fabric-loom:generated",
			metadata
		)) || "java".equals(id)) {
			badges.add(Badge.LIBRARY);
		}
		if ("deprecated".equals(CustomValueUtil.getString("fabric-api:module-lifecycle", metadata).orElse(null))) {
			badges.add(Badge.DEPRECATED);
		}
		if (metadata.containsCustomValue("patchwork:patcherMeta")) {
			badges.add(Badge.PATCHWORK_FORGE);
		}
		if (modpackMods.contains(getId()) && !"builtin".equals(this.metadata.getType())) {
			badges.add(Badge.MODPACK);
		}
		if ("minecraft".equals(getId())) {
			badges.add(Badge.MINECRAFT);
		}
	}

	public @NotNull ModContainer getContainer() {
		return container;
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
	public @NotNull DynamicTexture getIcon(NeoforgeIconHandler iconHandler, int i) {
		String iconSourceId = getId();
		String iconPath = metadata.getIconPath(i).orElse("assets/" + getId() + "/icon.png");
		if ("minecraft".equals(getId())) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/minecraft_icon.png";
		} else if ("java".equals(getId())) {
			iconSourceId = ModMenu.MOD_ID;
			iconPath = "assets/" + ModMenu.MOD_ID + "/java_icon.png";
		}
		final String finalIconSourceId = iconSourceId;
		net.neoforged.fml.ModContainer iconSource = ModList.get()
				.getModContainerById(iconSourceId)
			.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + finalIconSourceId));
		DynamicTexture icon = iconHandler.createIcon(iconSource, iconPath);
		if (icon == null) {
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
		var description = Mod.super.getTranslatedDescription();
		if (getId().equals("java")) {
			description = description + "\n" + I18n.get("modmenu.javaDistributionName", getName());
		}
		return description;
	}

	@Override
	public @NotNull String getVersion() {
		if ("java".equals(getId())) {
			return System.getProperty("java.version");
		}
		return metadata.getVersion().getFriendlyString();
	}

	public @NotNull String getPrefixedVersion() {
		return VersionUtil.getPrefixedVersion(getVersion());
	}

	@Override
	public @NotNull List<String> getAuthors() {
		List<String> authors = metadata.getAuthors().stream().map(Person::getName).collect(Collectors.toList());
		if (authors.isEmpty()) {
			if ("minecraft".equals(getId())) {
				return Lists.newArrayList("Mojang Studios");
			} else if ("java".equals(getId())) {
				return Lists.newArrayList(System.getProperty("java.vendor"));
			}
		}
		return authors;
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
		if ("minecraft".equals(getId())) {
			return "https://www.minecraft.net/";
		} else if ("java".equals(getId())) {
			return System.getProperty("java.vendor.url");
		}
		return metadata.getContact().get("homepage").orElse(null);
	}

	@Override
	public @Nullable String getIssueTracker() {
		if ("minecraft".equals(getId())) {
			return "https://aka.ms/snapshotbugs?ref=game";
		}
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
		if ("minecraft".equals(getId())) {
			return Sets.newHashSet("Minecraft EULA");
		}
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

	public @Nullable String getSha512Hash() throws IOException {
		if (container.getContainingMod().isEmpty() && container.getOrigin().getKind() == ModOrigin.Kind.PATH) {
			List<Path> paths = container.getOrigin().getPaths();
			var fileOptional = paths.stream()
				.filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
				.findFirst();
			if (fileOptional.isPresent()) {
				var file = fileOptional.get().toFile();
				if (file.isFile()) {
					return Files.asByteSource(file).hash(Hashing.sha512()).toString();
				}
			}
		}
		return null;
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
		return ModMenuConfig.hidden_mods.contains(this.getId());
	}

	public static boolean isFabricMod(String modid) {
		return FabricLoader.getInstance().getModContainer(modid).get().getMetadata().getCustomValue("modmenu") != null;
    }
}
