package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record DynamicTextNode(String id, ParserContext.Key<Function<String, Component>> key) implements TextNode {
    public static DynamicTextNode of(String id, ParserContext.Key<Function<String, Component>> key) {
        return new DynamicTextNode(id, key);
    }

    public static ParserContext.Key<Function<String, @Nullable Component>> key(String id) {
        return new ParserContext.Key<>("dynamic:" + id, null);
    }

    @Override
    public Component toComponent(ParserContext context, boolean removeBackslashes) {
        var x = context.get(key);
        if (x != null) {
            var t = x.apply(id);
            if (t != null) {
                return t;
            }
            return Component.literal("[INVALID KEY " + this.key.key() + " | " + this.id + "]").withStyle(ChatFormatting.ITALIC).withColor(0xFF0000);
        }
        return Component.literal("[MISSING CONTEXT FOR " + this.key.key() + " | " + this.id + "]").withStyle(ChatFormatting.ITALIC).withColor(0xFF0000);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
