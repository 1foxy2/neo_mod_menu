package com.terraformersmc.modmenu.util.mod.java;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.*;

public class JavaDummyMod implements Mod {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeMod");

	protected final ModMenuData modMenuData;

	private static final String modid = "java";

	protected final Map<String, String> links = new HashMap<>();
	protected final Set<String> badgeNames = new LinkedHashSet<>();

	protected boolean defaultIconWarning = true;

	protected boolean allowsUpdateChecks = true;

	protected boolean childHasUpdate = false;

	public JavaDummyMod() {
		allowsUpdateChecks = false;

		Optional<String> parentId = Optional.empty();
		badgeNames.add("library");

		this.modMenuData = new ModMenuData(parentId, null, modid);
	}


	@Override
	public @NotNull String getId() {
		return modid;
	}

	@Override
	public @NotNull String getName() {
		return System.getProperty("java.vm.name");
	}

	@Override
	public @NotNull Tuple<DynamicTexture, Dimension> getIcon(NeoforgeIconHandler iconHandler, int i, boolean isSmall) {
		String iconSourceId = ModMenu.MOD_ID;

		String iconResourceId = iconSourceId  + (isSmall ? "_small" : "");
		if (NeoforgeIconHandler.modResourceIconCache.containsKey(iconResourceId))
			return NeoforgeIconHandler.modResourceIconCache.get(iconResourceId);

		String iconPath = "assets/" + ModMenu.NAMESPACE + "/java_icon.png";

		final String finalIconSourceId = iconSourceId;
		ModContainer iconSource = ModList.get()
				.getModContainerById(iconSourceId)
			.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Neoforge mod with id " + finalIconSourceId));
		Tuple<DynamicTexture, Dimension> icon = iconHandler.createIcon(iconSource, iconPath);
		if (icon == null) {
			if (defaultIconWarning) {
				LOGGER.warn("Warning! Mod {} has a broken icon, loading default icon", modid);
				defaultIconWarning = false;
			}
			return iconHandler.createIcon(
				ModList.get()
						.getModContainerById(ModMenu.MOD_ID)
					.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Neoforge mod with id " + ModMenu.NAMESPACE)),
				"assets/" + ModMenu.NAMESPACE + "/unknown_icon.png"
			);
		}
		return icon;
	}

	@Override
	public @NotNull String getDescription() {
		return modid;
	}

	@Override
	public @NotNull String getTranslatedDescription() {
		var description = Mod.super.getTranslatedDescription();

		description = description + "\n" + I18n.get("modmenu.javaDistributionName", getName());

		return description;
	}

	@Override
	public @NotNull String getVersion() {
		return System.getProperty("java.runtime.version");
	}

	public @NotNull String getPrefixedVersion() {
		return VersionUtil.getPrefixedVersion(getVersion());
	}

	@Override
	public @NotNull List<String> getAuthors() {
		return Lists.newArrayList(System.getProperty("java.vendor"));
	}

	@Override
	public @NotNull Map<String, Collection<String>> getContributors() {
		return Map.of();
	}

	@Override
	public @NotNull SortedMap<String, Set<String>> getCredits() {
		return new TreeMap<>();
	}

	@Override
	public @NotNull Set<ModBadge> getBadges() {
		return modMenuData.getBadges();
	}

	@Override
	@NotNull
	public Set<String> getBadgeNames() {
		return badgeNames;
	}

	@Override
	public @Nullable String getWebsite() {
		return System.getProperty("java.vendor.url");
	}

	@Override
	public @Nullable String getIssueTracker() {
		return null;
	}

	@Override
	public @Nullable String getSource() {
		return null;
	}

	@Override
	public @Nullable String getParent() {
		return null;
	}

	@Override
	public @NotNull Set<String> getLicense() {
		return Sets.newHashSet();
	}

	@Override
	public @NotNull Map<String, String> getLinks() {
		return Map.of();
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
	public Optional<ModContainer> getContainer() {
		return Optional.empty();
	}
}
