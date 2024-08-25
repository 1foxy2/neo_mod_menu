package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ClickActionNode extends ParentNode {
    private final ClickEvent.Action action;
    private final TextNode value;

    public ClickActionNode(TextNode[] children, ClickEvent.Action action, TextNode value) {
        super(children);
        this.action = action;
        this.value = value;
    }

    public ClickEvent.Action action() {
        return action;
    }

    public TextNode value() {
        return value;
    }

    @Override
    protected Component applyFormatting(MutableComponent out, ParserContext context) {
        return out.setStyle(out.getStyle().withClickEvent(new ClickEvent(this.action, this.value.toText(context, true).getString())));
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new ClickActionNode(children, this.action, this.value);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        return new ClickActionNode(children, this.action, TextNode.asSingle(parser.parseNodes(this.value)));
    }

    @Override
    public boolean isDynamicNoChildren() {
        return this.value.isDynamic();
    }

    @Override
    public String toString() {
        return "ClickActionNode{" +
                "action=" + action +
                ", value=" + value +
                '}';
    }
}
