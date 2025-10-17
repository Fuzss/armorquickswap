package fuzs.armorquickswap.client.handler;

import com.mojang.blaze3d.platform.InputConstants;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class InventoryArmorClickHandler {

    public static EventResult onBeforeMouseClick(AbstractContainerScreen<?> screen, MouseButtonEvent mouseButtonEvent) {
        if (mouseButtonEvent.button() != InputConstants.MOUSE_BUTTON_RIGHT) {
            return EventResult.PASS;
        }

        Slot hoveredSlot = screen.getHoveredSlot(mouseButtonEvent.x(), mouseButtonEvent.y());
        if (hoveredSlot != null) {
            ItemStack itemStack = hoveredSlot.getItem();
            if (itemStack.has(DataComponents.EQUIPPABLE) && !itemStack.isStackable()) {
                if (hoveredSlot instanceof CreativeModeInventoryScreen.SlotWrapper slotWrapper) {
                    hoveredSlot = slotWrapper.target;
                }

                Inventory inventory = screen.minecraft.player.getInventory();
                if (hoveredSlot.container != inventory) {
                    return EventResult.PASS;
                }

                int armorSlotIndex = itemStack.get(DataComponents.EQUIPPABLE)
                        .slot()
                        .getIndex(inventory.getNonEquipmentItems().size());
                Slot armorSlot = LocalArmorStandGearHandler.findInventorySlot(screen.getMenu(), armorSlotIndex);
                if (armorSlot != null && !ItemStack.isSameItemSameComponents(itemStack, armorSlot.getItem())) {
                    swapInventorySlots(screen.minecraft.gameMode, screen.minecraft.player, armorSlot, hoveredSlot);
                    return EventResult.INTERRUPT;
                }
            }
        }

        return EventResult.PASS;
    }

    private static void swapInventorySlots(MultiPlayerGameMode gameMode, Player player, Slot destinationSlot, Slot clickedSlot) {
        if (player.hasInfiniteMaterials()) {
            swapCreativeInventorySlots(player, destinationSlot, clickedSlot);
        } else {
            swapSurvivalInventorySlots(gameMode, player, destinationSlot, clickedSlot);
        }
    }

    private static void swapCreativeInventorySlots(Player player, Slot destinationSlot, Slot clickedSlot) {
        ItemStack hoveredItemStack = clickedSlot.getItem();
        ItemStack armorItemStack = destinationSlot.getItem();
        player.getInventory().setItem(clickedSlot.getContainerSlot(), armorItemStack.copy());
        player.getInventory().setItem(destinationSlot.getContainerSlot(), hoveredItemStack.copy());
        player.inventoryMenu.broadcastChanges();
    }

    /**
     * Minecraft 1.20.4 introduced a fun limitation in
     * {@link net.minecraft.world.inventory.AbstractContainerMenu#doClick(int, int, ClickType, Player)} where the slot
     * being swapped with (the second slot in the method call) can only be from the hotbar or offhand.
     * <p>
     * Previously, freely swapping with any other inventory slot was possible.
     *
     * @param gameMode        the local game mode controller
     * @param player          the player
     * @param destinationSlot the slot the item will be but in / swapped with
     * @param clickedSlot     the item slot that was clicked
     */
    private static void swapSurvivalInventorySlots(MultiPlayerGameMode gameMode, Player player, Slot destinationSlot, Slot clickedSlot) {
        if (clickedSlot.getContainerSlot() >= 0 && clickedSlot.getContainerSlot() < Inventory.getSelectionSize()) {
            gameMode.handleInventoryMouseClick(player.containerMenu.containerId,
                    destinationSlot.index,
                    clickedSlot.getContainerSlot(),
                    ClickType.SWAP,
                    player);
        } else {
            gameMode.handleInventoryMouseClick(player.containerMenu.containerId,
                    clickedSlot.index,
                    InputConstants.MOUSE_BUTTON_LEFT,
                    ClickType.PICKUP,
                    player);
            gameMode.handleInventoryMouseClick(player.containerMenu.containerId,
                    destinationSlot.index,
                    InputConstants.MOUSE_BUTTON_LEFT,
                    ClickType.PICKUP,
                    player);
            gameMode.handleInventoryMouseClick(player.containerMenu.containerId,
                    clickedSlot.index,
                    InputConstants.MOUSE_BUTTON_LEFT,
                    ClickType.PICKUP,
                    player);
        }
    }
}
