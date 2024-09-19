package fuzs.armorquickswap.mixin.client.accessor;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementToast.class)
public interface AdvancementToastAccessor {

    @Accessor("advancement")
    Advancement armroquickswap$getAdvancement();
}
