package com.terraformersmc.modmenu.gui.widget;

import com.mojang.datafixers.util.Pair;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ParentButton extends Button {
    private final ModsScreen screen;
    static final Identifier ACCEPT_SPRITE = Identifier.withDefaultNamespace("pending_invite/accept");
    static final Identifier REJECT_SPRITE = Identifier.withDefaultNamespace("pending_invite/reject");

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
    protected void extractContents(GuiGraphicsExtractor guiGraphicsExtractor, int i, int i1, float v) {
        Identifier resourcelocation;
        Pair<Mod, List<Mod>> currentParent = ModMenu.CURRENT_PARENT;
        ModListEntry selected = screen.getSelectedEntry();
        if (currentParent != null) {
            if (currentParent.getFirst() == selected.getMod()) {
                resourcelocation = REJECT_SPRITE;
            } else {
                if (currentParent.getSecond().contains(selected.getMod())) {
                    resourcelocation = REJECT_SPRITE;
                } else {
                    resourcelocation = ACCEPT_SPRITE;
                }
            }
        } else {
            resourcelocation = ACCEPT_SPRITE;
        }
        guiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, resourcelocation, this.getX(), this.getY(), this.width, this.height);

    }
}
