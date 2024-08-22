package eu.pb4.placeholders.api;

import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.parent.ParentNode;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.minecraft.network.chat.Component;


/**
 * You should use {@link eu.pb4.placeholders.api.parsers.ParserBuilder} for stacked parsing
 * or {@link eu.pb4.placeholders.api.parsers.TagParser} for only tags to component.
 */
@Deprecated
public final class TextParserUtils {
    private TextParserUtils() {}

    public static Component formatText(String text) {
        return formatNodes(text).toComponent(null, true);
    }

    public static Component formatTextSafe(String text) {
        return formatNodesSafe(text).toComponent(null, true);
    }

    public static Component formatText(String text, TextParserV1.TagParserGetter getter) {
        return formatNodes(text, getter).toComponent(null, true);
    }

    public static ParentTextNode formatNodes(String text) {
        return new ParentNode(TextParserV1.DEFAULT.parseNodes(new LiteralNode(text)));
    }

    public static ParentTextNode formatNodesSafe(String text) {
        return new ParentNode(TextParserV1.DEFAULT.parseNodes(new LiteralNode(text)));
    }

    public static ParentTextNode formatNodes(String text, TextParserV1.TagParserGetter getter) {
        return new ParentNode(TextParserV1.parseNodesWith(new LiteralNode(text), getter));
    }
}
