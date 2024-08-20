package com.terraformersmc.modmenu.config.option;

import net.minecraft.client.OptionInstance;

public interface OptionConvertable {
	OptionInstance<?> asOption();
}
