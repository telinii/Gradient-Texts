package com.scrowl.gradienttext.gradient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds, from the game's own recipe data, the mapping of "material root" items
 * (ingots/gems) to every item whose recipe consumes that root. A gradient
 * configured on a root then cascades to all items made from it.
 *
 * Rule: root items (ingots/gems) are never treated as children of another
 * material. E.g. netherite_ingot is crafted with gold_ingot, but it is its own
 * material category and is never gold-gradient.
 */
public class MaterialCascade {
    public static final Set<String> ROOTS = new HashSet<>(Arrays.asList(
            "minecraft:iron_ingot",
            "minecraft:gold_ingot",
            "minecraft:copper_ingot",
            "minecraft:netherite_ingot",
            "minecraft:diamond",
            "minecraft:emerald",
            "minecraft:lapis_lazuli",
            "minecraft:redstone",
            "minecraft:coal",
            "minecraft:quartz",
            "minecraft:amethyst_shard"
    ));

    private static Map<String, List<String>> itemToRoots = new HashMap<>();
    private static boolean built = false;

    private MaterialCascade() {
    }

    public static void rebuild(RecipeManager recipeManager, RegistryAccess registryAccess) {
        if (recipeManager == null || registryAccess == null) return;

        Map<String, Set<String>> childrenByRoot = new HashMap<>();
        collect(recipeManager.getAllRecipesFor(RecipeType.CRAFTING), registryAccess, childrenByRoot);
        collect(recipeManager.getAllRecipesFor(RecipeType.SMITHING), registryAccess, childrenByRoot);

        Map<String, List<String>> inverted = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : childrenByRoot.entrySet()) {
            for (String child : entry.getValue()) {
                inverted.computeIfAbsent(child, k -> new ArrayList<>()).add(entry.getKey());
            }
        }

        itemToRoots = inverted;
        built = true;
        if (com.scrowl.gradienttext.GradientTextMod.LOGGER.isDebugEnabled()) {
            for (Map.Entry<String, Set<String>> entry : childrenByRoot.entrySet()) {
                com.scrowl.gradienttext.GradientTextMod.LOGGER.debug("MaterialCascade root {} -> {} items", entry.getKey(), entry.getValue().size());
            }
        }
    }

    private static void collect(List<? extends Recipe<?>> recipes, RegistryAccess registryAccess,
                                Map<String, Set<String>> childrenByRoot) {
        for (Recipe<?> recipe : recipes) {
            if (recipe instanceof SmithingTrimRecipe) continue;

            ItemStack result = recipe.getResultItem(registryAccess);
            if (result == null || result.isEmpty()) continue;

            String resultId = itemIdOf(result.getItem());
            if (resultId == null || ROOTS.contains(resultId)) continue;

            Set<String> matchedRoots = new HashSet<>();
            for (Ingredient ingredient : recipe.getIngredients()) {
                for (ItemStack stack : ingredient.getItems()) {
                    String ingId = itemIdOf(stack.getItem());
                    if (ingId != null && ROOTS.contains(ingId)) matchedRoots.add(ingId);
                }
            }

            for (String root : matchedRoots) {
                childrenByRoot.computeIfAbsent(root, k -> new HashSet<>()).add(resultId);
            }
        }
    }

    public static void ensureBuilt() {
        if (built) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        RegistryAccess registryAccess = null;
        RecipeManager recipeManager = null;

        ClientPacketListener connection = mc.getConnection();
        if (connection != null) {
            recipeManager = connection.getRecipeManager();
            registryAccess = connection.registryAccess();
        } else if (mc.getSingleplayerServer() != null) {
            recipeManager = mc.getSingleplayerServer().getRecipeManager();
            registryAccess = mc.getSingleplayerServer().registryAccess();
        }

        if (recipeManager != null && registryAccess != null) {
            rebuild(recipeManager, registryAccess);
        }
    }

    public static void ensureBuilt(MinecraftServer server) {
        if (built) return;
        if (server == null) {
            ensureBuilt();
            return;
        }
        rebuild(server.getRecipeManager(), server.registryAccess());
    }

    public static List<String> getRootsForItem(String itemId) {
        if (itemId == null || !built) return Collections.emptyList();
        List<String> roots = itemToRoots.get(itemId.toLowerCase());
        return roots != null ? roots : Collections.emptyList();
    }

    public static boolean isBuilt() {
        return built;
    }

    private static String itemIdOf(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key != null ? key.toString() : null;
    }
}
