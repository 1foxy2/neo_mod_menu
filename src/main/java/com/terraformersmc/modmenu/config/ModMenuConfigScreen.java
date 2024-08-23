package com.terraformersmc.modmenu.config;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.terraformersmc.modmenu.ModMenu;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TranslatableEnum;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModMenuConfigScreen extends ConfigurationScreen.ConfigurationSectionScreen {
    private static final OptionInstance.CaptionBasedToString<Boolean> BOOLEAN_TO_STRING = (component, aBoolean) -> {
        if (component.getContents() instanceof TranslatableContents contents) return Component.translatable(contents.getKey() + "." + aBoolean);
        return Component.empty();
    };

    private static final String LANG_PREFIX = "neoforge.configuration.uitext.";
    private static final String SECTION = LANG_PREFIX + "section";
    private static final String SECTION_TEXT = LANG_PREFIX + "sectiontext";
    protected static final ConfigurationScreen.TranslationChecker translationChecker = new ConfigurationScreen.TranslationChecker();

    public ModMenuConfigScreen(Screen parent, ModConfig.Type type, ModConfig modConfig, Component title) {
        super(parent, type, modConfig, title);
    }

    public ModMenuConfigScreen(Context parentContext, Screen parent, Map<String, Object> valueSpecs, String key, Set<? extends UnmodifiableConfig.Entry> entrySet, Component title) {
        super(parentContext, parent, valueSpecs, key, entrySet, title);
    }


    @Nullable
    @Override
    protected Element createSection(String key, UnmodifiableConfig subconfig, UnmodifiableConfig subsection) {
        if (subconfig.isEmpty()) return null;
        return new Element(Component.translatable(SECTION, getTranslationComponent(key)), getTooltipComponent(key, null),
                Button.builder(Component.translatable(SECTION, Component.translatable(translationChecker.check(getTranslationKey(key) + ".button", SECTION_TEXT))),
                                button -> minecraft.setScreen(sectionCache.computeIfAbsent(key,
                                        k -> new ModMenuConfigScreen(context, this, subconfig.valueMap(), key, subsection.entrySet(), Component.translatable(getTranslationKey(key))).rebuild())))
                        .tooltip(Tooltip.create(getTooltipComponent(key, null)))
                        .width(Button.DEFAULT_WIDTH)
                        .build(),
                false);
    }

    @Nullable
    @Override
    protected Element createBooleanValue(String key, ModConfigSpec.ValueSpec spec, Supplier<Boolean> source, Consumer<Boolean> target) {
        if (key.contains("modify") || key.contains("config_mode") || key.contains("drag_and_drop"))
            return super.createBooleanValue(key, spec, source, target);

        return new Element(getTranslationComponent(key), getTooltipComponent(key, null),
                new OptionInstance<>(getTranslationKey(key), getTooltip(key, null), BOOLEAN_TO_STRING,
                        Custom.BOOLEAN_VALUES_NO_PREFIX, source.get(), key.contains("count") ?
                        newValue -> {
                            ModMenu.clearModCountCache();
                    // regarding change detection: new value always is different (cycle button)
                    undoManager.add(v -> {
                        target.accept(v);
                        onChanged(key);
                    }, newValue, v -> {
                        target.accept(v);
                        onChanged(key);
                    }, source.get());
                } :
                        newValue -> {
                    // regarding change detection: new value always is different (cycle button)
                    undoManager.add(v -> {
                        target.accept(v);
                        onChanged(key);
                    }, newValue, v -> {
                        target.accept(v);
                        onChanged(key);
                    }, source.get());
                }));
    }

    @Nullable
    @Override
    protected <T extends Enum<T>> Element createEnumValue(String key, ModConfigSpec.ValueSpec spec, Supplier<T> source, Consumer<T> target) {
        final Class<T> clazz = (Class<T>) spec.getClazz();
        assert clazz != null;

        final List<T> list = Arrays.stream(clazz.getEnumConstants()).filter(spec::test).toList();

        return new Element(getTranslationComponent(key), getTooltipComponent(key, null),
                new OptionInstance<>(getTranslationKey(key), getTooltip(key, null), (caption, displayvalue) -> Component.translatable("modmenu.configuration." + key + "." + displayvalue.name().toLowerCase()),
                        new Custom<>(list), source.get(), newValue -> {
                    // regarding change detection: new value always is different (cycle button)
                    undoManager.add(v -> {
                        target.accept(v);
                        onChanged(key);
                    }, newValue, v -> {
                        target.accept(v);
                        onChanged(key);
                    }, source.get());
                }));
    }
}
