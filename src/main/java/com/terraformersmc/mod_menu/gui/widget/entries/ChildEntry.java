package com.terraformersmc.mod_menu.gui.widget.entries;

import com.terraformersmc.mod_menu.gui.widget.ModListWidget;
import com.terraformersmc.mod_menu.util.mod.Mod;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public class ChildEntry extends ModListEntry {
	private final boolean bottomChild;
	private final ParentEntry parent;
	private final int parentCount;

	public ChildEntry(Mod mod, ParentEntry parent, ModListWidget list, boolean bottomChild, int parentCount) {
		super(mod, list);
		this.bottomChild = bottomChild;
		this.parent = parent;
		this.parentCount = parentCount;
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
		boolean isSelected,
		float delta
	) {
		super.render(guiGraphics, index, y, x, rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		x += 4 + getXOffset() - 13;
		int color = 0xFFA0A0A0;
		int previousIndex = index - 1;
		int minYOffset = 0;
		while (previousIndex > 0) {
			int entryXOffset = list.getEntry(previousIndex).getXOffset();
			if (entryXOffset == getXOffset()) {
				minYOffset = y - list.getRowBottom(previousIndex);
				break;
			}
			if (entryXOffset == 0) {
				break;
			}
			previousIndex--;
		}
		guiGraphics.fill(x, y - 2 - minYOffset, x + 1, y + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);
		guiGraphics.fill(x, y + rowHeight / 2, x + 7, y + rowHeight / 2 + 1, color);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_LEFT) {
			list.setSelected(parent);
			list.ensureVisible(parent);
			return true;
		}
		return false;
	}

	@Override
	public int getXOffset() {
		return 13 * parentCount;
	}
}
