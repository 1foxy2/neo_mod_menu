package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import java.util.Arrays;


public final class FormattingNode extends SimpleStylingNode {
    private final ChatFormatting[] formatting;

    public FormattingNode(TextNode[] children, ChatFormatting formatting) {
        this(children, new ChatFormatting[]{ formatting });
    }

    public FormattingNode(TextNode[] children, ChatFormatting... formatting) {
        super(children);
        this.formatting = formatting;
    }

    @Override
    protected Style style(ParserContext context) {
        return Style.EMPTY.applyFormats(this.formatting);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new FormattingNode(children, this.formatting);
    }

    @Override
    public String toString() {
        return "FormattingNode{" +
                "formatting=" + formatting +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}
