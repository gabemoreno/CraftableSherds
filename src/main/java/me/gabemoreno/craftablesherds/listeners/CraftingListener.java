package me.gabemoreno.craftablesherds.listeners;

import me.gabemoreno.craftablesherds.core.RecipeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Recipe;

import java.util.List;

public class CraftingListener implements Listener {

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {

        Recipe craftingRecipe = event.getRecipe();
        if (craftingRecipe == null) return;

        Material resultType = craftingRecipe.getResult().getType();
        if (!resultType.name().endsWith("_POTTERY_SHERD")) return;

        List<NamespacedKey> registeredRecipes = RecipeManager.getRegisteredRecipes();

        for (NamespacedKey namespacedKey : registeredRecipes) {

            Recipe registeredRecipe = Bukkit.getServer().getRecipe(namespacedKey);

            if (registeredRecipe == null) continue;
            if (!registeredRecipe.equals(craftingRecipe)) continue;

            String permission = String.format("craftablesherds.craft.%s", namespacedKey.getKey());
            if (event.getView().getPlayer().hasPermission(permission)) return;

            //should cancel crafting
            event.getInventory().setResult(null);
            break;
        }
    }
}
