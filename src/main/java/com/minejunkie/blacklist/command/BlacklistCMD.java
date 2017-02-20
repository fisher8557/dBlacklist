package com.minejunkie.blacklist.command;

import com.minejunkie.blacklist.Blacklist;
import com.minejunkie.blacklist.json.PlayerInfo;
import com.minejunkie.blacklist.manager.DBManager;
import com.minejunkie.blacklist.mysql.MySQL;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class BlacklistCMD {

    @Command(
            aliases = "blacklist",
            desc = "Blacklist a player from joining the network.",
            min = 1,
            max = -1,
            flags = ":es"
    )
    @CommandPermissions("dblacklist.blacklist")

    public static void onBlacklistCMD(CommandContext args, CommandSender sender) throws CommandException, SQLException, ClassNotFoundException {

        // TEST
        long time = System.currentTimeMillis();

        /*  Initialize all classes used  */
        Connection c = Blacklist.getInstance().c;
        final MySQL m = Blacklist.getInstance().mysql;
        DBManager dbManager = Blacklist.getInstance().dbManager;
        PlayerInfo playerInfo = new PlayerInfo();
        /*                               */

        /* Initialize all variables used */
        Player target = null, p = null;
        boolean broadcast = !args.hasFlag('s');
        final boolean blacklisted[] = new boolean[1];
        /*                               */

        Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [1]");


        String name = args.getString(0), uuid = "", reason = "";

        try {
            if (Bukkit.getPlayer(name) != null) {
                target = Bukkit.getPlayer(name);
                uuid = target.getUniqueId().toString().replaceAll("-", "");
            } else if (playerInfo.getNullPlayer(name) != null) {
                uuid = playerInfo.getNullPlayer(name).getId();
                name = playerInfo.getNullPlayer(name).getName();
                Bukkit.getServer().broadcastMessage("NullPlayer found! Name: " + name + " UUID: " + uuid);
            }

        } catch (Exception e) { e.printStackTrace(); }

        Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [2]");

        if (sender instanceof Player)  p = (Player) sender;

        if (uuid.equals("")) {
            if (p != null) p.sendMessage(ChatColor.RED + "Invalid player name or could not reach uuid.");
            else Blacklist.getInstance().getLogger().log(Level.INFO, "Invalid player name or could not reach uuid.");
            return;
        }

        if (args.argsLength() > 1) reason = args.getRemainingString(1);

        Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [3]");

        blacklisted[0] = Blacklist.getInstance().blacklisted.contains(uuid);

        Bukkit.getServer().broadcastMessage("[3] Blacklist check has found to be " + blacklisted[0]);

        if (blacklisted[0] && args.hasFlag('e')) {
            dbManager.makeAsync(reason, name);
            if (p != null) p.sendMessage(ChatColor.GREEN + name + (name.endsWith("s") ? "'" : "'s") + " reason has been updated.");
            else Blacklist.getInstance().getLogger().log(Level.INFO, name + (name.endsWith("s") ? "'" : "'s") + " reason has been updated.");
            return;
        }

        Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [4]");

        if (!blacklisted[0]) {

            if (args.hasFlag('e')) {
                if (p != null) p.sendMessage(ChatColor.RED + name + " is not blacklisted. The '-e' flag is used to edit.");
                else Blacklist.getInstance().getLogger().log(Level.INFO, name + " is not blacklisted. The '-e' flag is used to edit.");
                return;
            }

            final PreparedStatement pS = c.prepareStatement("INSERT INTO blacklists (name, uuid, ip, banned_by, reason) VALUES (?, ?, ?, ?, ?);");
            pS.setString(1, name);
            pS.setString(2, uuid);
            pS.setString(3, target != null ? target.getAddress().getHostName() : "undefined");
            pS.setString(4, p != null ? p.getName() : "CONSOLE");
            pS.setString(5, reason);
            dbManager.makeAsync(pS);

            Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [5]");

            // Add uuid to list
            Blacklist.getInstance().blacklisted.add(uuid);

            // Kick player if online
            if (target != null) target.kickPlayer(ChatColor.RED + "You have been blacklisted from the MineJunkie network.");

            Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [6]");

            if (broadcast) Bukkit.getServer().broadcastMessage(ChatColor.GREEN + name + " has been blacklisted" + (p != null ? " by " + p.getName() + "." : ".") + " in " + (System.currentTimeMillis() - time) + "ms. [7]");
            if (p != null) p.sendMessage(ChatColor.GREEN + "You have successfully blacklisted " + name + ".");
        } else if (p != null) p.sendMessage(ChatColor.RED + name + " is already blacklisted. Use flag '-e' to update their reason.");
        else Blacklist.getInstance().getLogger().log(Level.INFO, name + " is already blacklisted. Use flag '-e' to update their reason.");
    }
}
