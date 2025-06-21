package com.terraformersmc.modmenu.util.mod;

import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Collectors;

public class ModBadge {
    private final String id;
    private final Component component;
    private final int fillColor;
    private final int outlineColor;
    private final int textColor;
    public static ModBadge LIBRARY = new ModBadge("library", "modmenu.badge.library", 0xFF107454, 0xFF093929);
    public static Map<String, ModBadge> DEFAULT_BADGES = Map.of(
            "library", LIBRARY,
            "client", new ModBadge("client", "modmenu.badge.clientsideOnly", 0xFF2b4b7c, 0xFF0e2a55),
            "deprecated", new ModBadge("deprecated", "modmenu.badge.deprecated", 0xFF841426, 0xFF530C17),
            "sinytra_fabric", new ModBadge("sinytra_fabric", "modmenu.badge.fabric", 0xFFc7b48b, 0xFF786d58),
            "sinytra_neoforge", new ModBadge("sinytra_neoforge", "modmenu.badge.neoforge", 0xFFe68c37, 0xFFa44e37),
            "modpack", new ModBadge("modpack", "modmenu.badge.modpack", 0xFF7a2b7c, 0xFF510d54),
            "minecraft", new ModBadge("minecraft", "modmenu.badge.minecraft", 0xFF6f6c6a, 0xFF31302f)
    );
    public static Map<String, ModBadge> CUSTOM_BADGES = new LinkedHashMap<>();
    public static List<Map<String, ModBadge>> BADGES = List.of(DEFAULT_BADGES, CUSTOM_BADGES);

    public ModBadge(String id, String displayName, int outlineColor, int fillColor) {
        this(id, displayName, outlineColor, fillColor, 0xFFCACACA);
    }

    public ModBadge(String id, String displayName, int outlineColor, int fillColor, int textColor) {
        this.id = id;
        this.component = Component.translatable(displayName);
        this.fillColor = fillColor;
        this.outlineColor = outlineColor;
        this.textColor = textColor;
    }

    public String getId() {
        return id;
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
