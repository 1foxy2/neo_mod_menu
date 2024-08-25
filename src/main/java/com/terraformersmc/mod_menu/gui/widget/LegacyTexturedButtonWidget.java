package com.terraformersmc.mod_menu.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LegacyTexturedButtonWidget extends ImageButton {
	private final int u;
	private final int v;
	private final int hoveredVOffset;

	private final ResourceLocation texture;

	private final int textureWidth;
	private final int textureHeight;

	public LegacyTexturedButtonWidget(
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
		OnPress pressAction,
		Component message
	) {
		super(x, y, width, height, 0, 0, 0, null, 0, 0, pressAction, message);

		this.u = u;
		this.v = v;
		this.hoveredVOffset = hoveredVOffset;

		this.texture = texture;

		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		int v = this.v;

		if (!this.isActive()) {
			v += this.hoveredVOffset * 2;
		} else if (this.isHoveredOrFocused()) {
			v += this.hoveredVOffset;
		}

		guiGraphics.blit(this.texture,
			this.getX(),
			this.getY(),
			this.u,
			v,
			this.width,
			this.height,
			this.textureWidth,
			this.textureHeight
		);
	}

	public static Builder legacyTexturedBuilder(Component message, OnPress onPress) {
		return new Builder(message, onPress);
	}

	public static class Builder {
		private final Component message;
		private final OnPress onPress;

		private int x;
		private int y;

		private int width;
		private int height;

		private int u;
		private int v;
		private int hoveredVOffset;

		private ResourceLocation texture;

		private int textureWidth;
		private int textureHeight;

		public Builder(Component message, OnPress onPress) {
			this.message = message;
			this.onPress = onPress;
		}

		public Builder position(int x, int y) {
			this.x = x;
			this.y = y;

			return this;
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;

			return this;
		}

		public Builder uv(int u, int v, int hoveredVOffset) {
			this.u = u;
			this.v = v;

			this.hoveredVOffset = hoveredVOffset;

			return this;
		}

		public Builder texture(ResourceLocation texture, int textureWidth, int textureHeight) {
			this.texture = texture;

			this.textureWidth = textureWidth;
			this.textureHeight = textureHeight;

			return this;
		}

		public LegacyTexturedButtonWidget build() {
			return new LegacyTexturedButtonWidget(this.x,
				this.y,
				this.width,
				this.height,
				this.u,
				this.v,
				this.hoveredVOffset,
				this.texture,
				this.textureWidth,
				this.textureHeight,
				this.onPress,
				this.message
			);
		}
	}
}
