package com.terraformersmc.modmenu.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.BetterModListConfig;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {
	protected MixinPauseScreen(Component title) {
		super(title);
	}

	@Definition(id = "integratedServer", local = @Local(type = IntegratedServer.class, name = "integratedServer"))
	@Expression("integratedServer = ?")
	@Inject(method = "createPauseMenu", at = @At("MIXINEXTRAS:EXPRESSION"))
	private void insertModMenuIconButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
		if (!ModMenu.getConfig().MODIFY_GAME_MENU.get()) return;
		BetterModListConfig.GameMenuButtonStyle style = ModMenu.getConfig().GAME_MENU_BUTTON_STYLE.get();
		if (style == BetterModListConfig.GameMenuButtonStyle.ICON) {
			iconButtonRow.addChild(new UpdateCheckerTexturedButtonWidget(
					0,
					0,
					20,
					20,
					0,
					0,
					20,
					ModMenuEventHandler.MODS_BUTTON_TEXTURE,
					32,
					64,
					_ -> Minecraft.getInstance().gui.setScreen(new ModsScreen(this)),
					ModMenu.createModsButtonText(true)
			));
		}
	}

	@WrapOperation(
			method = "createPauseMenu",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/SpriteIconButton;builder(" +
							"Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;" +
							"Z)Lnet/minecraft/client/gui/components/SpriteIconButton$Builder;",
					ordinal = 0
			)
	)
	private SpriteIconButton.Builder replaceForgeButton(Component message, Button.OnPress onPress, boolean iconOnly, Operation<SpriteIconButton.Builder> original) {
		if (!ModMenu.getConfig().MODIFY_GAME_MENU.get()) return original.call(message, onPress, iconOnly);
		BetterModListConfig.GameMenuButtonStyle style = ModMenu.getConfig().GAME_MENU_BUTTON_STYLE.get();
		if (style == BetterModListConfig.GameMenuButtonStyle.NEO_ICON) {
			return original.call(ModMenu.createModsButtonText(true), (Button.OnPress) _ -> Minecraft.getInstance().gui.setScreen(new ModsScreen(this)), iconOnly);
		}
		return original.call(message, onPress, iconOnly);
	}

	@WrapOperation(
			method = "createPauseMenu",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/layouts/LinearLayout;" +
							"addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;" +
							")Lnet/minecraft/client/gui/layouts/LayoutElement;",
					ordinal = 0
			)
	)
	private <T extends LayoutElement> T hideForgeButton(LinearLayout instance, T child, Operation<T> original) {
		if (!ModMenu.getConfig().MODIFY_GAME_MENU.get() ||
				ModMenu.getConfig().GAME_MENU_BUTTON_STYLE.get() == BetterModListConfig.GameMenuButtonStyle.NEO_ICON ||
				!ModMenu.getConfig().HIDE_NEOFORGE_BUTTON.get()
		) return original.call(instance, child);

		return child;
	}

	@Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;ILnet/minecraft/client/gui/layouts/LayoutSettings;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 1, shift = At.Shift.AFTER))
	private void insertModMenuFullButton(CallbackInfo ci, @Local(name = "helper") GridLayout.RowHelper helper) {
		if (!ModMenu.getConfig().MODIFY_GAME_MENU.get()) return;
		BetterModListConfig.GameMenuButtonStyle style = ModMenu.getConfig().GAME_MENU_BUTTON_STYLE.get();
		if (style == BetterModListConfig.GameMenuButtonStyle.INSERT) {
			final int fullWidthButton = 204; // PauseScreen.BUTTON_WIDTH_FULL
			helper.addChild(new ModMenuButtonWidget(
					0,
					0,
					fullWidthButton,
					20,
					ModMenu.createModsButtonText(true),
					this
			), 2);
		}
	}
}
