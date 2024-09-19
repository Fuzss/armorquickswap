package fuzs.armorquickswap.mixin.accessor;

import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ArmorStand.class)
public interface ArmorStandAccessor {

    @Accessor("disabledSlots")
    int armorquickswap$getDisabledSlots();
}
