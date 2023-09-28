package fuzs.armorquickswap.client.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public enum AdvancementBackgroundType {
    STONE("stone", 100, 0, 100, 100),
    DIRT("dirt", 100, 90, 100, 40),
    COAL_ORE("coal_ore", DIRT.minHeight, 0, 10),
    IRON_ORE("iron_ore", DIRT.minHeight, 0, 8),
    COPPER_ORE("copper_ore", DIRT.minHeight, 0, 5),
    GOLD_ORE("gold_ore", 50, 0, 3),
    LAPIS_ORE("lapis_ore", 50, 0, 3),
    REDSTONE_ORE("redstone_ore", 50, 0, 3),
    DIAMOND_ORE("diamond_ore", 25, 0, 1),
    BEDROCK("bedrock", 10, 0, 40, 100);

    private static final AdvancementBackgroundType[] VALUES = AdvancementBackgroundType.values();

    private final ResourceLocation background;
    private final int maxHeight;
    private final int minHeight;
    private final int heightRange;
    private final int topWeight;
    private final int bottomWeight;
    private final int weightRange;

    AdvancementBackgroundType(String background, int maxHeight, int minHeight, int weight) {
        this(background, maxHeight, minHeight, weight, weight);
    }

    AdvancementBackgroundType(String background, int maxHeight, int minHeight, int topWeight, int bottomWeight) {
        this.background = new ResourceLocation("textures/block/" + background + ".png");
        this.maxHeight = maxHeight;
        this.minHeight = minHeight;
        this.heightRange = maxHeight - minHeight;
        this.topWeight = topWeight;
        this.bottomWeight = bottomWeight;
        this.weightRange = topWeight - bottomWeight;
    }

    public int getWeightAtHeight(int height) {
        if (height >= this.minHeight && height <= this.maxHeight) {
            float scaledHeight = (float) (height - this.minHeight) / this.heightRange;
            return this.topWeight + (int) (scaledHeight * this.weightRange);
        }
        return 0;
    }

    public static AdvancementBackgroundType getBackgroundAtHeight(RandomSource random, int height) {
        if (height == 100) {
            return DIRT;
        } else if (height == 0) {
            return BEDROCK;
        } else {
            int totalWeight = 0;
            for (AdvancementBackgroundType type : VALUES) {
                totalWeight += type.getWeightAtHeight(height);
            }
            totalWeight = (int) (totalWeight * random.nextDouble());
            for (AdvancementBackgroundType type : VALUES) {
                totalWeight -= type.getWeightAtHeight(height);
                if (totalWeight <= 0) return type;
            }
            return STONE;
        }
    }

    public ResourceLocation getBackground() {
        return this.background;
    }
}
