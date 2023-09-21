package fuzs.armorquickswap.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "checkTotemDeathProtection", at = @At(value = "LOAD", ordinal = 0), ordinal = 0)
    private ItemStack checkTotemDeathProtection(ItemStack itemStack, DamageSource damageSource) {
        if (itemStack == null) {
            Container inventory = null;
            if (this instanceof InventoryCarrier inventoryCarrier) {
                inventory = inventoryCarrier.getInventory();
            } else if (LivingEntity.class.cast(this) instanceof Player player) {
                inventory = player.getInventory();
            }
            if (inventory != null) {
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack inventoryItem = inventory.getItem(i);
                    if (inventoryItem.is(Items.TOTEM_OF_UNDYING)) {
                        itemStack = inventoryItem.copy();
                        inventoryItem.shrink(1);
                        inventory.setItem(i, inventoryItem);
                        if (LivingEntity.class.cast(this) instanceof Player player) {
                            player.containerMenu.broadcastChanges();
                        }
                        break;
                    }
                }
            }
        }
        return itemStack;
    }
}
