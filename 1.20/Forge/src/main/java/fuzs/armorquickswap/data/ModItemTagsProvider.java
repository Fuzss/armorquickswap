package fuzs.armorquickswap.data;

import fuzs.armorquickswap.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

public class ModItemTagsProvider extends AbstractTagProvider.Items {

    public ModItemTagsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.HOLDABLE_WHILE_ROWING_ITEM_TAG).add(Items.FILLED_MAP);
    }
}
