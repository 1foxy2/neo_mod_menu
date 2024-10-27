package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Collectors;

public class ModBadge {
    private final Component component;
    private final int fillColor;
    private final int outlineColor;
    private final int textColor;
    public static ModBadge LIBRARY = new ModBadge("modmenu.badge.library", 0xff107454, 0xff093929);
    public static Map<String, ModBadge> DEFAULT_BADGES = Map.of(
            "library", LIBRARY,
            "client", new ModBadge("modmenu.badge.clientsideOnly", 0xff2b4b7c, 0xff0e2a55),
            "deprecated", new ModBadge("modmenu.badge.deprecated", 0xff841426, 0xff530C17),
            "sinytra_fabric", new ModBadge("modmenu.badge.fabric", 0xffc7b48b, 0xff786d58),
            "sinytra_neoforge", new ModBadge("modmenu.badge.neoforge", 0xffff7C26, 0xffd85600),
            "modpack", new ModBadge("modmenu.badge.modpack", 0xff7a2b7c, 0xff510d54),
            "minecraft", new ModBadge("modmenu.badge.minecraft", 0xff6f6c6a, 0xff31302f)
    );
    public static Map<String, ModBadge> CUSTOM_BADGES = new LinkedHashMap<>();
    public static List<Map<String, ModBadge>> BADGES = List.of(DEFAULT_BADGES, CUSTOM_BADGES);

    public ModBadge(String displayName, int outlineColor, int fillColor) {
        this(displayName, outlineColor, fillColor, 0);
    }

    public ModBadge(String displayName, int outlineColor, int fillColor, int textColor) {
        this.component = Component.translatable(displayName);
        this.fillColor = fillColor;
        this.outlineColor = outlineColor;
        this.textColor = textColor == 0 ? 0xCACACA : textColor;
    }

    public Component getComponent() {
        return this.component;
    }

    public int getOutlineColor() {
        return this.outlineColor;
    }

    public int getFillColor() {
        return this.fillColor;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public static Set<ModBadge> convert(Set<String> badgeKeys, String modId) {
        return badgeKeys.stream().map(key -> {
            if (DEFAULT_BADGES.containsKey(key)) {
                return DEFAULT_BADGES.get(key);
            }
            if (CUSTOM_BADGES.containsKey(key)) {
                return CUSTOM_BADGES.get(key);
            }

            ModMenu.LOGGER.warn("Skipping unknown badge key '{}' specified by mod '{}'", key, modId);
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
