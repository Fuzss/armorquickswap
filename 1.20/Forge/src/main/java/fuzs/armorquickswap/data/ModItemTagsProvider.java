package fuzs.armorquickswap.data;

import fuzs.armorquickswap.init.ModRegistry;
import fuzs.puzzleslib.api.data.v1.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraftforge.data.event.GatherDataEvent;

public class ModItemTagsProvider extends AbstractTagProvider.Items {

    public ModItemTagsProvider(GatherDataEvent evt, String modId) {
        super(evt, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.HOLDABLE_WHILE_ROWING_ITEM_TAG).add(Items.FILLED_MAP);
    }
}
