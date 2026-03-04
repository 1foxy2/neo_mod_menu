package com.terraformersmc.mod_menu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.mod_menu.ModMenu;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.internal.BrandingControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BrandingControl.class)
public abstract class MixinBrandingControl {

    @WrapOperation(
            method = "computeBranding",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/fml/i18n/FMLTranslations;" +
                            "parseMessage(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"
            )
    )
    private static String replaceBranding(String i18nMessage, Object[] args, Operation<String> original) {
        String neoForge = original.call(i18nMessage, args);
        if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
            if (ModMenu.getConfig().MOD_COUNT_LOCATION.get().isOnTitleScreen()) {
                String count = ModMenu.getDisplayedModCount();
                String specificKey = "mod_menu.mods." + count;
                String replacementKey = I18n.exists(specificKey) ? specificKey : "mod_menu.mods.n";
                if (ModMenu.getConfig().EASTER_EGGS.get() && I18n.exists(specificKey + ".secret")) {
                    replacementKey = specificKey + ".secret";
                }
                neoForge = neoForge.replace(I18n.get("fml.menu.branding", "", ModList.get().size()),
                        I18n.get(replacementKey, count));
            } else  {
                neoForge = neoForge.replace(I18n.get("fml.menu.branding", "", ModList.get().size()),
                        I18n.get("menu.modded"));
            }
        }
        return neoForge;
    }
}
