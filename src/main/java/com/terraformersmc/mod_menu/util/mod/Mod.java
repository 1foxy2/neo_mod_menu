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
import java.util.stream.Collectors;

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

	@NotNull Set<Badge> getBadges();

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

	void reCalculateLibraries();

	enum Badge {
		LIBRARY(
				"mod_menu.badge.library",
				0xff107454,
				0xff093929,
				"library"),
		CLIENT(
				"mod_menu.badge.clientsideOnly",
			0xff2b4b7c,
			0xff0e2a55,
			"client"
		),

		DEPRECATED(
				"mod_menu.badge.deprecated",
				0xff841426,
				0xff530C17,
				"deprecated"
		),

		PATCHWORK_FORGE(
			"mod_menu.badge.forge",
			0xff1f2d42,
			0xff101721,
			null
		),

		MODPACK(
				"mod_menu.badge.modpack",
				0xff7a2b7c,
				0xff510d54,
				null
		),


		MINECRAFT("mod_menu.badge.minecraft",
			0xff6f6c6a,
			0xff31302f,
			null
		);

		private final Component component;
		private final int outlineColor, fillColor;
		private final String key;
		private static final Map<String, Badge> KEY_MAP = new HashMap<>();

		Badge(String translationKey, int outlineColor, int fillColor, String key) {
			this.component = Component.translatable(translationKey);
			this.outlineColor = outlineColor;
			this.fillColor = fillColor;
			this.key = key;
		}

		public Component getComponent() {
			return this.component;
		}

		public int getOutlineColor() {
			return this.outlineColor;
		}

		public int getFillColor() {
			return this.fillColor;
		}

		public static Set<Badge> convert(Set<String> badgeKeys, String modId) {
			return badgeKeys.stream().map(key -> {
				if (!KEY_MAP.containsKey(key)) {
					ModMenu.LOGGER.warn("Skipping unknown badge key '{}' specified by mod '{}'", key, modId);
				}

				return KEY_MAP.get(key);
			}).filter(Objects::nonNull).collect(Collectors.toSet());
		}

		static {
			Arrays.stream(values()).forEach(badge -> KEY_MAP.put(badge.key, badge));
		}
	}

	static class ModMenuData {
		private final Set<Badge> badges;
		private Optional<String> parent;
		private @Nullable
		final DummyParentData dummyParentData;

		public ModMenuData(Set<String> badges, Optional<String> parent, DummyParentData dummyParentData, String id) {
			this.badges = Badge.convert(badges, id);
			this.parent = parent;
			this.dummyParentData = dummyParentData;
		}

		public Set<Badge> getBadges() {
			return badges;
		}

		public Optional<String> getParent() {
			return parent;
		}

		public @Nullable DummyParentData getDummyParentData() {
			return dummyParentData;
		}

		public void addClientBadge(boolean add) {
			if (add) {
				badges.add(Badge.CLIENT);
			}
		}

		public void addLibraryBadge(boolean add) {
			if (add) {
				badges.add(Badge.LIBRARY);
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
			private final Set<Badge> badges;

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
				this.badges = Badge.convert(badges, id);
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

			public Set<Badge> getBadges() {
				return badges;
			}
		}
	}
}
