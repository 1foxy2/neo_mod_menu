package eu.pb4.placeholders.api;

import com.mojang.authlib.GameProfile;
import eu.pb4.placeholders.impl.placeholder.ViewObjectImpl;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public record PlaceholderContext(MinecraftServer server,
                                 Supplier<CommandSourceStack> lazySource,
                                 @Nullable ServerLevel world,
                                 @Nullable ServerPlayer player,
                                 @Nullable Entity entity,
                                 @Nullable GameProfile gameProfile,
                                 ViewObject view
) {

    public PlaceholderContext(MinecraftServer server,
                              CommandSourceStack source,
                              @Nullable ServerLevel world,
                              @Nullable ServerPlayer player,
                              @Nullable Entity entity,
                              @Nullable GameProfile gameProfile,
                              ViewObject view
    ) {
        this(server, () -> source, world, player, entity, gameProfile, view);
    }

    public CommandSourceStack source() {
        return this.lazySource.get();
    }

    public PlaceholderContext(MinecraftServer server,
                              CommandSourceStack source,
                              @Nullable ServerLevel world,
                              @Nullable ServerPlayer player,
                              @Nullable Entity entity,
                              @Nullable GameProfile gameProfile) {
        this(server, source, world, player, entity, gameProfile, ViewObject.DEFAULT);
    }


    public static ParserContext.Key<PlaceholderContext> KEY = new ParserContext.Key<>("placeholder_context", PlaceholderContext.class);

    public boolean hasWorld() {
        return this.world != null;
    }

    public boolean hasPlayer() {
        return this.player != null;
    }

    public boolean hasGameProfile() {
        return this.gameProfile != null;
    }

    public boolean hasEntity() {
        return this.entity != null;
    }

    public ParserContext asParserContext() {
        return ParserContext.of(KEY, this);
    }

    public PlaceholderContext withView(ViewObject view) {
        return new PlaceholderContext(this.server, this.lazySource, this.world, this.player, this.entity, this.gameProfile, view);
    }

    public void addToContext(ParserContext context) {
        context.with(KEY, this);
    }


    public static PlaceholderContext of(MinecraftServer server) {
        return of(server, ViewObject.DEFAULT);
    }

    public static PlaceholderContext of(MinecraftServer server, ViewObject view) {
        return new PlaceholderContext(server, server::createCommandSourceStack, null, null, null, null, view);
    }

    public static PlaceholderContext of(GameProfile profile, MinecraftServer server) {
        return of(profile, server, ViewObject.DEFAULT);
    }

    public static PlaceholderContext of(GameProfile profile, MinecraftServer server, ViewObject view) {
        var name = profile.getName() != null ? profile.getName() : profile.getId().toString();
        return new PlaceholderContext(server, () -> new CommandSourceStack(CommandSource.NULL, Vec3.ZERO, Vec2.ZERO, server.overworld(), server.getProfilePermissions(profile), name, Component.literal(name), server, null), null, null, null, profile, view);
    }

    public static PlaceholderContext of(ServerPlayer player) {
        return of(player, ViewObject.DEFAULT);
    }

    public static PlaceholderContext of(ServerPlayer player, ViewObject view) {
        return new PlaceholderContext(player.getServer(), player::createCommandSourceStack, player.serverLevel(), player, player, player.getGameProfile(), view);
    }

    public static PlaceholderContext of(CommandSourceStack source) {
        return of(source, ViewObject.DEFAULT);
    }

    public static PlaceholderContext of(CommandSourceStack source, ViewObject view) {
        return new PlaceholderContext(source.getServer(), source, source.getLevel(), source.getPlayer(), source.getEntity(), source.getPlayer() != null ? source.getPlayer().getGameProfile() : null, view);
    }

    public static PlaceholderContext of(Entity entity) {
        return of(entity, ViewObject.DEFAULT);
    }

    public static PlaceholderContext of(Entity entity, ViewObject view) {
        if (entity instanceof ServerPlayer player) {
            return of(player, view);
        } else {
            return new PlaceholderContext(entity.getServer(), entity::createCommandSourceStack, (ServerLevel) entity.level(), null, entity, null, view);
        }
    }


    public interface ViewObject {
        ViewObject DEFAULT = of(new ResourceLocation("placeholder_api", "default"));

        static ViewObject of(ResourceLocation identifier) {
            return new ViewObjectImpl(identifier);
        }

        ResourceLocation identifier();
    }
}
