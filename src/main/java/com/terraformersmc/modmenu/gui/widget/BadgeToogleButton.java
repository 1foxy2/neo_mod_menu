package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BadgeToogleButton extends LegacyTexturedButtonWidget {
	private static final ResourceLocation BADGE_TOGGLE_TEXTURE =
			ResourceLocation.fromNamespaceAndPath(ModMenu.NAMESPACE, "textures/gui/badge_toggle_button.png");
	private boolean hasBadge;

	public BadgeToogleButton(
		int x,
		int y,
		int width,
		int height,
		int u,
		int v,
		int hoveredVOffset,
		OnPress pressAction,
		Component message,
		boolean hasBadge
	) {
		super(x, y, width, height, u, v, hoveredVOffset, BADGE_TOGGLE_TEXTURE, 22, 22, pressAction, message);

		this.hasBadge = hasBadge;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		int v = this.v;
		int u = this.u;

		if (!this.isActive()) {
			v += this.hoveredVOffset * 2;
		} else if (this.isHovered()) {
			v += this.hoveredVOffset;
		}
		if (hasBadge)
			u += 11;

		guiGraphics.blit(RenderType::guiTextured, texture,
			this.getX(),
			this.getY(),
			u,
			v,
			this.width,
			this.height,
			this.textureWidth,
			this.textureHeight
		);
	}

	public void toggle() {
		hasBadge = !hasBadge;
	}

	public static Builder badgeButtonBuilder(Component message, OnPress onPress, boolean hasBadge) {
		return new Builder(message, onPress, hasBadge);
	}

	public static class Builder {
		private final Component message;
		private final OnPress onPress;

		private int x;
		private int y;

		private boolean hasBadge;

		private int width;
		private int height;

		private int u;
		private int v;
		private int hoveredVOffset;

		public Builder(Component message, OnPress onPress, boolean hasBadge) {
			this.message = message;
			this.onPress = onPress;
			this.hasBadge = hasBadge;
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

		public BadgeToogleButton build() {
			return new BadgeToogleButton(this.x,
				this.y,
				this.width,
				this.height,
				this.u,
				this.v,
				this.hoveredVOffset,
				this.onPress,
				this.message,
					this.hasBadge
			);
		}
	}
}
