package com.minejunkie.blacklist.listener;

import com.minejunkie.blacklist.Blacklist;
import com.sk89q.minecraft.util.commands.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerListener implements Listener {

    private Blacklist plugin;

    public PlayerListener(Blacklist plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) throws SQLException, ClassNotFoundException {
        UUID uuid = e.getUniqueId();
        if (plugin.mysql.check("SELECT * FROM blacklists WHERE uuid LIKE '" + uuid.toString() + "';")) {
            e.disallow(Result.KICK_BANNED, ChatColor.RED + "You are blacklisted from the MineJunkie network.");

        }
    }
}
