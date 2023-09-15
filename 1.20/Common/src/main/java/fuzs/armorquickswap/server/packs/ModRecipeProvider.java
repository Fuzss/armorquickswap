package fuzs.armorquickswap.server.packs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import fuzs.armorquickswap.ArmorQuickSwap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.apache.commons.compress.utils.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModRecipeProvider extends RecipeProvider {
    private final Collection<Map<BlockFamilyToken, Block>> blocks;

    public ModRecipeProvider(PackOutput packOutput) {
        super(packOutput);
    }

    {
        Map<String, Map<BlockFamilyToken, Block>> blocks = Maps.newHashMap();
        for (Map.Entry<ResourceKey<Block>, Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
            for (BlockFamilyToken token : BlockFamilyToken.WOOD_FAMILY) {
                String path = entry.getKey().location().getPath();
                if (token.test(path, entry.getValue(), BlockFamilyToken.IndicatorType.WOOD_FAMILY_INDICATORS)) {
                    path = token.strip(path);
                    blocks.computeIfAbsent(path, $ -> Maps.newIdentityHashMap()).merge(token, entry.getValue(), (o1, o2) -> {
                        int tokenValue1 = BlockFamilyToken.IndicatorType.value(BlockFamilyToken.IndicatorType.WOOD_FAMILY_INDICATORS, o1);
                        int tokenValue2 = BlockFamilyToken.IndicatorType.value(BlockFamilyToken.IndicatorType.WOOD_FAMILY_INDICATORS, o2);
                        return tokenValue2 > tokenValue1 ? o2 : o1;
                    });
                    break;
                }
            }
        }
        this.blocks = ImmutableSet.copyOf(blocks.values());
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> writer) {
        for (Map<BlockFamilyToken, Block> tokens : this.blocks) {
            for (BlockFamilyRecipe recipe : BlockFamilyRecipe.values()) {
                recipe.apply(tokens::get, (ingredient, result, resultCount) -> stonecutterResultFromBase(writer, RecipeCategory.BUILDING_BLOCKS, result, ingredient, resultCount));
            }
        }
    }

    public static void stonecutterResultFromBase(Consumer<FinishedRecipe> exporter, RecipeCategory category, ItemLike result, ItemLike ingredient) {
        stonecutterResultFromBase(exporter, category, result, ingredient, 1);
    }

    public static void stonecutterResultFromBase(Consumer<FinishedRecipe> exporter, RecipeCategory category, ItemLike result, ItemLike ingredient, int resultCount) {
        String recipeId = getConversionRecipeName(result, ingredient) + "_stonecutting";
        SingleItemRecipeBuilder.stonecutting(Ingredient.of(ingredient), category, result, resultCount).unlockedBy(getHasName(ingredient), has(ingredient)).save(exporter, ArmorQuickSwap.id(recipeId));
    }

    private record BlockFamilyRecipe(Function<Function<BlockFamilyToken, ItemLike>, ItemLike> ingredient, Function<Function<BlockFamilyToken, ItemLike>, ItemLike> result, int resultCount) {
        private static final Collection<BlockFamilyRecipe> VALUES = Lists.newArrayList();

        static {
            register(BlockFamilyToken.LOG, BlockFamilyToken.STRIPPED_LOG);
            register(BlockFamilyToken.LOG, BlockFamilyToken.WOOD);
            register(BlockFamilyToken.LOG, BlockFamilyToken.STRIPPED_WOOD);
            register(BlockFamilyToken.WOOD, BlockFamilyToken.STRIPPED_WOOD);
            logOrWood(BlockFamilyToken.LOG);
            logOrWood(BlockFamilyToken.STRIPPED_LOG);
            logOrWood(BlockFamilyToken.WOOD);
            logOrWood(BlockFamilyToken.STRIPPED_WOOD);
            planks(BlockFamilyToken.PLANKS, 1);
        }

        private static void logOrWood(BlockFamilyToken ingredient) {
            register(ingredient, BlockFamilyToken.PLANKS, 6);
            planks(ingredient, 6);
        }

        private static void planks(BlockFamilyToken ingredient, int resultCount) {
            register(ingredient, BlockFamilyToken.SIGN, resultCount);
            register(ingredient, BlockFamilyToken.HANGING_SIGN, resultCount);
            register(ingredient, BlockFamilyToken.PRESSURE_PLATE, resultCount);
            register(ingredient, BlockFamilyToken.TRAPDOOR, resultCount);
            register(ingredient, BlockFamilyToken.STAIRS, resultCount);
            register(ingredient, BlockFamilyToken.BUTTON, resultCount);
            register(ingredient, BlockFamilyToken.SLAB, resultCount * 2);
            register(ingredient, BlockFamilyToken.FENCE_GATE, resultCount);
            register(ingredient, BlockFamilyToken.FENCE, resultCount);
            register(ingredient, BlockFamilyToken.DOOR, resultCount);
            register(ingredient, Items.LADDER, resultCount);
            register(ingredient, Items.BOWL, resultCount);
            register(ingredient, Items.STICK, resultCount * 2);
        }

        private static void register(BlockFamilyToken ingredient, BlockFamilyToken result) {
            register(ingredient, result, 1);
        }

        private static void register(BlockFamilyToken ingredient, BlockFamilyToken result, int resultCount) {
            VALUES.add(new BlockFamilyRecipe(access -> access.apply(ingredient), access -> access.apply(result), resultCount));
        }

        private static void register(BlockFamilyToken ingredient, ItemLike result, int resultCount) {
            VALUES.add(new BlockFamilyRecipe(access -> access.apply(ingredient), access -> result, resultCount));
        }

        public static Collection<BlockFamilyRecipe> values() {
            return Collections.unmodifiableCollection(VALUES);
        }

        public void apply(Function<BlockFamilyToken, ItemLike> access, StonecutterRecipeFactory factory) {
            ItemLike ingredient = this.ingredient.apply(access);
            ItemLike result = this.result.apply(access);
            if (ingredient != null && result != null) {
                factory.apply(ingredient, result, this.resultCount);
            }
        }
    }

    private interface StonecutterRecipeFactory {

        void apply(ItemLike ingredient, ItemLike result, int resultCount);
    }
}
