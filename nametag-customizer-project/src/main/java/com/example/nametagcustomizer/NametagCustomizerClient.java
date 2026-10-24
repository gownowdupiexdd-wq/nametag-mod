package com.example.nametagcustomizer;

import com.example.nametagcustomizer.config.NametagConfig;
import com.example.nametagcustomizer.gui.NametagConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class NametagCustomizerClient implements ClientModInitializer {
    public static final String MOD_ID = "nametagcustomizer";

    private static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        NametagConfig.load();

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nametagcustomizer.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.nametagcustomizer"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfigKey.wasPressed()) {
                client.setScreen(new NametagConfigScreen(client.currentScreen));
            }
        });
    }
}
