package fuzs.armorquickswap.handler;

import com.google.common.collect.ImmutableMap;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import fuzs.puzzleslib.api.event.v1.data.DefaultedValue;
import fuzs.puzzleslib.api.event.v1.data.MutableInt;
import fuzs.puzzleslib.api.event.v1.data.MutableValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Iterator;
import java.util.Map;

public class GrindstoneDisenchantHandler {
    private static final Item BOOK_ITEM = Items.ENCHANTED_BOOK;

    public static EventResult onGrindstoneUpdate(ItemStack topInput, ItemStack bottomInput, MutableValue<ItemStack> output, MutableInt experienceReward, Player player) {
        if (topInput.isEnchanted() && bottomInput.is(BOOK_ITEM)) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(topInput);
            ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(enchantments, itemStack);
            output.accept(itemStack);
        } else if (topInput.is(Items.ENCHANTED_BOOK) && bottomInput.is(BOOK_ITEM)) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(topInput);
            Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantments.entrySet().iterator();
            if (!iterator.hasNext()) return EventResult.PASS;
            ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
            EnchantmentHelper.setEnchantments(ImmutableMap.ofEntries(iterator.next()), itemStack);
            output.accept(itemStack);
        }
        experienceReward.accept(0);
        return EventResult.ALLOW;
    }

    public static void onGrindstoneUse(DefaultedValue<ItemStack> topInput, DefaultedValue<ItemStack> bottomInput, Player player) {
        if (topInput.get().isEnchanted() && bottomInput.get().is(BOOK_ITEM)) {
            topInput.accept(topInput.get().copy());
            topInput.get().removeTagKey(ItemStack.TAG_ENCH);
        } else if (topInput.get().is(Items.ENCHANTED_BOOK) && bottomInput.get().is(BOOK_ITEM)) {
            ItemStack itemStack = topInput.get().copy();
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
            Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantments.entrySet().iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
            itemStack.removeTagKey(EnchantedBookItem.TAG_STORED_ENCHANTMENTS);
            if (enchantments.isEmpty()) {
                CompoundTag tag = itemStack.getTag();
                itemStack = new ItemStack(Items.BOOK, itemStack.getCount());
                itemStack.setTag(tag);
            } else {
                EnchantmentHelper.setEnchantments(enchantments, itemStack);
            }
            topInput.accept(itemStack);
        }
    }
}
