package fuzs.armorquickswap.mixin.client.accessor;

import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AdvancementWidget.class)
public interface AdvancementWidgetAccessor {

    @Nullable
    @Accessor("progress")
    AdvancementProgress armorquickswap$getProgress();

    @Accessor("width")
    int armorquickswap$getWidth();

    @Accessor("width")
    @Mutable
    void armorquickswap$setWidth(int width);

    @Accessor("description")
    List<FormattedCharSequence> armorquickswap$getDescription();

    @Accessor("description")
    @Mutable
    void armorquickswap$setDescription(List<FormattedCharSequence> description);
}
