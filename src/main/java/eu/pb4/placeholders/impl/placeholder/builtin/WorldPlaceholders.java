package eu.pb4.placeholders.impl.placeholder.builtin;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WorldPlaceholders {
    static final int CHUNK_AREA = (int)Math.pow(17.0D, 2.0D);

    public static void register() {
        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "time"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }

            long dayTime = (long) (world.getDayTime() * 3.6 / 60);

            return PlaceholderResult.value(String.format("%02d:%02d", (dayTime / 60 + 6) % 24, dayTime % 60));
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "time_alt"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }

            long dayTime = (long) (world.getDayTime() * 3.6 / 60);
            long x = (dayTime / 60 + 6) % 24;
            long y = x % 12;
            if (y == 0) {
                y = 12;
            }
            return PlaceholderResult.value(String.format("%02d:%02d %s", y, dayTime % 60, x > 11 ? "PM" : "AM" ));
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "day"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }

            return PlaceholderResult.value("" + world.getDayTime() / 24000);
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "id"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }

            return PlaceholderResult.value(world.dimension().location().toString());
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "name"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }
            List<String> parts = new ArrayList<>();
            {
                String[] words = world.dimension().location().getPath().split("_");
                for (String word : words) {
                    String[] s = word.split("", 2);
                    s[0] = s[0].toUpperCase(Locale.ROOT);
                    parts.add(String.join("", s));
                }
            }
            return PlaceholderResult.value(String.join(" ", parts));
        });



        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "player_count"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }

            return PlaceholderResult.value("" + world.players().size());
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "mob_count_colored"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }

            NaturalSpawner.SpawnState spawnState = world.getChunkSource().getLastSpawnState();

            MobCategory mobCategory = null;
            if (arg != null) {
                mobCategory = MobCategory.valueOf(arg.toUpperCase(Locale.ROOT));
            }

            if (mobCategory != null) {
                int count = spawnState.getMobCategoryCounts().getInt(mobCategory);
                int cap = mobCategory.getMaxInstancesPerChunk() * spawnState.getSpawnableChunkCount() / CHUNK_AREA;

                return PlaceholderResult.value(count > 0 ? Component.literal("" + count).withStyle(count > cap ? ChatFormatting.LIGHT_PURPLE : count > 0.8 * cap ? ChatFormatting.RED : count > 0.5 * cap ? ChatFormatting.GOLD : ChatFormatting.GREEN) : Component.literal("-").withStyle(ChatFormatting.GRAY));
            } else {
                int cap = 0;

                for (MobCategory group : MobCategory.values()) {
                    cap += group.getMaxInstancesPerChunk();
                }
                cap = cap * spawnState.getSpawnableChunkCount() / CHUNK_AREA;

                int count = 0;

                for (int value : spawnState.getMobCategoryCounts().values()) {
                    count += value;
                }
                return PlaceholderResult.value(count > 0 ? Component.literal("" + count).withStyle(count > cap ? ChatFormatting.LIGHT_PURPLE : count > 0.8 * cap ? ChatFormatting.RED : count > 0.5 * cap ? ChatFormatting.GOLD : ChatFormatting.GREEN) : Component.literal("-").withStyle(ChatFormatting.GRAY));
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "mob_count"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }

            NaturalSpawner.SpawnState spawnState = world.getChunkSource().getLastSpawnState();

            MobCategory mobCategory = null;
            if (arg != null) {
                mobCategory = MobCategory.valueOf(arg.toUpperCase(Locale.ROOT));
            }

            if (mobCategory != null) {
                return PlaceholderResult.value("" + spawnState.getMobCategoryCounts().getInt(mobCategory));
            } else {
                int x = 0;

                for (int value : spawnState.getMobCategoryCounts().values()) {
                    x += value;
                }
                return PlaceholderResult.value("" + x);
            }
        });

        Placeholders.register(ResourceLocation.fromNamespaceAndPath("world", "mob_cap"), (ctx, arg) -> {
            ServerLevel world;
            if (ctx.player() != null) {
                world = ctx.player().serverLevel();
            } else {
                world = ctx.server().overworld();
            }

            NaturalSpawner.SpawnState spawnState = world.getChunkSource().getLastSpawnState();

            MobCategory mobCategory = null;
            if (arg != null) {
                mobCategory = MobCategory.valueOf(arg.toUpperCase(Locale.ROOT));
            }

            if (mobCategory != null) {
                return PlaceholderResult.value("" + mobCategory.getMaxInstancesPerChunk() * spawnState.getSpawnableChunkCount() / CHUNK_AREA);
            } else {
                int x = 0;

                for (MobCategory group : MobCategory.values()) {
                    x += group.getMaxInstancesPerChunk();
                }
                return PlaceholderResult.value("" + x * spawnState.getSpawnableChunkCount() / CHUNK_AREA);
            }
        });
    }
}
