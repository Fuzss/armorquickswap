package fuzs.armorquickswap.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ArmorStandEquipmentHandler {

    public static EventResult onUseInteraction(Minecraft minecraft, LocalPlayer player, InteractionHand interactionHand, HitResult hitResult) {

        if (hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult) hitResult).getEntity() instanceof ArmorStand armorStand && player.isShiftKeyDown()) {

            AbstractContainerMenu containerMenu = player.containerMenu;
            Slot slot = findInventorySlot(containerMenu, player.getInventory().selected);
            if (slot == null) return EventResult.PASS;

            // we pick up the selected item, which sets it to the cursor carried stack for the inventory menu (which is always open for the player while no other container menu is)
            // this is like a temporary storage that doesn't require using a different inventory slot, as we need the selected slot for interacting with the armor stand
            ItemStack itemStack = slot.getItem();
            boolean pickUpSelectedItem = slot.hasItem();
            if (pickUpSelectedItem) {

                // when the selected item is picked up here a copied item stack instance is set as the carried stack, while the original has its count set to zero
                // this is a problem for areas of the game that cache the held stack, since they will now want to update as the count of the stack they are holding on to has changed
                // this mainly affects the first person item renderer as well as the selected item tooltip above the hotbar
                // setting the count back to what it was does not trigger an update for those mentioned, and as the selected stack is returned at the end of this method no update should trigger
                int selectedItemCount = itemStack.getCount();
                minecraft.gameMode.handleInventoryMouseClick(containerMenu.containerId, slot.index, InputConstants.MOUSE_BUTTON_LEFT, ClickType.PICKUP, player);
                itemStack.setCount(selectedItemCount);
            }

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {

                // just do this for swapping armor, no intention for supporting hand items since armor stand arms are disabled in vanilla anyway
                if (equipmentSlot.isArmor()) {

                    Slot armorSlot = findInventorySlot(containerMenu, equipmentSlot.getIndex(player.getInventory().items.size()));
                    if (armorSlot == null) continue;

                    boolean playerHasArmor = armorSlot.hasItem();
                    boolean armorStandHasArmor = armorStand.hasItemInSlot(equipmentSlot);

                    if (playerHasArmor || armorStandHasArmor) {

                        if (playerHasArmor) {

                            // if we are wearing an armor piece corresponding to the armor stand part we are about to click swap it to the selected slot,
                            // so we can place or swap with the armor stand equipment
                            minecraft.gameMode.handleInventoryMouseClick(containerMenu.containerId, armorSlot.index, slot.getContainerSlot(), ClickType.SWAP, player);
                        }

                        Vec3 hitVector = hitResult.getLocation();
                        // set the y-value on the hit vector to a value corresponding to the current equipment slot
                        hitVector = new Vec3(hitVector.x(), armorStand.getY() + getEquipmentClickHeight(equipmentSlot, armorStand.isSmall()), hitVector.z());
                        minecraft.gameMode.interactAt(player, armorStand, new EntityHitResult(armorStand, hitVector), interactionHand);

                        if (armorStandHasArmor) {

                            // if the amor stand had equipment where we clicked we are holding that piece now, so set it to our armor slot
                            minecraft.gameMode.handleInventoryMouseClick(containerMenu.containerId, armorSlot.index, slot.getContainerSlot(), ClickType.SWAP, player);
                        } else if (minecraft.gameMode.hasInfiniteItems()) {

                            // creative mode doesn't remove armor item from hand if the armor stand has nothing to switch with (clicked armor stand equipment slot is empty)
                            // so we delete the armor item that was duplicated manually from the player hand so our loop may continue
                            containerMenu.getSlot(slot.index).setByPlayer(ItemStack.EMPTY);
                            minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, slot.index);
                        }
                    }
                }
            }

            if (pickUpSelectedItem) {

                // set back the originally selected item to the main hand slot which we parked as the cursor carried stack so we can freely use the selected slot
                minecraft.gameMode.handleInventoryMouseClick(containerMenu.containerId, slot.index, InputConstants.MOUSE_BUTTON_LEFT, ClickType.PICKUP, player);
            } else if (!minecraft.gameMode.hasInfiniteItems()) {

                // this is just a cleanup call, sometimes a swapped item stays for a split second in the main hand client-side
                // this just fixes the visual artifact that comes from that
                containerMenu.getSlot(slot.index).setByPlayer(ItemStack.EMPTY);
            }

            // manually swing the player hand since the event won't do it when cancelled
            player.swing(interactionHand);

            return EventResult.INTERRUPT;
        }

        return EventResult.PASS;
    }

    @Nullable
    public static Slot findInventorySlot(AbstractContainerMenu containerMenu, int slotNum) {
        // do not rely on hardcoded slot numbers, instead go out and search for the correct slot
        // container menu slots vs inventory slots really is a mess, so probably better to take this approach
        for (Slot slot : containerMenu.slots) {
            slot = InventoryArmorClickHandler.findNestedSlot(slot);
            if (slot.container instanceof Inventory && slot.getContainerSlot() == slotNum) {
                return slot;
            }
        }
        return null;
    }

    private static double getEquipmentClickHeight(EquipmentSlot equipmentSlot, boolean isSmall) {
        return switch (equipmentSlot) {
            // clickedHeight >= 0.1D && clickedHeight < (isSmall ? 0.9D : 0.55D)
            case FEET -> isSmall ? 0.5 : 0.375;
            // clickedHeight >= (isSmall ? 1.2D : 0.9D) && clickedHeight < (isSmall ? 1.9D : 1.6D)
            case CHEST -> isSmall ? 1.55 : 1.25;
            // clickedHeight >= 0.4D && clickedHeight < (isSmall ? 1.4D : 1.2D)
            case LEGS -> isSmall ? 0.9 : 0.8;
            // clickedHeight >= 1.6D && clickedHeight < 1.975D
            case HEAD -> 1.7875;
            default -> throw new RuntimeException();
        } * (isSmall ? 0.5 : 1.0);
    }
}
