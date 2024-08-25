package com.terraformersmc.mod_menu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import com.terraformersmc.mod_menu.ModMenu;
import com.terraformersmc.mod_menu.gui.widget.entries.ModListEntry;
import net.minecraft.client.renderer.texture.DynamicTexture;
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

	private final Map<Path, Tuple<DynamicTexture, Dimension>> modIconCache = new HashMap<>();

	public Tuple<DynamicTexture, Dimension> createIcon(ModContainer iconSource, String iconPath) {
		try {
			Path path = iconSource.getModInfo().getOwningFile().getFile().findResource(iconPath);
			Tuple<DynamicTexture, Dimension> cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			cachedIcon = getCachedModIcon(path);
			if (cachedIcon != null) {
				return cachedIcon;
			}
			try (InputStream inputStream = Files.newInputStream(path)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Tuple<DynamicTexture, Dimension> tex = new Tuple<>(new DynamicTexture(image),
						new Dimension(image.getWidth(), image.getHeight())) ;
				cacheModIcon(path, tex);
				return tex;
			}
		} catch (IllegalStateException e) {
			return null;
		} catch (Throwable t) {
			if (!iconPath.equals("assets/" + iconSource.getModId() + "/icon.png") && !iconPath.equals("icon.png") && !iconPath.contains("_small.png")) {
				LOGGER.error("Invalid mod icon for icon source {}: {}", iconSource.getModId(), iconPath, t);
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

	Tuple<DynamicTexture, Dimension> getCachedModIcon(Path path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(Path path, Tuple<DynamicTexture, Dimension> tex) {
		modIconCache.put(path, tex);
	}
}
