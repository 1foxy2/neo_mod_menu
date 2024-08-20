package com.terraformersmc.modmenu.gui.widget;


import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class UpdateCheckerTexturedButtonWidget extends LegacyTexturedButtonWidget {
	public UpdateCheckerTexturedButtonWidget(
		int x,
		int y,
		int width,
		int height,
		int u,
		int v,
		int hoveredVOffset,
		ResourceLocation texture,
		int textureWidth,
		int textureHeight,
		Button.OnPress pressAction,
		Component message
	) {
		super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, message);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.renderWidget(guiGraphics, mouseX, mouseY, delta);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(guiGraphics, this.getX() + this.width - 5, this.getY() - 3);
		}
	}
}
