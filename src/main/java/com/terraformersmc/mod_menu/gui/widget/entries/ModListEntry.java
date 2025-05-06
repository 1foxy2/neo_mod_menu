package com.terraformersmc.mod_menu.gui.widget.entries;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.widget.ModListWidget;
import com.terraformersmc.mod_menu.util.DrawingUtil;
import com.terraformersmc.mod_menu.util.ModMenuScreenTexts;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadgeRenderer;
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
import net.minecraft.util.Tuple;

import java.awt.*;

public class ModListEntry extends ObjectSelectionList.Entry<ModListEntry> {
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

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = Minecraft.getInstance();
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

		if (this.getIconTexture().getB().height == this.getIconTexture().getB().width) {
			guiGraphics.blit(
					this.getIconTexture().getA(),
					x, y, 0.0f, 0.0f,
					iconSize, iconSize,
					iconSize, iconSize);
		} else if (this.getSquareIconTexture().getB().height == this.getSquareIconTexture().getB().width) {
			guiGraphics.blit(
					this.getSquareIconTexture().getA(),
					x, y, 0.0f, 0.0f,
					iconSize, iconSize,
					iconSize, iconSize);
		} else {
			guiGraphics.blit(this.getSquareIconTexture().getA(),
					(int) (x + (iconSize - this.getSquareIconTexture().getB().width) / 2f),
					(int) (y + (iconSize - this.getSquareIconTexture().getB().height) / 2f),
					0.0f, 0.0f,
					this.getSquareIconTexture().getB().width, this.getSquareIconTexture().getB().height,
					this.getSquareIconTexture().getB().width, this.getSquareIconTexture().getB().height);
		}


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
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int delta) {
		list.select(this);
		if (ModMenu.getConfig().QUICK_CONFIGURE.get() && this.list.getParent().getModHasConfigScreen(this.mod.getContainer())) {
			int iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (mouseX - list.getRowLeft() <= iconSize) {
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

	public Tuple<ResourceLocation, Dimension> getIconTexture() {
		if (ModMenu.shouldResetCache) {
			this.smallIconLocation = null;
			this.iconLocation = null;
			ModMenu.shouldResetCache = false;
		}

		if (this.iconLocation == null) {
			this.iconLocation = new Tuple<>(ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, mod.getId() + "_icon"), new Dimension());
			Tuple<DynamicTexture, Dimension> icon = mod.getIcon(list.getNeoforgeIconHandler(),
				64 * this.client.options.guiScale().get(), false);


			if (icon != null) {
				float multiplier = 32f / icon.getB().height;
				this.iconLocation.setB(new Dimension(
						(int) (icon.getB().width * multiplier),
						(int) (icon.getB().height * multiplier)));

				this.client.getTextureManager().register(this.iconLocation.getA(), icon.getA());
			} else {
				this.iconLocation.setA(UNKNOWN_ICON);
			}
		}
		return iconLocation;
	}

	public Tuple<ResourceLocation, Dimension> getSquaredIconTexture() {
		Tuple<ResourceLocation, Dimension> icon = new Tuple<>(getIconTexture().getA(), iconLocation.getB().getSize()) ;
		float iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? ModListEntry.COMPACT_ICON_SIZE : ModListEntry.FULL_ICON_SIZE;
		float biggerValue = Math.max(icon.getB().width, icon.getB().height);
		icon.getB().setSize(icon.getB().width / biggerValue * iconSize, icon.getB().height / biggerValue * iconSize);
		return icon;
	}


	public Tuple<ResourceLocation, Dimension> getSquareIconTexture() {
		if (this.smallIconLocation == null) {
			this.smallIconLocation = new Tuple<>(ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, mod.getId() + "_icon_small"), new Dimension());
			Tuple<DynamicTexture, Dimension> icon = mod.getIcon(list.getNeoforgeIconHandler(),
				64 * this.client.options.guiScale().get(), true);
			if (icon != null) {
				this.smallIconLocation.setB(new Dimension());
				this.client.getTextureManager().register(this.smallIconLocation.getA(), icon.getA());
			} else {
				this.smallIconLocation = this.getSquaredIconTexture();
			}
		}
		return smallIconLocation;
	}

	public int getXOffset() {
		return 0;
	}

	@Override
	public String toString() {
		return "ModListEntry{mod_id=\"" + getMod().getId() + "\"}";
	}
}
