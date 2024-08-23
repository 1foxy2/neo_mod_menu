package com.terraformersmc.modmenu.mixin;

import com.google.common.collect.Lists;
import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.neoforge.internal.BrandingControl;
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

    @Shadow
    private static void computeBranding() {
    }

    @Shadow private static List<String> brandings;

    @Shadow private static List<String> brandingsNoMC;

    @Inject(method = "forEachLine", at = @At(value = "HEAD"), cancellable = true)
    private static void replaceBranding(boolean includeMC, boolean reverse, BiConsumer<Integer, String> lineConsumer, CallbackInfo ci) {
        if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
            final List<String> brandings = getBrandings(includeMC, reverse);
            if (ModMenu.getConfig().MOD_COUNT_LOCATION.get().isOnTitleScreen()) {
                String count = ModMenu.getDisplayedModCount();
                String specificKey = "modmenu.mods." + count;
                String replacementKey = I18n.exists(specificKey) ? specificKey : "modmenu.mods.n";
                if (ModMenu.getConfig().EASTER_EGGS.get() && I18n.exists(specificKey + ".secret")) {
                    replacementKey = specificKey + ".secret";
                }
                I18n.get("menu.modded");
                String[] neoForge = brandings.get(0).split(" ");
                lineConsumer.accept(0, neoForge[0] + " " + neoForge[1] + I18n.get(replacementKey, count));
                lineConsumer.accept(1, brandings.get(1));
            } else lineConsumer.accept(0, brandings.get(1));
            ci.cancel();
        }
    }

    @Unique
    private static List<String> getBrandings(boolean includeMC, boolean reverse) {
        computeBranding();
        if (includeMC) {
            return reverse ? Lists.reverse(brandings) : brandings;
        } else {
            return reverse ? Lists.reverse(brandingsNoMC) : brandingsNoMC;
        }
    }
}
