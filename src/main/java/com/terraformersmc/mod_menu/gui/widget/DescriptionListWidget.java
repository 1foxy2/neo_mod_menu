package com.terraformersmc.mod_menu.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import com.terraformersmc.mod_menu.gui.widget.entries.ModListEntry;
import com.terraformersmc.mod_menu.util.mod.Mod;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DescriptionListWidget extends AbstractSelectionList<DescriptionListWidget.DescriptionEntry> {

	private static final Component HAS_UPDATE_TEXT = Component.translatable("modmenu.hasUpdate");
	private static final Component EXPERIMENTAL_TEXT = Component.translatable("modmenu.experimental").withStyle(ChatFormatting.GOLD);
	private static final Component DOWNLOAD_TEXT = Component.translatable("modmenu.downloadLink")
		.withStyle(ChatFormatting.BLUE)
		.withStyle(ChatFormatting.UNDERLINE);
	private static final Component CHILD_HAS_UPDATE_TEXT = Component.translatable("modmenu.childHasUpdate");
	private static final Component LINKS_TEXT = Component.translatable("modmenu.links");
	private static final Component SOURCE_TEXT = Component.translatable("modmenu.source")
		.withStyle(ChatFormatting.BLUE)
		.withStyle(ChatFormatting.UNDERLINE);
	private static final Component LICENSE_TEXT = Component.translatable("modmenu.license");
	private static final Component VIEW_CREDITS_TEXT = Component.translatable("modmenu.viewCredits")
		.withStyle(ChatFormatting.BLUE)
		.withStyle(ChatFormatting.UNDERLINE);
	private static final Component CREDITS_TEXT = Component.translatable("modmenu.credits");

	private final ModsScreen parent;
	private final Font textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(
		Minecraft client,
		int width,
		int height,
		int y,
		int itemHeight,
		ModsScreen parent
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
		this.textRenderer = client.font;
	}

	@Override
	public DescriptionEntry getSelected() {
		return null;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + this.getX();
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput builder) {
		Mod mod = parent.getSelectedEntry().getMod();
		builder.add(NarratedElementType.TITLE, mod.getTranslatedName() + " " + mod.getPrefixedVersion());
	}

	@Override
	public void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clearEntries();
			setScrollAmount(-Double.MAX_VALUE);
			if (lastSelected != null) {
				DescriptionEntry emptyEntry = new DescriptionEntry(FormattedCharSequence.EMPTY);
				int wrapWidth = getRowWidth() - 5;

				Mod mod = lastSelected.getMod();
				Component description = mod.getFormattedDescription();
				if (!description.getString().isEmpty()) {
					for (FormattedCharSequence line : textRenderer.split(description, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenu.getConfig().HIDE_MOD_LINKS.get()) {
					children().add(emptyEntry);

					for (FormattedCharSequence line : textRenderer.split(LINKS_TEXT, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					if (sourceLink != null) {
						int indent = 8;
						for (FormattedCharSequence line : textRenderer.split(SOURCE_TEXT, wrapWidth - 16)) {
							children().add(new LinkEntry(line, sourceLink, indent));
							indent = 16;
						}
					}

					links.forEach((key, value) -> {
						int indent = 8;
						for (FormattedCharSequence line : textRenderer.split(Component.translatable(key)
								.withStyle(ChatFormatting.BLUE)
								.withStyle(ChatFormatting.UNDERLINE),
							wrapWidth - 16
						)) {
							children().add(new LinkEntry(line, value, indent));
							indent = 16;
						}
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenu.getConfig().HIDE_MOD_LICENSE.get() && !licenses.isEmpty()) {
					children().add(emptyEntry);

					for (FormattedCharSequence line : textRenderer.split(LICENSE_TEXT, wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					for (String license : licenses) {
						int indent = 8;
						for (FormattedCharSequence line : textRenderer.split(Component.literal(license), wrapWidth - 16)) {
							children().add(new DescriptionEntry(line, indent));
							indent = 16;
						}
					}
				}

				if (!ModMenu.getConfig().HIDE_MOD_CREDITS.get()) {
					if ("minecraft".equals(mod.getId())) {
						children().add(emptyEntry);

						for (FormattedCharSequence line : textRenderer.split(VIEW_CREDITS_TEXT, wrapWidth)) {
							children().add(new MojangCreditsEntry(line));
						}
					} else if (!"java".equals(mod.getId())) {
						var credits = mod.getCredits();

						if (!credits.isEmpty()) {
							children().add(emptyEntry);

							for (FormattedCharSequence line : textRenderer.split(CREDITS_TEXT, wrapWidth)) {
								children().add(new DescriptionEntry(line));
							}

							var iterator = credits.entrySet().iterator();

							while (iterator.hasNext()) {
								int indent = 8;

								var role = iterator.next();
								var roleName = role.getKey();

								for (var line : textRenderer.split(this.creditsRoleText(roleName),
									wrapWidth - 16
								)) {
									children().add(new DescriptionEntry(line, indent));
									indent = 16;
								}

								for (var contributor : role.getValue()) {
									indent = 16;

									for (var line : textRenderer.split(Component.literal(contributor), wrapWidth - 24)) {
										children().add(new DescriptionEntry(line, indent));
										indent = 24;
									}
								}

								if (iterator.hasNext()) {
									children().add(emptyEntry);
								}
							}
						}
					}
				}
			}
		}

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferBuilder;
		MeshData builtBuffer;

		//		{
		//			RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
		//			RenderSystem.setShaderTexture(0, Screen.OPTIONS_BACKGROUND_TEXTURE);
		//			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		//			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		//			bufferBuilder.vertex(this.getX(), this.getBottom(), 0.0D).texture(this.getX() / 32.0F, (this.getBottom() + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255);
		//			bufferBuilder.vertex(this.getRight(), this.getBottom(), 0.0D).texture(this.getRight() / 32.0F, (this.getBottom() + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255);
		//			bufferBuilder.vertex(this.getRight(), this.getY(), 0.0D).texture(this.getRight() / 32.0F, (this.getY() + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255);
		//			bufferBuilder.vertex(this.getX(), this.getY(), 0.0D).texture(this.getX() / 32.0F, (this.getY() + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255);
		//			tessellator.draw();
		//		}

		this.enableScissor(guiGraphics);
		super.renderListItems(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.disableScissor();

		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			GlStateManager.SourceFactor.ZERO,
			GlStateManager.DestFactor.ONE
		);
		//RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShader(CoreShaders.POSITION_COLOR);

		bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.addVertex(this.getX(), (this.getY() + 4), 0.0F).

			setColor(0, 0, 0, 0);

		bufferBuilder.addVertex(this.getRight(), (this.getY() + 4), 0.0F).

			setColor(0, 0, 0, 0);

		bufferBuilder.addVertex(this.getRight(), this.getY(), 0.0F).

			setColor(0, 0, 0, 255);

		bufferBuilder.addVertex(this.getX(), this.getY(), 0.0F).

			setColor(0, 0, 0, 255);

		bufferBuilder.addVertex(this.getX(), this.getBottom(), 0.0F).

			setColor(0, 0, 0, 255);

		bufferBuilder.addVertex(this.getRight(), this.getBottom(), 0.0F).

			setColor(0, 0, 0, 255);

		bufferBuilder.addVertex(this.getRight(), (this.getBottom() - 4), 0.0F).

			setColor(0, 0, 0, 0);

		bufferBuilder.addVertex(this.getX(), (this.getBottom() - 4), 0.0F).

			setColor(0, 0, 0, 0);

		try {
			builtBuffer = bufferBuilder.buildOrThrow();
			BufferUploader.drawWithShader(builtBuffer);
			builtBuffer.close();
		} catch (Exception e) {
			// Ignored
		}
		this.renderScrollBar(bufferBuilder, tessellator);

		RenderSystem.disableBlend();
	}

	public void renderScrollBar(BufferBuilder bufferBuilder, Tesselator tessellator) {
		MeshData builtBuffer;
		int scrollbarStartX = this.getScrollbarPosition();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			int p = (int) ((float) ((this.getBottom() - this.getY()) * (this.getBottom() - this.getY())) / (float) this.getMaxPosition());
			p = Mth.clamp(p, 32, this.getBottom() - this.getY() - 8);
			int q = (int) this.getScrollAmount() * (this.getBottom() - this.getY() - p) / maxScroll + this.getY();
			if (q < this.getY()) {
				q = this.getY();
			}

			bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.addVertex(scrollbarStartX, this.getBottom(), 0.0F).setColor(0, 0, 0, 255);
			bufferBuilder.addVertex(scrollbarEndX, this.getBottom(), 0.0F).setColor(0, 0, 0, 255);
			bufferBuilder.addVertex(scrollbarEndX, this.getY(), 0.0F).setColor(0, 0, 0, 255);
			bufferBuilder.addVertex(scrollbarStartX, this.getY(), 0.0F).setColor(0, 0, 0, 255);
			bufferBuilder.addVertex(scrollbarStartX, q + p, 0.0F).setColor(128, 128, 128, 255);
			bufferBuilder.addVertex(scrollbarEndX, q + p, 0.0F).setColor(128, 128, 128, 255);
			bufferBuilder.addVertex(scrollbarEndX, q, 0.0F).setColor(128, 128, 128, 255);
			bufferBuilder.addVertex(scrollbarStartX, q, 0.0F).setColor(128, 128, 128, 255);
			bufferBuilder.addVertex(scrollbarStartX, q + p - 1, 0.0F).setColor(192, 192, 192, 255);
			bufferBuilder.addVertex(scrollbarEndX - 1, q + p - 1, 0.0F).setColor(192, 192, 192, 255);
			bufferBuilder.addVertex(scrollbarEndX - 1, q, 0.0F).setColor(192, 192, 192, 255);
			bufferBuilder.addVertex(scrollbarStartX, q, 0.0F).setColor(192, 192, 192, 255);
			try {
				builtBuffer = bufferBuilder.buildOrThrow();
				BufferUploader.drawWithShader(builtBuffer);
				builtBuffer.close();
			} catch (Exception e) {
				// Ignored
			}
		}
	}

	private Component creditsRoleText(String roleName) {
		// Replace spaces and dashes in role names with underscores if they exist
		// Notably Quilted Fabric API does this with FabricMC as "Upstream Owner"
		var translationKey = roleName.replaceAll("[ -]", "_").toLowerCase();

		// Add an s to the default untranslated string if it ends in r since this
		// Fixes common role names people use in English (e.g. Author -> Authors)
		var fallback = roleName.endsWith("r") ? roleName + "s" : roleName;

		return Component.translatableWithFallback("modmenu.credits.role." + translationKey, fallback)
			.append(Component.literal(":"));
	}

	protected class DescriptionEntry extends ContainerObjectSelectionList.Entry<DescriptionEntry> {
		protected FormattedCharSequence text;
		protected int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(FormattedCharSequence text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(FormattedCharSequence text) {
			this(text, 0);
		}

		public DescriptionEntry setUpdateTextEntry() {
			this.updateTextEntry = true;
			return this;
		}

		@Override
		public void render(
			GuiGraphics guiGraphics,
			int index,
			int y,
			int x,
			int itemWidth,
			int itemHeight,
			int mouseX,
			int mouseY,
			boolean isSelected,
			float delta
		) {
			if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(guiGraphics, x + indent, y);
				x += 11;
			}
			guiGraphics.drawString(textRenderer, text, x + indent, y, 0xAAAAAA);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return Collections.emptyList();
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(FormattedCharSequence text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				minecraft.setScreen(new MinecraftCredits());
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		class MinecraftCredits extends CreditsAndAttributionScreen {
			public MinecraftCredits() {
				super(parent);
			}
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(FormattedCharSequence text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		public LinkEntry(FormattedCharSequence text, String link) {
			this(text, link, 0);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseOver(mouseX, mouseY)) {
				minecraft.setScreen(new ConfirmLinkScreen((open) -> {
					if (open) {
						Util.getPlatform().openUri(link);
					}
					minecraft.setScreen(parent);
				}, link, false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

}
