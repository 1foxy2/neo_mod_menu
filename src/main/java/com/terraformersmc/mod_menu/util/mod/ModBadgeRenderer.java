package com.terraformersmc.mod_menu.util.mod;

import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import com.terraformersmc.mod_menu.util.DrawingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FormattedCharSequence;

import java.util.Set;

public class ModBadgeRenderer {
	protected int startX, startY, badgeX, badgeY, badgeMax;
	protected Mod mod;
	protected Minecraft client;
	protected final ModsScreen screen;

	public ModBadgeRenderer(int startX, int startY, int endX, Mod mod, ModsScreen screen) {
		this.startX = startX;
		this.startY = startY;
		this.badgeMax = endX;
		this.mod = mod;
		this.screen = screen;
		this.client = Minecraft.getInstance();
	}

	public void draw(GuiGraphics guiGraphics) {
		this.badgeX = startX;
		this.badgeY = startY;
		Set<ModBadge> badges = mod.getBadges();
		badges.forEach(badge -> {
					if (!ModMenu.getConfig().HIDE_BADGE.get().contains(badge.getId())) {
						drawBadge(guiGraphics, badge);
					}
				}
		);
	}

	public void drawBadge(GuiGraphics guiGraphics, ModBadge badge) {
		this.drawBadge(guiGraphics,
			badge.getComponent().getVisualOrderText(),
			badge.getOutlineColor(),
			badge.getFillColor(),
			badge.getTextColor()
		);
	}

	public void drawBadge(
		GuiGraphics guiGraphics,
		FormattedCharSequence charSequence,
		int outlineColor,
		int fillColor,
		int textColor
	) {
		int width = client.font.width(charSequence) + 6;
		if (badgeX + width < badgeMax) {
			DrawingUtil.drawBadge(guiGraphics, badgeX, badgeY, width, charSequence, outlineColor, fillColor, textColor);
			badgeX += width + 3;
		}
	}

	public Mod getMod() {
		return mod;
	}
}
