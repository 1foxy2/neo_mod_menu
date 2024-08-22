package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.network.chat.Style;

import java.util.Collection;

public abstract class SimpleStylingNode extends ParentNode {

    public SimpleStylingNode(TextNode... children) {
        super(children);
    }

    public SimpleStylingNode(Collection<TextNode> children) {
        super(children);
    }

    @Override
    protected Style applyFormatting(Style style, ParserContext context) {
        return style.applyTo(this.style(context));
    }
    protected abstract Style style(ParserContext context);
}
