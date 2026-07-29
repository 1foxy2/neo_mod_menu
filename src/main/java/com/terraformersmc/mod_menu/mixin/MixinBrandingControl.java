package com.terraformersmc.mod_menu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.mod_menu.ModMenu;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.internal.BrandingControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BrandingControl.class)
public abstract class MixinBrandingControl {

    @WrapOperation(
            method = "computeBranding",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/common/ForgeI18n;" +
                            "parseMessage(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"
            ),
            remap = false
    )
    private static String replaceBranding(String i18nMessage, Object[] args, Operation<String> original) {
        String neoForge = original.call(i18nMessage, args);
        if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
            if (ModMenu.getConfig().MOD_COUNT_LOCATION.get().isOnTitleScreen()) {
                String count = ModMenu.getDisplayedModCount();
                String specificKey = "modmenu.mods." + count;
                String replacementKey = I18n.exists(specificKey) ? specificKey : "modmenu.mods.n";
                if (ModMenu.getConfig().EASTER_EGGS.get() && I18n.exists(specificKey + ".secret")) {
                    replacementKey = specificKey + ".secret";
                }
                neoForge = neoForge.replace(I18n.get("fml.menu.loadingmods", "", ModList.get().size()),
                        I18n.get(replacementKey, count));
            } else  {
                neoForge = neoForge.replace(I18n.get("fml.menu.loadingmods", "", ModList.get().size()),
                        I18n.get("menu.modded"));
            }
        }
        return neoForge;
    }
}
