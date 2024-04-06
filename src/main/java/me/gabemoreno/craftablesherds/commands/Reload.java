package me.gabemoreno.craftablesherds.commands;

import me.gabemoreno.craftablesherds.CraftableSherds;
import me.gabemoreno.craftablesherds.core.LoadingOutcome;
import me.gabemoreno.craftablesherds.core.RecipeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reload implements CommandExecutor {

    private final String prefix = ChatColor.GOLD + "[CraftableSherds] ";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        LoadingOutcome outcome = RecipeManager.reloadRecipes();

        if (!(sender instanceof Player)) return true;

        String feedback;

        switch (outcome) {
            case FAILURE:
                feedback = ChatColor.RED + "Failed to load recipes. See console for more details.";
                break;
            case WARNING:
                feedback = ChatColor.YELLOW + "Reloaded with warnings. See console for more details.";
                break;
            case SUCCESS:
                feedback = ChatColor.GREEN + "Reloaded all recipes.";
                break;
            default:
                feedback = ChatColor.RED + "Attempted to reload with unknown outcome. See console for more details.";
                break;
        }

        sender.sendMessage(prefix + feedback);

        return true;
    }
}
