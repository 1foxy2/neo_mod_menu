package com.terraformersmc.modmenu.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.BetterModListConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.widget.ModsButton;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.terraformersmc.modmenu.event.ModMenuEventHandler.MODS_BUTTON_TEXTURE;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
    protected MixinTitleScreen(Component title) {
        super(title);
    }

    @Shadow
	protected abstract int getHorizontalPosition(int currentButton, int numberOfButtons, int buttonWidth);

	@ModifyArg(at = @At(value = "INVOKE", target = "Lcom/mojang/realmsclient/gui/screens/RealmsNotificationsScreen;init(II)V"), method = "init", index = 1)
	private int adjustRealmsHeight(int height) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get() && ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.CLASSIC) {
			return height - 51;
		} else if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.REPLACE_REALMS || ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.SHRINK) {
			return -99999;
		} else {
			return height;
		}
	}

	@Definition(id = "numberOfButtons", local = @Local(type = int.class, name = "numberOfButtons"))
	@Expression("numberOfButtons = ?")
	@Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
	private void adjustAmountOfIconButtons(CallbackInfo ci, @Local(name = "numberOfButtons") LocalIntRef numberOfButtons, @Share("addModMenuIconWidget") LocalBooleanRef addModMenuIconWidget) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get() && ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.ICON) {
			addModMenuIconWidget.set(true);
			numberOfButtons.set(numberOfButtons.get() + 1);
		}
	}

	@Definition(id = "width", field = "Lnet/minecraft/client/gui/screens/TitleScreen;width:I")
	@Expression("this.width / 2 - 100")
	@Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	private void addModMenuIconWidget(CallbackInfo ci, @Local(name = "currentButton") LocalIntRef currentButton, @Local(name = "topPos") int topPos, @Local(name = "numberOfButtons") int numberOfButtons, @Share("addModMenuIconWidget") LocalBooleanRef addModMenuIconWidget) {
		if (!addModMenuIconWidget.get()) return;
		currentButton.set(currentButton.get()+1);
		addRenderableWidget(new UpdateCheckerTexturedButtonWidget(
				this.getHorizontalPosition(currentButton.get(), numberOfButtons, 20),
				topPos,
				20,
				20,
				0,
				0,
				20,
				MODS_BUTTON_TEXTURE,
				32,
				64,
				_ -> Minecraft.getInstance().gui.setScreen(new ModsScreen(this)),
				ModMenu.createModsButtonText(true)
		));
	}

	@WrapOperation(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/TitleScreen;addRenderableWidget(" +
							"Lnet/minecraft/client/gui/components/events/GuiEventListener;" +
							")Lnet/minecraft/client/gui/components/events/GuiEventListener;",
					ordinal = 0
			)
	)
	private GuiEventListener removeForgeButton(TitleScreen instance, GuiEventListener guiEventListener, Operation<GuiEventListener> original, @Local(name = "numberOfButtons") LocalIntRef numberOfButtons, @Local(name = "currentButton") LocalIntRef currentButton) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get() ) {
			if (ModMenu.getConfig().HIDE_NEOFORGE_BUTTON.get() && ModMenu.getConfig().MODS_BUTTON_STYLE.get() != BetterModListConfig.TitleMenuButtonStyle.NEO_ICON) {
				numberOfButtons.set(numberOfButtons.get() - 1);
				currentButton.set(currentButton.get() - 1);
			}
			return guiEventListener;
		}
		return original.call(instance, guiEventListener);
	}

	@WrapOperation(
			method = "init",
			at = @At(
					value = "INVOKE",
					target = "Lnet/neoforged/neoforge/client/gui/widget/ModsButton;setPosition(II)V",
					ordinal = 0
			)
	)
	private void addForgeButton(ModsButton instance, int x, int y, Operation<Void> original) {
		if (ModMenu.getConfig().MODIFY_TITLE_SCREEN.get() ) {
			if (ModMenu.getConfig().MODS_BUTTON_STYLE.get() == BetterModListConfig.TitleMenuButtonStyle.NEO_ICON) {
				addRenderableWidget(SpriteIconButton.builder(
						ModMenu.createModsButtonText(true),
						_ -> Minecraft.getInstance().gui.setScreen(new ModsScreen(this)), true
				).size(20, 20).sprite(new WidgetSprites(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "icon/neo_logo")), 15, 15).spriteOffset(0, -1).build()).setPosition(x, y);
			}
		}
		original.call(instance, x, y);
	}
}
