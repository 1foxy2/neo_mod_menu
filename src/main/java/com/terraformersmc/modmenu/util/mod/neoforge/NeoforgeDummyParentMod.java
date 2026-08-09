package com.terraformersmc.modmenu.util.mod.neoforge;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.ModMenuDisplayInfo;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
		if (id.equals("fabric_api")) {
			badgeNames.add("library");
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
		if (id.equals("fabric_api")) {
			return "Forgified Fabric API";
		}
		return id;
	}

	@Override
	public @Nullable String getIconPath(boolean isSmall) {
		NeoforgeMod.ModMenuData.DummyParentData parentData = host.getModMenuData().getDummyParentData();
		String iconPath = null;
		if (parentData != null) {
			iconPath = parentData.getIcon().orElse(null);
		}

		if ("inherit".equals(iconPath)) {
			return host.getIconPath(isSmall);
		}

		if (id.equals("fabric_api")) {
			iconPath = ModMenu.NAMESPACE + ":fabric.png";
		} else {
			iconPath = ModMenu.NAMESPACE + ":unknown_parent.png";
		}

		return iconPath;
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
	public @NotNull SortedMap<String, Set<String>> getCredits(ModDisplayInfo displayInfo) {
		return new TreeMap<>();
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
	public @NotNull Map<String, Optional<String>> getLicense() {
		return Collections.emptyMap();
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
	public ModMenuData getModMenuData() {
		return host.getModMenuData();
	}

	@Override
	public Optional<ModContainer> getContainer() {
		return Optional.empty();
	}

	@Override
	public ModDisplayInfo getDisplayInfo() {
		return new ModMenuDisplayInfo(this);
	}
}
