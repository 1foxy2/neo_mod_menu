package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuButtonWidget extends Button {
	public ModMenuButtonWidget(int x, int y, int width, int height, Component text, Screen screen) {
		super(x,
			y,
			width,
			height,
			text,
			button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)),
			Button.DEFAULT_NARRATION
		);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(guiGraphics, mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(guiGraphics,
				this.width + this.getX() - 16,
				this.height / 2 + this.getY() - 4
			);
		}
	}
}
