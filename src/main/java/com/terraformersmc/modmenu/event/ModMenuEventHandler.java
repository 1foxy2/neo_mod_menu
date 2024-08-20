package com.terraformersmc.modmenu.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import com.terraformersmc.modmenu.util.UpdateCheckerUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EventBusSubscriber(modid = ModMenu.MOD_ID, value = Dist.CLIENT)
public class ModMenuEventHandler {
	public static final ResourceLocation MODS_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, "textures/gui/mods_button.png");
	private static Lazy<KeyMapping> MENU_KEY_BIND = Lazy.of(() -> new KeyMapping("key.modmenu.open_menu",
			KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.misc"));;

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		onClientEndTick(Minecraft.getInstance());
	}

	@SubscribeEvent
	public static void onScreenInit(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		afterScreenInit(screen.getMinecraft(), screen, screen.width, screen.height);
	}

	public static void afterScreenInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
		if (screen instanceof TitleScreen) {
			afterTitleScreenInit(screen);
		}
	}

	private static void afterTitleScreenInit(Screen screen) {
		final List<AbstractWidget> buttons = new ArrayList<>();
		screen.renderables.stream().forEach(widget -> {
			if (widget instanceof AbstractWidget abstractWidget)
				buttons.add(abstractWidget);
		});
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				AbstractWidget widget = buttons.get(i);
				if (widget instanceof Button button) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
						if (button.visible) {
							shiftButtons(button, modsButtonIndex == -1, spacing);
							if (modsButtonIndex == -1) {
								buttonsY = button.getY();
							}
						}
					}
					if (buttonHasText(button, "menu.online")) {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() ==
							ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS) {
							buttons.set(i, new ModMenuButtonWidget(button.getX(),
								button.getY(),
								button.getWidth(),
								button.getHeight(),
								ModMenuApi.createModsButtonText(),
								screen
							));
						} else {
							if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() ==
								ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
								button.setWidth(98);
							}
							modsButtonIndex = i + 1;
							if (button.visible) {
								buttonsY = button.getY();
							}
						}
					}
				}

			}
			if (modsButtonIndex != -1) {
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					buttons.add(modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 100,
						buttonsY + spacing,
						200,
						20,
						ModMenuApi.createModsButtonText(),
						screen
					));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
					buttons.add(modsButtonIndex,
						new ModMenuButtonWidget(screen.width / 2 + 2,
							buttonsY,
							98,
							20,
							ModMenuApi.createModsButtonText(),
							screen
						)
					);
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
					buttons.add(modsButtonIndex, new UpdateCheckerTexturedButtonWidget(screen.width / 2 + 104,
						buttonsY,
						20,
						20,
						0,
						0,
						20,
						MODS_BUTTON_TEXTURE,
						32,
						64,
						button -> Minecraft.getInstance().setScreen(new ModsScreen(screen)),
						ModMenuApi.createModsButtonText()
					));
				}
			}
		}
		UpdateCheckerUtil.triggerV2DeprecatedToast();
	}

	private static void onClientEndTick(Minecraft client) {
		while (MENU_KEY_BIND.get().consumeClick()) {
			client.setScreen(new ModsScreen(client.screen));
		}
	}

	public static boolean buttonHasText(LayoutElement element, String... translationKeys) {
		if (element instanceof Button button) {
			Component component = button.getMessage();
			ComponentContents textContent = component.getContents();

			return textContent instanceof TranslatableContents && Arrays.stream(translationKeys)
				.anyMatch(s -> ((TranslatableContents) textContent).getKey().equals(s));
		}
		return false;
	}

	public static void shiftButtons(LayoutElement element, boolean shiftUp, int spacing) {
		if (shiftUp) {
			element.setY(element.getY() - spacing / 2);
		} else if (!(element instanceof AbstractWidget button &&
			button.getMessage().equals(Component.translatable("title.credits"))
		)) {
			element.setY(element.getY() + spacing / 2);
		}
	}

	@EventBusSubscriber(modid = ModMenu.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class modBusEvents {
		@SubscribeEvent
		public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
			event.register(ModMenuEventHandler.MENU_KEY_BIND.get());
		}
	}
}
