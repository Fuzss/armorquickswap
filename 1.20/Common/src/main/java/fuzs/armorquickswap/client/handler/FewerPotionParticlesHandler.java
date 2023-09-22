package fuzs.armorquickswap.client.handler;

import fuzs.armorquickswap.mixin.client.accessor.LivingEntityAccessor;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

public class FewerPotionParticlesHandler {
    private static ParticleStatus particleStatusConfigOption = ParticleStatus.MINIMAL;
    private static boolean invisible;

    public static void onStartPlayerTick(Player player) {
        if (player instanceof LocalPlayer) {
            switch (particleStatusConfigOption) {
                case MINIMAL:
                    invisible = player.isInvisible();
                    player.setInvisible(true);
                case DECREASED:
                    player.getEntityData().set(LivingEntityAccessor.armorquickswap$getdataEffectAmbienceId(), true);
            }
        }
    }

    public static void onEndPlayerTick(Player player) {
        if (player instanceof LocalPlayer && player.isInvisible() && particleStatusConfigOption == ParticleStatus.MINIMAL) {
            player.setInvisible(invisible);
        }
    }
}
