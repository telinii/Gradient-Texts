package com.scrowl.gradienttext.config;

import com.google.gson.*;
import com.scrowl.gradienttext.GradientTextMod;
import com.scrowl.gradienttext.config.GradientConfig.ItemGradientEntry;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final String CONFIG_FILE = "gradienttext.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configDir;

    public static void init() {
        configDir = FMLPaths.CONFIGDIR.get().resolve("gradienttext");
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            GradientTextMod.LOGGER.error("Failed to create config directory", e);
        }
        load();
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static void load() {
        Path configFile = configDir.resolve(CONFIG_FILE);
        if (!Files.exists(configFile)) {
            GradientTextMod.LOGGER.info("No config file found, using defaults");
            return;
        }

        try {
            String json = Files.readString(configFile);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            GradientConfig config = new GradientConfig();

            if (root.has("smoothGradient")) {
                config.setSmoothGradient(root.get("smoothGradient").getAsBoolean());
            }

            if (root.has("defaultToolGradients")) {
                config.setDefaultToolGradients(root.get("defaultToolGradients").getAsBoolean());
            }

            if (root.has("defaultArmorGradients")) {
                config.setDefaultArmorGradients(root.get("defaultArmorGradients").getAsBoolean());
            }

            if (root.has("defaultGradientMode")) {
                config.setDefaultGradientMode(root.get("defaultGradientMode").getAsString());
            }

            if (root.has("blacklistedItems")) {
                JsonArray blacklist = root.getAsJsonArray("blacklistedItems");
                for (JsonElement elem : blacklist) {
                    config.addBlacklistedItem(elem.getAsString());
                }
            }

            if (root.has("forcedGradients")) {
                JsonObject forced = root.getAsJsonObject("forcedGradients");
                for (String itemId : forced.keySet()) {
                    JsonObject entryObj = forced.getAsJsonObject(itemId);
                    ItemGradientEntry entry = parseItemEntry(entryObj);
                    if (entry != null) {
                        config.setForcedGradient(itemId, entry);
                    }
                }
            }

            GradientConfig.set(config);
            GradientTextMod.LOGGER.info("Config loaded successfully");
        } catch (Exception e) {
            GradientTextMod.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        Path configFile = configDir.resolve(CONFIG_FILE);
        GradientConfig config = GradientConfig.get();

        try {
            JsonObject root = new JsonObject();
            root.addProperty("smoothGradient", config.isSmoothGradient());
            root.addProperty("defaultToolGradients", config.isDefaultToolGradients());
            root.addProperty("defaultArmorGradients", config.isDefaultArmorGradients());
            root.addProperty("defaultGradientMode", config.getDefaultGradientMode());

            JsonArray blacklist = new JsonArray();
            for (String item : config.getBlacklistedItems()) {
                blacklist.add(item);
            }
            root.add("blacklistedItems", blacklist);

            JsonObject forced = new JsonObject();
            for (var entry : config.getForcedGradients().entrySet()) {
                forced.add(entry.getKey(), serializeItemEntry(entry.getValue()));
            }
            root.add("forcedGradients", forced);

            Files.writeString(configFile, GSON.toJson(root));
            GradientTextMod.LOGGER.info("Config saved successfully");
        } catch (Exception e) {
            GradientTextMod.LOGGER.error("Failed to save config", e);
        }
    }

    private static ItemGradientEntry parseItemEntry(JsonObject obj) {
        try {
            JsonArray colorsArr = obj.getAsJsonArray("colors");
            int[] colors = new int[colorsArr.size()];
            for (int i = 0; i < colorsArr.size(); i++) {
                colors[i] = colorsArr.get(i).getAsInt();
            }

            String direction = obj.has("direction") ? obj.get("direction").getAsString() : "HORIZONTAL";
            String mode = obj.has("mode") ? obj.get("mode").getAsString() : "STATIC";
            boolean bold = obj.has("bold") && obj.get("bold").getAsBoolean();
            float speed = obj.has("speed") ? obj.get("speed").getAsFloat() : 1.0f;
            String customName = obj.has("customName") ? obj.get("customName").getAsString() : "";

            return new ItemGradientEntry(colors, direction, mode, bold, speed, customName);
        } catch (Exception e) {
            GradientTextMod.LOGGER.error("Failed to parse item gradient entry", e);
            return null;
        }
    }

    private static JsonObject serializeItemEntry(ItemGradientEntry entry) {
        JsonObject obj = new JsonObject();

        JsonArray colors = new JsonArray();
        for (int color : entry.getColors()) {
            colors.add(color);
        }
        obj.add("colors", colors);

        obj.addProperty("direction", entry.getDirection());
        obj.addProperty("mode", entry.getMode());
        obj.addProperty("bold", entry.isBold());
        obj.addProperty("speed", entry.getSpeed());
        obj.addProperty("customName", entry.getCustomName());

        return obj;
    }
}
