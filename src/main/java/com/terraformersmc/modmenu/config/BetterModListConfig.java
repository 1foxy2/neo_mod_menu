package com.terraformersmc.modmenu.config;

import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeDummyParentMod;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class BetterModListConfig {
    public final ModConfigSpec.EnumValue<Sorting> SORTING;
    public final ModConfigSpec.BooleanValue COUNT_LIBRARIES;
    public final ModConfigSpec.BooleanValue COMPACT_LIST;
    public final ModConfigSpec.BooleanValue COUNT_CHILDREN;
    public final ModConfigSpec.EnumValue<TitleMenuButtonStyle> MODS_BUTTON_STYLE;
    public final ModConfigSpec.BooleanValue COUNT_HIDDEN_MODS;
    public final ModConfigSpec.EnumValue<GameMenuButtonStyle> GAME_MENU_BUTTON_STYLE;
    public final ModConfigSpec.EnumValue<ModCountLocation> MOD_COUNT_LOCATION;
    public final ModConfigSpec.BooleanValue HIDE_MOD_LINKS;
    public final ModConfigSpec.BooleanValue SHOW_LIBRARIES;
    public final ModConfigSpec.BooleanValue HIDE_MOD_LICENSE;
    public final ModConfigSpec.BooleanValue HIDE_BADGES;
    public final ModConfigSpec.BooleanValue HIDE_MOD_CREDITS;
    public final ModConfigSpec.BooleanValue EASTER_EGGS;
    public final ModConfigSpec.BooleanValue RANDOM_JAVA_COLORS;
    public final ModConfigSpec.BooleanValue TRANSLATE_NAMES;
    public final ModConfigSpec.BooleanValue TRANSLATE_DESCRIPTIONS;
    //public static final ModConfigSpec.BooleanValue UPDATE_CHECKER;
  //  public static final ModConfigSpec.BooleanValue BUTTON_UPDATE_BADGE;
//    public static final ModConfigSpec.BooleanValue UPDATE_CHANNEL;
    public final ModConfigSpec.BooleanValue QUICK_CONFIGURE;
    public final ModConfigSpec.BooleanValue USE_CATALOGUE_ICON;

    public final ModConfigSpec.BooleanValue MODIFY_TITLE_SCREEN;
    public final ModConfigSpec.BooleanValue MODIFY_GAME_MENU;
    public final ModConfigSpec.BooleanValue HIDE_CONFIG_BUTTONS;
    public final ModConfigSpec.BooleanValue HIDE_BADGE_BUTTONS;
    public final ModConfigSpec.BooleanValue HIDE_SCREEN_TOP;
    public final ModConfigSpec.BooleanValue CONFIG_MODE;
    public final ModConfigSpec.BooleanValue DISABLE_DRAG_AND_DROP;
    public final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_MODS;
    public final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_CONFIGS;
    public final ModConfigSpec.ConfigValue<List<? extends String>> MOD_BADGES;
    public final ModConfigSpec.ConfigValue<List<? extends String>> MOD_PARENTS;
    public final ModConfigSpec.ConfigValue<List<? extends String>> HIDE_BADGE;
  //  public static final ModConfigSpec.BooleanValue DISABLE_UPDATE_CHECKER;

    public final Map<String, Set<String>> mod_badges = new HashMap<>();
    public final Map<String, Set<String>> disabled_mod_badges = new HashMap<>();

    public BetterModListConfig(ModConfigSpec.Builder builder) {
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
        QUICK_CONFIGURE = builder.comment().comment("Shows config button above mod icon on the left")
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
        HIDE_MOD_LINKS = builder.comment("Hides links of the mod")
                .define("hide_mod_links", false);
        HIDE_MOD_LICENSE = builder.comment("Hides mod's license")
                .define("hide_mod_license", false);
        HIDE_BADGES = builder.comment("Hides mod's badges")
                .define("hide_badges", false);
        HIDE_BADGE = builder.comment("Add id of the badge to hide it")
                .defineList("hide_badge", ArrayList::new, String::new, object -> object instanceof String);
        HIDE_MOD_CREDITS = builder.comment("Hides mod's credits")
                .define("hide_mod_credits", false);
        HIDE_CONFIG_BUTTONS = builder.comment("Hides mod's config button")
                .define("hide_config_buttons", false);
        HIDE_BADGE_BUTTONS = builder.comment("hides button which allows changing mod's badge")
                .define("hide_badge_buttons", true);
        HIDE_SCREEN_TOP = builder.comment("Hides search bar and drag and drop text, also moves mod's icon up")
                .define("hide_screen_top", false);
        HIDDEN_MODS = builder.comment("Add modid of the mod to hide it from the modlist")
                .defineList("hidden_mods", ArrayList::new, String::new, object -> object instanceof String);
        HIDDEN_CONFIGS = builder.comment("Add modid of the mod to hide its config")
                .defineList("hidden_configs", ArrayList::new, String::new, object -> object instanceof String);
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
                .defineList("mod_badges", ArrayList::new, String::new, object -> object instanceof String);
        MOD_PARENTS = builder.comment("Make mods apear under another mod in this format \"parenModId=childId1, childId2\"")
                .defineList("mod_parents", ArrayList::new, String::new, object -> object instanceof String);

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
        String hostId = modParents.get(id).getFirst();
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
        @SerializedName(value = "replace", alternate = { "replace_bugs" }) REPLACE,
        @SerializedName(value = "insert", alternate = { "below_bugs"}) INSERT,
        ICON
    }
}
