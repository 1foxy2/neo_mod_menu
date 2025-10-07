package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.*;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadge;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import com.terraformersmc.modmenu.util.mod.neoforge.NeoforgeIconHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
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
			this.restoreScrollY = list.scrollAmount();
		}
	}

	@Override
	public void setScrollAmount(double amount) {
		super.setScrollAmount(amount);
		int denominator = Math.max(0, this.contentHeight() - (this.getBottom() - this.getY() - 4));
		if (denominator == 0) {
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
				.saySystemNow(Component.translatable("narrator.select", mod.getTranslatedName()));
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

	protected boolean isSelectedItem(int index) {
		ModListEntry selected = getSelected();
        ModListEntry entry = this.getEntry(index);
        return selected != null && entry != null && selected.getMod().getId().equals(entry.getMod().getId());
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

    @Nullable
    public ModListEntry getEntry(int index) {
        if (this.children().size() > index) {
            return this.children().get(index);
        }

        return null;
    }

	@Override
	protected void removeEntry(ModListEntry entry) {
		addedMods.remove(entry.mod);
        super.removeEntry(entry);
	}

    @Override
    public void clearEntries() {
        this.setSelected(null);
        addedMods.clear();
        super.clearEntries();
    }

	protected void remove(int index) {
        ModListEntry entry = this.children().get(index);
        addedMods.remove(entry.mod);
        super.removeEntry(entry);
	}

	public void finalizeInit() {
		reloadFilters();
		if (restoreScrollY != null) {
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
		}

		if (this.mods == null || refresh) {
			this.mods = new ArrayList<>();
			this.mods.addAll(mods);
			this.mods.sort(ModMenu.getConfig().SORTING.get().getComparator());
		}

		for (Mod mod : ModSearch.search(parent, searchTerm, this.mods)) {
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

		if (parent.getSelectedEntry() != null && !children().isEmpty() ||
				this.getSelected() != null && getSelected().getMod() != parent.getSelectedEntry().getMod()) {
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
		int entryLeft = this.getRowLeft();
		int entryWidth = this.getRowWidth();
		int entryHeight = this.defaultEntryHeight - 4;
        int x = this.getX();
        int y = this.getY();
        int yOffset = 2;
		int entryCount = this.getItemCount();
		for (int index = 0; index < entryCount; ++index) {
			int entryTop = this.getRowTop(index) + 2;
			int entryBottom = this.getRowBottom(index);
			if (entryBottom >= y && entryTop <= this.getBottom()) {
				ModListEntry entry = this.getEntry(index);
                if (entry == null) continue;
				if (this.isSelectedItem(index)) {
					int entryContentLeft = entryLeft + entry.getXOffset() - 2;
					int entryContentWidth = entryWidth - entry.getXOffset() + 4;
					this.renderSelection(
							guiGraphics,
							entryContentLeft,
							entryTop + yOffset,
							entryContentWidth,
							entryHeight,
							this.isFocused() ? CommonColors.WHITE : CommonColors.GRAY, CommonColors.BLACK
					);
				}
                entry.setYOffset(yOffset);
				entry.renderContent(
					guiGraphics,
					mouseX,
					mouseY,
					this.isMouseOver(mouseX, mouseY) && Objects.equals(this.getEntryAtPos(mouseX, mouseY), entry),
					delta
				);
			}
		}
	}

	/**
	 * Version of {@link #renderSelection(GuiGraphics, AbstractSelectionList.Entry, int)} with unconstrained positioning and sizing.
	 */
	protected void renderSelection(GuiGraphics context, int x, int y, int width, int height, int borderColor, int fillColor) {
		context.fill(x, y - 2, x + width, y + height + 2, borderColor);
		context.fill(x + 1, y - 1, x + width - 1, y + height + 1, fillColor);
	}

    public void ensureVisible(ModListEntry entry) {
        int i = this.getRowTop(this.children().indexOf(entry));
        int j = i - this.getY() - 4 - this.defaultEntryHeight;
        if (j < 0) {
            this.setScrollAmount(this.scrollAmount() + j);
        }

        int k = this.getBottom() - i - (this.defaultEntryHeight * 2);
        if (k < 0) {
            this.setScrollAmount(this.scrollAmount() - k);
        }
    }

	public boolean keyPressed(KeyEvent event) {
		if (event.isUp() || event.isDown()) {
			return super.keyPressed(event);
		}

		if (getSelected() != null) {
			return getSelected().keyPressed(event);
		}

		return false;
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = Mth.floor(y - (double) this.getY()) + (int) this.scrollAmount() - 4;
		int index = int_5 / this.defaultEntryHeight;
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
