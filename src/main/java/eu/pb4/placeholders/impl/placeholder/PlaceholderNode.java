package eu.pb4.placeholders.impl.placeholder;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.impl.GeneralUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;

@ApiStatus.Internal
public record PlaceholderNode(ParserContext.Key<PlaceholderContext> contextKey, String placeholder, Placeholders.PlaceholderGetter getter, boolean optionalContext, @Nullable String argument) implements TextNode {
    @Override
    public Component toText(ParserContext context, boolean removeBackslashes) {
        var ctx = context.get(contextKey);
        var handler = getter.getPlaceholder(placeholder, context);
        if ((ctx != null || this.optionalContext) && handler != null) {
            try {
                return handler.onPlaceholderRequest(ctx, argument).text();
            } catch (Throwable e) {
                GeneralUtils.LOGGER.error("Error occurred while parsing placeholder " + placeholder + " / " + contextKey.key() + "!", e);
                return Component.empty();
            }
        } else {
            if (GeneralUtils.IS_DEV) {
                GeneralUtils.LOGGER.error("Missing context for placeholders requiring them (" + placeholder + " / " + contextKey.key() + ")!", new NullPointerException());
            }
            return Component.empty();
        }
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
