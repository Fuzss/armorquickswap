package fuzs.armorquickswap.client.handler;

import fuzs.armorquickswap.mixin.client.accessor.ToastComponentAccessor;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.tutorial.TutorialSteps;

public class ToastControlHandler {

    public static EventResult onAddToast(ToastComponent toastManager, Toast toast) {
        if (toast instanceof RecipeToast || toast instanceof TutorialToast) {
            return EventResult.INTERRUPT;
        } else if (((ToastComponentAccessor) toastManager).armorquickswap$getQueued().size() >= 5) {
            return EventResult.INTERRUPT;
        }
        return EventResult.PASS;
    }

    public static void onLoadComplete() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.options.tutorialStep = TutorialSteps.NONE;
    }
}
