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
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.PauseScreen;
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

import java.util.Arrays;
import java.util.List;

@EventBusSubscriber(modid = ModMenu.MOD_ID, value = Dist.CLIENT)
public class ModMenuEventHandler {
	public static final ResourceLocation MODS_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, "textures/gui/mods_button.png");
	private static Lazy<KeyMapping> MENU_KEY_BIND = Lazy.of(() -> new KeyMapping("key.modmenu.open_menu",
			KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, "key.categories.misc"));;

	@SubscribeEvent
	public static void onScreenInit(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		//if (screen instanceof PauseScreen) {
		//	removeModsButton(screen);
	//	}
		if (screen instanceof TitleScreen) {
			removeModsButton(screen);
			afterTitleScreenInit(screen);
		}
	}

	private static void removeModsButton(Screen screen) {
		screen.renderables.removeIf(button -> buttonHasText((LayoutElement) button, "fml.menu.mods"));
		screen.narratables.removeIf(button -> buttonHasText((LayoutElement) button, "fml.menu.mods"));
		screen.children.removeIf(button -> buttonHasText((LayoutElement) button, "fml.menu.mods"));
	}

	private static void afterTitleScreenInit(Screen screen) {
		final List<Renderable> buttons = screen.renderables;

		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = screen.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				Renderable widget = buttons.get(i);
				if (widget instanceof Button button) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
						if (button.visible) {
							if (modsButtonIndex == -1) {
								buttonsY = button.getY();
							}
						}
					}
					if (buttonHasText(button, "menu.online")) {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() ==
							ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS) {
							set(screen, modsButtonIndex, new ModMenuButtonWidget(button.getX(),
											button.getY() + spacing,
											button.getWidth(),
											button.getHeight(),
											ModMenuApi.createModsButtonText(),
											screen));
							buttons.remove(i);
							screen.children().remove(buttons.get(i));
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
					add(screen, modsButtonIndex, new ModMenuButtonWidget(screen.width / 2 - 100,
						buttonsY + spacing,
						200,
						20,
						ModMenuApi.createModsButtonText(),
						screen
					));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
					add(screen, modsButtonIndex,
							new ModMenuButtonWidget(screen.width / 2 + 2,
							buttonsY,
							98,
							20,
							ModMenuApi.createModsButtonText(),
							screen
						)
					);
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
					add(screen, modsButtonIndex, new UpdateCheckerTexturedButtonWidget(screen.width / 2 + 104,
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

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		while (MENU_KEY_BIND.get().consumeClick()) {
			Minecraft.getInstance().setScreen(new ModsScreen(Minecraft.getInstance().screen));
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

	public static AbstractWidget set(Screen screen, int index, AbstractWidget element) {
		final int drawableIndex = translateIndex(screen.renderables, index, false);
		screen.renderables.set(drawableIndex, element);

		final int selectableIndex = translateIndex(screen.narratables, index, false);
		screen.narratables.set(selectableIndex, element);

		final int childIndex = translateIndex(screen.children, index, false);
		return (AbstractWidget) screen.children.set(childIndex, element);
	}

	public static void add(Screen screen, int index, AbstractWidget element) {
		// ensure no duplicates
		final int duplicateIndex = screen.renderables.indexOf(element);

		if (duplicateIndex >= 0) {
			screen.renderables.remove(element);
			screen.narratables.remove(element);
			screen.children.remove(element);

			if (duplicateIndex <= translateIndex(screen.renderables, index, true)) {
				index--;
			}
		}

		final int drawableIndex = translateIndex(screen.renderables, index, true);
		screen.renderables.add(drawableIndex, element);

		final int selectableIndex = translateIndex(screen.narratables, index, true);
		screen.narratables.add(selectableIndex, element);

		final int childIndex = translateIndex(screen.children, index, true);
		screen.children.add(childIndex, element);
	}

	private static int translateIndex(List<?> list, int index, boolean allowAfter) {
		int remaining = index;

		for (int i = 0, max = list.size(); i < max; i++) {
			if (list.get(i) instanceof AbstractWidget) {
				if (remaining == 0) {
					return i;
				}

				remaining--;
			}
		}

		if (allowAfter && remaining == 0) {
			return list.size();
		}

		throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, index - remaining));
	}

	@EventBusSubscriber(modid = ModMenu.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class modBusEvents {
		@SubscribeEvent
		public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
			event.register(ModMenuEventHandler.MENU_KEY_BIND.get());
		}
	}
}
