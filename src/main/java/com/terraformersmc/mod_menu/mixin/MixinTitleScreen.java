package com.terraformersmc.mod_menu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.config.ModMenuConfig;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import com.terraformersmc.mod_menu.util.CompatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(Lnet/minecraft/client/Minecraft;II)V"), method = "init", index = 2)
	private int adjustRealmsHeight(int height) {
        if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get() && !CompatUtils.isCustomMenu()) {
            if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
                return height + 16;
            } else if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
                return height + 64;
            } else {
                return -99999;
            }
        }
		return height;
	}


    @WrapOperation(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button;builder(" +
                            "Lnet/minecraft/network/chat/Component;" +
                            "Lnet/minecraft/client/gui/components/Button$OnPress;" +
                            ")Lnet/minecraft/client/gui/components/Button$Builder;",
                    ordinal = 0
            )
    )
    private Button.Builder replaceScreen(Component message, Button.OnPress onPress, Operation<Button.Builder> original) {
        if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get()) {
            onPress = button -> Minecraft.getInstance().setScreen(new ModsScreen((TitleScreen) (Object) this));
        }
        return original.call(message, onPress);
    }
}
