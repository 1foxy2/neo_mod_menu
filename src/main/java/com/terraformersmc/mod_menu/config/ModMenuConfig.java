package com.terraformersmc.mod_menu.config;

import com.google.gson.annotations.SerializedName;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeDummyParentMod;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public class ModMenuConfig {
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

    public final ModConfigSpec.BooleanValue MODIFY_TITLE_SCREEN;
    public final ModConfigSpec.BooleanValue MODIFY_GAME_MENU;
    public final ModConfigSpec.BooleanValue HIDE_CONFIG_BUTTONS;
    public final ModConfigSpec.BooleanValue HIDE_BADGE_BUTTONS;
    public final ModConfigSpec.BooleanValue CONFIG_MODE;
    public final ModConfigSpec.BooleanValue DISABLE_DRAG_AND_DROP;
    public final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_MODS;
    public final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_CONFIGS;
    public final ModConfigSpec.ConfigValue<List<? extends String>> LIBRARY_LIST;
    public final ModConfigSpec.ConfigValue<List<? extends String>> MOD_BADGES;
    public final ModConfigSpec.ConfigValue<List<? extends String>> MOD_PARENTS;
  //  public static final ModConfigSpec.BooleanValue DISABLE_UPDATE_CHECKER;

    public final Map<String, Set<String>> mod_badges = new HashMap<>();

    public ModMenuConfig(ModConfigSpec.Builder builder) {
        builder.push("main");
        SORTING = builder
                .defineEnum("sorting", Sorting.ASCENDING);
        COMPACT_LIST = builder
                .define("compact_list", false);
        MODS_BUTTON_STYLE = builder
                .defineEnum("mods_button_style", TitleMenuButtonStyle.CLASSIC);
        GAME_MENU_BUTTON_STYLE = builder
                .defineEnum("game_menu_button_style", GameMenuButtonStyle.REPLACE);
        MOD_COUNT_LOCATION = builder
                .defineEnum("mod_count_location", ModCountLocation.TITLE_SCREEN);
        EASTER_EGGS = builder
                .define("easter_eggs", true);
        RANDOM_JAVA_COLORS = builder
                .define("random_java_colors", false);
        TRANSLATE_NAMES = builder
                .define("translate_names", true);
        TRANSLATE_DESCRIPTIONS = builder
                .define("translate_descriptions", true);
        QUICK_CONFIGURE = builder
                .define("quick_configure", true);
        MODIFY_TITLE_SCREEN = builder
                .define("modify_title_screen", true);
        MODIFY_GAME_MENU = builder
                .define("modify_game_menu", true);
        CONFIG_MODE = builder
                .define("config_mode", false);
        DISABLE_DRAG_AND_DROP = builder
                .define("disable_drag_and_drop", false);
        builder.pop();

        builder.push("hide");
        SHOW_LIBRARIES = builder
                .define("show_libraries", false);
        HIDE_MOD_LINKS = builder
                .define("hide_mod_links", false);
        HIDE_MOD_LICENSE = builder
                .define("hide_mod_license", false);
        HIDE_BADGES = builder
                .define("hide_badges", false);
        HIDE_MOD_CREDITS = builder
                .define("hide_mod_credits", false);
        HIDE_CONFIG_BUTTONS = builder
                .define("hide_config_buttons", false);
        HIDE_BADGE_BUTTONS = builder
                .define("hide_badge_buttons", true);
        HIDDEN_MODS = builder
                .defineList("hidden_mods", ArrayList::new, String::new, object -> object instanceof String);
        HIDDEN_CONFIGS = builder
                .defineList("hidden_configs", ArrayList::new, String::new, object -> object instanceof String);
        LIBRARY_LIST = builder
                .defineList("library_list", ArrayList::new, String::new, object -> object instanceof String);
        builder.pop();

        builder.push("count");
        COUNT_HIDDEN_MODS = builder
                .define("count_hidden_mods", true);
        COUNT_CHILDREN = builder
                .define("count_children", true);
        COUNT_LIBRARIES = builder
                .define("count_libraries", true);
        builder.pop();

        MOD_BADGES = builder
                .defineList("mod_badges", ArrayList::new, String::new, object -> object instanceof String);
        MOD_PARENTS = builder
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
            if (badgeKeyValue.length != 1)
                this.mod_badges.put(badgeKeyValue[0], new LinkedHashSet<>(Arrays.stream(badgeKeyValue[1].split(", ")).toList()));
            else this.mod_badges.put(badgeKeyValue[0], new LinkedHashSet<>());
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
        this.MOD_PARENTS.get().forEach(parentToMods -> {
            if (parentToMods.isEmpty())
                return;

            String[] parentToMod = parentToMods.split("=");
            List<String> modIds = Arrays.stream(parentToMod[1].split(", ")).toList();
            for (String id : modIds) {
                Mod mod = ModMenu.MODS.get(id);

                if (mod == null)
                    continue;

                String parentId = parentToMod[0];

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

    public void save() {
        List<String> list = new ArrayList<>();
        this.mod_badges.forEach((key, values) -> {
            StringBuilder string = new StringBuilder();
            for (String value : values) {
                if (!string.isEmpty())
                    string.append(", ");

                string.append(value);
            }

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
        CLASSIC(), REPLACE_REALMS(), SHRINK(), ICON()
    }

    public enum GameMenuButtonStyle {
        @SerializedName(value = "replace", alternate = { "replace_bugs" }) REPLACE, @SerializedName(value = "insert", alternate = { "below_bugs" }) INSERT, ICON
    }
}
