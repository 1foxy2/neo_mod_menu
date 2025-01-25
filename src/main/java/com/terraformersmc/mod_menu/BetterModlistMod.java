package com.terraformersmc.mod_menu;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

@net.minecraftforge.fml.common.Mod(ModMenu.MOD_ID)
public class BetterModlistMod {
    public BetterModlistMod() {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER)
            return;

        new ModMenu();
    }
}
