package com.minejunkie.blacklist.command;

import com.minejunkie.blacklist.Blacklist;
import com.minejunkie.blacklist.manager.DBManager;
import com.minejunkie.blacklist.util.CommonUtils;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BlacklistCMD {

    @Command(
            aliases = "blacklist",
            desc = "Blacklist a player from joining the network.",
            min = 1,
            max = -1,
            flags = ":es"
    )
    @CommandPermissions("dblacklist.blacklist")

    public static void onBlacklistCMD(CommandContext args, CommandSender sender) throws SQLException {

        // TEST
        long time = System.currentTimeMillis();

        /*  Initialize all classes used  */
        DBManager dbManager = Blacklist.getInstance().dbManager;
        CommonUtils util = new CommonUtils();
        /*                               */

        /* Initialize all variables used */
        Player target = null, p = null;
        boolean broadcast = !args.hasFlag('s'), blacklisted;
        /*                               */

        Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [1]");


        final String name = args.getString(0);
        String uuid = "", reason = "";

        if (args.argsLength() > 1) reason = args.getRemainingString(1);
        if (sender instanceof Player) p = (Player) sender;

        try {
            if (Bukkit.getPlayer(name) != null) {
                target = Bukkit.getPlayer(name);
                uuid = target.getUniqueId().toString().replaceAll("-", "");
            } else {
                dbManager.blacklistAsync(p, name, reason, args.hasFlag('e'), broadcast);
                return;
            }

        } catch (Exception e) { e.printStackTrace(); }

        Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [2]");

        if (uuid.equals("")) {
            util.sendIfNotNull(p, ChatColor.RED + "Invalid player name or could not reach uuid.");
            return;
        }

        Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [3]");

        blacklisted = Blacklist.getInstance().blacklisted.contains(uuid);

        Bukkit.getServer().broadcastMessage("[3] Blacklist check has found to be " + blacklisted);

        if (blacklisted && args.hasFlag('e')) {
            dbManager.makeAsync(reason, name);
            util.sendIfNotNull(p, ChatColor.GREEN + name + (name.endsWith("s") ? "'" : "'s") + " reason has been updated.");
            return;
        }

        Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [4]");

        if (!blacklisted) {

            if (args.hasFlag('e')) {
                util.sendIfNotNull(p, ChatColor.RED + name + " is not blacklisted. The '-e' flag is used to edit.");
                return;
            }

            assert target != null;
            final PreparedStatement pS = util.getInsertStatement(name, uuid, target.getAddress().getHostName(), (p != null ? p.getName() : "CONSOLE"), reason);
            dbManager.makeAsync(pS);

            Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [5]");

            // Add uuid to list
            Blacklist.getInstance().blacklisted.add(uuid);

            // Kick player if online
            target.kickPlayer(ChatColor.RED + "You have been blacklisted from the MineJunkie network.");

            Bukkit.getServer().broadcastMessage((System.currentTimeMillis() - time) + "ms. [6]");

            if (broadcast) Bukkit.getServer().broadcastMessage(ChatColor.GREEN + name + " has been blacklisted" + (p != null ? " by " + p.getName() + "." : ".") + " in " + (System.currentTimeMillis() - time) + "ms. [7]");

            util.sendIfNotNull(p, ChatColor.GREEN + "You have successfully blacklisted " + name + ".");
        } else {
            util.sendIfNotNull(p, name + " is already blacklisted. Use flag '-e' to update their reason.");
        }
    }
}
