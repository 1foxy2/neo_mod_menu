package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(Lnet/minecraft/client/Minecraft;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
		if (ModMenuConfig.modify_title_screen && ModMenuConfig.mods_button_style == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
			return height;
		} else if (ModMenuConfig.mods_button_style == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS || ModMenuConfig.mods_button_style == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
			return -99999;
		}
		return height + 51;
	}
}
