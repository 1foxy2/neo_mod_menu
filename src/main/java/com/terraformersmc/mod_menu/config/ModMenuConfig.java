package com.terraformersmc.mod_menu.config;

import com.google.gson.annotations.SerializedName;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeDummyParentMod;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

public class ModMenuConfig {
    public final ForgeConfigSpec.EnumValue<Sorting> SORTING;
    public final ForgeConfigSpec.BooleanValue COUNT_LIBRARIES;
    public final ForgeConfigSpec.BooleanValue COMPACT_LIST;
    public final ForgeConfigSpec.BooleanValue COUNT_CHILDREN;
    public final ForgeConfigSpec.EnumValue<TitleMenuButtonStyle> MODS_BUTTON_STYLE;
    public final ForgeConfigSpec.BooleanValue COUNT_HIDDEN_MODS;
    public final ForgeConfigSpec.EnumValue<GameMenuButtonStyle> GAME_MENU_BUTTON_STYLE;
    public final ForgeConfigSpec.EnumValue<ModCountLocation> MOD_COUNT_LOCATION;
    public final ForgeConfigSpec.BooleanValue HIDE_MOD_LINKS;
    public final ForgeConfigSpec.BooleanValue SHOW_LIBRARIES;
    public final ForgeConfigSpec.BooleanValue HIDE_MOD_LICENSE;
    public final ForgeConfigSpec.BooleanValue HIDE_BADGES;
    public final ForgeConfigSpec.BooleanValue HIDE_MOD_CREDITS;
    public final ForgeConfigSpec.BooleanValue EASTER_EGGS;
    public final ForgeConfigSpec.BooleanValue RANDOM_JAVA_COLORS;
    public final ForgeConfigSpec.BooleanValue TRANSLATE_NAMES;
    public final ForgeConfigSpec.BooleanValue TRANSLATE_DESCRIPTIONS;
    //public static final ForgeConfigSpec.BooleanValue UPDATE_CHECKER;
  //  public static final ForgeConfigSpec.BooleanValue BUTTON_UPDATE_BADGE;
//    public static final ForgeConfigSpec.BooleanValue UPDATE_CHANNEL;
    public final ForgeConfigSpec.BooleanValue QUICK_CONFIGURE;
    public final ForgeConfigSpec.BooleanValue USE_CATALOGUE_ICON;

    public final ForgeConfigSpec.BooleanValue MODIFY_TITLE_SCREEN;
    public final ForgeConfigSpec.BooleanValue MODIFY_GAME_MENU;
    public final ForgeConfigSpec.BooleanValue HIDE_CONFIG_BUTTONS;
    public final ForgeConfigSpec.BooleanValue HIDE_BADGE_BUTTONS;
    public final ForgeConfigSpec.BooleanValue HIDE_SCREEN_TOP;
    public final ForgeConfigSpec.BooleanValue CONFIG_MODE;
    public final ForgeConfigSpec.BooleanValue DISABLE_DRAG_AND_DROP;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDDEN_MODS;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDDEN_CONFIGS;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> LIBRARY_LIST;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> MOD_BADGES;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> MOD_PARENTS;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> DISABLE_DEFAULT_BADGES;
    public final ForgeConfigSpec.BooleanValue DISABLE_DEFAULT_BADGES_ALL;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDE_BADGE;
  //  public static final ForgeConfigSpec.BooleanValue DISABLE_UPDATE_CHECKER;

    public final Map<String, Set<String>> mod_badges = new HashMap<>();
    public final Map<String, Set<String>> disabled_mod_badges = new HashMap<>();

