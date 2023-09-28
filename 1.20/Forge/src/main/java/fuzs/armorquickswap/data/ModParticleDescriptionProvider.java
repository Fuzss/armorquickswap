package fuzs.armorquickswap.data;

import fuzs.armorquickswap.client.init.ClientModRegistry;
import fuzs.puzzleslib.api.data.v2.client.AbstractParticleDescriptionProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.puzzleslib.api.data.v2.core.ForgeDataProviderContext;
import net.minecraft.resources.ResourceLocation;

public class ModParticleDescriptionProvider extends AbstractParticleDescriptionProvider {

    public ModParticleDescriptionProvider(DataProviderContext context) {
        // FIXME
        super((ForgeDataProviderContext) context);
    }

    @Override
    protected void addParticleDescriptions() {
        this.add(ClientModRegistry.BLOOD_PARTICLE_TYPE, new ResourceLocation("splash"), 0, 3);
    }
}
