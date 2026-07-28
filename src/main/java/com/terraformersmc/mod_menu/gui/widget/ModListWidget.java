package com.terraformersmc.mod_menu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.ModsScreen;
import com.terraformersmc.mod_menu.gui.widget.entries.*;
import com.terraformersmc.mod_menu.util.mod.Mod;
import com.terraformersmc.mod_menu.util.mod.ModBadge;
import com.terraformersmc.mod_menu.util.mod.ModSearch;
import com.terraformersmc.mod_menu.util.mod.neoforge.NeoforgeIconHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class ModListWidget extends ObjectSelectionList<ModListEntry> implements AutoCloseable {
	public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");
	private final ModsScreen parent;
	private List<Mod> mods = null;
	private final Set<Mod> addedMods = new HashSet<>();
	private String selectedModId = null;
	private boolean scrolling;
	private final NeoforgeIconHandler iconHandler = new NeoforgeIconHandler();
	private Double restoreScrollY = null;
	private final List<ModListEntry> draggingEntries = new ArrayList<>();

	public ModListWidget(
		Minecraft client,
		int width,
		int height,
		int y,
		int itemHeight,
		ModListWidget list,
		ModsScreen parent
	) {
		super(client, width, height, y, itemHeight);
		this.parent = parent;
		if (list != null) {
			this.mods = list.mods;
			this.restoreScrollY = list.getScrollAmount();
		}
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4));
		if (denominator <= 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(getScrollAmount() / Math.max(
				0,
				this.getMaxPosition() - (this.getBottom() - this.getY() - 4)
			));
		}
	}

	@Override
	public boolean isFocused() {
		return parent.getFocused() == this;
	}

	public void select(ModListEntry entry) {
		this.setSelected(entry);
		if (entry != null) {
			Mod mod = entry.getMod();
			this.minecraft.getNarrator()
				.sayNow(Component.translatable("narrator.select", mod.getTranslatedName()).getString());
		}
	}

	@Override
	public void setSelected(ModListEntry entry) {
		super.setSelected(entry);
		selectedModId = entry.getMod().getId();
		parent.updateSelectedEntry(getSelected());
	}

	@Override
	protected boolean isSelectedItem(int index) {
		ModListEntry selected = getSelected();
		return selected != null && selected.getMod().getId().equals(getEntry(index).getMod().getId());
	}

	@Override
	public int addEntry(ModListEntry entry) {
		if (addedMods.contains(entry.mod)) {
			return 0;
		}
		addedMods.add(entry.mod);
		int i = super.addEntry(entry);
		if (entry.getMod().getId().equals(selectedModId)) {
			setSelected(entry);
		}
		return i;
	}

	@Override
	protected boolean removeEntry(ModListEntry entry) {
		addedMods.remove(entry.mod);
		return super.removeEntry(entry);
	}

	@Override
	protected ModListEntry remove(int index) {
		addedMods.remove(getEntry(index).mod);
		return super.remove(index);
	}

	public void finalizeInit() {
		reloadFilters();
		if(restoreScrollY != null) {
			setScrollAmount(restoreScrollY);
			restoreScrollY = null;
		}
	}

	public void reloadFilters() {
		filter(parent.getSearchInput(), true, false);
	}


	public void filter(String searchTerm, boolean refresh) {
		filter(searchTerm, refresh, true);
	}

	private boolean hasVisibleChildMods(Mod parent) {
		List<Mod> children = ModMenu.PARENT_MAP.get(parent);
		boolean hideLibraries = !ModMenu.getConfig().SHOW_LIBRARIES.get();

		return !children.stream()
			.allMatch(child -> child.isHidden() || hideLibraries && child.getBadges().contains(ModBadge.LIBRARY));
	}

	public void filter(String searchTerm, boolean refresh, boolean reposition) {
		this.clearEntries();
		addedMods.clear();
		Collection<Mod> mods = ModMenu.MODS.values().stream().filter(mod -> {
			if (ModMenu.getConfig().CONFIG_MODE.get()) {
				return parent.getModHasConfigScreen(mod.getContainer());
			}

			return !mod.isHidden();
		}).collect(Collectors.toSet());

		if (DEBUG) {
			mods = new ArrayList<>(mods);
			//			mods.addAll(TestModContainer.getTestModContainers());
		}

		if (this.mods == null || refresh) {
			this.mods = new ArrayList<>();
			this.mods.addAll(mods);
			this.mods.sort(ModMenu.getConfig().SORTING.get().getComparator());
		}

		List<Mod> matched = ModSearch.search(parent, searchTerm, this.mods);

		for (Mod mod : matched) {
			String modId = mod.getId();

			//Hide parent lib mods when the config is set to hide
			if (mod.getBadges().contains(ModBadge.LIBRARY) && !ModMenu.getConfig().SHOW_LIBRARIES.get()) {
				continue;
			}

			if (!ModMenu.PARENT_MAP.values().contains(mod)) {
				if (ModMenu.PARENT_MAP.keySet().contains(mod) && hasVisibleChildMods(mod)) {
					//Add parent mods when not searching
					List<Mod> children = ModMenu.PARENT_MAP.get(mod);
					children.sort(ModMenu.getConfig().SORTING.get().getComparator());
					ParentEntry parent = new ParentEntry(mod, children, this);
					this.addEntry(parent);
					//Add children if they are meant to be shown
					if (this.parent.showModChildren.contains(modId)) {
						List<Mod> validChildren = ModSearch.search(this.parent, searchTerm, children);
						for (Mod child : validChildren) {
							addChildMod(child, validChildren, parent, List.of(parent), searchTerm, 1);
						}
					}
				} else {
					//A mod with no children
					this.addEntry(new IndependentEntry(mod, this));
				}
			}
		}

        if (!reposition) {
            // This generally leaves the same mod selected, but no mod highlighted, and the scrolling is unmodified.
            return;
        }

		if (parent.getSelectedEntry() != null && !children().isEmpty() || this.getSelected() != null && getSelected().getMod() != parent.getSelectedEntry()
			.getMod()) {
			for (ModListEntry entry : children()) {
				if (entry.getMod().equals(parent.getSelectedEntry().getMod())) {
					setSelected(entry);
				}
			}
		} else {
			if (getSelected() == null && !children().isEmpty() && getEntry(0) != null) {
				setSelected(getEntry(0));
			}
		}

		if (getScrollAmount() > Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4))) {
			setScrollAmount(Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4)));
		}
	}

	public void addChildMod(Mod child, List<Mod> validChildren, ParentEntry parent, List<ModListEntry> parents, String searchTerm, int parentCount) {
		if (ModMenu.PARENT_MAP.keySet().contains(child) && hasVisibleChildMods(child)) {
			//Add parent mods when not searching
			List<Mod> childChildren = ModMenu.PARENT_MAP.get(child);
			childChildren.sort(ModMenu.getConfig().SORTING.get().getComparator());
			ChildParentEntry childParentEntry = new ChildParentEntry(
					child,
					parent,
					parents,
					childChildren,
					this,
					validChildren.indexOf(child) == validChildren.size() - 1
			);
			this.addEntry(childParentEntry);
			//Add children if they are meant to be shown
			if (this.parent.showModChildren.contains(child.getId())) {
				List<Mod> validChildChildren = ModSearch.search(this.parent, searchTerm, childChildren);
				for (Mod childChild : validChildChildren) {
					List<ModListEntry> childParents = new ArrayList<>(parents);
					childParents.add(childParentEntry);
					addChildMod(childChild, validChildChildren, parent, childParents, searchTerm, parentCount + 1);
				}
			}
		} else {
			this.addEntry(new ChildEntry(
					child,
					parent,
					parents,
					this,
					validChildren.indexOf(child) == validChildren.size() - 1
			));
		}
	}

	@Override
	protected void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		int entryCount = this.getItemCount();
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer;

		for (int index = 0; index < entryCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowTop(index) + this.itemHeight;
			if (entryBottom >= this.getY() && entryTop <= this.getBottom()) {
				int entryHeight = this.itemHeight - 4;
				ModListEntry entry = this.getEntry(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (this.isSelectedItem(index)) {
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = this.getRowLeft() + rowWidth + 2;
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					RenderSystem.setShader(GameRenderer::getPositionShader);
					RenderSystem.setShaderColor(float_2, float_2, float_2, 1.0F);
					Matrix4f matrix = guiGraphics.pose().last().pose();
					MeshData builtBuffer;
					buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
					buffer.addVertex(matrix, entryLeft, entryTop + entryHeight + 2, 0.0F);
					buffer.addVertex(matrix, selectionRight, entryTop + entryHeight + 2, 0.0F);
					buffer.addVertex(matrix, selectionRight, entryTop - 2, 0.0F);
					buffer.addVertex(matrix, entryLeft, entryTop - 2, 0.0F);
					try {
						builtBuffer = buffer.buildOrThrow();
						BufferUploader.drawWithShader(builtBuffer);
						builtBuffer.close();
					} catch (Exception e) {
						// Ignored
					}
					RenderSystem.setShader(GameRenderer::getPositionShader);
					RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);
					buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
					buffer.addVertex(matrix, entryLeft + 1, entryTop + entryHeight + 1, 0.0F);
					buffer.addVertex(matrix, selectionRight - 1, entryTop + entryHeight + 1, 0.0F);
					buffer.addVertex(matrix, selectionRight - 1, entryTop - 1, 0.0F);
					buffer.addVertex(matrix, entryLeft + 1, entryTop - 1, 0.0F);
					try {
						builtBuffer = buffer.buildOrThrow();
						BufferUploader.drawWithShader(builtBuffer);
						builtBuffer.close();
					} catch (Exception e) {
						// Ignored
					}
				}

				entryLeft = this.getRowLeft();
				boolean isHovered = this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry);
				if (isHovered && !draggingEntries.isEmpty() && !draggingEntries.contains(entry)) {
					guiGraphics.pose().pushPose();
					guiGraphics.pose().translate(0, 0, 200);
					if (mouseY < entryBottom - itemHeight / 2f) {
						guiGraphics.fill(entryLeft, entryTop, entryLeft + getRowWidth(), entryTop + 5, 0xFFFF0000);
					} else {
						guiGraphics.fill(entryLeft, entryBottom - 5, entryLeft + getRowWidth(), entryBottom, 0xFF00FF00);
					}
					guiGraphics.pose().popPose();
				}
				entry.render(guiGraphics,
					index,
					entryTop,
					entryLeft,
					rowWidth,
					entryHeight,
					mouseX,
					mouseY,
					isHovered,
					delta
				);
			}
		}
	}

	public void ensureVisible(ModListEntry entry) {
		super.ensureVisible(entry);
	}

	@Override
	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		super.updateScrollingState(double_1, double_2, int_1);
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPosition() && double_1 < (double) (this.getScrollbarPosition() + 6);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.updateScrollingState(mouseX, mouseY, button);
		if (!this.isMouseOver(mouseX, mouseY)) {
			return false;
		} else {
			ModListEntry entry = this.getEntryAtPos(mouseX, mouseY);
			if (entry != null) {
				if (entry.mouseClicked(mouseX, mouseY, button)) {
					this.setFocused(entry);
					this.setDragging(true);
					return true;
				}
			} else if (button == 0 && this.clickedHeader((int) (mouseX - (double) (this.getX() + this.width / 2 - this.getRowWidth() / 2)),
				(int) (mouseY - (double) this.getY()) + (int) this.getScrollAmount() - 4
			)) {
				return true;
			}

			return this.scrolling;
		}
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (ModMenu.getConfig().EDITOR_MODE.get() && draggingEntries.isEmpty()) {
			double originalX = mouseX - dragX;
			double originalY = mouseY - dragY;
			if (!this.isMouseOver(originalX, originalY)) {
				return false;
			} else {
				ModListEntry entry = this.getEntryAtPos(originalX, originalY);
				if (entry != null) {
					draggingEntries.clear();
					draggingEntries.add(entry);
					return true;
				}
			}
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {

		if (!draggingEntries.isEmpty()) {
			if (isMouseOver(mouseX, mouseY)) {

				int int_5 = Mth.floor(mouseY - (double) this.getY()) - this.headerHeight + (int) this.getScrollAmount() - 4;
				int index = int_5 / this.itemHeight;
				ModListEntry entry = mouseX < (double) this.getScrollbarPosition() && mouseX >= (double) getRowLeft() && mouseX <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ?
						this.children().get(index) :
						null;
				if (entry != null && !draggingEntries.contains(entry)) {
					List<Mod> draggedMods = draggingEntries.stream().map(ModListEntry::getMod).toList();
					for (Map.Entry<Mod, Mod> listEntry : List.copyOf(ModMenu.PARENT_MAP.entries())) {
						if (draggedMods.contains(listEntry.getValue())) {
							ModMenu.PARENT_MAP.remove(listEntry.getKey(), listEntry.getValue());
						}
					}
					int rowTop = getRowTop(index);
					if (mouseY > rowTop + itemHeight / 2f) {
						ModMenu.PARENT_MAP.putAll(entry.getMod(), draggedMods);
					}
					reloadFilters();
					ModMenu.getConfig().saveParents();
				}
			}
			draggingEntries.clear();
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
		if (!draggingEntries.isEmpty()) {
			int iconSize = 40;
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 200);
			draggingEntries.forEach(entry -> entry.renderIcon(guiGraphics, mouseX - iconSize / 2, mouseY - iconSize / 2, iconSize));
			guiGraphics.pose().popPose();
		}
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
		if (getSelected() != null) {
			return getSelected().keyPressed(keyCode, scanCode, modifiers);
		}
		return false;
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = Mth.floor(y - (double) this.getY()) - this.headerHeight + (int) this.getScrollAmount() - 4;
		int index = int_5 / this.itemHeight;
		return x < (double) this.getScrollbarPosition() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ?
			this.children().get(index) :
			null;
	}

	public int getIndexAtY(double y) {
		int int_5 = Mth.floor(y - (double) this.getY()) - this.headerHeight + (int) this.getScrollAmount() - 4;
		return int_5 / this.itemHeight;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4)) > 0 ? 18 : 12);
	}

	@Override
	public int getRowLeft() {
		return this.getX() + 6;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.getY();
	}

	public ModsScreen getParent() {
		return parent;
	}

	@Override
	protected int getMaxPosition() {
		return super.getMaxPosition() + 4;
	}

	public int getDisplayedCountFor(Set<String> set) {
		int count = 0;
		for (ModListEntry c : children()) {
			if (set.contains(c.getMod().getId())) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void close() {
		iconHandler.close();
	}

	public NeoforgeIconHandler getNeoforgeIconHandler() {
		return iconHandler;
	}

	@Override
	public int getRowBottom(int index) {
		return super.getRowBottom(index);
	}

	@Override
	public int getRowTop(int index) {
		return super.getRowTop(index);
	}

	@Override
	public ModListEntry getEntry(int index) {
		return super.getEntry(index);
	}
}
