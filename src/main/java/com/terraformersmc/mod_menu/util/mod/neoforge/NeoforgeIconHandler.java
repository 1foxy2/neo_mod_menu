package com.terraformersmc.mod_menu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.util.ImageData;
import com.terraformersmc.mod_menu.util.ImageResource;
import com.terraformersmc.mod_menu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
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
	public static final ImageResource UNKNOWN = ImageResource.packAsset(
			ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, "unknown_icon.png"));
	public static final ImageResource UNKNOWN_PARENT = ImageResource.packAsset(
			ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, "unknown_parent.png"));

	public static ImageData createIcon(Mod mod, boolean small) {
		ImageResource imageResource;
		if (NeoforgeIconHandler.modResourceIconCache.containsKey(mod.getId())) {
			imageResource = NeoforgeIconHandler.modResourceIconCache.get(mod.getId());
		} else {
			imageResource = convertPath(mod.getIconPath(small), mod);
		}
		String type = small ? "icon" : "banner";
		IoSupplier<InputStream> resource = null;
		if (imageResource != null) {
			resource = imageResource.get(Minecraft.getInstance().getResourceManager());
		}

		boolean unknown = false;
		if (resource == null) {
			resource = (mod instanceof NeoforgeDummyParentMod ? UNKNOWN_PARENT : UNKNOWN)
					.get(Minecraft.getInstance().getResourceManager());
			unknown = true;
		}


		final NativeImage image;
		try (InputStream imageStream = resource.get()) {
			image = NativeImage.read(imageStream);
		} catch (IOException e) {
			LOGGER.warn("Failed to load {} resource {} for mod ID {}", type, imageResource, mod.getId());
			return null;
		}

		final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		final ResourceLocation sprite = ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, "mod/" + type + "/" + mod.getId());
		textureManager.register(sprite, new DynamicTexture(image));
		return new ImageData(sprite, image.getWidth(), image.getHeight(), unknown);
	}

	public static ImageResource convertPath(String path, Mod mod) {
		if (path == null) {
			return null;
		} else if (path.indexOf('#') > 0) {
			// Contains a pound sign -- it's a root resource, with parts of "<pack ID>#<path>"
			String[] split = path.split("#", 2);
			return ImageResource.packRoot(split[0], split[1]);
		} else if (path.indexOf(ResourceLocation.NAMESPACE_SEPARATOR) > 0) {
			// Contains a colon, therefore an identifier -- it's a pack resource
			return ImageResource.packAsset(ResourceLocation.parse(path));
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
}
