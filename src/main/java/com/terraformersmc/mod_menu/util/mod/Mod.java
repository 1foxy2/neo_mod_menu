package com.terraformersmc.mod_menu.util.mod;

import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.TextPlaceholderApiCompat;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeIconHandler;
import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

public interface Mod {

	@NotNull String getId();

	@NotNull String getName();

	@NotNull
	default String getTranslatedName() {
		String translationKey = "mod_menu.nameTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModMenu.getConfig().TRANSLATE_NAMES.get()) && I18n.exists(
			translationKey)) {
			return I18n.get(translationKey);
		}
		return getName();
	}

	@NotNull Tuple<DynamicTexture, Dimension> getIcon(NeoforgeIconHandler iconHandler, int i, boolean isSmall);

	@NotNull
	default String getSummary() {
		String string = getTranslatedSummary();
		return ModMenu.TEXT_PLACEHOLDER_COMPAT ?
			TextPlaceholderApiCompat.PARSER.parseText(string, ParserContext.of()).getString() :
			string;
	}

	@NotNull
	default String getTranslatedSummary() {
		String translationKey = "mod_menu.summaryTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModMenu.getConfig().TRANSLATE_DESCRIPTIONS.get()) && I18n.exists(
			translationKey)) {
			return I18n.get(translationKey);
		}
		return getTranslatedDescription();
	}

	@NotNull String getDescription();

	@NotNull
	default String getTranslatedDescription() {
		String translatableDescriptionKey = "mod_menu.descriptionTranslation." + getId();
		if ((getId().equals("minecraft") || getId().equals("java") || ModMenu.getConfig().TRANSLATE_DESCRIPTIONS.get()) && I18n.exists(
			translatableDescriptionKey)) {
			return I18n.get(translatableDescriptionKey);
		}
		return getDescription();
	}

	default Component getFormattedDescription() {
		String string = getTranslatedDescription();
		return ModMenu.TEXT_PLACEHOLDER_COMPAT ?
			TextPlaceholderApiCompat.PARSER.parseText(string, ParserContext.of()) :
				Component.literal(string);
	}

	@NotNull String getVersion();

	@NotNull String getPrefixedVersion();

	@NotNull List<String> getAuthors();

	/**
	 * @return a mapping of contributors to their roles.
	 */
	@NotNull Map<String, Collection<String>> getContributors();

	/**
	 * @return a mapping of roles to each contributor with that role.
	 */
	@NotNull SortedMap<String, Set<String>> getCredits();

	@NotNull Set<ModBadge> getBadges();

	@Nullable String getWebsite();

	@Nullable String getIssueTracker();

	@Nullable String getSource();

	@Nullable String getParent();

	@NotNull Set<String> getLicense();

	@NotNull Map<String, String> getLinks();

	boolean isReal();

	void setChildHasUpdate();

	boolean getChildHasUpdate();

	boolean isHidden();

	ModMenuData getModMenuData();

	Optional<ModContainer> getContainer();

	void reCalculateBadge();

	static class ModMenuData {
		private final Set<ModBadge> badges;
		private Optional<String> parent;
		private @Nullable
		final DummyParentData dummyParentData;

		//TODO: remove badges set from constructor
		public ModMenuData(Set<String> badges, Optional<String> parent, DummyParentData dummyParentData, String id) {
			this.badges = ModBadge.convert(badges, id);
			this.parent = parent;
			this.dummyParentData = dummyParentData;
		}

		public Set<ModBadge> getBadges() {
			return badges;
		}

		public Optional<String> getParent() {
			return parent;
		}

		public @Nullable DummyParentData getDummyParentData() {
			return dummyParentData;
		}

		public void addLibraryBadge(boolean add) {
			if (add) {
				badges.add(ModBadge.LIBRARY);
			}
		}

		public void fillParentIfEmpty(String parent) {
			if (!this.parent.isPresent()) {
				this.parent = Optional.of(parent);
			}
		}

		public static class DummyParentData {
			private final String id;
			private final Optional<String> name;
			private final Optional<String> description;
			private final Optional<String> icon;
			private final Set<ModBadge> badges;

			public DummyParentData(
					String id,
					Optional<String> name,
					Optional<String> description,
					Optional<String> icon,
					Set<String> badges
			) {
				this.id = id;
				this.name = name;
				this.description = description;
				this.icon = icon;
				this.badges = ModBadge.convert(badges, id);
			}

			public String getId() {
				return id;
			}

			public Optional<String> getName() {
				return name;
			}

			public Optional<String> getDescription() {
				return description;
			}

			public Optional<String> getIcon() {
				return icon;
			}

			public Set<ModBadge> getBadges() {
				return badges;
			}
		}
	}
}
