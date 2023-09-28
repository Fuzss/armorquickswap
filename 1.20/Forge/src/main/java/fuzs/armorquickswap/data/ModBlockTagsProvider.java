package fuzs.armorquickswap.data;

import fuzs.armorquickswap.init.ModRegistry;
import fuzs.puzzleslib.api.data.v2.AbstractTagProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

public class ModBlockTagsProvider extends AbstractTagProvider.Blocks {

    public ModBlockTagsProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.REPLANTABLES_BLOCK_TAG).add(Blocks.WHEAT, Blocks.BEETROOTS, Blocks.CARROTS, Blocks.POTATOES, Blocks.NETHER_WART, Blocks.SUGAR_CANE, Blocks.CACTUS);
        this.tag(ModRegistry.CLICK_THROUGH_BLOCK_TAG).addTag(BlockTags.WALL_SIGNS).addTag(BlockTags.BANNERS);
    }
}
