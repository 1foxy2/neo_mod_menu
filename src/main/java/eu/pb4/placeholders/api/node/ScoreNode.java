package eu.pb4.placeholders.api.node;

import eu.pb4.placeholders.api.ParserContext;
import net.minecraft.network.chat.Component;

public record ScoreNode(String name, String objective) implements TextNode {
    @Override
    public Component toText(ParserContext context, boolean removeBackslashes) {
        return Component.score(name, objective);
    }
}
