package com.terraformersmc.mod_menu.gui.widget.entries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.BadgeScreen;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import com.terraformersmc.mod_menu.gui.widget.ModListWidget;
import com.terraformersmc.mod_menu.util.DrawingUtil;
import com.terraformersmc.mod_menu.util.ImageData;
import com.terraformersmc.mod_menu.util.ModMenuScreenTexts;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadgeRenderer;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeIconHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;

import java.awt.*;
import java.io.Closeable;

public class ModListEntry extends ObjectSelectionList.Entry<ModListEntry> implements Closeable {
	public static final ResourceLocation UNKNOWN_ICON = ResourceLocation.withDefaultNamespace("textures/misc/unknown_pack.png");
	private static final ResourceLocation MOD_CONFIGURATION_ICON = ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID,
		"textures/gui/mod_configuration.png"
	);
	private static final ResourceLocation ERROR_ICON = ResourceLocation.withDefaultNamespace("world_list/error");
	private static final ResourceLocation ERROR_HIGHLIGHTED_ICON = ResourceLocation.withDefaultNamespace("world_list/error_highlighted");

	protected final Minecraft client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Tuple<ResourceLocation, Dimension> iconLocation;
	protected Tuple<ResourceLocation, Dimension> smallIconLocation;
	public static final int FULL_ICON_SIZE = 32;
	public static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;
	public final ImageData iconData;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = Minecraft.getInstance();
		this.iconData = getSquareIconTexture(mod);
	}

	@Override
	public Component getNarration() {
		return Component.literal(mod.getTranslatedName());
	}

	@Override
	public void render(
		GuiGraphics guiGraphics,
		int index,
		int y,
		int x,
		int rowWidth,
		int rowHeight,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();
		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, guiGraphics, x, y, iconSize, iconSize);
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableBlend();

		renderIcon(guiGraphics, x, y, iconSize, delta);

		RenderSystem.disableBlend();
		Component name = Component.literal(mod.getTranslatedName());
		FormattedText trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		Font font = this.client.font;
		if (font.width(name) > maxNameWidth) {
			FormattedText ellipsis = FormattedText.of("...");
			trimmedName = FormattedText.composite(font.substrByWidth(name, maxNameWidth - font.width(ellipsis)),
				ellipsis
			);
		}
		guiGraphics.drawString(font,
			Language.getInstance().getVisualOrder(trimmedName),
			x + iconSize + 3,
			y + 1,
			0xFFFFFF,
			true
		);
		var updateBadgeXOffset = 0;
		if (!ModMenu.getConfig().HIDE_BADGES.get()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.width(name) + 2 + updateBadgeXOffset,
				y,
				x + rowWidth,
				mod,
				list.getParent()
			).draw(guiGraphics);
		}
		if (!ModMenu.getConfig().COMPACT_LIST.get()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(guiGraphics,
				summary,
				(x + iconSize + 3 + 4),
				(y + client.font.lineHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0x808080
			);
		} else {
			DrawingUtil.drawWrappedString(guiGraphics,
				mod.getPrefixedVersion(),
				(x + iconSize + 3),
				(y + client.font.lineHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0x808080
			);
		}

		if (!(this instanceof ParentEntry) && !(this instanceof ChildParentEntry) && ModMenu.getConfig().QUICK_CONFIGURE.get() && (this.list.getParent()
				.getModHasConfigScreen(mod.getContainer()) || this.list.getParent().modScreenErrors.containsKey(modId))) {
			final int textureSize = ModMenu.getConfig().COMPACT_LIST.get() ?
				(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
				256;
			if (this.client.options.touchscreen().get() || hovered) {
				guiGraphics.fill(x, y, x + iconSize, y + iconSize, -1601138544);
				boolean hoveringIcon = mouseX - x < iconSize;
				if (this.list.getParent().modScreenErrors.containsKey(modId)) {
					guiGraphics.blitSprite(hoveringIcon ? ERROR_HIGHLIGHTED_ICON : ERROR_ICON,
						x,
						y,
						iconSize,
						iconSize
					);
					if (hoveringIcon) {
						Throwable e = this.list.getParent().modScreenErrors.get(modId);
						this.list.getParent()
							.setTooltipForNextRenderPass(this.client.font.split(
								ModMenuScreenTexts.configureError(modId, e),
								175
							));
					}
				} else {
					int v = hoveringIcon ? iconSize : 0;
					guiGraphics.blit(MOD_CONFIGURATION_ICON,
						x,
						y,
						0.0F,
						(float) v,
						iconSize,
						iconSize,
						textureSize,
						textureSize
					);
				}
			}
		}
		if (ModMenu.getConfig().EDITOR_MODE.get() && hovered) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 200);
			guiGraphics.blitSprite(
					x + rowWidth - iconSize < mouseX ? ModsScreen.BADGE_BUTTON_SPRITES.enabledFocused() : ModsScreen.BADGE_BUTTON_SPRITES.enabled(),
					x + rowWidth - iconSize,
					y,
					iconSize,
					iconSize
			);
			guiGraphics.pose().popPose();
		}
	}

	public void renderIcon(GuiGraphics guiGraphics, int x, int y, int iconSize, float partialTicks) {
		renderIcon(guiGraphics, x, y, iconSize, partialTicks, iconData);
	}

	public void renderIcon(GuiGraphics guiGraphics, int x, int y, int iconSize, float partialTicks, ImageData iconData) {
		if (iconData.height() == iconData.width()) {
			guiGraphics.blit(
					iconData.sprite(),
					x, y, 0.0f, 0.0f,
					iconSize, iconSize,
					iconSize, iconSize
			);
		} else {
			guiGraphics.blit(iconData.sprite(),
					(int) (x + (iconSize - iconData.width()) / 2f),
					(int) (y + (iconSize - iconData.height()) / 2f),
					0.0f, 0.0f,
					iconData.width(), iconData.height(),
					iconData.width(), iconData.height()
			);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int delta) {
		list.select(this);
		int iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		if (ModMenu.getConfig().EDITOR_MODE.get() && mouseX - list.getRowLeft() > list.getRowWidth() - iconSize) {
			this.client.pushGuiLayer(new BadgeScreen(
					mod,
					list.getRowLeft() + list.getRowWidth() - iconSize,
					list.getRowTop(list.getIndexAtY(mouseY)) + 2,
					iconSize
			));
		}
		if (ModMenu.getConfig().QUICK_CONFIGURE.get() && this.list.getParent().getModHasConfigScreen(this.mod.getContainer())) {
			if (mouseX - list.getRowLeft() <= iconSize + getXOffset()) {
				this.openConfig();
			} else if (Util.getMillis() - this.sinceLastClick < 250) {
				this.openConfig();
			}
		}
		this.sinceLastClick = Util.getMillis();
		return true;
	}

	public void openConfig() {
		mod.getContainer().ifPresent(container ->
				this.list.getParent().safelyOpenConfigScreen(container));

	}

	public Mod getMod() {
		return mod;
	}

	public ImageData getBannerTexture(Mod mod) {
		ImageData icon = NeoforgeIconHandler.createIcon(mod, false);

		float multiplier = 32f / icon.height();
		return new ImageData(icon.sprite(),
				(int) (icon.width() * multiplier),
				(int) (icon.height() * multiplier), icon.unknown());
	}

	public ImageData getSquareIconTexture(Mod mod) {
		ImageData icon = NeoforgeIconHandler.createIcon(mod, true);
		if (icon.width() == icon.height()) {
			return icon;
		} else {
			float multiplier = 32f / icon.height();
			float iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? ModListEntry.COMPACT_ICON_SIZE : ModListEntry.FULL_ICON_SIZE;
			float biggerValue = Math.max(icon.width(), icon.height()) * multiplier;
			return new ImageData(icon.sprite(),
					(int) (icon.width() * multiplier / biggerValue * iconSize),
					(int) (icon.height() * multiplier / biggerValue * iconSize), icon.unknown());
		}
	}

	public int getXOffset() {
		return 0;
	}

	@Override
	public String toString() {
		return "ModListEntry{mod_id=\"" + getMod().getId() + "\"}";
	}

	@Override
	public void close() {
		list.getParent().getMinecraft().getTextureManager().release(iconData.sprite());
	}
}
