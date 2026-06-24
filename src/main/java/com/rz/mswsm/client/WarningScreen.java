package com.rz.mswsm.client;

import com.rz.mswsm.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class WarningScreen extends Screen
{
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(Main.MOD_ID + "-client.properties");

    private static final Component TITLE =
            Component.translatable("screen.mswsm.warning.title");

    private static final Component WARNING_TEXT =
            Component.translatable("screen.mswsm.warning.body");

    private static final Component STOP_TEXT =
            Component.translatable("screen.mswsm.warning.stop");

    private static final Component CHECKBOX_TEXT =
            Component.translatable("screen.mswsm.warning.checkbox");

    private static final Component ACKNOWLEDGE_TEXT =
            Component.translatable("screen.mswsm.warning.accept");

    private Checkbox dontShowAgain;

    public WarningScreen()
    {
        super(TITLE);
    }

    @Override
    protected void init()
    {
        int panelWidth = 360;
        int panelHeight = 220;

        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;

        int panelCenterX = left + (panelWidth / 2);
        int checkboxWidth = 24 + this.font.width(CHECKBOX_TEXT);

        this.dontShowAgain = Checkbox.builder(
                        CHECKBOX_TEXT,
                        this.font
                )
                .pos(panelCenterX - (checkboxWidth / 2), top + 150)
                .build();

        this.addRenderableWidget(this.dontShowAgain);

        this.addRenderableWidget(Button.builder(
                ACKNOWLEDGE_TEXT,
                button -> {
                    if (this.dontShowAgain.selected())
                    {
                        saveDisabled();
                    }

                    Minecraft.getInstance().setScreen(null);
                }
        ).bounds(left + 105, top + 185, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBlurredBackground(partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int panelWidth = 360;
        int panelHeight = 220;

        int left = centerX - panelWidth / 2;
        int top = centerY - panelHeight / 2;
        int right = left + panelWidth;
        int bottom = top + panelHeight;

        graphics.fill(left, top, right, bottom, 0xD0000000);
        graphics.renderOutline(left, top, panelWidth, panelHeight, 0xFFFFFFFF);

        graphics.drawCenteredString(this.font, this.title, centerX, top + 18, 0xFFFFFF);

        graphics.drawWordWrap(
                this.font,
                WARNING_TEXT,
                left + 20,
                top + 45,
                panelWidth - 40,
                0xFFFFFF
        );

        graphics.drawCenteredString(
                this.font,
                STOP_TEXT,
                this.width / 2,
                top + 120,
                0xFFFF55
        );

        for (var widget : this.renderables)
        {
            widget.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    public static boolean shouldShow()
    {
        Properties props = new Properties();

        if (Files.exists(CONFIG_PATH))
        {
            try (var reader = Files.newBufferedReader(CONFIG_PATH))
            {
                props.load(reader);
            }
            catch (IOException ignored)
            {
            }
        }

        return !Boolean.parseBoolean(props.getProperty("hideWarning", "false"));
    }

    private static void saveDisabled()
    {
        Properties props = new Properties();
        props.setProperty("hideWarning", "true");

        try
        {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (var writer = Files.newBufferedWriter(CONFIG_PATH))
            {
                props.store(writer, "Client settings for " + Main.MOD_ID + " - Motion Sickness Warning Screen Mod");
            }
        }
        catch (IOException ignored)
        {
        }
    }
}
