package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.BetterModListConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(Lnet/minecraft/client/Minecraft;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get() && ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.CLASSIC) {
			return height + 16;
		} else if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.REPLACE_REALMS || ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.SHRINK) {
			return -99999;
		}
		return height;
	}
}
