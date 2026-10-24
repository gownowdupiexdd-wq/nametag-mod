package com.example.nametagcustomizer.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class NametagConfig {
    public static final float SCALE_MIN = 0.3f, SCALE_MAX = 3.0f;
    public static final float STRETCH_MIN = 0.3f, STRETCH_MAX = 3.0f;
    public static final float OFFSET_MIN = -60f, OFFSET_MAX = 60f;
    public static final float PADDING_MAX = 10f;
    public static final float THICKNESS_MIN = 0.5f, THICKNESS_MAX = 3f;
    public static final float DISTANCE_MAX = 64f;

    private static final Logger LOGGER = LoggerFactory.getLogger("nametagcustomizer");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("nametagcustomizer.json");

    private static NametagConfig instance = new NametagConfig();

    // ---- main ----
    public boolean enabled = true;
    public boolean playersOnly = true;

    // ---- text ----
    public boolean textVisible = true;
    public float textScale = 1.0f;
    public int textColor = 0xFFFFFF;
    public boolean forceTextColor = false;
    public boolean shadowEnabled = false;
    public int shadowColor = 0x3F3F3F;
    public boolean outlineEnabled = false;
    public int outlineColor = 0x000000;
    public float outlineThickness = 1.0f;

    // ---- background ----
    public boolean backgroundVisible = true;
    public int backgroundColor = 0x000000;
    public float backgroundOpacity = 0.25f;
    public float paddingX = 1.0f;
    public float paddingY = 1.0f;
    public boolean borderEnabled = false;
    public int borderColor = 0xFF7518;
    public float borderThickness = 1.0f;

    // ---- position ----
    public float offsetX = 0f;
    public float offsetY = 0f;
    public float stretchX = 1.0f;
    public float stretchY = 1.0f;
    public boolean throughWalls = true;
    public float maxDistance = 0f; // 0 = vanilla range

    // ---- effects ----
    public boolean rainbow = false;
    public float rainbowSpeed = 1.0f;
    public boolean pulse = false;
    public boolean halloweenText = false;

    // ---- glow for chosen players ----
    public boolean glowEnabled = true;
    public int glowColor = 0xFFAA00;
    public float glowStrength = 1.0f;
    public List<String> glowPlayers = new ArrayList<>();

    // ---- gui ----
    public boolean halloweenGui = false;

    public static NametagConfig get() {
        return instance;
    }

    public int backgroundArgb() {
        int alpha = Math.round(MathHelper.clamp(backgroundOpacity, 0f, 1f) * 255f);
        return (alpha << 24) | (backgroundColor & 0xFFFFFF);
    }

    /** True when the given (display) name contains one of the glowing nicknames as a whole word. */
    public boolean hasGlow(String displayName) {
        if (!glowEnabled || glowPlayers == null || glowPlayers.isEmpty() || displayName == null) {
            return false;
        }
        String lower = displayName.toLowerCase(Locale.ROOT);
        for (String nick : glowPlayers) {
            if (nick == null || nick.isEmpty()) continue;
            Pattern p = Pattern.compile("(?<![a-z0-9_])" + Pattern.quote(nick.toLowerCase(Locale.ROOT)) + "(?![a-z0-9_])");
            if (p.matcher(lower).find()) {
                return true;
            }
        }
        return false;
    }

    public static void load() {
        if (Files.exists(FILE)) {
            try (Reader reader = Files.newBufferedReader(FILE)) {
                NametagConfig loaded = GSON.fromJson(reader, NametagConfig.class);
                if (loaded != null) {
                    instance = loaded;
                }
            } catch (Exception e) {
                LOGGER.error("Could not read nametag config, using defaults", e);
            }
        }
        instance.sanitize();
        instance.save();
    }

    public static void reset() {
        boolean keepGui = instance.halloweenGui;
        instance = new NametagConfig();
        instance.halloweenGui = keepGui;
        instance.save();
    }

    public void sanitize() {
        textScale = MathHelper.clamp(textScale, SCALE_MIN, SCALE_MAX);
        stretchX = MathHelper.clamp(stretchX, STRETCH_MIN, STRETCH_MAX);
        stretchY = MathHelper.clamp(stretchY, STRETCH_MIN, STRETCH_MAX);
        offsetX = MathHelper.clamp(offsetX, OFFSET_MIN, OFFSET_MAX);
        offsetY = MathHelper.clamp(offsetY, OFFSET_MIN, OFFSET_MAX);
        paddingX = MathHelper.clamp(paddingX, 0f, PADDING_MAX);
        paddingY = MathHelper.clamp(paddingY, 0f, PADDING_MAX);
        backgroundOpacity = MathHelper.clamp(backgroundOpacity, 0f, 1f);
        outlineThickness = MathHelper.clamp(outlineThickness, THICKNESS_MIN, THICKNESS_MAX);
        borderThickness = MathHelper.clamp(borderThickness, THICKNESS_MIN, THICKNESS_MAX);
        maxDistance = MathHelper.clamp(maxDistance, 0f, DISTANCE_MAX);
        rainbowSpeed = MathHelper.clamp(rainbowSpeed, 0.1f, 5f);
        glowStrength = MathHelper.clamp(glowStrength, 0.5f, 3f);
        textColor &= 0xFFFFFF;
        shadowColor &= 0xFFFFFF;
        outlineColor &= 0xFFFFFF;
        backgroundColor &= 0xFFFFFF;
        borderColor &= 0xFFFFFF;
        glowColor &= 0xFFFFFF;
        if (glowPlayers == null) {
            glowPlayers = new ArrayList<>();
        }
    }

    public void save() {
        sanitize();
        try (Writer writer = Files.newBufferedWriter(FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            LOGGER.error("Could not save nametag config", e);
        }
    }
}
