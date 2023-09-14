package fuzs.armorquickswap;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;

public class RandomForwardingRuleTest extends RuleTest {
    private final RuleTest ruleTest;
    private final float probability;

    public RandomForwardingRuleTest(RuleTest ruleTest, float probability) {
        this.ruleTest = ruleTest;
        this.probability = probability;
    }

    @Override
    public boolean test(BlockState state, RandomSource random) {
        return random.nextFloat() < this.probability && this.ruleTest.test(state, random);
    }

    @Override
    protected RuleTestType<?> getType() {
        throw new RuntimeException();
    }
}
