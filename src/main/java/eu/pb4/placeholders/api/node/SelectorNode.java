package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public record SelectorNode(String pattern, Optional<TextNode> separator) implements TextNode {
    @Override
    public Component toText(ParserContext context, boolean removeBackslashes) {
        return Component.selector(pattern, separator.map(x -> x.toText(context, removeBackslashes)));
    }

    @Override
    public boolean isDynamic() {
        return separator.isPresent() && separator.get().isDynamic();
    }
}
