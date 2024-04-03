package me.gabemoreno.craftablesherds;

import me.gabemoreno.craftablesherds.commands.Reload;
import me.gabemoreno.craftablesherds.core.RecipeManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CraftableSherds extends JavaPlugin {

    private static CraftableSherds instance;

    public static CraftableSherds getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        RecipeManager.loadRecipes();
        Objects.requireNonNull(getCommand("csreload")).setExecutor(new Reload());

    }

    @Override
    public void onDisable() {
        RecipeManager.unloadRecipes();
    }

}
