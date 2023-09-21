package fuzs.armorquickswap.client.handler;

import fuzs.armorquickswap.mixin.client.accessor.AdvancementTabAccessor;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementToastAccessor;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementsScreenAccessor;
import fuzs.puzzleslib.api.client.screen.v2.ScreenHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

public class ClickableAdvancementsHandler {
    private static final Map<HoverEvent, Advancement> STYLES = new WeakHashMap<>();

    public static EventResult onBeforeMouseClick(ChatScreen screen, double mouseX, double mouseY, int button) {
        Minecraft minecraft = ScreenHelper.INSTANCE.getMinecraft(screen);
        Style style = minecraft.gui.getChat().getClickedComponentStyleAt(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            Advancement advancement = STYLES.get(style.getHoverEvent());
            if (advancement != null) {
                ClientAdvancements advancements = minecraft.getConnection().getAdvancements();
                advancements.setSelectedTab(advancement.getRoot(), false);
                AdvancementsScreen advancementsScreen = new AdvancementsScreen(advancements);
                minecraft.setScreen(advancementsScreen);
                AdvancementTab tab = ((AdvancementsScreenAccessor) advancementsScreen).armorquickswap$getSelectedTab();
                if (tab != null) {
                    AdvancementWidget widget = tab.getWidget(advancement);
                    if (widget != null) {
                        double posX = (((AdvancementTabAccessor) tab).armorquickswap$getMaxX() + ((AdvancementTabAccessor) tab).armorquickswap$getMinX()) / 2.0;
                        double posY = (((AdvancementTabAccessor) tab).armorquickswap$getMaxY() + ((AdvancementTabAccessor) tab).armorquickswap$getMinY()) / 2.0;
                        double scrollX = 117 - posX;
                        double scrollY = 56 - posY;
                        ((AdvancementTabAccessor) tab).armorquickswap$setScrollX(scrollX);
                        ((AdvancementTabAccessor) tab).armorquickswap$setScrollY(scrollY);
                        ((AdvancementTabAccessor) tab).armorquickswap$setCentered(true);
                        tab.scroll(posX - widget.getX() - 14, posY - widget.getY() - 13);
                    }
                }
                return EventResult.INTERRUPT;
            }
        }
        return EventResult.PASS;
    }

    public static EventResult onAddToast(Toast toast) {
        if (toast instanceof AdvancementToast) {
            Advancement advancement = ((AdvancementToastAccessor) toast).armroquickswap$getAdvancement();
            DisplayInfo display = advancement.getDisplay();
            if (display != null) {
                Style style = getAdvancementStyle(advancement.getChatComponent(), display.getTitle());
                if (style != null && style.getHoverEvent() != null) {
                    ClickableAdvancementsHandler.STYLES.put(style.getHoverEvent(), advancement);
                }
            }
        }
        return EventResult.PASS;
    }

    @Nullable
    private static Style getAdvancementStyle(Component advancementComponent, Component titleComponent) {
        if (titleComponent.getContents() instanceof TranslatableContents contents) {
            String key = contents.getKey();
            Queue<Component> queue = new ArrayDeque<>();
            queue.offer(advancementComponent);
            while (!queue.isEmpty()) {
                Component component = queue.poll();
                if (component.getContents() instanceof TranslatableContents contents1) {
                    if (contents1.getKey().equals(key)) {
                        return component.getStyle();
                    }
                    for (Object o : contents1.getArgs()) {
                        if (o instanceof Component component1) {
                            queue.offer(component1);
                        }
                    }
                }
            }
        }
        return null;
    }
}
