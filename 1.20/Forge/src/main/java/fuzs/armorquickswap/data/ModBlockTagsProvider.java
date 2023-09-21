package fuzs.armorquickswap.data;

import fuzs.armorquickswap.init.ModRegistry;
import fuzs.puzzleslib.api.data.v1.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.data.event.GatherDataEvent;

public class ModBlockTagsProvider extends AbstractTagProvider.Blocks {

    public ModBlockTagsProvider(GatherDataEvent evt, String modId) {
        super(evt, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.REPLANTABLES_BLOCK_TAG).add(Blocks.WHEAT, Blocks.BEETROOTS, Blocks.CARROTS, Blocks.POTATOES, Blocks.NETHER_WART, Blocks.SUGAR_CANE, Blocks.CACTUS);
        this.tag(ModRegistry.CLICK_THROUGH_BLOCK_TAG).addTag(BlockTags.WALL_SIGNS).addTag(BlockTags.BANNERS);
    }
}