    public ModMenuConfig(ForgeConfigSpec.Builder builder) {
        builder.push("main");
        SORTING = builder
                .defineEnum("sorting", Sorting.ASCENDING);
        COMPACT_LIST = builder.comment("Makes list more compacted")
                .define("compact_list", false);
        MODS_BUTTON_STYLE = builder
                .defineEnum("mods_button_style", TitleMenuButtonStyle.CLASSIC);
        GAME_MENU_BUTTON_STYLE = builder
                .defineEnum("game_menu_button_style", GameMenuButtonStyle.REPLACE);
        MOD_COUNT_LOCATION = builder
                .defineEnum("mod_count_location", ModCountLocation.TITLE_SCREEN);
        EASTER_EGGS = builder.comment("Shows secret mod count translations defined by modmenu.mods.MOD_COUND.secret")
                .define("easter_eggs", true);
        RANDOM_JAVA_COLORS = builder.comment("Makes java mod have random colors")
                .define("random_java_colors", false);
        TRANSLATE_NAMES = builder.comment("Make mod names translatable defining by modmenu.nameTranslation.modid")
                .define("translate_names", true);
        TRANSLATE_DESCRIPTIONS = builder.comment("Make mod descriptions translatable defining by modmenu.descriptionTranslation.modid")
                .define("translate_descriptions", true);
        QUICK_CONFIGURE = builder.comment("Shows config button above mod icon on the left")
                .define("quick_configure", true);
        MODIFY_TITLE_SCREEN = builder.comment("Modifies title screen, if false will be neoforge default with default mods button")
                .define("modify_title_screen", true);
        MODIFY_GAME_MENU = builder.comment("Changes pause screen's button position and replaces neoforge's mods button with better modlist's one")
                .define("modify_game_menu", true);
        CONFIG_MODE = builder.comment("Will only show mods with config available")
                .define("config_mode", false);
        DISABLE_DRAG_AND_DROP = builder.comment("Disables drag and drop mods adding")
                .define("disable_drag_and_drop", false);
        USE_CATALOGUE_ICON = builder.comment("Will use catalogue's icon if present")
                .define("use_catalogue_icon", true);
        builder.pop();

        builder.push("hide");
        SHOW_LIBRARIES = builder.comment("Shows mods with library badge")
                .define("show_libraries", false);
        HIDE_MOD_LINKS = builder.comment("Makes java mod have random colors")
                .define("hide_mod_links", false);
        HIDE_MOD_LICENSE = builder.comment("Hides mod's license")
                .define("hide_mod_license", false);
        HIDE_BADGES = builder.comment("Hides mod's badges")
                .define("hide_badges", false);
        HIDE_BADGE = builder.comment("Add id of the badge to hide it")
                .defineList("hide_badge", ArrayList::new, object -> object instanceof String);
        HIDE_MOD_CREDITS = builder.comment("Hides mod's credits")
                .define("hide_mod_credits", false);
        HIDE_CONFIG_BUTTONS = builder.comment("Hides mod's config button")
                .define("hide_config_buttons", false);
        HIDE_BADGE_BUTTONS = builder.comment("hides button which allows changing mod's badge")
                .define("hide_badge_buttons", true);
        HIDE_SCREEN_TOP = builder.comment("Hides search bar and drag and drop text, also moves mod's icon up")
                .define("hide_screen_top", false);
        HIDDEN_MODS = builder.comment("Add modid of the mod to hide it from the modlist")
                .defineList("hidden_mods", ArrayList::new, object -> object instanceof String);
        HIDDEN_CONFIGS = builder.comment("Add modid of the mod to hide its config")
                .defineList("hidden_configs", ArrayList::new, object -> object instanceof String);
        LIBRARY_LIST = builder.comment("deprecated")
                .defineList("library_list", ArrayList::new, object -> object instanceof String);
        builder.push("disable_default_badges_section");
        DISABLE_DEFAULT_BADGES_ALL = builder.comment("deprecated")
                .define("disable_default_badges_all", false);
        DISABLE_DEFAULT_BADGES = builder.comment("deprecated")
                .defineList("disable_default_badges", ArrayList::new, object -> object instanceof String);
        builder.pop();
        builder.pop();

        builder.push("count");
        COUNT_HIDDEN_MODS = builder.comment("Makes hidden mods count added to the total mods count")
                .define("count_hidden_mods", true);
        COUNT_CHILDREN = builder.comment("Makes childrens count get added to the total mods count")
                .define("count_children", true);
        COUNT_LIBRARIES = builder.comment("Makes libraries count get added to the total mods count")
                .define("count_libraries", true);
        builder.pop();

        MOD_BADGES = builder.comment("Adds badge to mod in this format \"modid=badge1, badge2\"")
                .defineList("mod_badges", ArrayList::new, object -> object instanceof String);
        MOD_PARENTS = builder.comment("Make mods apear under another mod in this format \"parenModId=childId1, childId2\"")
                .defineList("mod_parents", ArrayList::new, object -> object instanceof String);

        //    UPDATE_CHECKER = builder
        //            .define("translate_descriptions", true);
        //    BUTTON_UPDATE_BADGE = builder
        //            .define("button_update_badge", true);
        //  UPDATE_CHANNEL
    }

