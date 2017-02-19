package com.minejunkie.blacklist.command;

import com.minejunkie.blacklist.Blacklist;
import com.minejunkie.blacklist.manager.DBManager;
import com.minejunkie.blacklist.mysql.MySQL;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
        Connection c = Blacklist.getInstance().c;
        MySQL m = Blacklist.getInstance().mysql;
        DBManager dbManager = Blacklist.getInstance().dbManager;

        Player target = null, p = null;
        OfflinePlayer offlineTarget = null;
        boolean broadcast = !args.hasFlag('s');
        final boolean[] blacklisted = new boolean[1];

        if (sender instanceof Player)  p = (Player) sender;

        String name = args.getString(0), reason  = "";
        if (args.argsLength() > 1) reason = args.getRemainingString(1);

        dbManager.isBlacklisted(name, new DBManager.Callback<Boolean>() {

            @Override
            public void onSuccess(Boolean done) {
                blacklisted[0] = done;
            }

            @Override
            public void onFailure(Throwable cause) {}

        });

        if (!blacklisted[0] && args.hasFlag('e')) {
            if (p != null) p.sendMessage(ChatColor.RED + name + " is not blacklisted. The '-e' flag is used to edit.");
            else Blacklist.getInstance().getLogger().log(Level.INFO, name + " is not blacklisted. The '-e' flag is used to edit.");
            return;
        }

        if (blacklisted[0] && args.hasFlag('e')) {
            m.updateSQL("UPDATE blacklists SET reason = '" + reason + "' WHERE UPPER(name) LIKE '" + name.toUpperCase() + "';");
            if (p != null) p.sendMessage(ChatColor.GREEN + name + "'s reason has been updated.");
            else Blacklist.getInstance().getLogger().log(Level.INFO, name + "'s reason has been updated.");
            return;
        }

        if (!blacklisted[0]) {
            if (Bukkit.getServer().getPlayer(name) != null) target = Bukkit.getServer().getPlayer(name);
            else if (Bukkit.getServer().getOfflinePlayer(name) != null)
                offlineTarget = Bukkit.getServer().getOfflinePlayer(name);
            else if (p != null) {
                p.sendMessage(ChatColor.RED + "Player is not online and has never joined the server, will not broadcast.");
                if (broadcast) broadcast = false;
            } else {
                Blacklist.getInstance().getLogger().log(Level.INFO, "Player is not online and has never joined the server, will not broadcast.");
                if (broadcast) broadcast = false;
            }

            name = target != null ? target.getName() : offlineTarget != null ? offlineTarget.getName() : name;

            PreparedStatement pS = c.prepareStatement("INSERT INTO blacklists (name, uuid, ip, banned_by, reason) VALUES (?, ?, ?, ?, ?);");
            pS.setString(1, name);
            pS.setString(2, target != null ? target.getUniqueId().toString() : (offlineTarget != null ? offlineTarget.getUniqueId().toString() : "undefined"));
            pS.setString(3, target != null ? target.getAddress().getHostName() : "undefined");
            pS.setString(4, p != null ? p.getName() : "CONSOLE");
            pS.setString(5, reason);
            pS.executeUpdate();

            if (broadcast)
                Bukkit.getServer().broadcastMessage(ChatColor.GREEN + name + " has been blacklisted" + (p != null ? " by " + p.getName() + "." : "."));
            if (p != null) p.sendMessage(ChatColor.GREEN + "You have successfully blacklisted " + name + ".");
        } else if (p != null) p.sendMessage(ChatColor.RED + name + " is already blacklisted. Use flag '-e' to update their reason.");
        else Blacklist.getInstance().getLogger().log(Level.INFO, name + " is already blacklisted. Use flag '-e' to update their reason.");
    }
}
