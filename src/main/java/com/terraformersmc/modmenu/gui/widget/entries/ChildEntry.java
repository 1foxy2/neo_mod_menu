package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
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
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.renderContent(guiGraphics, mouseX, mouseY, isSelected, delta);
        int x = this.getContentX() - 15;
        int y = this.getContentY() + this.getYOffset();
//		int rowWidth = this.getContentWidth();
        int rowHeight = this.getContentHeight();
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
	public boolean keyPressed(KeyEvent event) {
		if (event.isLeft()) {
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
