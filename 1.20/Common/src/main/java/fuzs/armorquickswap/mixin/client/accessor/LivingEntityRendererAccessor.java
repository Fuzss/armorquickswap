package fuzs.armorquickswap.mixin.client.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor<T extends LivingEntity, M extends EntityModel<T>> {

    @Invoker("scale")
    void armorquickswap$callScale(T livingEntity, PoseStack matrixStack, float partialTickTime);

    @Accessor("layers")
    List<RenderLayer<T, M>> armorquickswap$getLayers();

    @Invoker("getBob")
    float armorquickswap$callGetBob(T livingBase, float partialTick);
}
