package com.terraformersmc.mod_menu.util.mod.neoforge;

import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadge;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

public class NeoforgeDummyParentMod implements Mod {
	private final String id;
	private final Mod host;
	private boolean childHasUpdate;
	private final Set<String> badgeNames = new LinkedHashSet<>();
	private final Set<ModBadge> badges = new LinkedHashSet<>();

	public NeoforgeDummyParentMod(Mod host, String id) {
		this.host = host;
		this.id = id;

		NeoforgeMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		if (parentData != null) {
			badgeNames.addAll(parentData.getBadges());
		}
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
	public @NotNull Tuple<DynamicTexture, Dimension> getIcon(NeoforgeIconHandler iconHandler, int i, boolean isSmall) {
		String iconSourceId = host.getId();

		String iconResourceId = id  + (isSmall ? "_small" : "");
		if (NeoforgeIconHandler.modResourceIconCache.containsKey(iconResourceId))
			return NeoforgeIconHandler.modResourceIconCache.get(iconResourceId);

		NeoforgeMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		String iconPath = null;
		if (parentData != null) {
			iconPath = parentData.getIcon().orElse(null);
		}
		if ("inherit".equals(iconPath)) {
			return host.getIcon(iconHandler, i, isSmall);
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
	public @NotNull Set<ModBadge> getBadges() {
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
	public ModMenuData getModMenuData() {
		return host.getModMenuData();
	}

	@Override
	public Optional<ModContainer> getContainer() {
		return Optional.empty();
	}

	@Override
	public void reCalculateBadge() {
		if (!ModMenu.getConfig().mod_badges.containsKey(getId())) {
			if (id.equals("fabric-api")) {
				badgeNames.add("library");
			}
			ModMenu.getConfig().mod_badges.put(getId(), badgeNames);
		}
		Set<String> badgelist = ModMenu.getConfig().mod_badges.get(this.getId());

		this.badges.addAll(ModBadge.convert(badgelist, this.getId()));

	}
}
