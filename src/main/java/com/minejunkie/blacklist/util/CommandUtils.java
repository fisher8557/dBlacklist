package com.minejunkie.blacklist.util;

import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtils {

    public Player getPlayer(String name) throws CommandException {
        Player player = Bukkit.getPlayer(name);
        if (player == null) throw new CommandException("Player not found.");
        return player;
    }

    public Player senderAsPlayer(CommandSender sender) throws CommandException {
        if (sender instanceof Player) return (Player) sender;
        throw new CommandException("You must be a player to use that command.");
    }
}