package com.terraformersmc.modmenu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.Closeable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NeoforgeIconHandler implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | NeoforgeIconHandler");

	private final Map<Identifier, Pair<DynamicTexture, Dimension>> modIconCache = new HashMap<>();
	public static final Map<String, Pair<DynamicTexture, Dimension>> modResourceIconCache = new HashMap<>();

	public Pair<DynamicTexture, Dimension> createIcon(ModContainer iconSource, String iconPath) {
		try {
			Pair<DynamicTexture, Dimension> cachedIcon = getCachedModIcon(Identifier.fromNamespaceAndPath(iconSource.getModId(), iconPath));
			if (cachedIcon != null) {
				return cachedIcon;
			}

			try (InputStream inputStream = iconSource.getModInfo().getOwningFile().getFile().getContents().openFile(iconPath)) {
				NativeImage image = NativeImage.read(Objects.requireNonNull(inputStream));
				Pair<DynamicTexture, Dimension> tex = new Pair<>(new DynamicTexture(() ->
						Identifier.fromNamespaceAndPath(ModMenu.MOD_ID, iconPath).toString(), image),
						new Dimension(image.getWidth(), image.getHeight())) ;
				cacheModIcon(Identifier.fromNamespaceAndPath(iconSource.getModId(), iconPath), tex);

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
		for (Pair<DynamicTexture, Dimension> tex : modIconCache.values()) {
			tex.getFirst().close();
		}
	}

	Pair<DynamicTexture, Dimension> getCachedModIcon(Identifier path) {
		return modIconCache.get(path);
	}

	void cacheModIcon(Identifier path, Pair<DynamicTexture, Dimension> tex) {
		modIconCache.put(path, tex);
	}
}
