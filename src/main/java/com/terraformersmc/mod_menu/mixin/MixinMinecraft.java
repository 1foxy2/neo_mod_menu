package com.terraformersmc.mod_menu.mixin;

import com.terraformersmc.mod_menu.ModMenu;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "reloadResourcePacks(ZLnet/minecraft/client/Minecraft$GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private void addBadges(boolean error, Minecraft.GameLoadCookie gameLoadCookie, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        ModMenu.createBadges();
    }
}