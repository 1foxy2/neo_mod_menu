package com.terraformersmc.mod_menu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class UpdateAvailableBadge {
	private static final ResourceLocation UPDATE_ICON = ResourceLocation.withDefaultNamespace("icon/trial_available");

	public static void renderBadge(GuiGraphics guiGraphics, int x, int y) {
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		int animOffset = 0;
		if ((Util.getMillis() / 800L & 1L) == 1L) {
			animOffset = 8;
		}
		guiGraphics.blitSprite(UPDATE_ICON, x, y, 8, 8);
	}
}
