package fuzs.armorquickswap.mixin.client.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("levelCallback")
    EntityInLevelCallback armorquickswap$getLevelCallback();

    @Invoker("unsetRemoved")
    void armorquickswap$callUnsetRemoved();
}
