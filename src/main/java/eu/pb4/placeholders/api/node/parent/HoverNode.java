package eu.pb4.placeholders.api.node.parent;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public final class HoverNode<T, H> extends ParentNode {
    private final Action<T, H> action;
    private final T value;

    public HoverNode(TextNode[] children, Action<T, H> action, T value) {
        super(children);
        this.action = action;
        this.value = value;
    }

    @Override
    protected Component applyFormatting(MutableComponent out, ParserContext context) {
        if (this.action == Action.TEXT) {
            return out.setStyle(out.getStyle().withHoverEvent(new HoverEvent((HoverEvent.Action<Object>) this.action.vanillaType(), ((TextNode) this.value).toText(context, true))));
        } else if (this.action == Action.ENTITY) {
            return out.setStyle(out.getStyle().withHoverEvent(new HoverEvent((HoverEvent.Action<Object>) this.action.vanillaType(), ((EntityNodeContent) this.value).toVanilla(context))));
        } else {
            return out.setStyle(out.getStyle().withHoverEvent(new HoverEvent((HoverEvent.Action<Object>) this.action.vanillaType(), this.value)));
        }

    }

    @Override
    public ParentTextNode copyWith(TextNode[] children) {
        return new HoverNode(children, this.action, this.value);
    }

    @Override
    public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
        if (this.action == Action.TEXT) {
            return new HoverNode(children, Action.TEXT, parser.parseNode((TextNode) this.value));
        } else if (this.action == Action.ENTITY && ((EntityNodeContent) this.value).name != null) {
            var val = ((EntityNodeContent) this.value);
            return new HoverNode(children, Action.ENTITY, new EntityNodeContent(val.entityType, val.uuid, parser.parseNode(val.name)));
        }
        return this.copyWith(children);
    }

    public Action<T, H> action() {
        return this.action;
    }

    public T value() {
        return this.value;
    }

    @Override
    public String toString() {
        return "HoverNode{" +
                "value=" + value +
                ", children=" + Arrays.toString(children) +
                '}';
    }

    @Override
    public boolean isDynamicNoChildren() {
        return (this.action == Action.TEXT && ((TextNode) this.value).isDynamic()) || (this.action == Action.ENTITY && ((EntityNodeContent) this.value).name.isDynamic());
    }

    public record Action<T, H>(HoverEvent.Action<H> vanillaType) {
        public static final Action<EntityNodeContent, HoverEvent.EntityTooltipInfo> ENTITY = new Action<>(HoverEvent.Action.SHOW_ENTITY);
        public static final Action<HoverEvent.ItemStackInfo, HoverEvent.ItemStackInfo> ITEM_STACK = new Action<>(HoverEvent.Action.SHOW_ITEM);
        public static final Action<TextNode, Component> TEXT = new Action<>(HoverEvent.Action.SHOW_TEXT);
    }

    public record EntityNodeContent(EntityType<?> entityType, UUID uuid, @Nullable TextNode name) {
        public HoverEvent.EntityTooltipInfo toVanilla(ParserContext context) {
            return new HoverEvent.EntityTooltipInfo(this.entityType, this.uuid, this.name != null ? this.name.toText(context, true) : null);
        }
    }
}
