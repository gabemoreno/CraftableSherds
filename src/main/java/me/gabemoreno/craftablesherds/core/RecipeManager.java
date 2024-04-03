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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RecipeManager {

    private static final JavaPlugin plugin = CraftableSherds.getInstance();
    private static final List<NamespacedKey> registeredRecipes = new ArrayList<>();
    private static final Logger logger = plugin.getLogger();
    private static FileConfiguration config;

//    public static void loadRecipes() {
//        plugin.reloadConfig();
//        FileConfiguration config = plugin.getConfig();
//        ConfigurationSection recipesSection = config.getConfigurationSection("recipes");
//
//        if (recipesSection == null) {
//            logger.warning("Failed to load recipes. Missing section 'recipes' in config.");
//            return;
//        }
//
//        recipesLoop:
//        for (String key : recipesSection.getKeys(false)) {
//
//            String path = "recipes." + key;
//
//            //check if disabled
//            boolean disabled = config.getBoolean(path + ".disabled", false);
//            if (disabled) continue;
//
//            //define result
//            String resultID = key.toUpperCase() + "_POTTERY_SHERD";
//
//            Material resultMaterial = Material.getMaterial(resultID);
//            if (resultMaterial == null) {
//                logger.warning(String.format("Failed to load recipe '%s'. Invalid result material '%s'.", key, resultID));
//                continue;
//            }
//
//            ItemStack resultItem = new ItemStack(resultMaterial);
//
//            //define shape
//            List<String> shape = config.getStringList(path + ".shape");
//
//            if (shape.size() != 3) {
//                logger.warning(String.format("Failed to load recipe '%s'. Missing valid shape.", key));
//                continue;
//            }
//
//            Bukkit.getServer().getLogger().severe("PATH: " + path + "| " + key + ": " + Arrays.toString(shape.toArray()));
//
//            for (String row : shape) {
//                if (row.length() != 3) {
//                    logger.warning(String.format("Failed to load recipe '%s'. Missing valid shape.", key));
//                    continue recipesLoop;
//                }
//            }
//
//
//            NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
//            ShapedRecipe recipe = new ShapedRecipe(namespacedKey, resultItem);
//            recipe.shape(shape.get(0), shape.get(1), shape.get(2));
//
//            ConfigurationSection ingredientSection = config.getConfigurationSection(path + ".ingredients");
//            if (ingredientSection == null) {
//                logger.warning(String.format("Failed to load recipe '%s'. Missing ingredients.", key));
//                continue;
//            }
//
//            Set<String> charKeys = ingredientSection.getKeys(false);
//
//            if (charKeys.isEmpty()) {
//                logger.warning(String.format("Failed to load recipe '%s'. Missing ingredients.", key));
//                continue;
//            }
//
//            //add ingredients mappings
//            for (String charKey : charKeys) {
//
//                String shapeChars = shape.get(0) + shape.get(1) + shape.get(2);
//
//                if (!shapeChars.contains(charKey.charAt(0) + "")) {
//                    logger.warning(String.format("Failed to load recipe '%s'. Ingredient '%s' not used in shape.", key, charKey));
//                    continue recipesLoop;
//                }
//
//                String ingredientID = ingredientSection.getString(charKey);
//
//                if (ingredientID == null) {
//                    logger.warning(String.format("Failed to load recipe '%s'. Missing ingredient for key '%s'.", key, charKey));
//                    continue recipesLoop;
//                }
//
//                Material ingredientMaterial = Material.getMaterial(ingredientID);
//
//                if (ingredientMaterial == null) {
//                    logger.warning(String.format("Failed to load recipe '%s'. Invalid ingredient material '%s'.", key, ingredientID));
//                    continue recipesLoop;
//                }
//
//                if (!ingredientMaterial.isItem()) {
//                    logger.warning(String.format("Failed to load recipe '%s'. Invalid ingredient material '%s'. Must be obtainable.", key, ingredientID));
//                    continue recipesLoop;
//                }
//
//                recipe.setIngredient(charKey.charAt(0), ingredientMaterial);
//            }
//
//            //add recipe to server
//            if (Bukkit.getServer().addRecipe(recipe)) {
//                registeredRecipes.add(namespacedKey);
//            } else {
//                logger.warning(String.format("Failed to add recipe '%s'. Does it already exist?", namespacedKey));
//            }
//        }
//
//        logger.info(String.format("Added %s recipes.", registeredRecipes.size()));
//    }

    public static void unloadRecipes() {

        int removedCounter = 0; // Tracks the number of recipes successfully removed

        for (NamespacedKey key : registeredRecipes) {

            if (Bukkit.getServer().removeRecipe(key)) {
                removedCounter++; // Increment if the recipe was successfully removed
            } else {
                logger.warning(String.format("Failed to remove recipe '%s'. Does it exist?", key));
            }

        }

        registeredRecipes.clear(); // Clear the list of registered recipes
        logger.info(String.format("Removed %s recipes.", removedCounter));
    }

    public static void reloadRecipes() {
        unloadRecipes(); // Remove all currently loaded recipes
        loadRecipes(); // Load recipes from configuration again
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

//    private static Material getResultMaterial(String recipeID) throws InvalidRecipeException {
//        String resultID = recipeID.toUpperCase() + "_POTTERY_SHERD";
//        Material resultMaterial = Material.getMaterial(resultID);
//        if (resultMaterial == null) {
//            throw new InvalidRecipeException(String.format("Failed to load recipe '%s'. Invalid result material '%s'.", recipeID, resultID));
//        }
//        return resultMaterial;
//    }

//    /**
//     * Extracts the shape of a crafting recipe from the configuration.
//     *
//     * @param config The plugin's configuration.
//     * @param recipeID The key identifying the recipe in the configuration.
//     * @return A List of strings representing the shape of the recipe.
//     */
//    private static List<String> getRecipeShape(FileConfiguration config, String recipeID) {
//        String path = "recipes." + recipeID + ".shape";
//        return config.getStringList(path);
//    }
//
//    /**
//     * Validates the shape of a crafting recipe, ensuring it meets the expected dimensions.
//     *
//     * @param shape A list of strings representing the recipe's shape.
//     * @param recipeID The recipe key, used for logging purposes.
//     * @throws InvalidRecipeException If the shape is invalid (not 3x3).
//     */
//    private static void validateShape(List<String> shape, String recipeID) throws InvalidRecipeException {
//        if (shape.size() != 3) {
//            throw new InvalidRecipeException(String.format("Failed to load recipe '%s'. Missing valid shape.", recipeID));
//        }
//        for (String row : shape) {
//            if (row.length() != 3) {
//                throw new InvalidRecipeException(String.format("Failed to load recipe '%s'. Missing valid shape.", recipeID));
//            }
//        }
//    }

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

//    private static void addShape(String recipeID, ShapedRecipe recipe) throws InvalidRecipeException {
//        String path = "recipes." + recipeID + ".shape";
//        List<String> shape = config.getStringList(path);
//
//        //validation
//        if (shape.size() != 3) {
//            throw new InvalidRecipeException("Invalid shape.");
//        }
//
//        for (String row : shape) {
//            if (row.length() != 3) {
//                throw new InvalidRecipeException("Invalid shape");
//            }
//        }
//
//        recipe.shape(shape.get(0), shape.get(1), shape.get(2));
//
//    }

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
//
//    private static void validateIngredients(Map<Character, String> ingredients) throws InvalidRecipeException {
//        for (String ingredientID : ingredients.values()) {
//            Material material = Material.getMaterial(ingredientID);
//            if (material == null || !material.isItem()) {
//                throw new InvalidRecipeException(String.format("Invalid or non-item ingredient material '%s'.", ingredientID));
//            }
//        }
//    }

//    private static Material validateIngredient(String ingredientID) throws InvalidRecipeException {
//
//        Material ingredient = Material.getMaterial(ingredientID);
//
//        if (ingredient == null || !ingredient.isItem()) {
//            throw new InvalidRecipeException(String.format("Invalid or non-item ingredient material '%s'.", ingredientID));
//        }
//
//        return ingredient;
//
//    }

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

//    private static void addIngredients(String recipeID, ShapedRecipe recipe) throws InvalidRecipeException {
//        String path = "recipes." + recipeID + ".ingredients";
//        ConfigurationSection ingredientsSection = config.getConfigurationSection(path);
//
//        if (ingredientsSection == null || ingredientsSection.getKeys(false).isEmpty()) {
//            throw new InvalidRecipeException("Could not find ingredients.");
//        }
//
//        String shapeChars = String.join("", recipe.getShape());
//
//        //validate charKey is used in shape
//        for (String charKey : ingredientsSection.getKeys(false)) {
//            if (!shapeChars.contains(charKey)) {
//                throw new InvalidRecipeException(String.format("Ingredient '%s' not utilized in shape.", charKey));
//            }
//
//            Material ingredientMaterial = getIngredientMaterial(ingredientsSection, charKey);
//            recipe.setIngredient(charKey.charAt(0), ingredientMaterial);
//        }
//    }

//    private static Material getIngredientMaterial(ConfigurationSection ingredientSection, String charKey) throws InvalidRecipeException {
//        String ingredientID = ingredientSection.getString(charKey);
//        Material ingredientMaterial = Material.getMaterial(ingredientID);
//
//        //validation
//        if (ingredientMaterial == null || !ingredientMaterial.isItem()) {
//            throw new InvalidRecipeException(String.format("Invalid or non-item ingredient material '%s'.", ingredientID));
//        }
//
//        return ingredientMaterial;
//    }

//    private static void validateIngredient(String ingredientID) throws InvalidRecipeException {
//        Material ingredientMaterial = Material.getMaterial(ingredientID);
//        if (ingredientMaterial == null || !ingredientMaterial.isItem()) {
//            throw new InvalidRecipeException(String.format("Invalid or non-item ingredient material '%s'.", ingredientID));
//        }



    private static void registerRecipe(NamespacedKey namespacedKey, ShapedRecipe recipe) {
        if (Bukkit.getServer().addRecipe(recipe)) {
            registeredRecipes.add(namespacedKey);
        } else {
            logger.warning(String.format("Failed to add recipe '%s'. Does it already exist?", namespacedKey));
        }
    }

}
