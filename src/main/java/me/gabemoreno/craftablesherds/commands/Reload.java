package me.gabemoreno.craftablesherds.commands;

import me.gabemoreno.craftablesherds.CraftableSherds;
import me.gabemoreno.craftablesherds.core.RecipeManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Reload implements CommandExecutor {

    private final String prefix = ChatColor.YELLOW + "[CraftableSherds]";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        RecipeManager.reloadRecipes();

        if (sender instanceof Player) {
            sender.sendMessage(prefix + ChatColor.GRAY + " Reloaded recipes. See console for more details.");;
        }
        return true;
    }
}
