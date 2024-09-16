package com.terraformersmc.mod_menu.gui;

import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.widget.BadgeToogleButton;
import com.terraformersmc.mod_menu.util.DrawingUtil;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;

public class BadgeScreen extends Screen {
    private @Nullable AbstractWidget badgeButton;
    private final Mod mod;
    private final int posX;

    protected BadgeScreen(Mod mod, int paneWidth, int searchBoxWidth) {
        super(CommonComponents.EMPTY);

        this.mod = mod;
        this.posX = paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 26;
    }

    @Override
    public void onClose() {
        ModMenu.getConfig().save();
        super.onClose();
    }

    @Override
    protected void init() {
        this.badgeButton = new ImageButton(posX, 22, 20, 20, 0, 0, 20, ModsScreen.BADGE_BUTTON_LOCATION, 32, 64, button ->
                        this.onClose(), CommonComponents.EMPTY);
        this.addRenderableWidget(badgeButton);

        int i = 0;
        final int buttonX = posX - 11;
        for (Map<String, ModBadge> badgeMap : ModBadge.BADGES) {
            for (Map.Entry<String, ModBadge> badgeEntry : badgeMap.entrySet()) {
                ModBadge badge = badgeEntry.getValue();
                this.addRenderableWidget(BadgeToogleButton.badgeButtonBuilder(CommonComponents.EMPTY, button -> {
                    ModMenu.LOGGER.warn(String.valueOf(ModMenu.getConfig().mod_badges));
                            if (mod.getBadges().contains(badge)) {
                                mod.getBadges().remove(badge);
                                ModMenu.getConfig().mod_badges.get(mod.getId()).remove(badgeEntry.getKey());
                            } else {
                                mod.getBadges().add(badge);
                                if (!ModMenu.getConfig().mod_badges.containsKey(mod.getId()))
                                    ModMenu.getConfig().mod_badges.put(mod.getId(), new HashSet<>());
                                ModMenu.getConfig().mod_badges.get(mod.getId()).add(badgeEntry.getKey());
                            }
                            ((BadgeToogleButton) button).toggle();
                        }, mod.getBadges().contains(badge))
                        .position(buttonX, 43 + 11 * i)
                        .size(11, 11)
                        .uv(0, 0, 11)
                        .build());
                i++;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int i = 0;
        ModBadge badge;
        for (Map<String, ModBadge> badges : ModBadge.BADGES) {
            for (Map.Entry<String, ModBadge> mapEntry : badges.entrySet()) {
                badge = mapEntry.getValue();
                int badgeWidth = minecraft.font.width(badge.getComponent().getVisualOrderText()) + 6;
                DrawingUtil.drawBadge(guiGraphics, posX, 43 + 11 * i, badgeWidth,
                        badge.getComponent().getVisualOrderText(),
                        badge.getOutlineColor(), badge.getFillColor(), 0xCACACA);
                i++;
            }
        }
    }
}
