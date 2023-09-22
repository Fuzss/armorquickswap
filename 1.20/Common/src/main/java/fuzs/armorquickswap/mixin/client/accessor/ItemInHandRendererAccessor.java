package fuzs.armorquickswap.mixin.client.accessor;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {

    @Accessor("mainHandItem")
    void armorquickswap$setMainHandItem(ItemStack mainHandItem);

    @Accessor("offHandItem")
    void armorquickswap$setOffHandItem(ItemStack offHandItem);

    @Accessor("mainHandItem")
    ItemStack armorquickswap$getMainHandItem();

    @Accessor("offHandItem")
    ItemStack armorquickswap$getOffHandItem();

    @Accessor("mainHandHeight")
    void armorquickswap$setMainHandHeight(float mainHandHeight);

    @Accessor("offHandHeight")
    void armorquickswap$setOffHandHeight(float offHandHeight);

    @Accessor("oMainHandHeight")
    float armorquickswap$getOMainHandHeight();

    @Accessor("oOffHandHeight")
    float armorquickswap$getOOffHandHeight();
}
