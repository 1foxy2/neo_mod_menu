package com.terraformersmc.mod_menu.gui.widget.entries;

import com.terraformersmc.mod_menu.gui.widget.ModListWidget;
import com.terraformersmc.mod_menu.util.mod.Mod;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ChildEntry extends ModListEntry {
	protected final boolean bottomChild;
	protected final ParentEntry parent;
	protected final List<ModListEntry> parents;

	public ChildEntry(Mod mod, ParentEntry parent, List<ModListEntry> parents, ModListWidget list, boolean bottomChild) {
		super(mod, list);
		this.bottomChild = bottomChild;
		this.parent = parent;
		this.parents = parents;
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
		x -= 9;
		int color = 0xFFA0A0A0;
		for (int i = 1; i < parents.size(); i++) {
			if (parents.get(i) instanceof ChildParentEntry childParent) {
				if (!childParent.bottomChild) {
					guiGraphics.fill(x + childParent.getXOffset(), y - 2, x + 1 + childParent.getXOffset(), y + rowHeight + 2, color);
				}
			}
		}
		x += getXOffset();
		guiGraphics.fill(x, y - 2, x + 1, y + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);
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
		return 13 * parents.size();
	}
}
