package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerLinks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PauseScreen.class)
public abstract class MixinGameMenu extends Screen {
	protected MixinGameMenu(Component title) {
		super(title);
	}

	@Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;visitWidgets(Ljava/util/function/Consumer;)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void onInitWidgets(CallbackInfo ci, GridLayout gridlayout, GridLayout.RowHelper gridlayout$rowhelper, ServerLinks serverlinks, Component component) {
		if (gridlayout != null) {
			final List<LayoutElement> buttons = ((AccessorGridWidget) gridlayout).getChildren();
			if (ModMenuConfig.MODIFY_GAME_MENU.getValue()) {
				int modsButtonIndex = -1;
				final int spacing = 24;
				int buttonsY = this.height / 4 + 8;
				ModMenuConfig.GameMenuButtonStyle style = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
				int vanillaButtonsY = this.height / 4 + 72 - 16 + 1;
				final int fullWidthButton = 204;

				for (int i = 0; i < buttons.size(); i++) {
					LayoutElement widget = buttons.get(i);
					if (style == ModMenuConfig.GameMenuButtonStyle.INSERT) {
						if (!(widget instanceof AbstractWidget button) || button.visible) {
							ModMenuEventHandler.shiftButtons(widget, modsButtonIndex == -1 || ModMenuEventHandler.buttonHasText(widget, "menu.reportBugs", "menu.server_links"), spacing);
							if (modsButtonIndex == -1) {
								buttonsY = widget.getY();
							}
						}
					}
					boolean isShortFeedback = ModMenuEventHandler.buttonHasText(widget, "menu.feedback");
					boolean isLongFeedback = ModMenuEventHandler.buttonHasText(widget, "menu.sendFeedback");
					if (ModMenuEventHandler.buttonHasText(widget, "menu.returnToMenu")) widget.setY(widget.getY() - spacing);
					if (isShortFeedback || isLongFeedback) {
						modsButtonIndex = i + 1;
						vanillaButtonsY = widget.getY();
						if (style == ModMenuConfig.GameMenuButtonStyle.REPLACE) {
							buttons.set(i, new ModMenuButtonWidget(
								widget.getX(),
								widget.getY(),
								isShortFeedback ? widget.getWidth() : fullWidthButton,
								widget.getHeight(),
								ModMenuApi.createModsButtonText(),
								this
							));
							buttons.stream()
								.filter(w -> ModMenuEventHandler.buttonHasText(w, "menu.reportBugs"))
								.forEach(w -> {
									if (w instanceof AbstractWidget cw) {
										cw.visible = false;
										cw.active = false;
									}
								});
						} else {
							modsButtonIndex = i + 1;
							if (!(widget instanceof AbstractWidget button) || button.visible) {
								buttonsY = widget.getY();
							}
						}
					}
				}
				if (modsButtonIndex != -1) {
					if (style == ModMenuConfig.GameMenuButtonStyle.INSERT) {
						buttons.add(modsButtonIndex, new ModMenuButtonWidget(
							this.width / 2 - 102,
							buttonsY + spacing,
							fullWidthButton,
							20,
							ModMenuApi.createModsButtonText(),
							this
						));
					} else if (style == ModMenuConfig.GameMenuButtonStyle.ICON) {
						buttons.add(modsButtonIndex, new UpdateCheckerTexturedButtonWidget(
							this.width / 2 + 4 + 100 + 2,
							vanillaButtonsY,
							20,
							20,
							0,
							0,
							20,
							ModMenuEventHandler.MODS_BUTTON_TEXTURE,
							32,
							64,
							button -> Minecraft.getInstance().setScreen(new ModsScreen(this)),
							ModMenuApi.createModsButtonText()
						));
					}
				}
				buttons.removeIf(button -> ModMenuEventHandler.buttonHasText(button, "fml.menu.mods"));
			}
		}
	}
}
