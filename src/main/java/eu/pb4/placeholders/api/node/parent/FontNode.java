package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;

public final class FontNode extends SimpleStylingNode {
    private final ResourceLocation font;

    public FontNode(TextNode[] children, ResourceLocation font) {
        super(children);
        this.font = font;
    }

    @Override
    protected Style style(ParserContext context) {
        return Style.EMPTY.withFont(font);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new FontNode(children, this.font);
    }

    @Override
    public String toString() {
        return "FontNode{" +
                "font=" + font +
                ", children=" + Arrays.toString(children) +
                '}';
    }
}
