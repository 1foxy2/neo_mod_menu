package com.terraformersmc.modmenu.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.BetterModListConfig;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.terraformersmc.modmenu.event.ModMenuEventHandler.buttonHasText;

@Mixin(PauseScreen.class)
public abstract class MixinPauseScreen extends Screen {
	protected MixinPauseScreen(Component title) {
		super(title);
	}

	@Inject(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;visitWidgets(Ljava/util/function/Consumer;)V"))
	private void onInitWidgets(CallbackInfo ci, @Local GridLayout gridlayout) {
		if (gridlayout != null) {
			final List<LayoutElement> buttons = ((AccessorGridLayout) gridlayout).getChildren();
			if (ModMenu.getConfig().MODIFY_GAME_MENU.get()) {
				int modsButtonIndex = -1;
				final int spacing = 24;
				int buttonsY = this.height / 4 + 8;
				BetterModListConfig.GameMenuButtonStyle style = ModMenu.getConfig().GAME_MENU_BUTTON_STYLE.get();
				int vanillaButtonsY = this.height / 4 + 72 - 16 + 1;
				int fullWidthButton = 204;
				boolean hadExitButton = false;

				for (int i = 0; i < buttons.size(); i++) {
					LayoutElement widget = buttons.get(i);

					if (buttonHasText(widget, "menu.returnToMenu")
							|| buttonHasText(widget, "menu.disconnect"))
						hadExitButton = true;

					ModMenuEventHandler.shiftButtons(widget, hadExitButton, spacing + (hadExitButton ? 12 : -12));

					if (style == BetterModListConfig.GameMenuButtonStyle.INSERT) {
						if (!(widget instanceof AbstractWidget button) || button.visible) {
							ModMenuEventHandler.shiftButtons(widget, modsButtonIndex == -1 || buttonHasText(widget, "menu.reportBugs", "menu.server_links"), spacing);
							if (modsButtonIndex == -1) {
								buttonsY = widget.getY();
							}
						}
					}

					boolean isShortFeedback = ModMenuEventHandler.buttonHasText(widget, "menu.feedback");
					boolean isLongFeedback = ModMenuEventHandler.buttonHasText(widget, "menu.sendFeedback");
					if (isShortFeedback || isLongFeedback) {
						modsButtonIndex = i + 1;
						vanillaButtonsY = widget.getY();
						if (style == BetterModListConfig.GameMenuButtonStyle.REPLACE) {
							if (widget instanceof AbstractWidget cw) {
								cw.visible = false;
								cw.active = false;
							}
							if (isShortFeedback) {
								fullWidthButton = widget.getWidth();
							}
							buttons.stream()
									.filter(w -> buttonHasText(w, "menu.reportBugs"))
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
					if (style == BetterModListConfig.GameMenuButtonStyle.INSERT) {
						buttons.add(modsButtonIndex, new ModMenuButtonWidget(
								this.width / 2 - 102,
								buttonsY + spacing,
								fullWidthButton,
								20,
								ModMenu.createModsButtonText(true),
								this
						));
					} else if (style == BetterModListConfig.GameMenuButtonStyle.REPLACE) {
						buttons.add(new ModMenuButtonWidget(
								this.width / 2 - 102,
								vanillaButtonsY,
								fullWidthButton,
								20,
								ModMenu.createModsButtonText(true),
								this
						));
					} else if (style == BetterModListConfig.GameMenuButtonStyle.ICON) {
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
								ModMenu.createModsButtonText(true)
						));
					}
				}
				buttons.removeIf(button -> ModMenuEventHandler.buttonHasText(button, "fml.menu.mods"));
			}
		}
	}
}
