package com.terraformersmc.modmenu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.ImageData;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforge.client.gui.modlist.ImageResource;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NeoforgeIconHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeIconHandler");

	public static final Map<String, ImageResource> modResourceIconCache = new HashMap<>();
	private static final Map<String, ImageData> modIconCache = new HashMap<>();
	public static final ImageResource UNKNOWN = ImageResource.packAsset(
			Identifier.fromNamespaceAndPath(ModMenu.NAMESPACE, "unknown_icon.png"));
	public static final ImageResource UNKNOWN_PARENT = ImageResource.packAsset(
			Identifier.fromNamespaceAndPath(ModMenu.NAMESPACE, "unknown_parent.png"));
	public static final ImageData EMPTY = new ImageData(Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, ""), 32, 32, true);

	public static ImageData createIcon(Mod mod, ModDisplayInfo displayInfo, boolean small) {
		String cacheId = mod.getId() + small;
		ImageData data = modIconCache.get(cacheId);
		if (data != null) {
			return data;
		}

		ImageResource imageResource = NeoforgeIconHandler.modResourceIconCache.get(mod.getId());
		if (imageResource == null) {
			imageResource = small ? displayInfo.icon() : displayInfo.banner();
		}
		String type = small ? "icon" : "banner";
		IoSupplier<InputStream> resource = null;
		if (imageResource != null) {
			try {
				resource = imageResource.get(Minecraft.getInstance().getResourceManager());
			} catch (Throwable ignored) {}
		}

		boolean unknown = false;
		if (resource == null) {
			resource = (mod instanceof NeoforgeDummyParentMod ? UNKNOWN_PARENT : UNKNOWN)
					.get(Minecraft.getInstance().getResourceManager());
			unknown = true;
		}


		NativeImage image;
		try (InputStream imageStream = resource.get()) {
			image = NativeImage.read(imageStream);
		} catch (IOException e) {
			LOGGER.warn("Failed to load {} resource {} for mod ID {}", type, imageResource, mod.getId());
			resource = (mod instanceof NeoforgeDummyParentMod ? UNKNOWN_PARENT : UNKNOWN)
					.get(Minecraft.getInstance().getResourceManager());
			unknown = true;
			try (InputStream imageStream = resource.get()) {
				image = NativeImage.read(imageStream);
			} catch (IOException e2) {
				modIconCache.put(cacheId, EMPTY);
				return EMPTY;
			}
		}

		final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		final Identifier sprite = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "mod/" + type + "/" + mod.getId());
		textureManager.register(sprite, new DynamicTexture(sprite::toString, image));
		data = new ImageData(sprite, image.getWidth(), image.getHeight(), unknown);
		modIconCache.put(cacheId, data);
		return data;
	}

	public static void close(TextureManager textureManager) {
		for (ImageData tex : modIconCache.values()) {
			textureManager.release(tex.sprite());
		}
		modIconCache.clear();
	}
}
