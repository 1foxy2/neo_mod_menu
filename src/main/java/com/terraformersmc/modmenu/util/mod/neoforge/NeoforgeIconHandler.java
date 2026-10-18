package com.terraformersmc.modmenu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.ImageData;
import com.terraformersmc.modmenu.util.ImageResource;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

	public static ImageData createIcon(Mod mod, boolean small) {
		String cacheId = mod.getId() + small;
		ImageData data = modIconCache.get(cacheId);
		if (data != null) {
			return data;
		}

		ImageResource imageResource = NeoforgeIconHandler.modResourceIconCache.get(mod.getId());
		if (imageResource == null) {
			imageResource = convertPath(mod.getIconPath(small), mod);
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

	public static ImageResource convertPath(String path, Mod mod) {
		if (path == null) {
			return null;
		} else if (path.indexOf('#') > 0) {
			// Contains a pound sign -- it's a root resource, with parts of "<pack ID>#<path>"
			String[] split = path.split("#", 2);
			return ImageResource.packRoot(split[0], split[1]);
		} else if (path.indexOf(Identifier.NAMESPACE_SEPARATOR) > 0) {
			// Contains a colon, therefore an identifier -- it's a pack resource
			return ImageResource.packAsset(Identifier.parse(path));
		} else {
			// It's a root resource; get from the mod's resource pack
			if (mod.getContainer().isEmpty()) {
				return null;
			}
			IModFileInfo modFileInfo = mod.getContainer().get().getModInfo().getOwningFile();
			String packId = "mod/" + modFileInfo.getFile().getModInfos().stream().map(IModInfo::getModId).collect(Collectors.joining(","));
			return ImageResource.packRoot(packId, path);
		}
	}

	public static void close(TextureManager textureManager) {
		for (ImageData tex : modIconCache.values()) {
			textureManager.release(tex.sprite());
		}
		modIconCache.clear();
	}
}
