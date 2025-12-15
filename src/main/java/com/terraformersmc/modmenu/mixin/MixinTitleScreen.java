package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.BetterModListConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(II)V"), method = "init", index = 1)
	private int adjustRealmsHeight(int height) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
			if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.CLASSIC) {
				return height + 16;
			} else if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.ICON) {
				return height + 64;
			} else {
				return -99999;
			}
		}
		return height;
	}

	@ModifyArg(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/Button;builder(" +
							"Lnet/minecraft/network/chat/Component;" +
							"Lnet/minecraft/client/gui/components/Button$OnPress;" +
							")Lnet/minecraft/client/gui/components/Button$Builder;",
					ordinal = 0
			),
			index = 0
	)
	private Component bettermodlist$modifyForgeModsButton(Component message) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
			return ModMenu.createModsButtonText(true);
		}
		return message;
	}

	@ModifyArg(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/Button;builder(" +
							"Lnet/minecraft/network/chat/Component;" +
							"Lnet/minecraft/client/gui/components/Button$OnPress;" +
							")Lnet/minecraft/client/gui/components/Button$Builder;",
					ordinal = 0
			),
			index = 1
	)
	private Button.OnPress bettermodlist$modifyForgeModsButton(Button.OnPress onPress) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
			return button -> Minecraft.getInstance().setScreen(new ModsScreen((TitleScreen) (Object) this));
		}
		return onPress;
	}
}
