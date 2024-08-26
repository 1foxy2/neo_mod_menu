package com.terraformersmc.mod_menu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class UpdateAvailableBadge {
	private static final ResourceLocation UPDATE_ICON = new ResourceLocation("realms", "textures/gui/realms/trial_icon.png");

	public static void renderBadge(GuiGraphics DrawContext, int x, int y) {
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		int animOffset = 0;
		if ((Util.getMillis() / 800L & 1L) == 1L) {
			animOffset = 8;
		}
		DrawContext.blit(UPDATE_ICON, x, y, 0f, animOffset, 8, 8, 8, 16);
	}
}
