package com.terraformersmc.modmenu.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class DrawingUtil {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void drawRandomVersionBackground(
		Mod mod,
		GuiGraphics guiGraphics,
		int x,
		int y,
		int width,
		int height
	) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		Random random = new Random(seed);
		int color = 0xFF000000 | Mth.hsvToRgb(random.nextFloat(1f), random.nextFloat(0.7f, 0.8f), 0.9f);
		if (!ModMenu.getConfig().RANDOM_JAVA_COLORS.get()) {
			color = 0xFFDD5656;
		}

		guiGraphics.fill(x, y, x + width, y + height, color);
	}

	public static void drawWrappedString(
		GuiGraphics guiGraphics,
		String string,
		int x,
		int y,
		int wrapWidth,
		int lines,
		int color
	) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<FormattedText> strings = CLIENT.font.getSplitter()
			.splitLines(Component.literal(string), wrapWidth, Style.EMPTY);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			FormattedText renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable = FormattedText.composite(strings.get(i), FormattedText.of("..."));
			}
			FormattedCharSequence line = Language.getInstance().getVisualOrder(renderable);
			int x1 = x;
			if (CLIENT.font.isBidirectional()) {
				int width = CLIENT.font.width(line);
				x1 += (float) (wrapWidth - width);
			}
			guiGraphics.drawString(CLIENT.font, line, x1, y + i * CLIENT.font.lineHeight, color);
		}
	}

	public static void drawBadge(
		GuiGraphics guiGraphics,
		int x,
		int y,
		int tagWidth,
		FormattedCharSequence charSequence,
		int outlineColor,
		int fillColor,
		int textColor
	) {
		guiGraphics.fill(x + 1, y - 1, x + tagWidth, y, outlineColor);
		guiGraphics.fill(x, y, x + 1, y + CLIENT.font.lineHeight, outlineColor);
		guiGraphics.fill(x + 1,
			y + 1 + CLIENT.font.lineHeight - 1,
			x + tagWidth,
			y + CLIENT.font.lineHeight + 1,
			outlineColor
		);
		guiGraphics.fill(x + tagWidth, y, x + tagWidth + 1, y + CLIENT.font.lineHeight, outlineColor);
		guiGraphics.fill(x + 1, y, x + tagWidth, y + CLIENT.font.lineHeight, fillColor);
		guiGraphics.drawString(CLIENT.font,
			charSequence,
			(int) (x + 1 + (tagWidth - CLIENT.font.width(charSequence)) / (float) 2),
			y + 1,
			textColor,
			false
		);
	}
}
