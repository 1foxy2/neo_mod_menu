package com.terraformersmc.modmenu.util;

import com.google.common.base.Joiner;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.modlist.ImageResource;
import net.neoforged.neoforge.client.gui.modlist.ModDisplayInfo;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.List;

public class ModMenuDisplayInfo implements ModDisplayInfo {
    private final Mod mod;

    public ModMenuDisplayInfo(Mod mod) {
        this.mod = mod;
    }

    @Override
    public String id() {
        return mod.getId();
    }

    @Override
    public Component displayName() {
        return Component.literal(mod.getTranslatedName());
    }

    @Override
    public String version() {
        return mod.getPrefixedVersion();
    }

    @Override
    public Component authors() {
        List<String> names = mod.getAuthors();
        if (names.isEmpty()) {
            return Component.empty();
        }
        String authors;
        if (names.size() > 1) {
            authors = Joiner.on(", ").join(names);
        } else {
            authors = names.getFirst();
        }
        return Component.literal(authors);
    }

    @Override
    public Component credits() {
        return Component.literal(mod.getForgeCredits());
    }

    @Override
    public Component description() {
        return mod.getFormattedDescription();
    }

    @Override
    public Component license() {
        return Component.empty();
    }

    @Override
    @Nullable
    public ImageResource banner() {
        return convertPath(mod.getIconPath(false));
    }

    @Override
    @Nullable
    public ImageResource icon() {
        return convertPath(mod.getIconPath(true));
    }

    public ImageResource convertPath(String path) {
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
            IModFileInfo modFileInfo = ModList.get().getModFileById(id());
            String packId = ResourcePackLoader.getPackName(modFileInfo.getFile());
            return ImageResource.packRoot(packId, path);
        }
    }

    @Override
    public @Nullable URI displayUrl() {
        String uri = mod.getWebsite();
        if (uri == null) {
            return null;
        }

        return URI.create(uri);
    }

    @Override
    public @Nullable URI issuesUrl() {
        String uri = mod.getIssueTracker();
        if (uri == null) {
            return null;
        }

        return URI.create(mod.getIssueTracker());
    }
}
