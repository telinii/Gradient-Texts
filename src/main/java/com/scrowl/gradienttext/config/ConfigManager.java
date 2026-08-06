package com.scrowl.gradienttext.config;

import com.google.gson.*;
import com.scrowl.gradienttext.GradientTextMod;
import com.scrowl.gradienttext.config.GradientConfig.GroupEntry;
import com.scrowl.gradienttext.config.GradientConfig.ItemGradientEntry;
import com.scrowl.gradienttext.config.GradientConfig.ListEntry;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            parseTopLevel(root, config);

            GradientConfig.set(config);
            GradientTextMod.LOGGER.info("Config loaded successfully");
        } catch (Exception e) {
            GradientTextMod.LOGGER.error("Failed to load config", e);
        }
    }

    private static void parseTopLevel(JsonObject root, GradientConfig config) {
        if (root.has("smoothGradient")) {
            config.setSmoothGradient(root.get("smoothGradient").getAsBoolean());
        }

        if (root.has("defaultToolGradients")) {
            config.setDefaultToolGradients(root.get("defaultToolGradients").getAsBoolean());
        }

        if (root.has("defaultArmorGradients")) {
            config.setDefaultArmorGradients(root.get("defaultArmorGradients").getAsBoolean());
        }

        if (root.has("defaultMaterialGradients")) {
            config.setDefaultMaterialGradients(root.get("defaultMaterialGradients").getAsBoolean());
        }

        if (root.has("defaultGradientMode")) {
            config.setDefaultGradientMode(root.get("defaultGradientMode").getAsString());
        }

        if (root.has("backgroundPattern")) {
            config.setBackgroundPattern(root.get("backgroundPattern").getAsString());
        }

        if (root.has("blacklistedItems")) {
            JsonArray blacklist = root.getAsJsonArray("blacklistedItems");
            for (JsonElement elem : blacklist) {
                config.addBlacklistedItem(elem.getAsString());
            }
        }

        boolean hasNew = root.has("groups") || root.has("unassigned");

        if (hasNew) {
            loadNewStructure(config, root);
        } else if (root.has("forcedGradients")) {
            migrateOldStructure(config, root);
            GradientTextMod.LOGGER.info("Migrated legacy forcedGradients config to new structure");
        }
    }

    private static void loadNewStructure(GradientConfig config, JsonObject root) {
        if (root.has("groups")) {
            JsonArray groupsArr = root.getAsJsonArray("groups");
            List<GroupEntry> groups = new ArrayList<>();
            for (JsonElement gElem : groupsArr) {
                JsonObject gObj = gElem.getAsJsonObject();
                GroupEntry group = new GroupEntry(gObj.has("name") ? gObj.get("name").getAsString() : "");
                if (gObj.has("lists")) {
                    JsonArray listsArr = gObj.getAsJsonArray("lists");
                    for (JsonElement lElem : listsArr) {
                        JsonObject lObj = lElem.getAsJsonObject();
                        ListEntry list = new ListEntry(lObj.has("name") ? lObj.get("name").getAsString() : "");
                        if (lObj.has("items")) {
                            for (JsonElement iElem : lObj.getAsJsonArray("items")) {
                                ItemGradientEntry entry = parseItemEntry(iElem.getAsJsonObject());
                                if (entry != null) {
                                    list.getItems().add(entry);
                                }
                            }
                        }
                        group.getLists().add(list);
                    }
                }
                groups.add(group);
            }
            config.setGroups(groups);
        }

        if (root.has("unassigned")) {
            List<ItemGradientEntry> unassigned = new ArrayList<>();
            for (JsonElement iElem : root.getAsJsonArray("unassigned")) {
                ItemGradientEntry entry = parseItemEntry(iElem.getAsJsonObject());
                if (entry != null) {
                    unassigned.add(entry);
                }
            }
            config.setUnassigned(unassigned);
        }
    }

    private static void migrateOldStructure(GradientConfig config, JsonObject root) {
        JsonObject forced = root.getAsJsonObject("forcedGradients");
        if (forced == null) return;

        GroupEntry group = new GroupEntry(GradientConfig.DEFAULT_GROUP_NAME);
        ListEntry list = new ListEntry(GradientConfig.DEFAULT_LIST_NAME);

        for (String itemId : forced.keySet()) {
            ItemGradientEntry entry = parseItemEntry(forced.getAsJsonObject(itemId));
            if (entry == null) continue;
            entry.setItemId(itemId);
            list.getItems().add(entry);
        }

        if (!list.getItems().isEmpty() || forced.size() == 0) {
            group.getLists().add(list);
            config.getGroups().add(group);
        }
    }

    public static void save() {
        if (configDir == null) return;
        Path configFile = configDir.resolve(CONFIG_FILE);

        try {
            Files.writeString(configFile, GSON.toJson(serializeConfig(GradientConfig.get())));
            GradientTextMod.LOGGER.info("Config saved successfully");
        } catch (Exception e) {
            GradientTextMod.LOGGER.error("Failed to save config", e);
        }
    }

    private static JsonObject serializeConfig(GradientConfig config) {
        JsonObject root = new JsonObject();
        root.addProperty("smoothGradient", config.isSmoothGradient());
        root.addProperty("defaultToolGradients", config.isDefaultToolGradients());
        root.addProperty("defaultArmorGradients", config.isDefaultArmorGradients());
        root.addProperty("defaultMaterialGradients", config.isDefaultMaterialGradients());
        root.addProperty("defaultGradientMode", config.getDefaultGradientMode());
        root.addProperty("backgroundPattern", config.getBackgroundPattern());

        JsonArray blacklist = new JsonArray();
        for (String item : config.getBlacklistedItems()) {
            blacklist.add(item);
        }
        root.add("blacklistedItems", blacklist);

        JsonArray groups = new JsonArray();
        for (GroupEntry group : config.getGroups()) {
            JsonObject gObj = new JsonObject();
            gObj.addProperty("name", group.getName());
            JsonArray lists = new JsonArray();
            for (ListEntry list : group.getLists()) {
                JsonObject lObj = new JsonObject();
                lObj.addProperty("name", list.getName());
                JsonArray items = new JsonArray();
                for (ItemGradientEntry entry : list.getItems()) {
                    items.add(serializeItemEntry(entry));
                }
                lObj.add("items", items);
                lists.add(lObj);
            }
            gObj.add("lists", lists);
            groups.add(gObj);
        }
        root.add("groups", groups);

        JsonArray unassigned = new JsonArray();
        for (ItemGradientEntry entry : config.getUnassigned()) {
            unassigned.add(serializeItemEntry(entry));
        }
        root.add("unassigned", unassigned);

        return root;
    }

    // ================================================================
    // PRESETS
    // ================================================================
    private static Path getPresetsDir() {
        if (configDir == null) return null;
        Path dir = configDir.resolve("presets");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            GradientTextMod.LOGGER.error("Failed to create presets directory", e);
        }
        return dir;
    }

    public static String exportPreset(String name) {
        Path dir = getPresetsDir();
        if (dir == null) return null;
        try {
            if (name == null || name.trim().isEmpty()) {
                name = "preset-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            }
            String safe = name.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
            if (!safe.toLowerCase().endsWith(".json")) safe += ".json";
            Files.writeString(dir.resolve(safe), GSON.toJson(serializeConfig(GradientConfig.get())));
            GradientTextMod.LOGGER.info("Preset exported: {}", safe);
            return safe;
        } catch (Exception e) {
            GradientTextMod.LOGGER.error("Failed to export preset", e);
            return null;
        }
    }

    public static List<String> listPresets() {
        Path dir = getPresetsDir();
        if (dir == null) return new ArrayList<>();
        try (var s = Files.list(dir)) {
            return s.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(p -> p.getFileName().toString())
                    .sorted(java.util.Comparator.reverseOrder())
                    .collect(java.util.stream.Collectors.toList());
        } catch (IOException e) {
            GradientTextMod.LOGGER.error("Failed to list presets", e);
            return new ArrayList<>();
        }
    }

    public static boolean importPreset(String fileName) {
        Path dir = getPresetsDir();
        if (dir == null) return false;
        try {
            Path preset = dir.resolve(fileName).normalize();
            if (!preset.startsWith(dir) || !Files.exists(preset)) return false;
            GradientConfig config = new GradientConfig();
            parseTopLevel(JsonParser.parseString(Files.readString(preset)).getAsJsonObject(), config);
            GradientConfig.set(config);
            save();
            GradientTextMod.LOGGER.info("Preset imported: {}", fileName);
            return true;
        } catch (Exception e) {
            GradientTextMod.LOGGER.error("Failed to import preset " + fileName, e);
            return false;
        }
    }

    private static ItemGradientEntry parseItemEntry(JsonObject obj) {
        try {
            String itemId = obj.has("itemId") ? obj.get("itemId").getAsString() : "";

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

            return new ItemGradientEntry(itemId, colors, direction, mode, bold, speed, customName);
        } catch (Exception e) {
            GradientTextMod.LOGGER.error("Failed to parse item gradient entry", e);
            return null;
        }
    }

    private static JsonObject serializeItemEntry(ItemGradientEntry entry) {
        JsonObject obj = new JsonObject();

        obj.addProperty("itemId", entry.getItemId());

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