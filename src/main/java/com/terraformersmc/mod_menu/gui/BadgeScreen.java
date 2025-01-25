package com.terraformersmc.mod_menu.gui;

import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.config.ModMenuConfig;
import com.terraformersmc.mod_menu.gui.widget.BadgeToogleButton;
import com.terraformersmc.mod_menu.gui.widget.LegacyTexturedButtonWidget;
import com.terraformersmc.mod_menu.util.DrawingUtil;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadge;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
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
        this.badgeButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(CommonComponents.EMPTY, button ->
                        this.onClose())
                .position(posX, 22)
                .size(20, 20)
                .uv(0, 0, 20)
                .texture(ModsScreen.BADGE_BUTTON_LOCATION, 32, 64)
                .build();
        this.addRenderableWidget(badgeButton);

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
                        badge.getOutlineColor(), badge.getFillColor(), badge.getTextColor());
                i++;
            }
        }
    }
}
