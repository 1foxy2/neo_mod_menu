package com.terraformersmc.modmenu.util.mod.java;

import com.google.common.collect.Lists;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.ModMenuDisplayInfo;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public @NotNull String getIconPath(boolean isSmall) {
		return ModMenu.NAMESPACE + ":java_icon.png";
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
	public @NotNull SortedMap<String, Set<String>> getCredits(ModDisplayInfo displayInfo) {
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
	public @NotNull Map<String, Optional<String>> getLicense() {
		return Collections.emptyMap();
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
	public Optional<ModContainer> getContainer() {
		return Optional.empty();
	}

	@Override
	public ModDisplayInfo getDisplayInfo() {
		return new ModMenuDisplayInfo(this);
	}
}
