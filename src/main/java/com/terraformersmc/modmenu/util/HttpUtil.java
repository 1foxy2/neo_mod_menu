package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.SharedConstants;
import net.neoforged.fml.ModList;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtil {
	private static final String USER_AGENT = buildUserAgent();
	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

	private HttpUtil() {
	}

	public static <T> HttpResponse<T> request(
		HttpRequest.Builder builder,
		HttpResponse.BodyHandler<T> handler
	) throws IOException, InterruptedException {
		builder.setHeader("User-Agent", USER_AGENT);
		return HTTP_CLIENT.send(builder.build(), handler);
	}

	private static String buildUserAgent() {
		String env = ModMenu.DEV_ENVIRONMENT ? "/development" : "";
		String loader = ModMenu.HAS_SINYTRA ? "quilt" : "fabric";

		var modMenuVersion = getModMenuVersion();
		var minecraftVersion = SharedConstants.getCurrentVersion().getName();

		// -> TerraformersMC/ModMenu/9.1.0 (1.20.3/quilt/development)
		return "%s/%s (%s/%s%s)".formatted(ModMenu.GITHUB_REF, modMenuVersion, minecraftVersion, loader, env);
	}

	private static String getModMenuVersion() {
		var container = ModList.get().getModContainerById(ModMenu.MOD_ID);

		if (container.isEmpty()) {
			throw new RuntimeException("Unable to find Modmenu's own mod container!");
		}

		return VersionUtil.removeBuildMetadata(container.get().getModInfo().getVersion().getQualifier());
	}
}
