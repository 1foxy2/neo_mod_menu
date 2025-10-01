package com.terraformersmc.mod_menu.mixin;

import com.google.common.collect.Lists;
import com.terraformersmc.mod_menu.ModMenu;
import net.minecraft.client.resources.language.I18n;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.internal.BrandingControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BiConsumer;

@Mixin(BrandingControl.class)
public abstract class MixinBrandingControl {

    @Shadow(remap = false)
    private static void computeBranding() {
    }

    @Shadow(remap = false) private static List<String> brandings;

    @Shadow(remap = false) private static List<String> brandingsNoMC;

    @Inject(method = "forEachLine", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private static void replaceBranding(boolean includeMC, boolean reverse, BiConsumer<Integer, String> lineConsumer, CallbackInfo ci) {
        if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
            final List<String> brandings = forge_mod_menu$getBrandings(includeMC, reverse);
            String forge = brandings.get(0);
            if (ModMenu.getConfig().MOD_COUNT_LOCATION.get().isOnTitleScreen()) {
                String count = ModMenu.getDisplayedModCount();
                String specificKey = "mod_menu.mods." + count;
                String replacementKey = I18n.exists(specificKey) ? specificKey : "mod_menu.mods.n";
                if (ModMenu.getConfig().EASTER_EGGS.get() && I18n.exists(specificKey + ".secret")) {
                    replacementKey = specificKey + ".secret";
                }
                forge = forge.replace(I18n.get("fml.menu.branding", "", ModList.get().size()),
                        I18n.get(replacementKey, count));
            } else {
                forge = forge.replace(I18n.get("fml.menu.branding", "", ModList.get().size()),
                        I18n.get("menu.modded"));
            }
            lineConsumer.accept(0, forge);
            lineConsumer.accept(1, brandings.get(1));
            ci.cancel();
        }
    }

    @Unique
    private static List<String> forge_mod_menu$getBrandings(boolean includeMC, boolean reverse) {
        computeBranding();
        if (includeMC) {
            return reverse ? Lists.reverse(brandings) : brandings;
        } else {
            return reverse ? Lists.reverse(brandingsNoMC) : brandingsNoMC;
        }
    }
}
