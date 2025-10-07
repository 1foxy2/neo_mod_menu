package com.terraformersmc.modmenu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.Closeable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NeoforgeIconHandler implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeIconHandler");

	private final Map<ResourceLocation, Tuple<DynamicTexture, Dimension>> modIconCache = new HashMap<>();
	public static final Map<String, Tuple<DynamicTexture, Dimension>> modResourceIconCache = new HashMap<>();

	public Tuple<DynamicTexture, Dimension> createIcon(ModContainer iconSource, String iconPath) {
		try {
			Tuple<DynamicTexture, Dimension> cachedIcon = getCachedModIcon(ResourceLocation.fromNamespaceAndPath(iconSource.getModId(), iconPath));
			if (cachedIcon != null) {
				return cachedIcon;
			}

			try (InputStream inputStream = iconSource.getModInfo().getOwningFile().getFile().getContents().openFile(iconPath)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Tuple<DynamicTexture, Dimension> tex = new Tuple<>(new DynamicTexture(() ->
						ResourceLocation.fromNamespaceAndPath(ModMenu.MOD_ID, iconPath).toString(), image),
						new Dimension(image.getWidth(), image.getHeight())) ;
				cacheModIcon(ResourceLocation.fromNamespaceAndPath(iconSource.getModId(), iconPath), tex);
				return tex;
			}
		} catch (IllegalStateException e) {
			return null;
		} catch (Throwable t) {
			if (!iconPath.equals("assets/" + iconSource.getModId() + "/icon.png") && !iconPath.equals("icon.png") && !iconPath.contains("_small.png")) {
				LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getModId(), iconPath);
			}
			return null;
		}
	}

	@Override
	public void close() {
		for (Tuple<DynamicTexture, Dimension> tex : modIconCache.values()) {
			tex.getA().close();
		}
	}

	Tuple<DynamicTexture, Dimension> getCachedModIcon(ResourceLocation path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(ResourceLocation path, Tuple<DynamicTexture, Dimension> tex) {
		modIconCache.put(path, tex);
	}
}
