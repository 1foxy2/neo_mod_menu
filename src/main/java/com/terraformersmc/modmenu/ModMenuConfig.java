package com.terraformersmc.modmenu;

import com.google.gson.annotations.SerializedName;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

@EventBusSubscriber(modid = ModMenu.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModMenuConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static final ModConfigSpec SPEC;

    private static final ModConfigSpec.EnumValue<Sorting> SORTING;
    private static final ModConfigSpec.BooleanValue COUNT_LIBRARIES;
    private static final ModConfigSpec.BooleanValue COMPACT_LIST;
    public static final ModConfigSpec.BooleanValue COUNT_CHILDREN;
    private static final ModConfigSpec.EnumValue<TitleMenuButtonStyle> MODS_BUTTON_STYLE;
    private static final ModConfigSpec.BooleanValue COUNT_HIDDEN_MODS;
    private static final ModConfigSpec.EnumValue<GameMenuButtonStyle> GAME_MENU_BUTTON_STYLE;
    private static final ModConfigSpec.EnumValue<ModCountLocation> MOD_COUNT_LOCATION;
    public static final ModConfigSpec.BooleanValue HIDE_MOD_LINKS;
    public static final ModConfigSpec.BooleanValue SHOW_LIBRARIES;
    public static final ModConfigSpec.BooleanValue HIDE_MOD_LICENSE;
    public static final ModConfigSpec.BooleanValue HIDE_BADGES;
    public static final ModConfigSpec.BooleanValue HIDE_MOD_CREDITS;
    public static final ModConfigSpec.BooleanValue EASTER_EGGS;
    public static final ModConfigSpec.BooleanValue RANDOM_JAVA_COLORS;
    public static final ModConfigSpec.BooleanValue TRANSLATE_NAMES;
    public static final ModConfigSpec.BooleanValue TRANSLATE_DESCRIPTIONS;
    //public static final ModConfigSpec.BooleanValue UPDATE_CHECKER;
  //  public static final ModConfigSpec.BooleanValue BUTTON_UPDATE_BADGE;
//    public static final ModConfigSpec.BooleanValue UPDATE_CHANNEL;
    public static final ModConfigSpec.BooleanValue QUICK_CONFIGURE;

    public static final ModConfigSpec.BooleanValue MODIFY_TITLE_SCREEN;
    public static final ModConfigSpec.BooleanValue MODIFY_GAME_MENU;
    public static final ModConfigSpec.BooleanValue HIDE_CONFIG_BUTTONS;
    public static final ModConfigSpec.BooleanValue CONFIG_MODE;
    public static final ModConfigSpec.BooleanValue DISABLE_DRAG_AND_DROP;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_MODS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_CONFIGS;
  //  public static final ModConfigSpec.BooleanValue DISABLE_UPDATE_CHECKER;

    public static Sorting sorting = Sorting.ASCENDING;
    public static boolean count_libraries = true;
    public static boolean compact_list = false;
    public static boolean count_children = true;
    public static TitleMenuButtonStyle mods_button_style = TitleMenuButtonStyle.CLASSIC;
    public static GameMenuButtonStyle game_menu_button_style = GameMenuButtonStyle.REPLACE;
    public static boolean count_hidden_mods = true;
    public static ModCountLocation mod_count_location = ModCountLocation.TITLE_SCREEN;
    public static boolean hide_mod_links = false;
    public static boolean show_libraries = false;
    public static boolean hide_mod_license = false;
    public static boolean hide_badges = false;
    public static boolean hide_mod_credits = false;
    public static boolean easter_eggs = true;
    public static boolean random_java_colors = false;
    public static boolean translate_names = true;
    public static boolean translate_descriptions = true;
   // public static boolean update_checker = true;
   //public static boolean button_update_badge = true;
   //public static UpdateChannel update_channel = UpdateChannel.RELEASE;
   public static boolean quick_congigure = true;

   public static boolean modify_title_screen = true;
   public static boolean modify_game_menu = true;
   public static boolean hide_config_buttons = false;
   public static boolean config_mode = false;
   public static boolean disable_drag_and_drop = false;
   public static HashSet<String> hidden_mods = new HashSet<>();
   public static HashSet<String> hidden_configs = new HashSet<>();


    static {
        SORTING = BUILDER
                .defineEnum("sorting", Sorting.ASCENDING);
        COUNT_LIBRARIES = BUILDER
                .define("count_libraries", true);
        COMPACT_LIST = BUILDER
                .define("count_libraries", false);
        COUNT_CHILDREN = BUILDER
                .define("count_libraries", true);
        MODS_BUTTON_STYLE = BUILDER
                .defineEnum("mods_button_style", TitleMenuButtonStyle.CLASSIC);
        GAME_MENU_BUTTON_STYLE = BUILDER
                .defineEnum("game_menu_button_style", GameMenuButtonStyle.REPLACE);
        COUNT_HIDDEN_MODS = BUILDER
                .define("count_hidden_mods", true);
        MOD_COUNT_LOCATION = BUILDER
                .defineEnum("mod_count_location", ModCountLocation.TITLE_SCREEN);
        HIDE_MOD_LINKS = BUILDER
                .define("hide_mod_links", false);
        SHOW_LIBRARIES = BUILDER
                .define("show_libraries", false);
        HIDE_MOD_LICENSE = BUILDER
                .define("hide_mod_license", false);
        HIDE_BADGES = BUILDER
                .define("hide_badges", false);
        HIDE_MOD_CREDITS = BUILDER
                .define("hide_mod_credits", false);
        EASTER_EGGS = BUILDER
                .define("easter_eggs", true);
        RANDOM_JAVA_COLORS = BUILDER
                .define("random_java_colors", false);
        TRANSLATE_NAMES = BUILDER
                .define("translate_names", true);
        TRANSLATE_DESCRIPTIONS = BUILDER
                .define("translate_descriptions", true);
    //    UPDATE_CHECKER = BUILDER
    //            .define("translate_descriptions", true);
    //    BUTTON_UPDATE_BADGE = BUILDER
    //            .define("button_update_badge", true);
      //  UPDATE_CHANNEL
        QUICK_CONFIGURE = BUILDER
                .define("quick_configure", true);
        MODIFY_TITLE_SCREEN = BUILDER
                .define("modify_title_screen", true);
        MODIFY_GAME_MENU = BUILDER
                .define("modify_game_menu", true);
        HIDE_CONFIG_BUTTONS = BUILDER
                .define("modify_game_menu", false);
        CONFIG_MODE = BUILDER
                .define("config_mode", false);
        DISABLE_DRAG_AND_DROP = BUILDER
                .define("disable_drag_and_drop", false);
        HIDDEN_MODS = BUILDER
                .defineList("hidden_mods", ArrayList::new, null, object -> object instanceof String);
        HIDDEN_CONFIGS = BUILDER
                .defineList("hidden_configs", ArrayList::new, null, object -> object instanceof String);

        SPEC = BUILDER.build();
    }
    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        sorting = SORTING.get();
        count_libraries = COUNT_LIBRARIES.get();
        compact_list = COMPACT_LIST.get();
        count_children = COUNT_CHILDREN.get();
        mods_button_style = MODS_BUTTON_STYLE.get();
        game_menu_button_style = GAME_MENU_BUTTON_STYLE.get();
        count_hidden_mods = COUNT_HIDDEN_MODS.get();
        mod_count_location = MOD_COUNT_LOCATION.get();
        hide_mod_links = HIDE_MOD_LINKS.get();
        show_libraries = SHOW_LIBRARIES.get();
        hide_mod_license = HIDE_MOD_LICENSE.get();
        hide_badges = HIDE_BADGES.get();
        hide_mod_credits = HIDE_MOD_CREDITS.get();
        easter_eggs = EASTER_EGGS.get();
        random_java_colors = RANDOM_JAVA_COLORS.get();
        translate_names = TRANSLATE_NAMES.get();
        translate_descriptions = TRANSLATE_DESCRIPTIONS.get();
        quick_congigure = QUICK_CONFIGURE.get();
        modify_title_screen = MODIFY_TITLE_SCREEN.get();
        modify_game_menu = MODIFY_GAME_MENU.get();
        hide_config_buttons = HIDE_CONFIG_BUTTONS.get();
        config_mode = CONFIG_MODE.get();
        disable_drag_and_drop = DISABLE_DRAG_AND_DROP.get();
        hidden_mods.clear();
        hidden_mods.addAll(HIDDEN_MODS.get());
        hidden_configs.clear();
        hidden_configs.addAll(HIDDEN_CONFIGS.get());
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
