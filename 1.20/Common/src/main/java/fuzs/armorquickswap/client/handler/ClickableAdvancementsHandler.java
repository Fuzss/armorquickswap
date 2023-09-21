package fuzs.armorquickswap.client.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementTabAccessor;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementToastAccessor;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementWidgetAccessor;
import fuzs.armorquickswap.mixin.client.accessor.AdvancementsScreenAccessor;
import fuzs.puzzleslib.api.client.screen.v2.ScreenHelper;
import fuzs.puzzleslib.api.event.v1.core.EventResult;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class ClickableAdvancementsHandler {
    private static final Component TITLE_COMPONENT = Component.translatable("gui.advancements");
    private static final Map<HoverEvent, Advancement> STYLES_CACHE = Maps.newHashMap();

    @Nullable
    private static AdvancementWidget widget;
    private static double scrollX;
    private static double scrollY;

    public static EventResult onBeforeMouseClick(ChatScreen screen, double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
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
        }
        return EventResult.PASS;
    }

    public static EventResult onBeforeMouseClick(AdvancementsScreen screen, double mouseX, double mouseY, int button) {
        boolean grantAdvancement = button == InputConstants.MOUSE_BUTTON_LEFT;
        if (grantAdvancement || button == InputConstants.MOUSE_BUTTON_RIGHT) {
            Minecraft minecraft = ScreenHelper.INSTANCE.getMinecraft(screen);
            AdvancementTab tab = ((AdvancementsScreenAccessor) screen).armorquickswap$getSelectedTab();
            if (tab != null && Screen.hasControlDown() && minecraft.gameMode.hasInfiniteItems()) {
                int leftPos = (screen.width - AdvancementsScreen.WINDOW_WIDTH) / 2;
                int topPos = (screen.height - AdvancementsScreen.WINDOW_HEIGHT) / 2;
                int scrollX = Mth.floor(((AdvancementTabAccessor) tab).armorquickswap$getScrollX());
                int scrollY = Mth.floor(((AdvancementTabAccessor) tab).armorquickswap$getScrollY());
                mouseX -= leftPos + 9;
                mouseY -= topPos + 18;
                if (mouseX > 0 && mouseX < AdvancementsScreen.WINDOW_INSIDE_WIDTH && mouseY > 0 && mouseY < AdvancementsScreen.WINDOW_INSIDE_HEIGHT) {
                    for (Map.Entry<Advancement, AdvancementWidget> entry : ((AdvancementTabAccessor) tab).armorquickswap$getWidgets().entrySet()) {
                        if (entry.getValue().isMouseOver(scrollX, scrollY, (int) mouseX, (int) mouseY)) {
                            AdvancementProgress advancementProgress = ((AdvancementWidgetAccessor) entry.getValue()).armorquickswap$getProgress();
                            boolean isDone = advancementProgress != null && advancementProgress.isDone();
                            if (grantAdvancement != isDone) {
                                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, grantAdvancement ? 0.7F : 0.2F, 1.0F));
                                minecraft.getConnection().sendCommand("advancement %s @s only %s".formatted(grantAdvancement ? "grant" : "revoke", entry.getKey().getId()));
                            }
                            return EventResult.INTERRUPT;
                        }
                    }
                }
            }
        }
        return EventResult.PASS;
    }

    public static void onAfterRender(AdvancementsScreen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        AdvancementTab tab = ((AdvancementsScreenAccessor) screen).armorquickswap$getSelectedTab();
        if (tab != null && widget != null && Math.abs(scrollX - ((AdvancementTabAccessor) tab).armorquickswap$getScrollX()) < 2.0 && Math.abs(scrollY - ((AdvancementTabAccessor) tab).armorquickswap$getScrollY()) < 2.0) {
            int leftPos = (screen.width - AdvancementsScreen.WINDOW_WIDTH) / 2;
            int topPos = (screen.height - AdvancementsScreen.WINDOW_HEIGHT) / 2;
            int scrollX = Mth.floor(((AdvancementTabAccessor) tab).armorquickswap$getScrollX());
            int scrollY = Mth.floor(((AdvancementTabAccessor) tab).armorquickswap$getScrollY());
            mouseX -= leftPos + 9;
            mouseY -= topPos + 18;
            if (mouseX > 0 && mouseX < AdvancementsScreen.WINDOW_INSIDE_WIDTH && mouseY > 0 && mouseY < AdvancementsScreen.WINDOW_INSIDE_HEIGHT) {
                for (AdvancementWidget advancementWidget : ((AdvancementTabAccessor) tab).armorquickswap$getWidgets().values()) {
                    if (advancementWidget.isMouseOver(scrollX, scrollY, mouseX, mouseY)) {
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

    public static EventResult onBeforeMouseScroll(AdvancementsScreen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        AdvancementTab tab = ((AdvancementsScreenAccessor) screen).armorquickswap$getSelectedTab();
        if (tab != null) {
            List<Map.Entry<Advancement, AdvancementTab>> tabs = List.copyOf(((AdvancementsScreenAccessor) screen).armorquickswap$getTabs().entrySet());
            int leftPos = (screen.width - AdvancementsScreen.WINDOW_WIDTH) / 2;
            int topPos = (screen.height - AdvancementsScreen.WINDOW_HEIGHT) / 2;
            boolean scrollOnTabs = false;
            for (Map.Entry<Advancement, AdvancementTab> entry : tabs) {
                if (entry.getValue().isMouseOver(leftPos, topPos, mouseX, mouseY)) {
                    scrollOnTabs = true;
                    break;
                }
            }
            if (scrollOnTabs) {
                for (int i = 0; i < tabs.size(); i++) {
                    if (tabs.get(i).getValue() == tab) {
                        i = Mth.positiveModulo(i + (int) -Math.signum(horizontalAmount), tabs.size());
                        screen.onSelectedTabChanged(tabs.get(i).getKey());
                        setTitleFromSelectedTab(screen);
                        break;
                    }
                }
            } else if (leftPos <= mouseX && mouseX < leftPos + AdvancementsScreen.WINDOW_WIDTH && topPos <= mouseY && mouseY < topPos + AdvancementsScreen.WINDOW_HEIGHT) {
                tab.scroll(horizontalAmount, verticalAmount);
            }
            return EventResult.INTERRUPT;
        }
        return EventResult.PASS;
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

    public static void onAfterInit(Minecraft minecraft, Screen screen, int screenWidth, int screenHeight, List<AbstractWidget> widgets, Consumer<AbstractWidget> addWidget, Consumer<AbstractWidget> removeWidget) {
        if (screen instanceof AdvancementsScreen advancementsScreen) {
            addWidget.accept(Button.builder(CommonComponents.GUI_DONE, (p_251194_) -> {
                screen.onClose();
            }).bounds(screen.width / 2 - 100, screen.height / 4 + 144, 200, 20).build());
            setTitleFromSelectedTab(advancementsScreen);
            ClientAdvancements advancements = minecraft.getConnection().getAdvancements();
            Map<Advancement, AdvancementTab> tabs = ((AdvancementsScreenAccessor) screen).armorquickswap$getTabs();
            for (AdvancementTab tab : tabs.values()) {
                Map<Advancement, AdvancementWidget> widgets1 = ((AdvancementTabAccessor) tab).armorquickswap$getWidgets();
                for (Map.Entry<Advancement, AdvancementWidget> entry : widgets1.entrySet()) {
                    Set<String> strings = entry.getKey().getCriteria().keySet();
                    if (!strings.isEmpty()) {
                        AdvancementWidget advancementWidget = entry.getValue();
                        List<FormattedCharSequence> description = Lists.newArrayList(((AdvancementWidgetAccessor) advancementWidget).armorquickswap$getDescription());
                        int maxWidth = 163 - minecraft.font.width(CommonComponents.ELLIPSIS);
                        for (String string : strings) {
                            string = string.replaceAll(".*:", "");
                            FormattedCharSequence formattedCharSequence;
                            if (minecraft.font.width(string) > 163) {
                                FormattedText text = minecraft.font.substrByWidth(Component.literal(string), maxWidth);
                                text = FormattedText.composite(text, CommonComponents.ELLIPSIS);
                                formattedCharSequence = Language.getInstance().getVisualOrder(text);
                            } else {
                                formattedCharSequence = Component.literal(string).getVisualOrderText();
                            }
                            description.add(formattedCharSequence);
                        }
                        ((AdvancementWidgetAccessor) advancementWidget).armorquickswap$setDescription(description);
                        description.stream().mapToInt(minecraft.font::width).max().stream().map(t -> t + 8).filter(t -> t > ((AdvancementWidgetAccessor) advancementWidget).armorquickswap$getWidth()).findAny().ifPresent(((AdvancementWidgetAccessor) advancementWidget)::armorquickswap$setWidth);
                    }
                }
            }
        }
    }

    public static void onAfterMouseClick(AdvancementsScreen screen, double mouseX, double mouseY, int button) {
        setTitleFromSelectedTab(screen);
    }

    private static void setTitleFromSelectedTab(AdvancementsScreen screen) {
        AdvancementTab selectedTab = ((AdvancementsScreenAccessor) screen).armorquickswap$getSelectedTab();
        if (selectedTab != null) {
            Component title = Component.empty().append(selectedTab.getTitle()).append(CommonComponents.SPACE).append(ClickableAdvancementsHandler.TITLE_COMPONENT);
            AdvancementsScreenAccessor.armorquickswap$setTitle(title);
        }
    }
}