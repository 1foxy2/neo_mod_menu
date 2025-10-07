package com.terraformersmc.modmenu.gui;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import com.terraformersmc.modmenu.gui.widget.LegacyTexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.ModMenuScreenTexts;
import com.terraformersmc.modmenu.util.TranslationUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModsScreen extends Screen {
	private static final ResourceLocation FILTERS_BUTTON_LOCATION = ResourceLocation.fromNamespaceAndPath(ModMenu.NAMESPACE,
		"textures/gui/filters_button.png"
	);
	private static final ResourceLocation CONFIGURE_BUTTON_LOCATION = ResourceLocation.fromNamespaceAndPath(ModMenu.NAMESPACE,
		"textures/gui/configure_button.png"
	);
	public static final ResourceLocation BADGE_BUTTON_LOCATION = ResourceLocation.fromNamespaceAndPath(ModMenu.NAMESPACE,
			"textures/gui/badge_button.png"
	);

	private static final Logger LOGGER = LoggerFactory.getLogger("Better ModList | ModsScreen");
	private final Screen previousScreen;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private double scrollPercent = 0;
	private boolean keepFilterOptionsShown = false;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	protected static final int RIGHT_PANE_Y = 48;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();

	private EditBox searchBox;
	private @Nullable AbstractWidget filtersButton;
	private AbstractWidget sortingButton;
	private AbstractWidget librariesButton;
	private ModListWidget modList;
	private @Nullable AbstractWidget configureButton;
	private @Nullable AbstractWidget badgeButton;
	private AbstractWidget websiteButton;
	private AbstractWidget issuesButton;
	private DescriptionListWidget descriptionListWidget;
	private AbstractWidget modsFolderButton;
	private AbstractWidget doneButton;

	public final Map<ModContainer, Boolean> modHasConfigScreen = new HashMap<>();
	public final Map<String, Throwable> modScreenErrors = new HashMap<>();

	private static final Component SEND_FEEDBACK_TEXT = Component.translatable("menu.sendFeedback");
	private static final Component REPORT_BUGS_TEXT = Component.translatable("menu.reportBugs");

	public ModsScreen(Screen previousScreen) {
		super(ModMenuScreenTexts.TITLE);
		this.previousScreen = previousScreen;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (modList.isMouseOver(mouseX, mouseY)) {
			return this.modList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		if (descriptionListWidget.isMouseOver(mouseX, mouseY)) {
			return this.descriptionListWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}

		return false;
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
		this.paneWidth = this.width / 2 - 8;
		this.rightPaneX = this.width - this.paneWidth;

		// Mod list (initialized early for updateFiltersX)
		this.modList = new ModListWidget(this.minecraft,
				this.paneWidth,
				this.height - paneY - 36,
				paneY,
				ModMenu.getConfig().COMPACT_LIST.get() ? 23 : 36,
				this.modList,
				this
		);
		this.modList.setX(0);

		// Search box
		int filtersButtonSize = (ModMenu.getConfig().CONFIG_MODE.get() ? 0 : 22);
		int searchWidthMax = this.paneWidth - 32 - filtersButtonSize;
		int searchBoxWidth = ModMenu.getConfig().CONFIG_MODE.get() ? Math.min(200, searchWidthMax) : searchWidthMax;

		this.searchBoxX = this.paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;
		this.searchBox = new EditBox(this.font,
			this.searchBoxX,
			22,
			searchBoxWidth,
			20,
			this.searchBox,
			ModMenuScreenTexts.SEARCH
		);
		if (ModMenu.getConfig().HIDE_SCREEN_TOP.get()) {
			searchBox.visible = false;
			searchBox.active = false;
		}
		this.searchBox.setResponder(text -> {
			this.modList.filter(text, false);
		});

		// Filters button
		Component sortingText = ModMenuScreenTexts.getSortingComponent();
		Component librariesText = ModMenuScreenTexts.getLibrariesComponent();

		int sortingWidth = font.width(sortingText) + 20;
		int librariesWidth = font.width(librariesText) + 20;

		this.filtersWidth = librariesWidth + sortingWidth + 2;
		this.searchRowWidth = this.searchBoxX + searchBoxWidth + 22;

		this.updateFiltersX(true);

		if (!ModMenu.getConfig().CONFIG_MODE.get()) {
			this.filtersButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(ModMenuScreenTexts.TOGGLE_FILTER_OPTIONS,
					button -> {
						this.setFilterOptionsShown(!this.filterOptionsShown);
					}
				)
				.position(this.paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22)
				.size(20, 20)
				.uv(0, 0, 20)
				.texture(FILTERS_BUTTON_LOCATION, 32, 64)
				.build();

			this.filtersButton.setTooltip(Tooltip.create(ModMenuScreenTexts.TOGGLE_FILTER_OPTIONS));
			if (ModMenu.getConfig().HIDE_SCREEN_TOP.get()) {
				filtersButton.visible = false;
				filtersButton.active = false;
			}
		}

		// Sorting button
		this.sortingButton = Button.builder(sortingText, button -> {
			ModMenu.getConfig().SORTING.get().cycleValue();
			ModMenu.CONFIG.getRight().save();
			modList.reloadFilters();
			button.setMessage(ModMenuScreenTexts.getSortingComponent());
		}).pos(this.filtersX, 45).size(sortingWidth, 20).build();

		// Show libraries button
		this.librariesButton = Button.builder(librariesText, button -> {
			ModMenu.getConfig().SHOW_LIBRARIES.set(!ModMenu.getConfig().SHOW_LIBRARIES.get());
			ModMenu.CONFIG.getRight().save();
			modList.reloadFilters();
			button.setMessage(ModMenuScreenTexts.getLibrariesComponent());
		}).pos(this.filtersX + sortingWidth + 2, 45).size(librariesWidth, 20).build();

		// Configure button
		if (!ModMenu.getConfig().HIDE_CONFIG_BUTTONS.get()) {
			this.configureButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(CommonComponents.EMPTY, button -> {
					final Mod mod = Objects.requireNonNull(selected).getMod();
						if (getModHasConfigScreen(mod.getContainer())) {
							this.safelyOpenConfigScreen(mod.getContainer().get());
					} else {
						button.active = false;
					}
				})
				.position(width - 24, rightPaneY)
				.size(20, 20)
				.uv(0, 0, 20)
				.texture(CONFIGURE_BUTTON_LOCATION, 32, 64)
				.build();
		}

		if (!ModMenu.getConfig().HIDE_BADGE_BUTTONS.get()) {
			this.badgeButton = LegacyTexturedButtonWidget.legacyTexturedBuilder(CommonComponents.EMPTY, button ->
						this.minecraft.pushGuiLayer(new BadgeScreen(this.selected.mod, paneWidth, searchBoxWidth)))
					.position(paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 26, 22)
					.size(20, 20)
					.uv(0, 0, 20)
					.texture(BADGE_BUTTON_LOCATION, 32, 64)
					.build();
		}

		// Website button
		int urlButtonWidths = this.paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		this.websiteButton = Button.builder(ModMenuScreenTexts.WEBSITE, button -> {
				final Mod mod = Objects.requireNonNull(selected).getMod();
				boolean isMinecraft = selected.getMod().getId().equals("minecraft");
				if (isMinecraft) {
					var url = SharedConstants.getCurrentVersion().stable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK;
					ConfirmLinkScreen.confirmLinkNow(this, url, true);
				} else {
					var url = mod.getWebsite();
                    if (url != null) {
                        ConfirmLinkScreen.confirmLinkNow(this, url, false);
                    }
				}
			})
			.pos(this.rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), rightPaneY + 36)
			.size(Math.min(urlButtonWidths, 200), 20)
			.build();

		// Issues button
		this.issuesButton = Button.builder(ModMenuScreenTexts.ISSUES, button -> {
				final Mod mod = Objects.requireNonNull(selected).getMod();
				boolean isMinecraft = selected.getMod().getId().equals("minecraft");
				if (isMinecraft) {
					ConfirmLinkScreen.confirmLinkNow(this, CommonLinks.SNAPSHOT_BUGS_FEEDBACK, true);
				} else {
					var url = mod.getIssueTracker();
                    if (url != null) {
                        ConfirmLinkScreen.confirmLinkNow(this, url, false);
                    }
				}
			})
			.pos(
					this.rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2),
					rightPaneY + 36
			)
			.size(Math.min(urlButtonWidths, 200), 20)
			.build();

		// Description list
		this.descriptionListWidget = new DescriptionListWidget(
			this.minecraft,
			this.paneWidth,
			this.height - rightPaneY - 96,
				rightPaneY + 60,
			font.lineHeight + 1,
			this.descriptionListWidget,
			this
		);
		this.descriptionListWidget.setX(this.rightPaneX);

		// Mods folder button
		this.modsFolderButton = Button.builder(ModMenuScreenTexts.MODS_FOLDER, button -> {
			Util.getPlatform().openUri(FMLPaths.MODSDIR.get().toUri());
		}).pos(this.width / 2 - 154, this.height - 28).size(150, 20).build();

		// Done button
		this.doneButton = Button.builder(CommonComponents.GUI_DONE, button -> {
			minecraft.setScreen(previousScreen);
		}).pos(this.width / 2 + 4, this.height - 28).size(150, 20).build();

		// Initialize data
		modList.finalizeInit();
		this.setFilterOptionsShown(this.keepFilterOptionsShown && this.filterOptionsShown);

		// Add children
		this.addWidget(this.searchBox);
		this.setInitialFocus(this.searchBox);
		if (this.filtersButton != null) {
			this.addRenderableWidget(this.filtersButton);
		}

		this.addRenderableWidget(this.sortingButton);
		this.addRenderableWidget(this.librariesButton);
		this.addWidget(this.modList);
		if (this.configureButton != null) {
			this.addRenderableWidget(this.configureButton);
		}
		if (this.badgeButton != null) {
			this.addRenderableWidget(this.badgeButton);
		}

		this.addRenderableWidget(this.websiteButton);
		this.addRenderableWidget(this.issuesButton);
		this.addWidget(this.descriptionListWidget);
		this.addRenderableWidget(this.modsFolderButton);
		this.addRenderableWidget(this.doneButton);

		this.init = true;
		this.keepFilterOptionsShown = true;
	}

    @Override
    public boolean keyPressed(KeyEvent event) {
		return super.keyPressed(event) || this.searchBox.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return this.searchBox.charTyped(event);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.render(guiGraphics, mouseX, mouseY, delta);
		boolean hideTop = ModMenu.getConfig().HIDE_SCREEN_TOP.get();
		int rightPaneY = hideTop ? 5 : RIGHT_PANE_Y;
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(guiGraphics, mouseX, mouseY, delta);
		}

		this.modList.render(guiGraphics, mouseX, mouseY, delta);
		this.searchBox.render(guiGraphics, mouseX, mouseY, delta);
		if (!hideTop) {
			guiGraphics.drawCenteredString(this.font, this.title, this.modList.getWidth() / 2, 8, 16777215);
		}
		assert minecraft != null;
		int grayColor = 0xFFAAAAAA;
		if (!(ModMenu.getConfig().DISABLE_DRAG_AND_DROP.get() || hideTop)) {
			guiGraphics.drawCenteredString(
				this.font,
				ModMenuScreenTexts.DROP_INFO_LINE_1,
				this.width - this.modList.getWidth() / 2,
				RIGHT_PANE_Y / 2 - minecraft.font.lineHeight - 1,
				grayColor
			);
			guiGraphics.drawCenteredString(
				this.font,
				ModMenuScreenTexts.DROP_INFO_LINE_2,
				this.width - this.modList.getWidth() / 2,
				RIGHT_PANE_Y / 2 + 1,
				grayColor
			);
		}

		if (!ModMenu.getConfig().CONFIG_MODE.get()) {
			Component fullModCount = this.computeModCountText(true, false);
			if (!ModMenu.getConfig().CONFIG_MODE.get() && this.updateFiltersX(false)) {
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
							0xFFFFFFFF,
							true
					);
				} else {
					guiGraphics.drawString(font,
							computeModCountText(false, false).getVisualOrderText(),
							this.searchBoxX,
							showingModTextY,
							0xFFFFFFFF,
							true
					);
					guiGraphics.drawString(font,
							computeLibraryCountText(false).getVisualOrderText(),
							this.searchBoxX,
							showingModTextY + 11,
							0xFFFFFFFF,
							true
					);
				}
			}
		}

		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = this.rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, guiGraphics, x, rightPaneY, 32, 32);
			}

			Tuple<ResourceLocation, Dimension> iconProperties = selectedEntry.getIconTexture();

			int imageOffset = iconProperties.getB().width;
			int imageHeight = iconProperties.getB().height;
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, iconProperties.getA(), x, rightPaneY, 0.0F, 0.0F,
					imageOffset, imageHeight,
					imageOffset, imageHeight);

			imageOffset += 4;

			int lineSpacing = font.lineHeight + 1;
			Component name = Component.literal(mod.getTranslatedName());
			FormattedText trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (font.width(name) > maxNameWidth) {
				FormattedText ellipsis = FormattedText.of("...");
				trimmedName = FormattedText.composite(font.substrByWidth(name,
					maxNameWidth - font.width(ellipsis)
				), ellipsis);
			}

			guiGraphics.drawString(font,
				Language.getInstance().getVisualOrder(trimmedName),
				x + imageOffset,
				rightPaneY + 1,
					0xFFFFFFFF,
				true
			);

			if (mouseX > x + imageOffset && mouseY > rightPaneY + 1 &&
				mouseY < rightPaneY + 1 + font.lineHeight &&
				mouseX < x + imageOffset + font.width(trimmedName)) {
				guiGraphics.setTooltipForNextFrame(ModMenuScreenTexts.modIdTooltip(mod.getId()), mouseX, mouseY);
			}

			if (this.init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(
					x + imageOffset + this.minecraft.font.width(trimmedName) + 2,
					rightPaneY,
					width - 28,
					selectedEntry.mod,
					this
				);
				this.init = false;
			}

			if (!ModMenu.getConfig().HIDE_BADGES.get()) {
				modBadgeRenderer.draw(guiGraphics);
			}

			if (mod.isReal()) {
				guiGraphics.drawString(font,
					mod.getPrefixedVersion(),
					x + imageOffset,
					rightPaneY + 2 + lineSpacing,
					0xFFAAAAAA,
					true
				);
			}

			String authors;
			List<String> names = mod.getAuthors();
			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.getFirst();
				}
				DrawingUtil.drawWrappedString(
					guiGraphics,
					I18n.get("modmenu.authorPrefix", authors),
					x + imageOffset,
					rightPaneY + 2 + lineSpacing * 2,
					this.paneWidth - imageOffset - 4,
					1,
					0x808080
				);
			}
		}
	}

	private Component computeModCountText(boolean includeLibs, boolean onInit) {
		int[] rootMods = formatModCount(ModMenu.ROOT_MODS.values()
			.stream()
			.filter(mod -> !mod.isHidden() && !mod.getBadges().contains(ModBadge.LIBRARY))
			.map(Mod::getId)
			.collect(Collectors.toSet()), onInit);
		if (includeLibs && ModMenu.getConfig().SHOW_LIBRARIES.get() && !onInit) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values()
				.stream()
				.filter(mod -> !mod.isHidden() && mod.getBadges().contains(ModBadge.LIBRARY))
				.map(Mod::getId)
				.collect(Collectors.toSet()), false);
			return TranslationUtil.translateNumeric("modmenu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("modmenu.showingMods", rootMods);
		}
	}

	private Component computeLibraryCountText(boolean onInit) {
		if (ModMenu.getConfig().SHOW_LIBRARIES.get() && !onInit) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values()
				.stream()
				.filter(mod -> !mod.isHidden() && mod.getBadges().contains(ModBadge.LIBRARY))
				.map(Mod::getId)
				.collect(Collectors.toSet()), false);
			return TranslationUtil.translateNumeric("modmenu.showingLibraries", rootLibs);
		} else {
			return Component.empty();
		}
	}

	private int[] formatModCount(Set<String> set, boolean allVisible) {
		int visible = this.modList.getDisplayedCountFor(set);
		int total = set.size();
		if (visible == total || allVisible) {
			return new int[]{ total };
		}
		return new int[]{ visible, total };
	}

	@Override
	public void onClose() {
		this.modList.close();
		this.minecraft.setScreen(this.previousScreen);
	}

	private void setFilterOptionsShown(boolean filterOptionsShown) {
		this.filterOptionsShown = filterOptionsShown;
		this.sortingButton.visible = filterOptionsShown;
		this.librariesButton.visible = filterOptionsShown;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry == null) {
			return;
		}

		this.selected = entry;
		String modId = selected.getMod().getId();

		this.descriptionListWidget.updateSelectedMod(selected.getMod());

		if (this.configureButton != null) {

			this.configureButton.active = getModHasConfigScreen(selected.mod.getContainer());
			this.configureButton.visible =
					selected != null && getModHasConfigScreen(selected.mod.getContainer()) || modScreenErrors.containsKey(modId);

			if (modScreenErrors.containsKey(modId)) {
				Throwable e = modScreenErrors.get(modId);
				this.configureButton.setTooltip(Tooltip.create(ModMenuScreenTexts.configureError(modId, e)));
			} else {
				this.configureButton.setTooltip(Tooltip.create(ModMenuScreenTexts.CONFIGURE));
			}
		}

		boolean isMinecraft = modId.equals("minecraft");
		this.websiteButton.setMessage(isMinecraft ? SEND_FEEDBACK_TEXT : ModMenuScreenTexts.WEBSITE);
		this.issuesButton.setMessage(isMinecraft ? REPORT_BUGS_TEXT : ModMenuScreenTexts.ISSUES);

		this.websiteButton.visible = true;
		this.websiteButton.active = isMinecraft || selected.getMod().getWebsite() != null;

		this.issuesButton.visible = true;
		this.issuesButton.active = isMinecraft || selected.getMod().getIssueTracker() != null;
	}

	public double getScrollPercent() {
		return scrollPercent;
	}

	public void updateScrollPercent(double scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return this.searchBox.getValue();
	}

	private boolean updateFiltersX(boolean onInit) {
		if ((this.filtersWidth + font.width(this.computeModCountText(true, onInit)) + 20) >= this.searchRowWidth &&
			((this.filtersWidth + font.width(this.computeModCountText(false, onInit)) + 20) >= this.searchRowWidth ||
				(this.filtersWidth + font.width(this.computeLibraryCountText(onInit)) + 20) >= this.searchRowWidth
			)) {
			this.filtersX = this.paneWidth / 2 - this.filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			this.filtersX = this.searchRowWidth - this.filtersWidth + 1;
			return true;
		}
	}

	@Override
	public void onFilesDrop(List<Path> paths) {
		Path modsDirectory = FMLPaths.MODSDIR.get();

		// Filter out none mods
		List<Path> mods = paths.stream().filter(ModsScreen::isValidMod).toList();

		if (mods.isEmpty()) {
			return;
		}

		String modList = mods.stream().map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
		this.minecraft.setScreen(new ConfirmScreen((value) -> {
			if (value) {
				boolean allSuccessful = true;

				for (Path path : mods) {
					try {
						Files.copy(path, modsDirectory.resolve(path.getFileName()));
					} catch (IOException e) {
						LOGGER.warn("Failed to copy mod from {} to {}",
							path,
							modsDirectory.resolve(path.getFileName())
						);
						SystemToast.onPackCopyFailure(minecraft, path.toString());
						allSuccessful = false;
						break;
					}
				}

				if (allSuccessful) {
					SystemToast.add(minecraft.getToastManager(),
						SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
						ModMenuScreenTexts.DROP_SUCCESSFUL_LINE_1,
						ModMenuScreenTexts.DROP_SUCCESSFUL_LINE_2
					);
				}
			}
			this.minecraft.setScreen(this);
		}, ModMenuScreenTexts.DROP_CONFIRM, Component.literal(modList)));
	}

	private static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fabric.mod.json") != null;
		} catch (IOException | UnsupportedOperationException e) {
			return false;
		}
    }

	private static boolean isValidMod(Path mod) {
		return isFabricMod(mod) || isNeoforgeMod(mod);
	}

	private static boolean isNeoforgeMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("META-INF/neoforge.mods.toml") != null;
		} catch (IOException | UnsupportedOperationException e) {
			return false;
		}
    }

	public boolean getModHasConfigScreen(Optional<ModContainer> containerOptional) {
		if (containerOptional.isEmpty()) return false;

		ModContainer container = containerOptional.get();

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
