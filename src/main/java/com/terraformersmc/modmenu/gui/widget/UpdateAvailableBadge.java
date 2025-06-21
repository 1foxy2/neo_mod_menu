package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class UpdateAvailableBadge {
	private static final ResourceLocation UPDATE_ICON = ResourceLocation.withDefaultNamespace("icon/trial_available");

	public static void renderBadge(GuiGraphics guiGraphics, int x, int y) {
		guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, UPDATE_ICON, x, y, 8, 8, 0xFFFFFFFF);
	}
}
