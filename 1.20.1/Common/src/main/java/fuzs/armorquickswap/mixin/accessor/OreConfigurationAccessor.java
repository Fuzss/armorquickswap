package fuzs.armorquickswap.mixin.accessor;

import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(OreConfiguration.class)
public interface OreConfigurationAccessor {

    @Accessor("targetStates")
    @Mutable
    void armorquickswap$setTargetStates(List<OreConfiguration.TargetBlockState> targetStates);
}
