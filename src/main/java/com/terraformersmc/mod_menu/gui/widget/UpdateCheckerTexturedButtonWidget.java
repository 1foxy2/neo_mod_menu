package com.terraformersmc.mod_menu.gui.widget;


import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class UpdateCheckerTexturedButtonWidget extends ImageButton {
	public UpdateCheckerTexturedButtonWidget(int x, int y, int width, int height, int u, int v, int hoveredVOffset, ResourceLocation texture, int textureWidth, int textureHeight, Button.OnPress pressAction, Component message) {
		super(x, y, width, height, u, v, hoveredVOffset, texture, textureWidth, textureHeight, pressAction, message);
	}

	@Override
	public void renderWidget(GuiGraphics DrawContext, int mouseX, int mouseY, float delta) {
		super.renderWidget(DrawContext, mouseX, mouseY, delta);
	}
}
