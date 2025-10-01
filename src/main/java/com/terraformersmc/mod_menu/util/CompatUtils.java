package com.terraformersmc.mod_menu.util;

import com.aetherteam.cumulus.CumulusConfig;
import net.neoforged.fml.ModList;

public class CompatUtils {
    public static boolean isCustomMenu () {
        return ModList.get().isLoaded("cumulus_menus") && !CumulusConfig.CLIENT.active_menu.get().equals("minecraft:minecraft");
    }
}
