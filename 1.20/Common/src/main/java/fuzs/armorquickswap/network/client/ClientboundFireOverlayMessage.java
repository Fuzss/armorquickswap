package fuzs.armorquickswap.network.client;

import fuzs.puzzleslib.api.network.v3.ClientMessageListener;
import fuzs.puzzleslib.api.network.v3.ClientboundMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.Block;

public record ClientboundFireOverlayMessage(Block block) implements ClientboundMessage<ClientboundFireOverlayMessage> {

    @Override
    public ClientMessageListener<ClientboundFireOverlayMessage> getHandler() {
        return new ClientMessageListener<>() {

            @Override
            public void handle(ClientboundFireOverlayMessage message, Minecraft client, ClientPacketListener handler, LocalPlayer player, ClientLevel level) {

            }
        };
    }
}
