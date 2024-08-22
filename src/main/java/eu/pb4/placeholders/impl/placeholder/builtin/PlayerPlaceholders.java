package eu.pb4.placeholders.impl.placeholder.builtin;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.impl.GeneralUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import org.apache.commons.lang3.time.DurationFormatUtils;


public class PlayerPlaceholders {
    public static void register() {
        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "name"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(ctx.player().getName());
            } else if (ctx.hasGameProfile()) {
                return PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "name_visual"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(GeneralUtils.removeHoverAndClick(ctx.player().getName()));
            } else if (ctx.hasGameProfile()) {
                return PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "name_unformatted"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(ctx.player().getName().getString());
            } else if (ctx.hasGameProfile()) {
                return PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "ping"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(String.valueOf(ctx.player().connection.latency()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "ping_colored"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                int x = ctx.player().connection.latency();
                return PlaceholderResult.value(Component.literal(String.valueOf(x)).withStyle(x < 100 ? ChatFormatting.GREEN : x < 200 ? ChatFormatting.GOLD : ChatFormatting.RED));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "displayname"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(ctx.player().getDisplayName());
            } else if (ctx.hasGameProfile()) {
                return PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "display_name"), Placeholders.getPlaceholders().get(ResourceLocation.fromNamespaceAndPath("player", "displayname")));

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "displayname_visual"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(GeneralUtils.removeHoverAndClick(ctx.player().getDisplayName()));
            } else if (ctx.hasGameProfile()) {
                return PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "display_name_visual"), Placeholders.getPlaceholders().get(ResourceLocation.fromNamespaceAndPath("player", "displayname_visual")));

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "displayname_unformatted"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(Component.literal(ctx.player().getDisplayName().getString()));
            } else if (ctx.hasGameProfile()) {
                return PlaceholderResult.value(Component.nullToEmpty(ctx.gameProfile().getName()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });
        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "display_name_unformatted"), Placeholders.getPlaceholders().get(ResourceLocation.fromNamespaceAndPath("player", "displayname_unformatted")));

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "inventory_slot"), (ctx, arg) -> {
            if (ctx.hasPlayer() && arg != null) {
                try {
                    int slot = Integer.parseInt(arg);

                    var inventory = ctx.player().getInventory();

                    if (slot >= 0 && slot < inventory.getContainerSize()) {
                        var stack = inventory.getItem(slot);

                        return PlaceholderResult.value(GeneralUtils.getItemText(stack, true));
                    }

                } catch (Exception e) {
                    // noop
                }
                return PlaceholderResult.invalid("Invalid argument");
            } else {
                return PlaceholderResult.invalid("No player or invalid argument!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "inventory_slot_no_rarity"), (ctx, arg) -> {
            if (ctx.hasPlayer() && arg != null) {
                try {
                    int slot = Integer.parseInt(arg);

                    var inventory = ctx.player().getInventory();

                    if (slot >= 0 && slot < inventory.getContainerSize()) {
                        var stack = inventory.getItem(slot);

                        return PlaceholderResult.value(GeneralUtils.getItemText(stack, false));
                    }

                } catch (Exception e) {
                    // noop
                }
                return PlaceholderResult.invalid("Invalid argument");
            } else {
                return PlaceholderResult.invalid("No player or invalid argument!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "equipment_slot"), (ctx, arg) -> {
            if (ctx.hasPlayer() && arg != null) {
                try {
                    var slot = EquipmentSlot.byName(arg);

                    var stack = ctx.player().getItemBySlot(slot);
                    return PlaceholderResult.value(GeneralUtils.getItemText(stack, true));
                } catch (Exception e) {
                    // noop
                }
                return PlaceholderResult.invalid("Invalid argument");
            } else {
                return PlaceholderResult.invalid("No player or invalid argument!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "equipment_slot_no_rarity"), (ctx, arg) -> {
            if (ctx.hasPlayer() && arg != null) {
                try {
                    var slot = EquipmentSlot.byName(arg);

                    var stack = ctx.player().getItemBySlot(slot);
                    return PlaceholderResult.value(GeneralUtils.getItemText(stack, false));
                } catch (Exception e) {
                    // noop
                }
                return PlaceholderResult.invalid("Invalid argument");
            } else {
                return PlaceholderResult.invalid("No player or invalid argument!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "playtime"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                int x = ctx.player().getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
                return PlaceholderResult.value(arg != null
                        ? DurationFormatUtils.formatDuration((long) x * 50, arg, true)
                        : GeneralUtils.durationToString((long) x / 20)
                );
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "statistic"), (ctx, arg) -> {
            if (ctx.hasPlayer() && arg != null) {
                try {
                    var args = arg.split(" ");

                    if (args.length == 1) {
                        var identifier = ResourceLocation.tryParse(args[0]);
                        if (identifier != null) {
                            var stat = Stats.CUSTOM.get(BuiltInRegistries.CUSTOM_STAT.get(identifier));
                            int x = ctx.player().getStats().getValue(stat);
                            return PlaceholderResult.value(stat.format(x));
                        }
                    } else if (args.length >= 2) {
                        var type = ResourceLocation.tryParse(args[0]);
                        var id = ResourceLocation.tryParse(args[1]);
                        if (type != null) {
                            var statType = (StatType<Object>) BuiltInRegistries.STAT_TYPE.get(type);

                            if (statType != null) {
                                var key = statType.getRegistry().get(id);
                                if (key != null) {
                                    var stat = statType.get(key);
                                    int x = ctx.player().getStats().getValue(stat);
                                    return PlaceholderResult.value(stat.format(x));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    /* Into the void you go! */
                }
                return PlaceholderResult.invalid("Invalid statistic!");
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "statistic_raw"), (ctx, arg) -> {
            if (ctx.hasPlayer() && arg != null) {
                try {
                    var args = arg.split(" ");

                    if (args.length == 1) {
                        var identifier = ResourceLocation.tryParse(args[0]);
                        if (identifier != null) {
                            var stat = Stats.CUSTOM.get(BuiltInRegistries.CUSTOM_STAT.get(identifier));
                            int x = ctx.player().getStats().getValue(stat);
                            return PlaceholderResult.value(String.valueOf(x));
                        }
                    } else if (args.length >= 2) {
                        var type = ResourceLocation.tryParse(args[0]);
                        var id = ResourceLocation.tryParse(args[1]);
                        if (type != null) {
                            var statType = (StatType<Object>) BuiltInRegistries.STAT_TYPE.get(type);

                            if (statType != null) {
                                var key = statType.getRegistry().get(id);
                                if (key != null) {
                                    var stat = statType.get(key);
                                    int x = ctx.player().getStats().getValue(stat);
                                    return PlaceholderResult.value(String.valueOf(x));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    /* Into the void you go! */
                }
                return PlaceholderResult.invalid("Invalid statistic!");
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "objective"), (ctx, arg) -> {
            if (ctx.hasPlayer() && arg != null) {
                try {
                    ServerScoreboard scoreboard = ctx.server().getScoreboard();
                    Objective scoreboardObjective = scoreboard.getObjective(arg);
                    if (scoreboardObjective == null) {
                        return PlaceholderResult.invalid("Invalid objective!");
                    }
                    ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(ctx.player(), scoreboardObjective);
                    return PlaceholderResult.value(String.valueOf(score.value()));
                } catch (Exception e) {
                    /* Into the void you go! */
                }
                return PlaceholderResult.invalid("Invalid objective!");
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "pos_x"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                double value = ctx.player().getX();
                String format = "%.2f";

                if (arg != null) {
                    try {
                        int x = Integer.parseInt(arg);
                        format = "%." + x + "f";
                    } catch (Exception e) {
                        format = "%.2f";
                    }
                }

                return PlaceholderResult.value(String.format(format, value));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "pos_y"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                double value = ctx.player().getY();
                String format = "%.2f";

                if (arg != null) {
                    try {
                        int x = Integer.parseInt(arg);
                        format = "%." + x + "f";
                    } catch (Exception e) {
                        format = "%.2f";
                    }
                }

                return PlaceholderResult.value(String.format(format, value));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "pos_z"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                double value = ctx.player().getZ();
                String format = "%.2f";

                if (arg != null) {
                    try {
                        int x = Integer.parseInt(arg);
                        format = "%." + x + "f";
                    } catch (Exception e) {
                        format = "%.2f";
                    }
                }

                return PlaceholderResult.value(String.format(format, value));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "uuid"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(ctx.player().getStringUUID());
            } else if (ctx.hasGameProfile()) {
                return PlaceholderResult.value(Component.nullToEmpty("" + ctx.gameProfile().getId()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "health"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(String.format("%.0f", ctx.player().getHealth()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "max_health"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(String.format("%.0f", ctx.player().getMaxHealth()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "hunger"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(String.valueOf(ctx.player().getFoodData().getFoodLevel()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "saturation"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                return PlaceholderResult.value(String.format("%.0f", ctx.player().getFoodData().getSaturationLevel()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "team_name"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                var team = ctx.player().getTeam();
                return PlaceholderResult.value(team==null ? Component.empty() : Component.nullToEmpty(team.getName()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "team_displayname"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                var team = (PlayerTeam) ctx.player().getTeam();
                return PlaceholderResult.value(team==null ? Component.empty() : team.getDisplayName());
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("player", "team_displayname_formatted"), (ctx, arg) -> {
            if (ctx.hasPlayer()) {
                var team = (PlayerTeam) ctx.player().getTeam();
                return PlaceholderResult.value(team==null ? Component.empty() : team.getFormattedDisplayName());
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });
    }
}
