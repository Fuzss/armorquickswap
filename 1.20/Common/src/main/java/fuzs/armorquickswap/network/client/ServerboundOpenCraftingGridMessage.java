package fuzs.armorquickswap.network.client;

import fuzs.puzzleslib.api.network.v3.ServerMessageListener;
import fuzs.puzzleslib.api.network.v3.ServerboundMessage;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

public record ServerboundOpenCraftingGridMessage() implements ServerboundMessage<ServerboundOpenCraftingGridMessage> {
    public static final ResourceLocation STORY_ROOT_IDENTIFIER = new ResourceLocation("story/root");

    @Override
    public ServerMessageListener<ServerboundOpenCraftingGridMessage> getHandler() {
        return new ServerMessageListener<>() {
            private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

            @Override
            public void handle(ServerboundOpenCraftingGridMessage message, MinecraftServer server, ServerGamePacketListenerImpl handler, ServerPlayer player, ServerLevel level) {
                Advancement advancement = server.getAdvancements().getAdvancement(STORY_ROOT_IDENTIFIER);
                if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                    player.openMenu(new SimpleMenuProvider((i, inventory, player1) -> {
                        return new CraftingMenu(i, inventory, ContainerLevelAccess.create(level, player1.blockPosition())) {

                            @Override
                            public boolean stillValid(Player player) {
                                return player.isAlive();
                            }
                        };
                    }, CONTAINER_TITLE));
                }
            }
        };
    }
}
