package fuzs.armorquickswap.init;

import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.armorquickswap.capability.FireOverlayCapability;
import fuzs.puzzleslib.api.capability.v2.CapabilityController;
import fuzs.puzzleslib.api.capability.v2.data.CapabilityKey;
import fuzs.puzzleslib.api.init.v3.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.BoundTagFactory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModRegistry {
    static final RegistryManager REGISTRY = RegistryManager.from(ArmorQuickSwap.MOD_ID);

    static final BoundTagFactory TAGS = BoundTagFactory.make(ArmorQuickSwap.MOD_ID);
    public static final TagKey<Block> REPLANTABLES_BLOCK_TAG = TAGS.registerBlockTag("replantables");
    public static final TagKey<Block> CLICK_THROUGH_BLOCK_TAG = TAGS.registerBlockTag("click_through");
    public static final TagKey<Item> HOLDABLE_WHILE_ROWING_ITEM_TAG = TAGS.registerItemTag("holdable_while_rowing");
    public static final TagKey<EntityType<?>> CLICK_THROUGH_ENTITY_TYPE_TAG = TAGS.registerEntityTypeTag("click_through");

    static final CapabilityController CAPABILITY = CapabilityController.from(ArmorQuickSwap.MOD_ID);
    public static final CapabilityKey<FireOverlayCapability> FIRE_OVERLAY_CAPABILITY = CAPABILITY.registerEntityCapability("fire_overlay", FireOverlayCapability.class, FireOverlayCapability::new, Entity.class);

    public static void touch() {

    }
}
