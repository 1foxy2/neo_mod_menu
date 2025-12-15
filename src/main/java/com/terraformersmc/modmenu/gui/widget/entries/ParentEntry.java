package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import net.minecraft.util.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ParentEntry extends ModListEntry {
	private static final Identifier PARENT_MOD_TEXTURE = Identifier.fromNamespaceAndPath(ModMenu.NAMESPACE, "textures/gui/parent_mod.png");
	protected List<Mod> children;
	protected ModListWidget list;
	protected boolean hoveringIcon = false;

	public ParentEntry(Mod parent, List<Mod> children, ModListWidget list) {
		super(parent, list);
		this.children = children;
		this.list = list;
	}

	@Override
	public void renderContent(
		GuiGraphics guiGraphics,
		int mouseX,
		int mouseY,
		boolean isSelected,
		float delta
	) {
		super.renderContent(guiGraphics, mouseX, mouseY, isSelected, delta);
		Font font = client.font;
        int x = this.getContentX() - 2;
        int y = this.getContentY() + this.getYOffset();
		int childrenBadgeHeight = font.lineHeight;
		int childrenBadgeWidth = font.lineHeight;
		int shownChildren = ModSearch.search(list.getParent(), list.getParent().getSearchInput(), getChildren()).size();
		int allChildren = children.stream().filter(child -> !child.isHidden() &&
				(ModMenu.getConfig().SHOW_LIBRARIES.get()
						|| !child.getBadges().contains(ModBadge.LIBRARY))).toList().size();
		Component str = shownChildren == allChildren ?
				Component.literal(String.valueOf(shownChildren)) :
				Component.literal(shownChildren + "/" + allChildren);
		int childrenWidth = font.width(str) - 1;
		if (childrenBadgeWidth < childrenWidth + 4) {
			childrenBadgeWidth = childrenWidth + 4;
		}
		int iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		int childrenBadgeX = x + iconSize - childrenBadgeWidth;
		int childrenBadgeY = y + iconSize - childrenBadgeHeight;
		int childrenOutlineColor = 0xff107454;
		int childrenFillColor = 0xff093929;
		guiGraphics.fill(
				childrenBadgeX + 1,
				childrenBadgeY,
				childrenBadgeX + childrenBadgeWidth - 1,
				childrenBadgeY + 1,
				childrenOutlineColor
		);
		guiGraphics.fill(
				childrenBadgeX,
				childrenBadgeY + 1,
				childrenBadgeX + 1,
				childrenBadgeY + childrenBadgeHeight - 1,
				childrenOutlineColor
		);
		guiGraphics.fill(
				childrenBadgeX + childrenBadgeWidth - 1,
				childrenBadgeY + 1,
				childrenBadgeX + childrenBadgeWidth,
				childrenBadgeY + childrenBadgeHeight - 1,
				childrenOutlineColor
		);
		guiGraphics.fill(
				childrenBadgeX + 1,
				childrenBadgeY + 1,
				childrenBadgeX + childrenBadgeWidth - 1,
				childrenBadgeY + childrenBadgeHeight - 1,
				childrenFillColor
		);
		guiGraphics.fill(
				childrenBadgeX + 1,
				childrenBadgeY + childrenBadgeHeight - 1,
				childrenBadgeX + childrenBadgeWidth - 1,
				childrenBadgeY + childrenBadgeHeight,
				childrenOutlineColor
		);
		guiGraphics.drawString(
				font,
				str.getVisualOrderText(),
				(int) (childrenBadgeX + (float) childrenBadgeWidth / 2 - (float) childrenWidth / 2),
				childrenBadgeY + 1,
				0xFFCACACA,
				false
		);
		this.hoveringIcon = mouseX >= x - 1 && mouseX <= x - 1 + iconSize && mouseY >= y - 1 && mouseY <= y - 1 + iconSize;
		if (isMouseOver(mouseX, mouseY)) {
			guiGraphics.fill(x, y, x + iconSize, y + iconSize, 0xA0909090);
			int xOffset = list.getParent().showModChildren.contains(getMod().getId()) ? iconSize : 0;
			int yOffset = hoveringIcon ? iconSize : 0;
			guiGraphics.blit(
					RenderPipelines.GUI_TEXTURED,
					PARENT_MOD_TEXTURE,
					x,
					y,
					xOffset,
					yOffset,
					iconSize + xOffset,
					iconSize + yOffset,
					ModMenu.getConfig().COMPACT_LIST.get() ?
							(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
							256,
					ModMenu.getConfig().COMPACT_LIST.get() ?
							(int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) :
							256,
					0xFFFFFFFF
			);
		}
	}

    @Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubleClick) {
		int iconSize = ModMenu.getConfig().COMPACT_LIST.get() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		boolean quickConfigure = ModMenu.getConfig().QUICK_CONFIGURE.get();
		if (click.x() - list.getRowLeft() <= iconSize) {
			this.toggleChildren();
			return true;
		} else if (!quickConfigure && Util.getMillis() - this.sinceLastClick < 250) {
			this.toggleChildren();
			return true;
		} else {
			return super.mouseClicked(click, doubleClick);
		}
	}

	private void toggleChildren() {
		String id = getMod().getId();
		if (list.getParent().showModChildren.contains(id)) {
			list.getParent().showModChildren.remove(id);
		} else {
			list.getParent().showModChildren.add(id);
		}

		list.filter(list.getParent().getSearchInput(), false);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		String modId = getMod().getId();
		if (event.isSelection()) {
			if (list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.remove(modId);
			} else {
				list.getParent().showModChildren.add(modId);
			}

			list.filter(list.getParent().getSearchInput(), false);
			return true;
		} else if (event.isLeft()) {
			if (list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.remove(modId);
				list.filter(list.getParent().getSearchInput(), false);
			}

			return true;
		} else if (event.isRight()) {
			if (!list.getParent().showModChildren.contains(modId)) {
				list.getParent().showModChildren.add(modId);
				list.filter(list.getParent().getSearchInput(), false);
				return true;
			} else {
				return list.keyPressed(new KeyEvent(GLFW.GLFW_KEY_DOWN, 0, 0));
			}
		}

		return super.keyPressed(event);
	}

	public void setChildren(List<Mod> children) {
		this.children = children;
	}

	public void addChildren(List<Mod> children) {
		this.children.addAll(children);
	}

	public void addChildren(Mod... children) {
		this.children.addAll(Arrays.asList(children));
	}

	public List<Mod> getChildren() {
		return children;
	}

	@Override
	public boolean isMouseOver(double double_1, double double_2) {
		return Objects.equals(this.list.getEntryAtPos(double_1, double_2), this);
	}
}
