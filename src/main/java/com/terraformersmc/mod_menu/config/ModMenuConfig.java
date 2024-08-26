package com.terraformersmc.mod_menu.config;

import com.google.gson.annotations.SerializedName;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.util.mod.Mod;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    public final ModConfigSpec.BooleanValue CONFIG_MODE;
    public final ModConfigSpec.BooleanValue DISABLE_DRAG_AND_DROP;
    public final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_MODS;
    public final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_CONFIGS;
    public final ModConfigSpec.ConfigValue<List<? extends String>> LIBRARY_LIST;
  //  public static final ModConfigSpec.BooleanValue DISABLE_UPDATE_CHECKER;

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

        //    UPDATE_CHECKER = builder
        //            .define("translate_descriptions", true);
        //    BUTTON_UPDATE_BADGE = builder
        //            .define("button_update_badge", true);
        //  UPDATE_CHANNEL
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
