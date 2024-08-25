package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;

public final class StrikethroughNode extends ParentNode {
    private final boolean value;

    public StrikethroughNode(TextNode[] nodes, boolean value) {
        super(nodes);
        this.value = value;
    }

    @Override
    protected Component applyFormatting(MutableComponent out, ParserContext context) {
        return out.setStyle(out.getStyle().withStrikethrough(this.value));
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new StrikethroughNode(children, this.value);
    }

    @Override
    public String toString() {
        return "StrikethroughNode{" +
                "children=" + Arrays.toString(children) +
                ", value=" + value +
                '}';
    }
}
