package com.terraformersmc.modmenu.util.mod.neoforge;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.UpdateChecker;
import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;

public class NeoforgeDummyParentMod implements Mod {
	private final String id;
	private final NeoforgeMod host;
	private boolean childHasUpdate;

	public NeoforgeDummyParentMod(NeoforgeMod host, String id) {
		this.host = host;
		this.id = id;
	}

	@Override
	public @NotNull String getId() {
		return id;
	}

	@Override
	public @NotNull String getName() {
		NeoforgeMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if (parentData != null) {
			return parentData.getName().orElse("");
		}
		if (id.equals("fabric-api")) {
			return "Fabric API";
		}
		return id;
	}

	@Override
	public @NotNull DynamicTexture getIcon(NeoforgeIconHandler iconHandler, int i) {
		String iconSourceId = host.getId();
		NeoforgeMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		String iconPath = null;
		if (parentData != null) {
			iconPath = parentData.getIcon().orElse(null);
		}
		if ("inherit".equals(iconPath)) {
			return host.getIcon(iconHandler, i);
		}
		if (iconPath == null) {
			iconSourceId = ModMenu.MOD_ID;
			if (id.equals("fabric-api")) {
				iconPath = "assets/" + ModMenu.MOD_ID + "/fabric.png";
			} else {
				iconPath = "assets/" + ModMenu.MOD_ID + "/unknown_parent.png";
			}
		}
		final String finalIconSourceId = iconSourceId;
		ModContainer iconSource = ModList.get()
			.getModContainerById(iconSourceId)
			.orElseThrow(() -> new RuntimeException("Cannot get ModContainer for Fabric mod with id " + finalIconSourceId));
		return Objects.requireNonNull(
			iconHandler.createIcon(iconSource, iconPath),
			"Mod icon for " + getId() + " is null somehow (should be filled with default in this case)"
		);
	}

	@Override
	public @NotNull String getDescription() {
		NeoforgeMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if (parentData != null) {
			return parentData.getDescription().orElse("");
		}
		return "";
	}

	@Override
	public @NotNull String getVersion() {
		return "";
	}

	@Override
	public @NotNull String getPrefixedVersion() {
		return "";
	}

	@Override
	public @NotNull List<String> getAuthors() {
		return new ArrayList<>();
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
	public @NotNull Set<Badge> getBadges() {
		NeoforgeMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if (parentData != null) {
			return parentData.getBadges();
		}
		var badges = new HashSet<Badge>();
		if (id.equals("fabric-api")) {
			badges.add(Badge.LIBRARY);
		}

		boolean modpackChildren = true;
		for (Mod mod : ModMenu.PARENT_MAP.get(this)) {
			if (!mod.getBadges().contains(Badge.MODPACK)) {
				modpackChildren = false;
			}
		}
		if (modpackChildren) {
			badges.add(Badge.MODPACK);
		}

		return badges;
	}

	@Override
	public @Nullable String getWebsite() {
		return null;
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
		return new HashSet<>();
	}

	@Override
	public @NotNull Map<String, String> getLinks() {
		return new HashMap<>();
	}

	@Override
	public boolean isReal() {
		return false;
	}

	@Override
	public boolean allowsUpdateChecks() {
		return false;
	}

	@Override
	public @Nullable UpdateChecker getUpdateChecker() {
		return null;
	}

	@Override
	public void setUpdateChecker(@Nullable UpdateChecker updateChecker) {

	}

	@Override
	public @Nullable UpdateInfo getUpdateInfo() {
		return null;
	}

	@Override
	public void setUpdateInfo(@Nullable UpdateInfo updateInfo) {

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
		return ModMenuConfig.HIDDEN_MODS.getValue().contains(this.getId());
	}
}
