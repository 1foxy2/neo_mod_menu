package com.terraformersmc.mod_menu.gui.widget;

import com.terraformersmc.mod_menu.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenuButtonWidget extends Button {
	public ModMenuButtonWidget(int x, int y, int width, int height, Component text, Screen screen) {
		super(x,
				y,
				width,
				height,
				text,
				button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)),
				Button.DEFAULT_NARRATION
		);
	}
}
