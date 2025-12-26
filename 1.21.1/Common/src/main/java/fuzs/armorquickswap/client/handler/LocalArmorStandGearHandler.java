package fuzs.armorquickswap.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LocalArmorStandGearHandler {

    public static EventResult onUseInteraction(Minecraft minecraft, LocalPlayer player, InteractionHand interactionHand, HitResult hitResult) {

        // only run when missing on the server, as the server-side implementation is much safer
        if (ModLoaderEnvironment.INSTANCE.isModPresentServerside(ArmorQuickSwap.MOD_ID)) return EventResult.PASS;

        if (hitResult.getType() == HitResult.Type.ENTITY
                && ((EntityHitResult) hitResult).getEntity() instanceof ArmorStand armorStand
                && player.isShiftKeyDown()) {

            AbstractContainerMenu containerMenu = player.containerMenu;
            Slot slot = findInventorySlot(containerMenu, player.getInventory().selected);
            if (slot == null) {
                return EventResult.PASS;
            }

            // we pick up the selected item, which sets it to the cursor carried stack for the inventory menu (which is always open for the player while no other container menu is)
            // this is like a temporary storage that doesn't require using a different inventory slot, as we need the selected slot for interacting with the armor stand
            ItemStack itemStack = slot.getItem();
            boolean hasItemInHand = slot.hasItem();

            if (hasItemInHand) {

                // When the selected item is picked up here, a copied item stack is set as the carried stack, while the original has its count set to zero.
                // This is a problem for areas of the game that cache the held stack, since they will now want to update as the count of the stack they are holding on to has changed.
                // This mainly affects the first-person item renderer as well as the selected item tooltip above the hotbar.
                // Setting the count back to what it was does not trigger an update for those mentioned, and as the selected stack is returned at the end of this method, no update should trigger.
                int selectedItemCount = itemStack.getCount();
                minecraft.gameMode.handleInventoryMouseClick(containerMenu.containerId,
                        slot.index,
                        InputConstants.MOUSE_BUTTON_LEFT,
                        ClickType.PICKUP,
                        player);
                itemStack.setCount(selectedItemCount);
            }

            for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {

                // just do this for swapping armor, no intention for supporting hand items since armor stand arms are disabled in vanilla anyway
                if (equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {

                    Slot armorSlot = findInventorySlot(containerMenu,
                            equipmentSlot.getIndex(player.getInventory().items.size()));
                    if (armorSlot == null) {
                        continue;
                    }

                    boolean playerHasArmor = armorSlot.hasItem();
                    boolean armorStandHasArmor = armorStand.hasItemInSlot(equipmentSlot);

                    if (playerHasArmor || armorStandHasArmor) {

                        // if we are wearing an armor piece corresponding to the armor stand part we are about to click swap it to the selected slot,
                        // so we can place or swap with the armor stand equipment
                        minecraft.gameMode.handleInventoryMouseClick(containerMenu.containerId,
                                armorSlot.index,
                                slot.getContainerSlot(),
                                ClickType.SWAP,
                                player);

                        Vec3 hitVector = hitResult.getLocation();

                        if (!playerHasArmor) {

                            // set the y-value on the hit vector to a value corresponding to the current equipment slot
                            // we don't need this when the player is holding an armor item to swap with; vanilla will select the correct piece from the stand
                            hitVector = new Vec3(hitVector.x(),
                                    armorStand.getY() + getEquipmentClickHeight(armorStand, equipmentSlot),
                                    hitVector.z());
                        }

                        minecraft.gameMode.interactAt(player,
                                armorStand,
                                new EntityHitResult(armorStand, hitVector),
                                interactionHand);
                        // also perform interaction client-side to avoid potential desyncs
                        // this ignores disabled slots as the client doesn't know them, but that should be a rare scenario
                        interactAt(armorStand, player, hitVector.subtract(armorStand.position()), interactionHand);

                        if (!armorStandHasArmor && minecraft.player.hasInfiniteMaterials()) {

                            // creative mode doesn't remove armor item from hand if the armor stand has nothing to switch with (clicked armor stand equipment slot is empty)
                            // so we delete the armor item that was duplicated manually from the player hand so our loop may continue
                            containerMenu.getSlot(slot.index).setByPlayer(ItemStack.EMPTY);
                            minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, slot.index);
                        }

                        // if the amor stand had equipment where we clicked we are holding that piece now, so set it to our armor slot
                        minecraft.gameMode.handleInventoryMouseClick(containerMenu.containerId,
                                armorSlot.index,
                                slot.getContainerSlot(),
                                ClickType.SWAP,
                                player);
                    }
                }
            }

            if (hasItemInHand) {

                // set back the originally selected item to the main hand slot which we parked as the cursor carried stack, so we can freely use the selected slot
                minecraft.gameMode.handleInventoryMouseClick(containerMenu.containerId,
                        slot.index,
                        InputConstants.MOUSE_BUTTON_LEFT,
                        ClickType.PICKUP,
                        player);
            }

            // manually swing the player hand since the event won't do it when cancelled
            player.swing(interactionHand);
            return EventResult.INTERRUPT;
        }

        return EventResult.PASS;
    }

    @Nullable
    public static Slot findInventorySlot(AbstractContainerMenu containerMenu, int slotIndex) {
        // do not rely on hardcoded slot numbers, instead go out and search for the correct slot
        // container menu slots vs inventory slots really is a mess, so probably better to take this approach
        for (Slot slot : containerMenu.slots) {
            if (slot instanceof CreativeModeInventoryScreen.SlotWrapper slotWrapper) {
                slot = slotWrapper.target;
            }

            if (slot.container instanceof Inventory && slot.getContainerSlot() == slotIndex) {
                return slot;
            }
        }

        return null;
    }

    /**
     * @see ArmorStand#getClickedSlot(Vec3)
     */
    private static double getEquipmentClickHeight(ArmorStand armorStand, EquipmentSlot equipmentSlot) {
        boolean isSmallArmorStand = armorStand.isSmall();
        return switch (equipmentSlot) {
            case FEET -> Mth.lerp(0.5, 0.1, 0.1 + (isSmallArmorStand ? 0.8 : 0.45));
            case CHEST -> Mth.lerp(0.5, 0.9 + (isSmallArmorStand ? 0.3 : 0.0), 0.9 + (isSmallArmorStand ? 1.0 : 0.7));
            case LEGS -> Mth.lerp(0.5, 0.4, 0.4 + (isSmallArmorStand ? 1.0 : 0.8));
            case HEAD -> Mth.lerp(0.5, 1.6, EntityType.ARMOR_STAND.getHeight());
            default -> throw new IllegalArgumentException();
        } * armorStand.getScale() * armorStand.getAgeScale();
    }

    private static InteractionResult interactAt(ArmorStand armorStand, Player player, Vec3 hitVector, InteractionHand interactionHand) {
        ItemStack itemInHand = player.getItemInHand(interactionHand);
        if (!armorStand.isMarker() && !itemInHand.is(Items.NAME_TAG)) {
            if (player.isSpectator()) {
                return InteractionResult.SUCCESS;
            } else {
                // cut out the client check here so the client also changes the gear
                if (itemInHand.isEmpty()) {
                    EquipmentSlot equipmentSlot = getClickedSlot(armorStand, hitVector);
                    if (armorStand.hasItemInSlot(equipmentSlot) && swapItem(armorStand,
                            player,
                            equipmentSlot,
                            itemInHand,
                            interactionHand)) {
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    EquipmentSlot equipmentSlot = player.getEquipmentSlotForItem(itemInHand);
                    if (equipmentSlot.getType() == EquipmentSlot.Type.HAND && !armorStand.isShowArms()) {
                        return InteractionResult.FAIL;
                    }

                    if (swapItem(armorStand, player, equipmentSlot, itemInHand, interactionHand)) {
                        return InteractionResult.SUCCESS;
                    }
                }

                return InteractionResult.PASS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    /**
     * @see ArmorStand#getClickedSlot(Vec3)
     */
    private static EquipmentSlot getClickedSlot(ArmorStand armorStand, Vec3 hitVector) {
        boolean isSmallArmorStand = armorStand.isSmall();
        double hitLocationHeight = hitVector.y / (armorStand.getScale() * armorStand.getAgeScale());
        if (hitLocationHeight >= 0.1 && hitLocationHeight < 0.1 + (isSmallArmorStand ? 0.8 : 0.45)
                && armorStand.hasItemInSlot(EquipmentSlot.FEET)) {
            return EquipmentSlot.FEET;
        } else if (hitLocationHeight >= 0.9 + (isSmallArmorStand ? 0.3 : 0.0) && hitLocationHeight < 0.9 + (
                isSmallArmorStand ? 1.0 : 0.7) && armorStand.hasItemInSlot(EquipmentSlot.CHEST)) {
            return EquipmentSlot.CHEST;
        } else if (hitLocationHeight >= 0.4 && hitLocationHeight < 0.4 + (isSmallArmorStand ? 1.0 : 0.8)
                && armorStand.hasItemInSlot(EquipmentSlot.LEGS)) {
            return EquipmentSlot.LEGS;
        } else if (hitLocationHeight >= 1.6 && armorStand.hasItemInSlot(EquipmentSlot.HEAD)) {
            return EquipmentSlot.HEAD;
        } else if (!armorStand.hasItemInSlot(EquipmentSlot.MAINHAND)
                && armorStand.hasItemInSlot(EquipmentSlot.OFFHAND)) {
            return EquipmentSlot.OFFHAND;
        } else {
            return EquipmentSlot.MAINHAND;
        }
    }

    private static boolean swapItem(ArmorStand armorStand, Player player, EquipmentSlot equipmentSlot, ItemStack itemStack, InteractionHand interactionHand) {
        ItemStack itemInSlot = armorStand.getItemBySlot(equipmentSlot);
        if (player.getAbilities().instabuild && itemInSlot.isEmpty() && !itemStack.isEmpty()) {
            armorStand.setItemSlot(equipmentSlot, itemStack.copyWithCount(1));
            return true;
        } else if (!itemStack.isEmpty() && itemStack.getCount() > 1) {
            if (!itemInSlot.isEmpty()) {
                return false;
            } else {
                armorStand.setItemSlot(equipmentSlot, itemStack.split(1));
                return true;
            }
        } else {
            armorStand.setItemSlot(equipmentSlot, itemStack);
            player.setItemInHand(interactionHand, itemInSlot);
            return true;
        }
    }
}
