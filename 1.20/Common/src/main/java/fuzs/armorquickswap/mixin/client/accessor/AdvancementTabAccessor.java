package fuzs.armorquickswap.mixin.client.accessor;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementTab.class)
public interface AdvancementTabAccessor {

    @Accessor("scrollX")
    double armorquickswap$getScrollX();

    @Accessor("scrollY")
    double armorquickswap$getScrollY();

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

    @Accessor("fade")
    void armorquickswap$setFade(float fade);

    @Accessor("widgets")
    Map<Advancement, AdvancementWidget> armorquickswap$getWidgets();
}
