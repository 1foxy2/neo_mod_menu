package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.impl.GeneralUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.Collection;

public class ParentNode implements ParentTextNode {
    public static final ParentNode EMPTY = new ParentNode(new TextNode[0]);
    protected final TextNode[] children;

    public ParentNode(TextNode... children) {
        this.children = children;
    }

    public ParentNode(Collection<TextNode> children) {
        this(children.toArray(GeneralUtils.CASTER));
    }

    @Override
    public final TextNode[] getChildren() {
        return this.children;
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new ParentNode(children);
    }

    @Override
    public final Component toText(ParserContext context, boolean removeBackslashes) {
        var compact = context != null && context.get(ParserContext.Key.COMPACT_TEXT) != Boolean.FALSE;

        if (this.children.length == 0) {
            return Component.empty();
        } else if ((this.children.length == 1 && this.children[0] != null) && compact) {
            var out = this.children[0].toText(context, true);
            if (GeneralUtils.isEmpty(out)) {
                return out;
            }

            return ((MutableComponent) this.applyFormatting(out.copy(), context));
        } else {
            MutableComponent base = compact ? null : Component.empty();

            for (int i = 0; i < this.children.length; i++) {
                if (this.children[i] != null) {
                    var child = this.children[i].toText(context, true);

                    if (!GeneralUtils.isEmpty(child)) {
                        if (base == null) {
                            if (child.getStyle().isEmpty()) {
                                base = child.copy();
                            } else {
                                base = Component.empty();
                                base.append(child);
                            }
                        } else {
                            base.append(child);
                        }
                    }
                }
            }

            if (base == null || GeneralUtils.isEmpty(base)) {
                return Component.empty();
            }

            return this.applyFormatting(base, context);
        }
    }

    protected Component applyFormatting(MutableComponent out, ParserContext context) { return out; };

    @Override
    public String toString() {
        return "ParentNode{" +
                "children=" + Arrays.toString(children) +
                '}';
    }
}
