package fuzs.armorquickswap.mixin.client.accessor;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AgeableListModel.class)
public interface AgeableListModelAccessor {

    @Invoker("headParts")
    Iterable<ModelPart> armorquickswap$callHeadParts();

    @Invoker("bodyParts")
    Iterable<ModelPart> armorquickswap$callBodyParts();

    @Accessor("scaleHead")
    boolean armorquickswap$getScaleHead();
    
    @Accessor("babyYHeadOffset")
    float armorquickswap$getBabyYHeadOffset();
    
    @Accessor("babyZHeadOffset")
    float armorquickswap$getBabyZHeadOffset();
    
    @Accessor("babyHeadScale")
    float armorquickswap$getBabyHeadScale();
    
    @Accessor("babyBodyScale")
    float armorquickswap$getBabyBodyScale();
    
    @Accessor("bodyYOffset")
    float armorquickswap$getBodyYOffset();
}
