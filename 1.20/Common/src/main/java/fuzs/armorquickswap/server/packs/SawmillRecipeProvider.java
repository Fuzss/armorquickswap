package fuzs.armorquickswap.server.packs;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import fuzs.puzzleslib.api.data.v2.AbstractRecipeProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.compress.utils.Lists;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SawmillRecipeProvider extends AbstractRecipeProvider {
    private final Collection<Map<BlockFamilyToken, Item>> items;

    {
        Map<String, Map<BlockFamilyToken, Item>> blocks = Maps.newHashMap();
        for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
            BlockFamilyToken token = null;
            for (BlockFamilyToken currentToken : BlockFamilyToken.getBlockFamily(BlockFamilyToken.Type.WOOD)) {
                String path = entry.getKey().location().getPath();
                if (currentToken.test(path) && (token == null || currentToken.strip(path).length() < token.strip(path).length())) {
                    token = currentToken;
                }
            }
            if (token != null) {
                String path = entry.getKey().location().getPath();
                blocks.computeIfAbsent(token.strip(path), $ -> Maps.newIdentityHashMap()).putIfAbsent(token, entry.getValue());
            }
        }
        this.items = ImmutableSet.copyOf(blocks.values());
    }

    public SawmillRecipeProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> exporter) {
        for (Map<BlockFamilyToken, Item> tokens : this.items) {
            for (BlockFamilyRecipe recipe : BlockFamilyRecipe.values()) {
                recipe.apply(tokens::get, (ingredient, result, resultCount) -> stonecutterResultFromBase(exporter, RecipeCategory.BUILDING_BLOCKS, result, ingredient, resultCount));
            }
        }
    }

    private interface StonecutterRecipeFactory {

        void apply(ItemLike ingredient, ItemLike result, int resultCount);
    }

    private record BlockFamilyRecipe(Function<Function<BlockFamilyToken, ItemLike>, ItemLike> ingredient,
                                     Function<Function<BlockFamilyToken, ItemLike>, ItemLike> result, int resultCount) {
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
            register(ingredient, BlockFamilyToken.BOAT);
            register(ingredient, BlockFamilyToken.HANGING_SIGN);
        }

        private static void planks(BlockFamilyToken ingredient, int resultCount) {
            register(ingredient, BlockFamilyToken.SIGN, resultCount);
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
            register(ingredient, Items.STICK, resultCount * 3);
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
}
