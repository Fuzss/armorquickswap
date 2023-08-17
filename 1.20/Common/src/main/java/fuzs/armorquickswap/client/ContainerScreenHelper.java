package fuzs.armorquickswap.client;

import fuzs.puzzleslib.api.client.screen.v2.ScreenHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class ContainerScreenHelper {

    @Nullable
    public static Slot findSlot(AbstractContainerScreen<?> screen, double mouseX, double mouseY) {
        for(int i = 0; i < screen.getMenu().slots.size(); ++i) {
            Slot slot = screen.getMenu().slots.get(i);
            if (isHovering(screen, slot, mouseX, mouseY) && slot.isActive()) {
                return slot;
            }
        }

        return null;
    }

    public static boolean isHovering(AbstractContainerScreen<?> screen, Slot slot, double mouseX, double mouseY) {
        return isHovering(screen, slot.x, slot.y, 16, 16, mouseX, mouseY);
    }

    public static boolean isHovering(AbstractContainerScreen<?> screen, int posX, int posY, int width, int height, double mouseX, double mouseY) {
        mouseX -= ScreenHelper.INSTANCE.getLeftPos(screen);
        mouseY -= ScreenHelper.INSTANCE.getTopPos(screen);
        return mouseX >= (double)(posX - 1) && mouseX < (double)(posX + width + 1) && mouseY >= (double)(posY - 1) && mouseY < (double)(posY + height + 1);
    }
}
