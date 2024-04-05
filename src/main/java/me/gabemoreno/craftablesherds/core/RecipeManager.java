package me.gabemoreno.craftablesherds.core;

import me.gabemoreno.craftablesherds.CraftableSherds;
import me.gabemoreno.craftablesherds.exceptions.InvalidRecipeException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

public class RecipeManager {

    private static final JavaPlugin plugin = CraftableSherds.getInstance();
    private static final List<NamespacedKey> registeredRecipes = new ArrayList<>();
    private static final Logger logger = plugin.getLogger();
    private static FileConfiguration config;

    public static void unloadRecipes() {

        int removedCounter = 0;

        for (NamespacedKey key : registeredRecipes) {

            if (Bukkit.getServer().removeRecipe(key)) {
                removedCounter++;
            } else {
                logger.warning(String.format("Failed to remove recipe '%s'. Does it exist?", key));
            }

        }

        registeredRecipes.clear();
        logger.info(String.format("Removed %s recipes.", removedCounter));
    }

    public static void reloadRecipes() {
        unloadRecipes();
        loadRecipes();
    }

    public static void loadRecipes() {
        plugin.reloadConfig();
        config = plugin.getConfig();

        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");

        if (!config.contains("recipes") || recipesSection == null) {
            logger.warning("Failed to load recipes. Missing section 'recipes' in config.");
            return;
        }

        for (String recipeID : recipesSection.getKeys(false)) {

            if (!isRecipeEnabled(config, recipeID)) continue;

            try {
                ItemStack result = getResult(recipeID);
                NamespacedKey namespacedKey = new NamespacedKey(plugin, recipeID);
                ShapedRecipe recipe = new ShapedRecipe(namespacedKey, result);
                addShape(recipeID, recipe);
                addIngredients(recipeID, recipe);
                registerRecipe(namespacedKey, recipe);

            } catch (InvalidRecipeException exception) {
                logger.warning(String.format("Failed to load recipe '%s'. ", recipeID) + exception.getMessage());
            }

        }

        logger.info(String.format("Added %s recipes.", registeredRecipes.size()));
    }

    private static boolean isRecipeEnabled(FileConfiguration config, String recipeID) {
        String path = "recipes." + recipeID + ".enabled";
        return config.getBoolean(path, true);
    }

    private static ItemStack getResult(String recipeID) throws InvalidRecipeException {
        String resultID = recipeID.toUpperCase() + "_POTTERY_SHERD";
        Material resultMaterial = Material.getMaterial(resultID);
        if (resultMaterial == null) {
            throw new InvalidRecipeException(String.format("Invalid result material '%s'.", resultID));
        }
        return new ItemStack(resultMaterial);
    }

    private static String[] loadShape(String recipeID) throws InvalidRecipeException {
        String path = "recipes." + recipeID + ".shape";

        if (!(config.contains(path, true))) {
            throw new InvalidRecipeException("Could not find shape for this recipe.");
        }

        List<String> shape = config.getStringList(path);
        validateShape(shape);
        return new String[] {shape.get(0), shape.get(1), shape.get(2)};
    }

    private static void validateShape(List<String> shape) throws InvalidRecipeException {
        if (shape.size() != 3) {
            throw new InvalidRecipeException("Invalid recipe shape.");
        }

        for (String row : shape) {
            if (row.length() != 3) {
                throw new InvalidRecipeException("Invalid recipe shape");
            }
        }
    }

    private static void addShape(String recipeID, ShapedRecipe recipe) throws InvalidRecipeException {
        String[] shape = loadShape(recipeID);
        recipe.shape(shape);
    }


    private static Map<Character, String> loadIngredients(String recipeID) throws InvalidRecipeException {

        String path = "recipes." + recipeID + ".ingredients";
        ConfigurationSection ingredientsSection = config.getConfigurationSection(path);

        if (ingredientsSection == null || ingredientsSection.getKeys(false).isEmpty()) {
            throw new InvalidRecipeException("Could not find ingredients for this recipe.");
        }

        Map<Character, String> ingredients = new HashMap<>();

        for (String key : ingredientsSection.getKeys(false)) {

            if (key.length() != 1) {
                throw new InvalidRecipeException(String.format("Ingredient key '%s' must be a single character.", key));
            }

            String ingredientID = ingredientsSection.getString(key);

            if (ingredientID == null) {
                throw new InvalidRecipeException(String.format("Missing ingredient material for key '%s'", key));
            }

            ingredients.put(key.charAt(0), ingredientID);

        }

        return ingredients;

    }

    private static Map<Character, Material> validateIngredients(Map<Character, String> ingredients, ShapedRecipe recipe) throws InvalidRecipeException {

        Map<Character, Material> validatedIngredients = new HashMap<>();

        String shapeKeyString = String.join("", recipe.getShape());

        for (Character key : ingredients.keySet()) {

            String ingredientID = ingredients.get(key);

            Material ingredient = Material.getMaterial(ingredientID);

            if (ingredient == null || ingredient.isAir() || !ingredient.isItem()) {
                throw new InvalidRecipeException(String.format("Invalid or non-item ingredient material '%s'.", ingredientID));
            }

            if (!shapeKeyString.contains(Character.toString(key))) {
                throw new InvalidRecipeException(String.format("Ingredient under key '%s' not utilized in shape.", key));
            }

            validatedIngredients.put(key, ingredient);
        }

        return validatedIngredients;

    }

    private static void addIngredients(String recipeID, ShapedRecipe recipe) throws InvalidRecipeException {

        Map<Character, Material> ingredients = validateIngredients(loadIngredients(recipeID), recipe);

        for (Character key : ingredients.keySet()) {
            recipe.setIngredient(key, ingredients.get(key));
        }

    }

    private static void registerRecipe(NamespacedKey namespacedKey, ShapedRecipe recipe) {
        if (Bukkit.getServer().addRecipe(recipe)) {
            registeredRecipes.add(namespacedKey);
        } else {
            logger.warning(String.format("Failed to add recipe '%s'. Does it already exist?", namespacedKey));
        }
    }

    public static List<NamespacedKey> getRegisteredRecipes() {
        return Collections.unmodifiableList(registeredRecipes);
    }

}
