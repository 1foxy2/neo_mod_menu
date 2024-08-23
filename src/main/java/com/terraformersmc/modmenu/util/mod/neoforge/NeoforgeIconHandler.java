package com.terraformersmc.modmenu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.neoforged.fml.ModContainer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NeoforgeIconHandler implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeIconHandler");

	private final Map<Path, DynamicTexture> modIconCache = new HashMap<>();

	public DynamicTexture createIcon(ModContainer iconSource, String iconPath) {
		try {
			Path path = iconSource.getModInfo().getOwningFile().getFile().findResource(iconPath);
			DynamicTexture cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Validate.validState(image.getHeight() == image.getWidth(), "Must be square icon");
				DynamicTexture tex =new DynamicTexture(image);
				cacheModIcon(path, tex);
				return tex;
			}

		} catch (IllegalStateException e) {
			if (e.getMessage().equals("Must be square icon")) {
				LOGGER.error("Mod icon must be a square for icon source {}: {}",
						iconSource.getModId(),
						iconPath,
						e
				);
			}

			return null;
		} catch (Throwable t) {
			if (!iconPath.equals("assets/" + iconSource.getModId() + "/icon.png") && !iconPath.equals("icon.png")) {
				LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getModId(), iconPath, t);
			}
			return null;
		}
	}

	@Override
	public void close() {
		for (DynamicTexture tex : modIconCache.values()) {
			tex.close();
		}
	}

	DynamicTexture getCachedModIcon(Path path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(Path path, DynamicTexture tex) {
		modIconCache.put(path, tex);
	}
}
