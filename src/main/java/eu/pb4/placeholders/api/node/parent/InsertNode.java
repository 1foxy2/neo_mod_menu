package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;

public final class InsertNode extends ParentNode {
    private final TextNode value;

    public InsertNode(TextNode[] children, TextNode value) {
        super(children);
        this.value = value;
    }

    public TextNode value() {
        return this.value;
    }

    @Override
    protected Component applyFormatting(MutableComponent out, ParserContext context) {
        return out.setStyle(out.getStyle().withInsertion(value.toText(context, true).getString()));
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new InsertNode(children, this.value);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        return new InsertNode(children, TextNode.asSingle(parser.parseNodes(this.value)));
    }

    @Override
    public String toString() {
        return "InsertNode{" +
                "value=" + value +
                ", children=" + Arrays.toString(children) +
                '}';
    }

    @Override
    public boolean isDynamicNoChildren() {
        return this.value.isDynamic();
    }
}
