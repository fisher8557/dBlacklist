package com.minejunkie.blacklist.listener;

import com.minejunkie.blacklist.Blacklist;
import com.minejunkie.blacklist.util.CommonUtils;
import com.sk89q.minecraft.util.commands.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import java.util.UUID;

public class PlayerListener implements Listener {

    private final Blacklist plugin;
    private CommonUtils util = new CommonUtils();

    public PlayerListener(Blacklist plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        UUID uuid = e.getUniqueId();
        if (plugin.blacklisted.contains(uuid.toString().replaceAll("-", ""))) {
            e.disallow(Result.KICK_BANNED, util.colorize(Blacklist.getInstance().getConfig().getString("lang.blacklisted")));
        }
    }
}
