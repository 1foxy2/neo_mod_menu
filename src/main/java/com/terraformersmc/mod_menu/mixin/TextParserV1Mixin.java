package com.terraformersmc.mod_menu.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.minecraftforge.fml.ModList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TextParserV1.class)
public class TextParserV1Mixin {
    @WrapOperation(
            method = "register",
            at = @At(
                    value = "FIELD",
                    target = "Leu/pb4/placeholders/api/parsers/TextParserV1;allowOverrides:Z",
                    opcode = Opcodes.GETFIELD
            ),
            remap = false
    )
    private boolean removeCrash(TextParserV1 instance, Operation<Boolean> original) {
        if (ModList.get().isLoaded("goeticlegacy")) {
            return true;
        }
        return original.call(instance);
    }
}
