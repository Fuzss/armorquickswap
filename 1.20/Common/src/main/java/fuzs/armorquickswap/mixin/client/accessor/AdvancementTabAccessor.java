package fuzs.armorquickswap.mixin.client.accessor;

import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AdvancementTab.class)
public interface AdvancementTabAccessor {

    @Accessor("scrollX")
    void armorquickswap$setScrollX(double scrollX);

    @Accessor("scrollY")
    void armorquickswap$setScrollY(double scrollY);

    @Accessor("minX")
    int armorquickswap$getMinX();

    @Accessor("minY")
    int armorquickswap$getMinY();

    @Accessor("maxX")
    int armorquickswap$getMaxX();

    @Accessor("maxY")
    int armorquickswap$getMaxY();

    @Accessor("centered")
    void armorquickswap$setCentered(boolean centered);
}
