package fuzs.armorquickswap.client.handler;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementTabAccessor;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementToastAccessor;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementsScreenAccessor;
import fuzs.puzzleslib.api.client.screen.v2.ScreenHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class ClickableAdvancementsHandler {
    private static final Map<HoverEvent, Advancement> STYLES_CACHE = Maps.newHashMap();

    @Nullable
    private static AdvancementWidget widget;
    private static double scrollX;
    private static double scrollY;

    public static EventResult onBeforeMouseClick(ChatScreen screen, double mouseX, double mouseY, int button) {
        Minecraft minecraft = ScreenHelper.INSTANCE.getMinecraft(screen);
        Style style = minecraft.gui.getChat().getClickedComponentStyleAt(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            ClientAdvancements advancements = minecraft.getConnection().getAdvancements();
            Advancement advancement = getAdvancement(style.getHoverEvent(), advancements);
            if (advancement != null) {
                advancements.setSelectedTab(advancement.getRoot(), false);
                AdvancementsScreen advancementsScreen = new AdvancementsScreen(advancements);
                minecraft.setScreen(advancementsScreen);
                AdvancementTab tab = ((AdvancementsScreenAccessor) advancementsScreen).armorquickswap$getSelectedTab();
                if (tab != null) {
                    widget = tab.getWidget(advancement);
                    if (widget != null) {
                        double posX = (((AdvancementTabAccessor) tab).armorquickswap$getMaxX() + ((AdvancementTabAccessor) tab).armorquickswap$getMinX()) / 2.0;
                        double posY = (((AdvancementTabAccessor) tab).armorquickswap$getMaxY() + ((AdvancementTabAccessor) tab).armorquickswap$getMinY()) / 2.0;
                        ((AdvancementTabAccessor) tab).armorquickswap$setScrollX(AdvancementsScreen.WINDOW_INSIDE_WIDTH / 2.0 - posX);
                        ((AdvancementTabAccessor) tab).armorquickswap$setScrollY(AdvancementsScreen.WINDOW_INSIDE_HEIGHT / 2.0 - posY);
                        ((AdvancementTabAccessor) tab).armorquickswap$setCentered(true);
                        tab.scroll(posX - widget.getX() - 14, posY - widget.getY() - 13);
                        scrollX = ((AdvancementTabAccessor) tab).armorquickswap$getScrollX();
                        scrollY = ((AdvancementTabAccessor) tab).armorquickswap$getScrollY();
                    }
                }
                return EventResult.INTERRUPT;
            }
        }
        return EventResult.PASS;
    }

    public static void onAfterRender(AdvancementsScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        AdvancementTab tab = ((AdvancementsScreenAccessor) screen).armorquickswap$getSelectedTab();
        if (tab != null && widget != null && Math.abs(scrollX - ((AdvancementTabAccessor) tab).armorquickswap$getScrollX()) < 1.0 && Math.abs(scrollY - ((AdvancementTabAccessor) tab).armorquickswap$getScrollY()) < 1.0) {
            int leftPos = (screen.width - AdvancementsScreen.WINDOW_WIDTH) / 2;
            int topPos = (screen.height - AdvancementsScreen.WINDOW_HEIGHT) / 2;
            int scrollX = Mth.floor(((AdvancementTabAccessor) tab).armorquickswap$getScrollX());
            int scrollY = Mth.floor(((AdvancementTabAccessor) tab).armorquickswap$getScrollY());
            mouseX -= leftPos + 9;
            mouseY -= topPos + 18;
            if (mouseX > 0 && mouseX < AdvancementsScreen.WINDOW_INSIDE_WIDTH && mouseY > 0 && mouseY < AdvancementsScreen.WINDOW_INSIDE_HEIGHT) {
                for (AdvancementWidget advancementWidget : ((AdvancementTabAccessor) tab).armorquickswap$getWidgets().values()) {
                    if (advancementWidget != widget && advancementWidget.isMouseOver(scrollX, scrollY, mouseX, mouseY)) {
                        widget = null;
                        return;
                    }
                }
            }
            guiGraphics.pose().pushPose();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(leftPos + 9, topPos + 18, 200.0F);
            RenderSystem.enableDepthTest();
            widget.drawHover(guiGraphics, scrollX, scrollY, 0.3F, leftPos, topPos);
            RenderSystem.disableDepthTest();
            guiGraphics.pose().popPose();
            ((AdvancementTabAccessor) tab).armorquickswap$setFade(0.3F);
        } else {
            widget = null;
        }
    }

    public static void onLoggedIn(LocalPlayer player, MultiPlayerGameMode multiPlayerGameMode, Connection connection) {
        STYLES_CACHE.clear();
    }

    public static EventResult onAddToast(Toast toast) {
        if (toast instanceof AdvancementToast) {
            Advancement advancement = ((AdvancementToastAccessor) toast).armroquickswap$getAdvancement();
            HoverEvent hoverEvent = getAdvancementHoverStyle(advancement);
            if (hoverEvent != null) {
                ClickableAdvancementsHandler.STYLES_CACHE.put(hoverEvent, advancement);
            }
        }
        return EventResult.PASS;
    }

    @Nullable
    private static Advancement getAdvancement(HoverEvent hoverEvent, ClientAdvancements advancements) {
        return STYLES_CACHE.computeIfAbsent(hoverEvent, $ -> {
            for (Advancement advancement : advancements.getAdvancements().getAllAdvancements()) {
                HoverEvent otherHoverEvent = getAdvancementHoverStyle(advancement);
                if (hoverEvent.equals(otherHoverEvent)) {
                    return advancement;
                }
            }
            return null;
        });
    }

    @Nullable
    private static HoverEvent getAdvancementHoverStyle(Advancement advancement) {
        if (advancement.getDisplay() != null) {
            Style style = getAdvancementStyle(advancement.getChatComponent(), advancement.getDisplay().getTitle());
            if (style != null) {
                return style.getHoverEvent();
            }
        }
        return null;
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
