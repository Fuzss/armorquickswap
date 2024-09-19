package fuzs.armorquickswap.mixin.client.accessor;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public interface AdvancementsScreenAccessor {

    @Nullable
    @Accessor("selectedTab")
    AdvancementTab armorquickswap$getSelectedTab();

    @Accessor("tabs")
    Map<Advancement, AdvancementTab> armorquickswap$getTabs();

    @Accessor("TITLE")
    @Mutable
    static void armorquickswap$setTitle(Component title) {
        throw new RuntimeException();
    };
}
