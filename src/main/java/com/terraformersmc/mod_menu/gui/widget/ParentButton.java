package com.terraformersmc.mod_menu.gui.widget;

import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import com.terraformersmc.mod_menu.gui.widget.entries.ModListEntry;
import com.terraformersmc.mod_menu.util.mod.Mod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ParentButton extends Button {
    private final ModsScreen screen;
    static final ResourceLocation ACCEPT_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/accept");
    static final ResourceLocation REJECT_SPRITE = ResourceLocation.withDefaultNamespace("pending_invite/reject");

    public ParentButton(
            int x,
            int y,
            int width,
            int height,
            Button.OnPress onPress,
            ModsScreen screen) {
        super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.screen = screen;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        ResourceLocation resourcelocation;
        Pair<Mod, List<Mod>> currentParent = ModMenu.CURRENT_PARENT;
        ModListEntry selected = screen.getSelectedEntry();
        if (currentParent != null) {
            if (currentParent.getLeft() == selected.getMod()) {
                resourcelocation = REJECT_SPRITE;
            } else {
                if (currentParent.getRight().contains(selected.getMod())) {
                    resourcelocation = REJECT_SPRITE;
                } else {
                    resourcelocation = ACCEPT_SPRITE;
                }
            }
        } else {
            resourcelocation = ACCEPT_SPRITE;
        }
        guiGraphics.blitSprite(resourcelocation, this.getX(), this.getY(), this.width, this.height);
    }
}
