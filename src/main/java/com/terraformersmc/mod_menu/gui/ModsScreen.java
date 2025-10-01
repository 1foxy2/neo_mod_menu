package com.terraformersmc.mod_menu.gui;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.widget.DescriptionListWidget;
import com.terraformersmc.mod_menu.gui.widget.ModListWidget;
import com.terraformersmc.mod_menu.gui.widget.entries.ModListEntry;
import com.terraformersmc.mod_menu.util.DrawingUtil;
import com.terraformersmc.mod_menu.util.ModMenuScreenTexts;
import com.terraformersmc.mod_menu.util.TranslationUtil;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadge;
import com.terraformersmc.mod_menu.util.mod.ModBadgeRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModsScreen extends Screen {
	private static final ResourceLocation FILTERS_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/filters_button.png");
	private static final ResourceLocation CONFIGURE_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/configure_button.png");
	public static final ResourceLocation BADGE_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/badge_button.png");
	private static final Component TOGGLE_FILTER_OPTIONS = Component.translatable("mod_menu.toggleFilterOptions");
	private static final Component CONFIGURE = Component.translatable("mod_menu.configure");
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | ModsScreen");
	private EditBox searchBox;
	private DescriptionListWidget descriptionListWidget;
	private final Screen previousScreen;
	private ModListWidget modList;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private double scrollPercent = 0;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	protected static final int RIGHT_PANE_Y = 48;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	public final Map<ModContainer, Boolean> modHasConfigScreen = new HashMap<>();
	public final Map<String, Throwable> modScreenErrors = new HashMap<>();

	public ModsScreen(Screen previousScreen) {
		super(Component.translatable("mod_menu.title"));
		this.previousScreen = previousScreen;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if (modList.isMouseOver(mouseX, mouseY)) {
			return this.modList.mouseScrolled(mouseX, mouseY, amount);
		}
		if (descriptionListWidget.isMouseOver(mouseX, mouseY)) {
			return this.descriptionListWidget.mouseScrolled(mouseX, mouseY, amount);
		}
		return false;
	}

	@Override
	public void tick() {
		this.searchBox.tick();
	}

	@Override
	protected void init() {
        boolean hideTop = ModMenu.getConfig().HIDE_SCREEN_TOP.get();
        int paneY;
        int rightPaneY;
        if (hideTop) {
            paneY = 30;
            rightPaneY = 5;
        } else {
            paneY = ModMenu.getConfig().CONFIG_MODE.get() ? 48 : 48 + 19;
            rightPaneY = RIGHT_PANE_Y;
        }
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int filtersButtonSize = (ModMenu.getConfig().CONFIG_MODE.get() ? 0 : 22);
		int searchWidthMax = paneWidth - 32 - filtersButtonSize;
		int searchBoxWidth = ModMenu.getConfig().CONFIG_MODE.get() ? Math.min(200, searchWidthMax) : searchWidthMax;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;
		this.searchBox = new EditBox(this.font, searchBoxX, 22, searchBoxWidth, 20, this.searchBox, Component.translatable("mod_menu.search"));
        if (ModMenu.getConfig().HIDE_SCREEN_TOP.get()) {
            searchBox.visible = false;
            searchBox.active = false;
        }
        this.searchBox.setResponder((string_1) -> this.modList.filter(string_1, false));

		this.modList = new ModListWidget(this.minecraft, paneWidth, this.height, paneY, this.height - 36, ModMenu.getConfig().COMPACT_LIST.get() ? 23 : 36, this.searchBox.getValue(), this.modList, this);
		this.modList.setLeftPos(0);

		this.descriptionListWidget = new DescriptionListWidget(this.minecraft,
                paneWidth,
                this.height,
                rightPaneY + 60,
                this.height - 36,
                font.lineHeight + 1,
                this.descriptionListWidget,
                this);
		this.descriptionListWidget.setLeftPos(rightPaneX);
		Button configureButton = new ImageButton(width - 24, rightPaneY, 20, 20, 0, 0, 20, CONFIGURE_BUTTON_LOCATION, 32, 64, button -> {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			if (getModHasConfigScreen(mod.getContainer())) {
				this.safelyOpenConfigScreen(mod.getContainer().get());
			} else {
				button.active = false;
			}
		}) {
			@Override
			public void render(GuiGraphics DrawContext, int mouseX, int mouseY, float delta) {
				String modId = selected.getMod().getId();
				if (selected != null) {
					active = getModHasConfigScreen(selected.getMod().getContainer());
				} else {
					active = false;
					visible = false;
				}
				visible = selected != null && getModHasConfigScreen(selected.getMod().getContainer())
						|| modScreenErrors.containsKey(modId);

				if (modScreenErrors.containsKey(modId)) {
					Throwable e = modScreenErrors.get(modId);
					this.setTooltip(Tooltip.create(Component.translatable("mod_menu.configure.error", modId, modId).copy().append("\n\n").append(e.toString()).withStyle(ChatFormatting.RED)));
				} else {
					this.setTooltip(Tooltip.create(CONFIGURE));
				}
				super.render(DrawContext, mouseX, mouseY, delta);
			}

			@Override
			public void renderWidget(GuiGraphics DrawContext, int mouseX, int mouseY, float delta) {
				RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
				RenderSystem.setShaderColor(1, 1, 1, 1f);
				super.renderWidget(DrawContext, mouseX, mouseY, delta);
			}
		};
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		Button websiteButton = new Button(rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), rightPaneY + 36, Math.min(urlButtonWidths, 200), 20,
				Component.translatable("mod_menu.website"), button -> {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			this.minecraft.setScreen(new ConfirmLinkScreen((bool) -> {
				if (bool) {
					Util.getPlatform().openUri(mod.getWebsite());
				}
				this.minecraft.setScreen(this);
			}, mod.getWebsite(), false));
		}, Supplier::get) {
			@Override
			public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
				visible = selected != null;
				active = visible && selected.getMod().getWebsite() != null;
				super.render(guiGraphics, mouseX, mouseY, delta);
			}
		};

		Button badgeButton = null;
		if (!ModMenu.getConfig().HIDE_BADGE_BUTTONS.get()) {
			badgeButton = new ImageButton(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 26, 22, 20, 20, 0, 0, 20, BADGE_BUTTON_LOCATION, 32, 64, button ->
							this.minecraft.pushGuiLayer(new BadgeScreen(this.selected.mod, paneWidth, searchBoxWidth)), CommonComponents.EMPTY);
		}

		Button issuesButton = new Button(rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), rightPaneY + 36, Math.min(urlButtonWidths, 200), 20,
				Component.translatable("mod_menu.issues"), button -> {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			this.minecraft.setScreen(new ConfirmLinkScreen((bool) -> {
				if (bool) {
					Util.getPlatform().openUri(mod.getIssueTracker());
				}
				this.minecraft.setScreen(this);
			}, mod.getIssueTracker(), false));
		}, Supplier::get) {
			@Override
			public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
				visible = selected != null;
				active = visible && selected.getMod().getIssueTracker() != null;
				super.render(guiGraphics, mouseX, mouseY, delta);
			}
		};
		this.addWidget(this.searchBox);
		Button filtersButton = new ImageButton(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, 20, FILTERS_BUTTON_LOCATION, 32, 64, button -> filterOptionsShown = !filterOptionsShown, TOGGLE_FILTER_OPTIONS);
		filtersButton.setTooltip(Tooltip.create(TOGGLE_FILTER_OPTIONS));
		if (!ModMenu.getConfig().CONFIG_MODE.get()) {
			this.addRenderableWidget(filtersButton);
		}
        if (ModMenu.getConfig().HIDE_SCREEN_TOP.get()) {
            filtersButton.visible = false;
            filtersButton.active = false;
        }
		Component showLibrariesText = ModMenuScreenTexts.getLibrariesComponent();
		Component sortingText = ModMenuScreenTexts.getSortingComponent();
		int showLibrariesWidth = font.width(showLibrariesText) + 20;
		int sortingWidth = font.width(sortingText) + 20;
		filtersWidth = showLibrariesWidth + sortingWidth + 2;
		searchRowWidth = searchBoxX + searchBoxWidth + 22;
		updateFiltersX();
		this.addRenderableWidget(new Button(filtersX, 45, sortingWidth, 20, sortingText, button -> {
			ModMenu.getConfig().SORTING.get().cycleValue();
			ModMenu.CONFIG.getRight().save();
			modList.reloadFilters();
		}, Supplier::get) {
			@Override
			public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
				guiGraphics.pose().translate(0, 0, 1);
				visible = filterOptionsShown;
				this.setMessage(ModMenuScreenTexts.getSortingComponent());
				super.render(guiGraphics, mouseX, mouseY, delta);
			}
		});
		this.addRenderableWidget(new Button(filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20, showLibrariesText, button -> {
			ModMenu.getConfig().SHOW_LIBRARIES.set(!ModMenu.getConfig().SHOW_LIBRARIES.get());
			ModMenu.CONFIG.getRight().save();
			modList.reloadFilters();
		}, Supplier::get) {
			@Override
			public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
				guiGraphics.pose().translate(0, 0, 1);
				visible = filterOptionsShown;
				this.setMessage(ModMenuScreenTexts.getLibrariesComponent());
				super.render(guiGraphics, mouseX, mouseY, delta);
			}
		});
		this.addWidget(this.modList);
		if (!ModMenu.getConfig().HIDE_CONFIG_BUTTONS.get()) {
			this.addRenderableWidget(configureButton);
		}
		if (badgeButton != null) {
			this.addRenderableWidget(badgeButton);
		}

		this.addRenderableWidget(websiteButton);
		this.addRenderableWidget(issuesButton);
		this.addWidget(this.descriptionListWidget);
		this.addRenderableWidget(
				Button.builder(Component.translatable("mod_menu.modsFolder"), button -> Util.getPlatform().openFile(FMLPaths.MODSDIR.get().toFile()))
						.pos(this.width / 2 - 154, this.height - 28)
						.size(150, 20)
						.createNarration(Supplier::get)
						.build());
		this.addRenderableWidget(
				Button.builder(CommonComponents.GUI_DONE, button -> minecraft.setScreen(previousScreen))
						.pos(this.width / 2 + 4, this.height - 28)
						.size(150, 20)
						.createNarration(Supplier::get)
						.build());
        modList.finalizeInit();
		this.searchBox.setFocused(true);

		init = true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return super.keyPressed(keyCode, scanCode, modifiers) || this.searchBox.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int keyCode) {
		return this.searchBox.charTyped(chr, keyCode);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        boolean hideTop = ModMenu.getConfig().HIDE_SCREEN_TOP.get();
        int rightPaneY = hideTop ? 5 : RIGHT_PANE_Y;
		this.renderDirtBackground(guiGraphics);
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(guiGraphics, mouseX, mouseY, delta);
		}
		this.modList.render(guiGraphics, mouseX, mouseY, delta);
		this.searchBox.render(guiGraphics, mouseX, mouseY, delta);
		RenderSystem.disableBlend();
        if (!hideTop) {
		guiGraphics.drawCenteredString(this.font, this.title, this.modList.getWidth() / 2, 8, 16777215);
        }
		if (!ModMenu.getConfig().DISABLE_DRAG_AND_DROP.get() || hideTop) {
			guiGraphics.drawCenteredString(this.font, Component.translatable("mod_menu.dropInfo.line1").withStyle(ChatFormatting.GRAY), this.width - this.modList.getWidth() / 2, rightPaneY / 2 - minecraft.font.lineHeight - 1, 16777215);
			guiGraphics.drawCenteredString(this.font, Component.translatable("mod_menu.dropInfo.line2").withStyle(ChatFormatting.GRAY), this.width - this.modList.getWidth() / 2, rightPaneY / 2 + 1, 16777215);
		}
		if (!ModMenu.getConfig().CONFIG_MODE.get()) {
			Component fullModCount = computeModCountText(true);
			if (!ModMenu.getConfig().CONFIG_MODE.get() && updateFiltersX()) {
                int showingModTextY;
                if (hideTop) {
                    showingModTextY = 6;
                } else {
                    showingModTextY = 46;
                }
                if (!ModMenu.getConfig().SHOW_LIBRARIES.get() ||
                        font.width(fullModCount) <= (this.filterOptionsShown ? this.filtersX : modList.getWidth()) - 5) {
                    guiGraphics.drawString(font,
                            fullModCount.getVisualOrderText(),
                            this.searchBoxX,
                            showingModTextY + 6,
                            0xFFFFFF,
                            true
                    );
                } else {
                    guiGraphics.drawString(font,
                            computeModCountText(false).getVisualOrderText(),
                            this.searchBoxX,
                            showingModTextY,
                            0xFFFFFF,
                            true
                    );
                    guiGraphics.drawString(font,
                            computeLibraryCountText().getVisualOrderText(),
                            this.searchBoxX,
                            showingModTextY + 11,
                            0xFFFFFF,
                            true
                    );
                }
			}
		}
		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, guiGraphics, x, rightPaneY, 32, 32);
			}
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.enableBlend();
			Tuple<ResourceLocation, Dimension> iconProperties = selectedEntry.getIconTexture();

			int imageOffset = iconProperties.getB().width;
			int imageHeight = iconProperties.getB().height;
			guiGraphics.blit(iconProperties.getA(), x, rightPaneY, 0.0F, 0.0F,
					imageOffset, imageHeight,
					imageOffset, imageHeight);

			imageOffset += 4;

			RenderSystem.disableBlend();
			int lineSpacing = font.lineHeight + 1;
			Component name = Component.literal(mod.getTranslatedName());
			FormattedText trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (font.width(name) > maxNameWidth) {
				FormattedText ellipsis = FormattedText.of("...");
				trimmedName = FormattedText.composite(font.substrByWidth(name, maxNameWidth - font.width(ellipsis)), ellipsis);
			}
			guiGraphics.drawString(font, Language.getInstance().getVisualOrder(trimmedName), x + imageOffset, rightPaneY + 1, 0xFFFFFF, false);
			if (mouseX > x + imageOffset && mouseY > rightPaneY + 1 && mouseY < rightPaneY + 1 + font.lineHeight && mouseX < x + imageOffset + font.width(trimmedName)) {
				this.setTooltipForNextRenderPass(Component.translatable("mod_menu.modIdToolTip", mod.getId()));
			}
			if (init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(x + imageOffset + this.minecraft.font.width(trimmedName) + 2, rightPaneY, width - 28, selectedEntry.mod, this);
				init = false;
			}
			if (!ModMenu.getConfig().HIDE_BADGES.get()) {
				modBadgeRenderer.draw(guiGraphics);
			}
			if (mod.isReal()) {
				guiGraphics.drawString(font, mod.getPrefixedVersion(), x + imageOffset, rightPaneY + 2 + lineSpacing, 0x808080, false);
			}
			String authors;
			List<String> names = mod.getAuthors();

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				DrawingUtil.drawWrappedString(guiGraphics, I18n.get("mod_menu.authorPrefix", authors), x + imageOffset, rightPaneY + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
			}
		}
		super.render(guiGraphics, mouseX, mouseY, delta);
	}

	private Component computeModCountText(boolean includeLibs) {
		int[] rootMods = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && !mod.getBadges().contains(ModBadge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));

		if (includeLibs && ModMenu.getConfig().SHOW_LIBRARIES.get()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && mod.getBadges().contains(ModBadge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("mod_menu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("mod_menu.showingMods", rootMods);
		}
	}

	private Component computeLibraryCountText() {
		if (ModMenu.getConfig().SHOW_LIBRARIES.get()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && mod.getBadges().contains(ModBadge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("mod_menu.showingLibraries", rootLibs);
		} else {
			return Component.literal(null);
		}
	}

	private int[] formatModCount(Set<String> set) {
		int visible = modList.getDisplayedCountFor(set);
		int total = set.size();
		if (visible == total) {
			return new int[]{total};
		}
		return new int[]{visible, total};
	}

	@Override
	public void onClose() {
		this.modList.close();
		this.minecraft.setScreen(this.previousScreen);
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
            this.descriptionListWidget.updateSelectedModIfRequired(selected.getMod());
        }
	}

	public double getScrollPercent() {
		return scrollPercent;
	}

	public void updateScrollPercent(double scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return searchBox.getValue();
	}

	private boolean updateFiltersX() {
		if ((filtersWidth + font.width(computeModCountText(true)) + 20) >= searchRowWidth && ((filtersWidth + font.width(computeModCountText(false)) + 20) >= searchRowWidth || (filtersWidth + font.width(computeLibraryCountText()) + 20) >= searchRowWidth)) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			filtersX = searchRowWidth - filtersWidth + 1;
			return true;
		}
	}

	@Override
	public void onFilesDrop(List<Path> paths) {
		Path modsDirectory = FabricLoader.getInstance().getGameDir().resolve("mods");

		// Filter out none mods
		List<Path> mods = paths.stream()
				.filter(ModsScreen::isMod)
				.toList();

		if (mods.isEmpty()) {
			return;
		}

		String modList = mods.stream()
				.map(Path::getFileName)
				.map(Path::toString)
				.collect(Collectors.joining(", "));

		this.minecraft.setScreen(new ConfirmScreen((value) -> {
			if (value) {
				boolean allSuccessful = true;

				for (Path path : mods) {
					try {
						Files.copy(path, modsDirectory.resolve(path.getFileName()));
					} catch (IOException e) {
						LOGGER.warn("Failed to copy mod from {} to {}", path, modsDirectory.resolve(path.getFileName()));
						SystemToast.onPackCopyFailure(minecraft, path.toString());
						allSuccessful = false;
						break;
					}
				}

				if (allSuccessful) {
					SystemToast.add(minecraft.getToasts(),
							SystemToast.SystemToastIds.TUTORIAL_HINT,
							Component.translatable("mod_menu.dropSuccessful.line1"),
							Component.translatable("mod_menu.dropSuccessful.line2"));
				}
			}
			this.minecraft.setScreen(this);
		}, Component.translatable("mod_menu.dropConfirm"), Component.literal(modList)));
	}

	private static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fabric.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean isMod(Path mod) {
		return isFabricMod(mod) || isNeoforgeMod(mod);
	}

	private static boolean isNeoforgeMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("META-INF/mods.toml") != null;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean getModHasConfigScreen(Optional<ModContainer> optionalModContainer) {
		if (optionalModContainer.isEmpty()) return false;

		ModContainer container = optionalModContainer.get();

		if (this.modScreenErrors.containsKey(container.getModId())) {
			return false;
		} else {
			return this.modHasConfigScreen.computeIfAbsent(container, ModMenu::hasConfigScreen);
		}
	}

	public void safelyOpenConfigScreen(ModContainer modId) {
		try {
			Screen screen = ModMenu.getConfigScreen(modId, this);
			if (screen != null) {
				this.minecraft.setScreen(screen);
			}
		} catch (java.lang.NoClassDefFoundError e) {
			LOGGER.warn(
					"The '" + modId + "' mod config screen is not available because " + e.getLocalizedMessage() +
							" is missing.");
			modScreenErrors.put(modId.getModId(), e);
		} catch (Throwable e ) {
			LOGGER.error("Error from mod '" + modId + "'", e);
			modScreenErrors.put(modId.getModId(), e);
		}
	}
}
