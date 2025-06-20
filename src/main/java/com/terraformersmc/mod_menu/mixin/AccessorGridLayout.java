package com.terraformersmc.mod_menu.mixin;

import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(GridLayout.class)
public interface AccessorGridLayout {
	@Accessor
	List<LayoutElement> getChildren();
}
