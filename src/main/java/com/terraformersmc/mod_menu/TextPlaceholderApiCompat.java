package com.terraformersmc.mod_menu;

import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TextParserV1;

public class TextPlaceholderApiCompat {
	public static final NodeParser PARSER = NodeParser.merge(TextParserV1.DEFAULT, TextParserV1.DEFAULT);
}
