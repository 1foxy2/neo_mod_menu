package com.terraformersmc.modmenu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.util.ImageData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforge.client.gui.modlist.ImageResource;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NeoforgeIconHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeIconHandler");

	public static final Map<String, ImageResource> modResourceIconCache = new HashMap<>();
	public static final ImageResource UNKNOWN = ImageResource.packAsset(
			Identifier.fromNamespaceAndPath(ModMenu.NAMESPACE, "unknown_icon.png"));

	public static ImageData createIcon(String modId, ModDisplayInfo displayInfo, boolean small) {

			ImageResource imageResource;
			if (NeoforgeIconHandler.modResourceIconCache.containsKey(modId)) {
				imageResource = NeoforgeIconHandler.modResourceIconCache.get(modId);
			} else {
				imageResource = small ? displayInfo.icon() : displayInfo.banner();
			}
			String type = small ? "icon" : "banner";
			IoSupplier<InputStream> resource = null;
			if (imageResource != null) {
				resource = imageResource.get(Minecraft.getInstance().getResourceManager());
			}

			boolean unknown = false;
			if (resource == null) {
				resource = UNKNOWN.get(Minecraft.getInstance().getResourceManager());
				unknown = true;
			}


			final NativeImage image;
			try (InputStream imageStream = resource.get()) {
				image = NativeImage.read(imageStream);
			} catch (IOException e) {
				LOGGER.warn("Failed to load {} resource {} for mod ID {}", type, imageResource, modId);
				return null;
			}

			final TextureManager textureManager = Minecraft.getInstance().getTextureManager();
			final Identifier sprite = Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, "mod/" + type + "/" + modId);
			textureManager.register(sprite, new DynamicTexture(sprite::toString, image));
			return new ImageData(sprite, image.getWidth(), image.getHeight(), unknown);
	}
}
