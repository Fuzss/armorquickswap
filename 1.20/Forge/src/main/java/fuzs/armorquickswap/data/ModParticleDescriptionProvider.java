package fuzs.armorquickswap.data;

import fuzs.armorquickswap.client.init.ClientModRegistry;
import fuzs.puzzleslib.api.data.v1.AbstractParticleDescriptionProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.data.event.GatherDataEvent;

public class ModParticleDescriptionProvider extends AbstractParticleDescriptionProvider {

    public ModParticleDescriptionProvider(GatherDataEvent evt, String modId) {
        super(evt, modId);
    }

    @Override
    protected void addParticleDescriptions() {
        this.add(ClientModRegistry.BLOOD_PARTICLE_TYPE, new ResourceLocation("splash"), 0, 3);
    }
}
