package fuzs.armorquickswap.client;

import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ArmorStandHandler {

    public static EventResultHolder<InteractionResult> onUseEntityAt(Player player, Level level, InteractionHand interactionHand, Entity entity, Vec3 hitVector) {

        if (level.isClientSide && entity instanceof ArmorStand armorStand && player.isShiftKeyDown()) {

            Minecraft minecraft = Minecraft.getInstance();

            Inventory inventory = player.getInventory();
            int selectedSlot = InventoryMenu.USE_ROW_SLOT_START + inventory.selected;
            ItemStack selectedItem = inventory.getSelected();
            boolean holdingItem = !selectedItem.isEmpty();
            if (holdingItem) {
                int selectedItemCount = selectedItem.getCount();
                minecraft.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, selectedSlot, 0, ClickType.PICKUP, player);
                selectedItem.setCount(selectedItemCount);
            }

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                if (equipmentSlot.isArmor()) {
                    boolean playerHasArmor = !inventory.getArmor(equipmentSlot.getIndex()).isEmpty();
                    boolean armorStandHasArmor = armorStand.hasItemInSlot(equipmentSlot);
                    if (playerHasArmor || armorStandHasArmor) {

                        int armorSlot = InventoryMenu.ARMOR_SLOT_END - 1 - equipmentSlot.getIndex();
                        if (playerHasArmor) {
                            minecraft.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, armorSlot, inventory.selected, ClickType.SWAP, player);
                        }
                        Vec3 hitVector2 = hitVector.add(entity.position());
                        hitVector2 = new Vec3(hitVector2.x(), entity.getY() + ArmorStandEquipmentHelper.getEquipmentClickHeight(equipmentSlot, armorStand.isSmall()), hitVector2.z());
                        minecraft.gameMode.interactAt(player, entity, new EntityHitResult(entity, hitVector2), interactionHand);
                        if (armorStandHasArmor) {
                            minecraft.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, armorSlot, inventory.selected, ClickType.SWAP, player);
                        } else if (minecraft.gameMode.hasInfiniteItems()) {
                            // creative mode doesn't remove armor item from hand if the armor stand has nothing to switch with (armor stand slot is empty)
                            // so we delete the armor item that was duplicated manually from the player hand so our loop may continue
                            int slotNum = InventoryMenu.USE_ROW_SLOT_START + inventory.selected;
                            player.inventoryMenu.getSlot(slotNum).setByPlayer(ItemStack.EMPTY);
                            minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, slotNum);
                        }
                    }
                }
            }

            if (holdingItem) {
                minecraft.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, selectedSlot, 0, ClickType.PICKUP, player);

            } else if (!minecraft.gameMode.hasInfiniteItems()) {
                //
                int slotNum = InventoryMenu.USE_ROW_SLOT_START + inventory.selected;
                player.inventoryMenu.getSlot(slotNum).setByPlayer(ItemStack.EMPTY);
            }

            player.swing(interactionHand);

            return EventResultHolder.interrupt(InteractionResult.FAIL);
        }

        return EventResultHolder.pass();
    }
}
