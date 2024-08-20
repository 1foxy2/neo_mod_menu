package com.terraformersmc.modmenu.util.mod.neoforge;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NeoforgeIconHandler implements Closeable {
	private static final Logger LOGGER = LoggerFactory.getLogger("Mod Menu | FabricIconHandler");

	private final Map<String, ResourceLocation> modIconCache = new HashMap<>();

	public DynamicTexture createIcon(ModContainer iconSource, String iconPath) {

		return (DynamicTexture) iconSource.getModInfo().getLogoFile().map(logoFile -> {
			TextureManager tm = Minecraft.getInstance().getTextureManager();
			if (modIconCache.containsKey(iconPath)) return tm.getTexture(modIconCache.get(iconPath));
			final Pack.ResourcesSupplier resourcePack = ResourcePackLoader.getPackFor(iconSource.getModId())
					.orElse(ResourcePackLoader.getPackFor("neoforge").orElseThrow(() -> new RuntimeException("Can't find neoforge, WHAT!")));
			try (PackResources packResources = resourcePack.openPrimary(new PackLocationInfo("mod/" + iconSource.getModId(), Component.empty(), PackSource.BUILT_IN, Optional.empty()))) {
				NativeImage logo = null;
				IoSupplier<InputStream> logoResource = packResources.getRootResource(logoFile.split("[/\\\\]"));
				if (logoResource != null)
					logo = NativeImage.read(logoResource.get());
				if (logo != null) {

					modIconCache.put(iconPath, tm.register("modlogo", new DynamicTexture(logo) {
						@Override
						public void upload() {
							this.bind();
							NativeImage td = this.getPixels();
							// Use custom "blur" value which controls texture filtering (nearest-neighbor vs linear)
							this.getPixels().upload(0, 0, 0, 0, 0, td.getWidth(), td.getHeight(), iconSource.getModInfo().getLogoBlur(), false, false, false);
						}
					}));

				}
			} catch (IOException | IllegalArgumentException e) {}
			return tm.getTexture(modIconCache.get(iconPath));
		}).orElse(null);
	}

	@Override
	public void close() {
		for (ResourceLocation tex : modIconCache.values()) {
			Minecraft.getInstance().getTextureManager().getTexture(tex).close();
		}
	}
}
