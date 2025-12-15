package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Tuple;

import java.awt.*;

public class ModListEntry extends ObjectSelectionList.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = Identifier.withDefaultNamespace("textures/misc/unknown_pack.png");
	private static final Identifier MOD_CONFIGURATION_ICON = Identifier.fromNamespaceAndPath(ModMenu.NAMESPACE,
		"textures/gui/mod_configuration.png"
	);
	private static final Identifier ERROR_ICON = Identifier.withDefaultNamespace("world_list/error");
	private static final Identifier ERROR_HIGHLIGHTED_ICON = Identifier.withDefaultNamespace("world_list/error_highlighted");

	protected final Minecraft client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Tuple<Identifier, Dimension> iconLocation;
	protected Tuple<Identifier, Dimension> smallIconLocation;
	public static final int FULL_ICON_SIZE = 32;
	public static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;
    protected int yOffset = 0;

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
	public void renderContent(
		GuiGraphics guiGraphics,
		int mouseX,
		int mouseY,
		boolean hovered,
		float delta
	) {
        int x = this.getX() + this.getXOffset();
        int y = this.getContentY() + this.getYOffset();
        int rowWidth = this.getContentWidth();
		rowWidth -= getXOffset();
		int iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();

		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, guiGraphics, x, y, iconSize, iconSize);
		}

		if (this.getIconTexture().getB().height == this.getIconTexture().getB().width) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
					this.getIconTexture().getA(),
					x, y, 0.0f, 0.0f,
					iconSize, iconSize,
					iconSize, iconSize,
					ARGB.white(1.0F));
		} else if (this.getSquareIconTexture().getB().height == this.getSquareIconTexture().getB().width) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
					this.getSquareIconTexture().getA(),
					x, y, 0.0f, 0.0f,
					iconSize, iconSize,
					iconSize, iconSize,
					ARGB.white(1.0F));
		} else {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.getSquareIconTexture().getA(),
					(int) (x + (iconSize - this.getSquareIconTexture().getB().width) / 2f),
					(int) (y + (iconSize - this.getSquareIconTexture().getB().height) / 2f),
					0.0f, 0.0f,
					this.getSquareIconTexture().getB().width, this.getSquareIconTexture().getB().height,
					this.getSquareIconTexture().getB().width, this.getSquareIconTexture().getB().height,
					ARGB.white(1.0F));
		}

		Component name = Component.literal(mod.getTranslatedName());
		FormattedText trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		Font font = this.client.font;
		if (font.width(name) > maxNameWidth) {
			FormattedText ellipsis = FormattedText.of("...");
			trimmedName = FormattedText.composite(font.substrByWidth(name,
							maxNameWidth - font.width(ellipsis)), ellipsis
			);
		}

		guiGraphics.drawString(font,
			Language.getInstance().getVisualOrder(trimmedName),
			x + iconSize + 3,
			y + 1,
			0xFFFFFFFF
		);

		if (!ModMenu.getConfig().HIDE_BADGES.get()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.width(name) + 2,
				y,
				x + rowWidth,
				mod,
				list.getParent()
			).draw(guiGraphics);
		}

		if (!ModMenu.getConfig().COMPACT_LIST.get()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(
				guiGraphics,
				summary,
				(x + iconSize + 3 + 4),
				(y + client.font.lineHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0xFF808080
			);
		} else {
			DrawingUtil.drawWrappedString(
				guiGraphics,
				mod.getPrefixedVersion(),
				(x + iconSize + 3),
				(y + client.font.lineHeight + 2),
				rowWidth - iconSize - 7,
				2,
				0xFF808080
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
					guiGraphics.blitSprite(
						RenderPipelines.GUI_TEXTURED,
						hoveringIcon ? ERROR_HIGHLIGHTED_ICON : ERROR_ICON,
						x,
						y,
						iconSize,
						iconSize
					);
					if (hoveringIcon) {
						Throwable e = this.list.getParent().modScreenErrors.get(modId);
						guiGraphics.setTooltipForNextFrame(this.client.font.split(
								ModMenuScreenTexts.configureError(modId, e),
								175
						), mouseX, mouseY);
					}
				} else {
					int v = hoveringIcon ? iconSize : 0;
					guiGraphics.blit(
						RenderPipelines.GUI_TEXTURED,
						MOD_CONFIGURATION_ICON,
						x,
						y,
						0.0F,
						(float) v,
						iconSize,
						iconSize,
						textureSize,
						textureSize,
						ARGB.white(1.0F)
					);
				}
                if (hoveringIcon) {
                    guiGraphics.requestCursor(this.shouldTakeFocusAfterInteraction() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
                }
			}
		}
	}

    @Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
		list.select(this);
		if (ModMenu.getConfig().QUICK_CONFIGURE.get() && this.list.getParent().getModHasConfigScreen(this.mod.getContainer())) {
			int iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (click.x() - list.getRowLeft() <= iconSize + getXOffset()) {
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

	public Tuple<Identifier, Dimension> getIconTexture() {
		if (ModMenu.shouldResetCache) {
			this.smallIconLocation = null;
			this.iconLocation = null;
			ModMenu.shouldResetCache = false;
		}

		if (this.iconLocation == null) {
			this.iconLocation = new Tuple<>(Identifier.fromNamespaceAndPath(ModMenu.NAMESPACE, mod.getId() + "_icon"), new Dimension());
			Tuple<DynamicTexture, Dimension> icon = mod.getIcon(list.getNeoforgeIconHandler(),
					64 * this.client.options.guiScale().get(), false);

			float multiplier = 32f / icon.getB().height;
			this.iconLocation.setB(new Dimension(
					(int) (icon.getB().width * multiplier),
					(int) (icon.getB().height * multiplier)));

			this.client.getTextureManager().register(this.iconLocation.getA(), icon.getA());
		}
		return iconLocation;
	}

	public Tuple<Identifier, Dimension> getSquaredIconTexture() {
		Tuple<Identifier, Dimension> icon = new Tuple<>(getIconTexture().getA(), iconLocation.getB().getSize()) ;
		float iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? ModListEntry.COMPACT_ICON_SIZE : ModListEntry.FULL_ICON_SIZE;
		float biggerValue = Math.max(icon.getB().width, icon.getB().height);
		icon.getB().setSize(icon.getB().width / biggerValue * iconSize, icon.getB().height / biggerValue * iconSize);
		return icon;
	}


	public Tuple<Identifier, Dimension> getSquareIconTexture() {
		if (this.smallIconLocation == null) {
			this.smallIconLocation = new Tuple<>(Identifier.fromNamespaceAndPath(ModMenu.NAMESPACE, mod.getId() + "_icon_small"), new Dimension());
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

    public void setYOffset(int offset) {
        this.yOffset = offset;
    }

    public int getYOffset() {
        return this.yOffset;
    }
}