    public void onLoad() {
        this.MOD_BADGES.get().forEach(badge -> {
            String[] badgeKeyValue = badge.split("=");
            if (badgeKeyValue.length != 1) {
                Set<String> badges = new LinkedHashSet<>();
                Set<String> disabledBadges = new LinkedHashSet<>();
                Arrays.stream(badgeKeyValue[1].split(", ")).toList().forEach(badgeId -> {
                    if (badgeId.startsWith("!"))
                        disabledBadges.add(badgeId.substring(1));
                    else
                        badges.add(badgeId);
                });
                this.mod_badges.put(badgeKeyValue[0], badges);
                this.disabled_mod_badges.put(badgeKeyValue[0], disabledBadges);
            }
        });
        if (!this.LIBRARY_LIST.get().isEmpty()) {
            this.LIBRARY_LIST.get().forEach(string -> {
                this.mod_badges.putIfAbsent(string, new LinkedHashSet<>());
                this.mod_badges.get(string).add("library");
            });
            this.LIBRARY_LIST.set(new ArrayList<>());
        }
        Map<String, Mod> dummyParents = new HashMap<>();

        // Initialize parent map
        HashSet<String> modParentSet = new HashSet<>();
        Map<String, List<String>> modParents = new HashMap<>();
        this.MOD_PARENTS.get().forEach(parentToMods -> {
            if (parentToMods.isEmpty())
                return;

            String[] parentToMod = parentToMods.split("=");
            modParents.put(parentToMod[0], Arrays.stream(parentToMod[1].split(", ")).toList());
        });
        modParents.forEach((parentString, children) -> {
            for (String id : children) {
                Mod mod = ModMenu.MODS.getOrDefault(id, dummyParents.get(id));

                if (mod == null) {
                    Mod fakeModHost = getModHost(modParents, dummyParents, id);
                    if (fakeModHost == null) {
                        continue;
                    }
                    mod = new NeoforgeDummyParentMod(fakeModHost, id);
                    dummyParents.put(id, mod);
                }

                String parentId = parentString;

            Mod parent;
            modParentSet.clear();
            while (true) {
                parent = ModMenu.MODS.getOrDefault(parentId, dummyParents.get(parentId));
                if (parent == null) {
                    parent = new NeoforgeDummyParentMod(mod, parentId);
                    dummyParents.put(parentId, parent);
                }

                parentId = parent != null ? parent.getParent() : null;
                if (parentId == null) {
                    // It will most likely end here in the first iteration
                    break;
                }

                if (modParentSet.contains(parentId)) {
                    ModMenu.LOGGER.warn("Mods contain each other as parents: {}", modParentSet);
                    parent = null;
                    break;
                }
                modParentSet.add(parentId);
            }

            if (parent == null) {
                continue;
            }
            ModMenu.ROOT_MODS.remove(mod.getId(), mod);
            ModMenu.PARENT_MAP.put(parent, mod);
        }
        });
        ModMenu.MODS.putAll(dummyParents);
    }

    private Mod getModHost(Map<String, List<String>> modParents, Map<String, Mod> dummyParents, String id) {
        if (!modParents.containsKey(id)) {
            return null;
        }
        String hostId = modParents.get(id).get(0);
        if (hostId == null) {
            return null;
        }
        Mod host = ModMenu.MODS.get(hostId);
        if (host == null) {
            host = getModHost(modParents, dummyParents, hostId);
            if (host == null) {
                return null;
            }

            host = new NeoforgeDummyParentMod(host, hostId);
            dummyParents.put(id, host);
        }

        return host;
    }

    public void save() {
        List<String> list = new ArrayList<>();
        this.mod_badges.forEach((key, values) -> {
            Set<String> disabledBadges = disabled_mod_badges.get(key);
            if (values.isEmpty() && disabledBadges == null)
                return;

            StringBuilder string = new StringBuilder();
            for (String value : values) {
                if (!string.isEmpty())
                    string.append(", ");

                string.append(value);
            }

            if (disabledBadges != null)
                for (String value : disabledBadges) {
                    if (!string.isEmpty())
                        string.append(", ");

                    string.append("!").append(value);
                }

            if (!string.isEmpty())
                list.add(key + "=" + string);
        });

        this.MOD_BADGES.set(list);
        ModMenu.CONFIG.getRight().save();
    }

    public enum Sorting {
        ASCENDING(Comparator.comparing(mod -> mod.getTranslatedName()
                .toLowerCase(Locale.ROOT))), DESCENDING(ASCENDING.getComparator().reversed());

        private final Comparator<Mod> comparator;

        Sorting(Comparator<Mod> comparator) {
            this.comparator = comparator;
        }

        public Comparator<Mod> getComparator() {
            return comparator;
        }

        public void cycleValue() {
            ModMenu.getConfig().SORTING.set(values()[ordinal() + 1 == values().length ? 0 : ordinal() + 1]);
        }
    }

    public enum ModCountLocation {
        TITLE_SCREEN(true, false), MODS_BUTTON(false, true), TITLE_SCREEN_AND_MODS_BUTTON(true, true), NONE(false,
                false
        );

        private final boolean titleScreen, modsButton;

        ModCountLocation(boolean titleScreen, boolean modsButton) {
            this.titleScreen = titleScreen;
            this.modsButton = modsButton;
        }

        public boolean isOnTitleScreen() {
            return titleScreen;
        }

        public boolean isOnModsButton() {
            return modsButton;
        }
    }

    public enum TitleMenuButtonStyle {
        CLASSIC(), REPLACE_REALMS(), SHRINK(), SHRINK_LEFT(), ICON()
    }

    public enum GameMenuButtonStyle {
        @SerializedName(value = "replace", alternate = { "replace_bugs" }) REPLACE, @SerializedName(value = "insert", alternate = { "below_bugs" }) INSERT, ICON
    }
}
