package com.terraformersmc.mod_menu.mixin;

import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.config.ModMenuConfig;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(Lnet/minecraft/client/Minecraft;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get() && ModMenu.getConfig().MODS_BUTTON_STYLE.get() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
			return height + 16;
		} else if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS || ModMenu.getConfig().MODS_BUTTON_STYLE.get() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
			return -99999;
		}
		return height;
	}
}
