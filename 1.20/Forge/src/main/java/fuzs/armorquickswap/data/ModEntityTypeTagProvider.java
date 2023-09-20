package fuzs.armorquickswap.data;

import fuzs.armorquickswap.init.ModRegistry;
import fuzs.puzzleslib.api.data.v1.AbstractTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.data.event.GatherDataEvent;

public class ModEntityTypeTagProvider extends AbstractTagProvider.EntityTypes {

    public ModEntityTypeTagProvider(GatherDataEvent evt, String modId) {
        super(evt, modId);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModRegistry.CLICK_THROUGH_ENTITY_TYPE_TAG).add(EntityType.ITEM_FRAME, EntityType.GLOW_ITEM_FRAME, EntityType.PAINTING);
    }
}
