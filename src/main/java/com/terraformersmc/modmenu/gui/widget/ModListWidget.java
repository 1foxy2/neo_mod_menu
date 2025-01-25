package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ChildEntry;
import com.terraformersmc.modmenu.gui.widget.entries.IndependentEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ParentEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.CoreShaders;
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
		}
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4));
		if (denominator <= 0) {
			parent.updateScrollPercent(0);
		} else {
			parent.updateScrollPercent(scrollAmount() / Math.max(
				0,
				this.contentHeight() - (this.getBottom() - this.getY() - 4)
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
		if (entry == null) {
			selectedModId = null;
		} else {
			selectedModId = entry.getMod().getId();
		}
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

	private void filter(String searchTerm, boolean refresh, boolean search) {
		this.clearEntries();
		addedMods.clear();
		Collection<Mod> mods = ModMenu.MODS.values().stream().filter(mod -> {
			if (ModMenu.getConfig().CONFIG_MODE.get()) {
				return !parent.getModHasConfigScreen(mod.getContainer());
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
							this.addEntry(new ChildEntry(child,
								parent,
								this,
								validChildren.indexOf(child) == validChildren.size() - 1
							));
						}
					}
				} else {
					//A mod with no children
					this.addEntry(new IndependentEntry(mod, this));
				}
			}
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

		if (scrollAmount() > Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4))) {
			setScrollAmount(Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4)));
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
					RenderSystem.setShader(CoreShaders.POSITION);
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
					RenderSystem.setShader(CoreShaders.POSITION);
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
				entry.render(guiGraphics,
					index,
					entryTop,
					entryLeft,
					rowWidth,
					entryHeight,
					mouseX,
					mouseY,
					this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry),
					delta
				);
			}
		}
	}

	public void ensureVisible(ModListEntry entry) {
		super.ensureVisible(entry);
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
		int int_5 = Mth.floor(y - (double) this.getY()) - this.headerHeight + (int) this.scrollAmount() - 4;
		int index = int_5 / this.itemHeight;
		return x < (double) this.scrollBarX() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.getItemCount() ?
			this.children().get(index) :
			null;
	}

	@Override
	protected int scrollBarX() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4)) > 0 ? 18 : 12);
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
	protected int contentHeight() {
		return super.contentHeight() + 4;
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
}
