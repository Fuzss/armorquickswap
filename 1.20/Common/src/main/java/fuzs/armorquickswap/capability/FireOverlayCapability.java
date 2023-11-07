package fuzs.armorquickswap.capability;

import fuzs.puzzleslib.api.capability.v2.data.CapabilityComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;

public class FireOverlayCapability implements CapabilityComponent {
    private Block fireBlock;

    public FireOverlayCapability(Entity entity) {

    }

    public Block getFireBlock() {
        return this.fireBlock;
    }

    public void setFireBlock(Block fireBlock) {
        this.fireBlock = fireBlock;
    }
}
