package com.terraformersmc.mod_menu.gui;

import com.mojang.logging.LogUtils;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.config.ModMenuConfig;
import com.terraformersmc.mod_menu.gui.widget.BadgeToogleButton;
import com.terraformersmc.mod_menu.gui.widget.LegacyTexturedButtonWidget;
import com.terraformersmc.mod_menu.util.DrawingUtil;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class BadgeScreen extends Screen {
    private @Nullable AbstractWidget badgeButton;
    private final Mod mod;
    private final int posX;
    private final int posY;
    private final int iconSize;
    private boolean inverted;

    public BadgeScreen(Mod mod, int posX, int posY, int iconSize) {
        super(CommonComponents.EMPTY);

        this.mod = mod;
        this.posX = posX;
        this.posY = posY;
        this.iconSize = iconSize;
    }

    @Override
    public void onClose() {
        ModMenu.getConfig().save();
        super.onClose();
    }

    @Override
    protected void init() {
        this.badgeButton = new ImageButton(
                posX, posY, iconSize, iconSize, ModsScreen.BADGE_BUTTON_SPRITES, button ->
                this.onClose()
        );
        this.addRenderableWidget(badgeButton);
        int totalBadges = ModBadge.CUSTOM_BADGES.size() + ModBadge.DEFAULT_BADGES.size();
        inverted = posY + iconSize + 2 + 11 * totalBadges > height;
        int i = 0;
        final int buttonX = posX - 11;
        for (Map<String, ModBadge> badgeMap : ModBadge.BADGES) {
            for (Map.Entry<String, ModBadge> badgeEntry : badgeMap.entrySet()) {
                ModBadge badge = badgeEntry.getValue();
                this.addRenderableWidget(BadgeToogleButton.badgeButtonBuilder(CommonComponents.EMPTY, button -> {
                            ModMenuConfig config = ModMenu.getConfig();
                            if (mod.getBadges().contains(badge)) {
                                mod.getBadges().remove(badge);
                                config.mod_badges.get(mod.getId()).remove(badgeEntry.getKey());

                                if (mod.getBadgeNames().contains(badgeEntry.getKey())) {
                                    config.disabled_mod_badges.computeIfAbsent(mod.getId(),
                                            v -> new LinkedHashSet<>()).add(badgeEntry.getKey());
                                }
                            } else {
                                mod.getBadges().add(badge);

                                Set<String> disabled_badges = config.disabled_mod_badges.get(mod.getId());
                                if (disabled_badges != null && disabled_badges.contains(badgeEntry.getKey())) {
                                    disabled_badges.remove(badgeEntry.getKey());
                                } else {
                                    config.mod_badges.get(mod.getId()).add(badgeEntry.getKey());
                                }
                            }
                            ((BadgeToogleButton) button).toggle();
                        }, mod.getBadges().contains(badge))
                        .position(buttonX, getYForIndex(i) - 1)
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
                DrawingUtil.drawBadge(guiGraphics, posX, getYForIndex(i), badgeWidth,
                        badge.getComponent().getVisualOrderText(),
                        badge.getOutlineColor(), badge.getFillColor(), badge.getTextColor());
                i++;
            }
        }
    }

    public int getYForIndex(int i) {
        return posY + (inverted ? -11 : iconSize + 2) + 11 * (inverted ? -i : i);
    }
}
