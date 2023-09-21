package fuzs.armorquickswap.init;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.puzzleslib.api.init.v3.RegistryManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModRegistry {
    static final RegistryManager REGISTRY = RegistryManager.from(ArmorQuickSwap.MOD_ID);
    public static final TagKey<Block> REPLANTABLES_BLOCK_TAG = REGISTRY.registerBlockTag("replantables");
    public static final TagKey<Block> CLICK_THROUGH_BLOCK_TAG = REGISTRY.registerBlockTag("click_through");
    public static final TagKey<Item> HOLDABLE_WHILE_ROWING_ITEM_TAG = REGISTRY.registerItemTag("holdable_while_rowing");
    public static final TagKey<EntityType<?>> CLICK_THROUGH_ENTITY_TYPE_TAG = REGISTRY.registerEntityTypeTag("click_through");

    public static void touch() {

    }
}
