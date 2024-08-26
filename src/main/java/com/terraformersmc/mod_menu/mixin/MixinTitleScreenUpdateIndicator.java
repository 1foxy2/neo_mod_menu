package com.terraformersmc.mod_menu.mixin;

import net.minecraftforge.client.gui.TitleScreenModUpdateIndicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreenModUpdateIndicator.class)
public class MixinTitleScreenUpdateIndicator {
	@Shadow private boolean hasCheckedForUpdates;

	@Inject(method = "init()V", at = @At(value = "HEAD"))
	private void disableUpdateBadge(CallbackInfo ci) {
		this.hasCheckedForUpdates = true;
	}
}
