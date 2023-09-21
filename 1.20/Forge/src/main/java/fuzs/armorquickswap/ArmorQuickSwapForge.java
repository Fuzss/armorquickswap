package fuzs.armorquickswap;

import com.google.common.collect.ImmutableMap;
import fuzs.armorquickswap.data.ModBlockTagProvider;
import fuzs.armorquickswap.data.ModEntityTypeTagProvider;
import fuzs.armorquickswap.data.ModParticleDescriptionProvider;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ToastAddEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

import java.util.Iterator;
import java.util.Map;

@Mod(ArmorQuickSwap.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ArmorQuickSwapForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        if (ModLoaderEnvironment.INSTANCE.isServer()) return;
        ModConstructor.construct(ArmorQuickSwap.MOD_ID, ArmorQuickSwap::new);
        registerHandlers();
    }

    private static void registerHandlers() {
        MinecraftForge.EVENT_BUS.addListener((final BlockEvent.BlockToolModificationEvent evt) -> {
            if (evt.getToolAction() == ToolActions.SHOVEL_FLATTEN) {
                if (evt.getState().is(Blocks.DIRT_PATH)) {
                    evt.setFinalState(Blocks.DIRT.defaultBlockState());
                }
            } else if (evt.getToolAction() == ToolActions.AXE_STRIP) {
                if (evt.getState().is(Blocks.STRIPPED_OAK_LOG)) {
                    evt.setFinalState(Blocks.OAK_LOG.defaultBlockState());
                }
            }
        });
        Item book = Items.ENCHANTED_BOOK;
        MinecraftForge.EVENT_BUS.addListener((final GrindstoneEvent.OnPlaceItem evt) -> {
            if (evt.getTopItem().isEnchanted() && evt.getBottomItem().is(book)) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(evt.getTopItem());
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.setEnchantments(enchantments, itemStack);
                evt.setOutput(itemStack);
            } else if (evt.getTopItem().is(Items.ENCHANTED_BOOK) && evt.getBottomItem().is(book)) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(evt.getTopItem());
                Iterator<Map.Entry<Enchantment, Integer>> iterator = enchantments.entrySet().iterator();
                if (!iterator.hasNext()) return;
                ItemStack itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantmentHelper.setEnchantments(ImmutableMap.ofEntries(iterator.next()), itemStack);
                evt.setOutput(itemStack);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((final GrindstoneEvent.OnTakeItem evt) -> {
            if (evt.getTopItem().isEnchanted() && evt.getBottomItem().is(book)) {
                evt.setNewTopItem(evt.getTopItem().copy());
                evt.getNewTopItem().removeTagKey(ItemStack.TAG_ENCH);
                evt.setXp(0);
            } else if (evt.getTopItem().is(Items.ENCHANTED_BOOK) && evt.getBottomItem().is(book)) {
                ItemStack itemStack = evt.getTopItem().copy();
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
                evt.setNewTopItem(itemStack);
                evt.setXp(0);
            }
        });
    }

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent evt) {
        evt.getGenerator().addProvider(true, new ModBlockTagProvider(evt, ArmorQuickSwap.MOD_ID));
        evt.getGenerator().addProvider(true, new ModEntityTypeTagProvider(evt, ArmorQuickSwap.MOD_ID));
        evt.getGenerator().addProvider(true, new ModParticleDescriptionProvider(evt, ArmorQuickSwap.MOD_ID));
    }
}
