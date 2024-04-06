package me.gabemoreno.craftablesherds.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

public class CraftingListener implements Listener {

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {

        Recipe craftingRecipe = event.getRecipe();
        if (craftingRecipe == null) return;
        //add support later for shapeless recipes?
        if (!(craftingRecipe instanceof ShapedRecipe)) return;
        ShapedRecipe shapedRecipe = (ShapedRecipe) craftingRecipe;

        Material resultType = craftingRecipe.getResult().getType();
        String sherdSuffix = "_POTTERY_SHERD";
        if (!resultType.name().endsWith(sherdSuffix)) return;
        String sherdID = resultType.name().replace(sherdSuffix, "").toLowerCase();

        NamespacedKey namespacedKey = shapedRecipe.getKey();
        if (!namespacedKey.getNamespace().equals("craftablesherds")) return;
        if (!namespacedKey.getKey().equals(sherdID)) return;

        if (event.getView().getPlayer().hasPermission(String.format("craftablesherds.craft.%s", sherdID))) return;

        event.getInventory().setResult(null);
    }
}
