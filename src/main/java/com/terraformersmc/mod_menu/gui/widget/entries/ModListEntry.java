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
import net.neoforged.fml.ModList;

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
	protected ResourceLocation iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
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

		guiGraphics.blit(this.getIconTexture(), x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
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
			).draw(guiGraphics, mouseX, mouseY);
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

		if (!(this instanceof ParentEntry) && ModMenu.getConfig().QUICK_CONFIGURE.get() && (this.list.getParent()
			.getModHasConfigScreen()
			.get(modId) || this.list.getParent().modScreenErrors.containsKey(modId))) {
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
		if (ModMenu.getConfig().QUICK_CONFIGURE.get() && this.list.getParent()
			.getModHasConfigScreen()
			.get(this.mod.getId())) {
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
		Minecraft.getInstance().setScreen(ModMenu.getConfigScreen(mod.getContainer().get(), list.getParent()));
	}

	public Mod getMod() {
		return mod;
	}

	public ResourceLocation getIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, mod.getId() + "_icon");
			DynamicTexture icon = mod.getIcon(list.getNeoforgeIconHandler(),
				64 * this.client.options.guiScale().get()
			);
			if (icon != null) {
				this.client.getTextureManager().register(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		return iconLocation;
	}

	public int getXOffset() {
		return 0;
	}
}
