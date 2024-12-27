package fuzs.armorquickswap.handler;

import fuzs.armorquickswap.mixin.accessor.ArmorStandAccessor;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ArmorStandGearHandler {

    public static EventResultHolder<InteractionResult> onUseEntityAt(Player player, Level level, InteractionHand interactionHand, Entity entity, Vec3 hitVector) {

        if (entity instanceof ArmorStand armorStand && player.isShiftKeyDown() && !armorStand.isMarker() &&
                !player.isSpectator()) {

            if (!level.isClientSide) {

                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {

                    if (equipmentSlot.isArmor()) {

                        swapItem(armorStand, player, equipmentSlot);
                    }
                }

                // manually trigger hand swing on server-side, so it works for vanilla clients when connected to a server with the mod
                player.swing(interactionHand, true);
            }

            // also must prevent the swing from playing on the client
            return EventResultHolder.interrupt(InteractionResult.CONSUME);
        }

        return EventResultHolder.pass();
    }

    private static boolean swapItem(ArmorStand armorStand, Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        ItemStack itemInSlot = armorStand.getItemBySlot(slot);
        // we respect disabled slots on the server, this is not possible to do with the client-side implementation,
        // since the field is not synced to the client, the interaction should just fail then
        int disabledSlots = ((ArmorStandAccessor) armorStand).armorquickswap$getDisabledSlots();
        if ((disabledSlots & 1 << slot.getFilterBit(0)) != 0) {
            return false;
        } else if (!stack.isEmpty() && stack.getCount() > 1) {
            if (!itemInSlot.isEmpty()) {
                return false;
            } else {
                armorStand.setItemSlot(slot, stack.split(1));
                return true;
            }
        } else {
            armorStand.setItemSlot(slot, stack);
            player.setItemSlot(slot, itemInSlot);
            return true;
        }
    }
}
