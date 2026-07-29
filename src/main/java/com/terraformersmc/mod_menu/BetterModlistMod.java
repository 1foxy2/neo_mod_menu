package com.terraformersmc.mod_menu;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ModMenu.MOD_ID)
public class BetterModlistMod {
    public BetterModlistMod(FMLJavaModLoadingContext context) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER)
            return;

        new ModMenu(context);
    }
}
