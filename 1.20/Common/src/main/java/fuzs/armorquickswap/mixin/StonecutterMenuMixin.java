package fuzs.armorquickswap.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StonecutterMenu.class)
abstract class StonecutterMenuMixin extends AbstractContainerMenu {
    @Shadow
    @Final
    private DataSlot selectedRecipeIndex;
    @Shadow
    private ItemStack input;
    @Shadow
    long lastSoundTime;
    @Shadow
    @Final
    Slot inputSlot;
    @Shadow
    @Final
    @Mutable
    Slot resultSlot;
    @Shadow
    @Final
    ResultContainer resultContainer;
    @Nullable
    private Item armorquickswap$lastItem;
    private int armorquickswap$lastRecipeIndex;
    private int armorquickswap$movedStacks;

    protected StonecutterMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    public void init(int containerId, Inventory playerInventory, final ContainerLevelAccess access, CallbackInfo callback) {
        int index = this.resultSlot.index;
        this.resultSlot = new Slot(this.resultContainer, 1, 143, 33) {
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            public void onTake(Player player, ItemStack stack) {
                stack.onCraftedBy(player.level(), player, stack.getCount());
                ItemStack itemStack = StonecutterMenuMixin.this.inputSlot.getItem();
                for (int i = 0, k = slots.size(); i < k; i++) {
                    int j = (i + 2) % k;
                    if (ItemStack.isSameItem(slots.get(j).getItem(), itemStack)) {
                        StonecutterMenuMixin.this.resultContainer.awardUsedRecipes(player, List.of(slots.get(j).getItem()));
                        slots.get(j).remove(1);
                        break;
                    }
                }

                if (!itemStack.isEmpty()) {
                    StonecutterMenuMixin.this.setupResultSlot();
                }

                access.execute((level, blockPos) -> {
                    long l = level.getGameTime();
                    if (StonecutterMenuMixin.this.lastSoundTime != l) {
                        level.playSound(null, blockPos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
                        StonecutterMenuMixin.this.lastSoundTime = l;
                    }

                });
                super.onTake(player, stack);
            }
        };
        this.resultSlot.index = index;
        this.slots.set(index, this.resultSlot);
    }

    @Inject(method = "slotsChanged", at = @At("HEAD"))
    public void slotsChanged$0(Container container, CallbackInfo callback) {
        ItemStack itemStack = this.inputSlot.getItem();
        if (!itemStack.is(this.input.getItem()) && itemStack.isEmpty()) {
            this.armorquickswap$lastItem = this.input.getItem();
            this.armorquickswap$lastRecipeIndex = this.selectedRecipeIndex.get();
        }
    }

    @Inject(method = "slotsChanged", at = @At("TAIL"))
    public void slotsChanged$1(Container container, CallbackInfo callback) {
        if (!this.inputSlot.getItem().isEmpty() && this.inputSlot.getItem().is(this.armorquickswap$lastItem)) {
            this.armorquickswap$lastItem = null;
            this.selectedRecipeIndex.set(this.armorquickswap$lastRecipeIndex);
            this.setupResultSlot();
        }
    }

    @Shadow
    abstract void setupResultSlot();

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    public void quickMoveStack$0(Player player, int index, CallbackInfoReturnable<ItemStack> callback) {
        if (player.level().getGameTime() == this.lastSoundTime) {
            if (++this.armorquickswap$movedStacks > this.slots.get(index).getMaxStackSize()) callback.setReturnValue(ItemStack.EMPTY);
        } else {
            this.armorquickswap$movedStacks = 1;
        }
    }

    @Inject(method = "quickMoveStack", at = @At("TAIL"), cancellable = true)
    public void quickMoveStack$1(Player player, int index, CallbackInfoReturnable<ItemStack> callback) {
        this.lastSoundTime = player.level().getGameTime();
    }

//    @Override
//    public void clicked(int slotId, int button, ClickType clickType, Player player) {
//
//        // TODO add crash handling back in here
//
//    if ((clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
//        if (slotId != -999) {
//            if (slotId < 0) {
//                return;
//            }
//
//            Slot slot6 = this.slots.get(slotId);
//            if (!slot6.mayPickup(player)) {
//                return;
//            }
//
//            int i = 1;
//            for(ItemStack itemstack8 = this.quickMoveStack(player, slotId); !itemstack8.isEmpty() && ItemStack.isSameItem(slot6.getItem(), itemstack8) && i < 64; itemstack8 = this.quickMoveStack(player, slotId)) {
//                i++;
//            }
//            return;
//            }
//        }
//
//        super.clicked(slotId, button, clickType, player);
//    }
}
