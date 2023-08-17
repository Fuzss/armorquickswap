package fuzs.armorquickswap.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.api.client.event.v1.ScreenMouseEvents;
import fuzs.puzzleslib.api.client.screen.v2.ScreenHelper;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

public class ArmorQuickSwapClient implements ClientModConstructor {

    @Override
    public void onConstructMod() {

        if (!ModLoaderEnvironment.INSTANCE.isForge()) {
            PlayerInteractEvents.USE_ENTITY_AT.register(ArmorStandHandler::onUseEntityAt);
        }

        ScreenMouseEvents.beforeMouseClick(AbstractContainerScreen.class).register((screen, mouseX, mouseY, button) -> {
            if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                Slot slot = ContainerScreenHelper.findSlot(screen, mouseX, mouseY);
                if (slot.getItem().getItem() instanceof ArmorItem item) {
                    Minecraft minecraft = ScreenHelper.INSTANCE.getMinecraft(screen);
                    LocalPlayer player = minecraft.player;
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
}
