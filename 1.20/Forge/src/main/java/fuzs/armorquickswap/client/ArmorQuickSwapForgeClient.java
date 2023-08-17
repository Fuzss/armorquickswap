package fuzs.armorquickswap.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.armorquickswap.ArmorQuickSwap;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.ScreenMouseEvents;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

@Mod.EventBusSubscriber(modid = ArmorQuickSwap.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ArmorQuickSwapForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwapClient::new);
    }
    
    private static final Map<Class<?>, UnaryOperator<Slot>> HANDLES = Maps.newIdentityHashMap();

    private static Slot unwrapSlot(Slot slot) {
        if (slot.getClass() != Slot.class) {
            return (Slot) HANDLES.computeIfAbsent(slot.getClass(), clazz -> {
                for (Field field : slot.getClass().getDeclaredFields()) {
                    if (Slot.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        try {
                            MethodHandle methodHandle = MethodHandles.lookup().unreflectGetter(field);
                            return s -> {
                                try {
                                    return unwrapSlot((Slot) methodHandle.invoke(s));
                                } catch (Throwable e) {
                                    throw new RuntimeException(e);
                                }
                            };
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                return UnaryOperator.identity();
            }).apply(slot);
        }
        Objects.requireNonNull(slot, "slot is null");
        return slot;
    }

    private static void registerHandlers() {

        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register((screen, mouseX, mouseY, button) -> {
            if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                Slot slot = screen.getSlotUnderMouse();
                if (slot.getItem().getItem() instanceof ArmorItem item) {
                    Minecraft minecraft = screen.getMinecraft();
                    LocalPlayer player = minecraft.player;
//                    screen.getMinecraft().gameMode.handleInventoryMouseClick(screen.getMenu().containerId, unwrapSlot(slot).index, item.getEquipmentSlot().getIndex(Inventory.INVENTORY_SIZE), ClickType.SWAP, player);
//                    return EventResult.INTERRUPT;
                    Slot fromSlot = null;
                    int armorId = item.getEquipmentSlot().getIndex(Inventory.INVENTORY_SIZE);
                    Inventory inventory = player.getInventory();
                    for (Slot slot1 : screen.getMenu().slots) {
                        slot1 = unwrapSlot(slot1);
                        if (slot1.container == inventory && slot1.getContainerSlot() == armorId) {
                            fromSlot = slot1;
                            break;
                        }
                    }
                    slot = unwrapSlot(slot);
                    if (fromSlot != null && slot.container == inventory) {
                        if (minecraft.gameMode.hasInfiniteItems()) {
                            ItemStack itemstack3 = inventory.getItem(slot.getContainerSlot());
                            ItemStack itemstack6 = fromSlot.getItem();
                            inventory.setItem(slot.getContainerSlot(), itemstack6);
                            fromSlot.setByPlayer(itemstack3);
                            fromSlot.onTake(player, itemstack6);
                            minecraft.gameMode.handleCreativeModeItemAdd(itemstack6, slot.index);
                            minecraft.gameMode.handleCreativeModeItemAdd(itemstack3, fromSlot.index);
                        } else {
                            minecraft.gameMode.handleInventoryMouseClick(screen.getMenu().containerId, fromSlot.index, slot.getContainerSlot(), ClickType.SWAP, player);
                        }
                        return EventResult.INTERRUPT;
                    }
                }
            }
            return EventResult.PASS;
        });

        MinecraftForge.EVENT_BUS.addListener((final InputEvent.InteractionKeyMappingTriggered evt) -> {

            Minecraft minecraft = Minecraft.getInstance();
            if (evt.isUseItem() && minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.ENTITY) {

                EntityHitResult hitResult = (EntityHitResult) minecraft.hitResult;
                Entity entity = hitResult.getEntity();
                if (minecraft.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {

                    LocalPlayer player = minecraft.player;
                    if (entity instanceof ArmorStand armorStand && player.isShiftKeyDown()) {

                        // sized(0.5F, 1.975F)

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
                                    Vec3 hitVector = hitResult.getLocation();
                                    hitVector = new Vec3(hitVector.x(), entity.getY() + getEquipmentClickHeight(equipmentSlot, armorStand.isSmall()), hitVector.z());
                                    minecraft.gameMode.interactAt(player, entity, new EntityHitResult(hitResult.getEntity(), hitVector), evt.getHand());
                                    hitVector = hitVector.subtract(entity.getX(), entity.getY(), entity.getZ());
//                                    interactAt(armorStand, player, hitVector, evt.getHand());
                                    if (armorStandHasArmor) {
                                        minecraft.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, armorSlot, inventory.selected, ClickType.SWAP, player);
                                    } else if (minecraft.gameMode.hasInfiniteItems()) {
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
                            minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, slotNum);
                        }

                        evt.setCanceled(true);
                    }


                }
            }
        });
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

    public static InteractionResult interactAt(ArmorStand armorStand, Player player, Vec3 hitVector, InteractionHand interactionHand) {
        ItemStack itemstack = player.getItemInHand(interactionHand);
        if (!armorStand.isMarker() && !itemstack.is(Items.NAME_TAG)) {
            if (player.isSpectator()) {
                return InteractionResult.SUCCESS;
            } else if (false && player.level().isClientSide) {
                return InteractionResult.CONSUME;
            } else {
                EquipmentSlot equipmentslot = Mob.getEquipmentSlotForItem(itemstack);
                if (itemstack.isEmpty()) {
                    EquipmentSlot equipmentslot1 = getClickedSlot(armorStand, hitVector);
                    EquipmentSlot equipmentslot2 = equipmentslot1;
                    if (armorStand.hasItemInSlot(equipmentslot2) && swapItem(armorStand, player, equipmentslot2, itemstack, interactionHand)) {
                        return InteractionResult.SUCCESS;
                    }
                } else {
//                    if (armorStand.isDisabled(equipmentslot)) {
//                        return InteractionResult.FAIL;
//                    }

                    if (equipmentslot.getType() == EquipmentSlot.Type.HAND && !armorStand.isShowArms()) {
                        return InteractionResult.FAIL;
                    }

                    if (swapItem(armorStand, player, equipmentslot, itemstack, interactionHand)) {
                        return InteractionResult.SUCCESS;
                    }
                }

                return InteractionResult.PASS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private static EquipmentSlot getClickedSlot(ArmorStand armorStand, Vec3 hitVector) {
        EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
        boolean bl = armorStand.isSmall();
        double d = bl ? hitVector.y * 2.0 : hitVector.y;
        EquipmentSlot equipmentSlot2 = EquipmentSlot.FEET;
        if (d >= 0.1 && d < 0.1 + (bl ? 0.8 : 0.45) && armorStand.hasItemInSlot(equipmentSlot2)) {
            equipmentSlot = EquipmentSlot.FEET;
        } else if (d >= 0.9 + (bl ? 0.3 : 0.0) && d < 0.9 + (bl ? 1.0 : 0.7) && armorStand.hasItemInSlot(EquipmentSlot.CHEST)) {
            equipmentSlot = EquipmentSlot.CHEST;
        } else if (d >= 0.4 && d < 0.4 + (bl ? 1.0 : 0.8) && armorStand.hasItemInSlot(EquipmentSlot.LEGS)) {
            equipmentSlot = EquipmentSlot.LEGS;
        } else if (d >= 1.6 && armorStand.hasItemInSlot(EquipmentSlot.HEAD)) {
            equipmentSlot = EquipmentSlot.HEAD;
        } else if (!armorStand.hasItemInSlot(EquipmentSlot.MAINHAND) && armorStand.hasItemInSlot(EquipmentSlot.OFFHAND)) {
            equipmentSlot = EquipmentSlot.OFFHAND;
        }

        return equipmentSlot;
    }

    private static boolean swapItem(ArmorStand armorStand, Player player, EquipmentSlot slot, ItemStack stack, InteractionHand hand) {
        ItemStack itemStack = armorStand.getItemBySlot(slot);
        if (player.getAbilities().instabuild && itemStack.isEmpty() && !stack.isEmpty()) {
            armorStand.setItemSlot(slot, stack.copyWithCount(1));
            return true;
        } else if (!stack.isEmpty() && stack.getCount() > 1) {
            if (!itemStack.isEmpty()) {
                return false;
            } else {
                armorStand.setItemSlot(slot, stack.split(1));
                return true;
            }
        } else {
            armorStand.setItemSlot(slot, stack);
            player.setItemInHand(hand, itemStack);
            return true;
        }
    }
}
