package com.minejunkie.blacklist.util;

import com.minejunkie.blacklist.Blacklist;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class CommonUtils {

    private final Connection c = Blacklist.getInstance().c;

    public String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);
            return buffer.toString();
        } finally {
            if (reader != null) reader.close();
        }
    }

    public void sendIfNotNull(Player player, String message) {
        if (player != null) player.sendMessage(message);
        else Bukkit.getLogger().log(Level.INFO, ChatColor.stripColor(message));
    }

    public PreparedStatement getInsertStatement(String name, String uuid, String targetIp, String senderName, String reason) throws SQLException {
        final PreparedStatement pS = c.prepareStatement("INSERT INTO blacklists (name, uuid, ip, banned_by, reason) VALUES (?, ?, ?, ?, ?);");
        pS.setString(1, name);
        pS.setString(2, uuid);
        pS.setString(3, targetIp);
        pS.setString(4, senderName);
        pS.setString(5, reason);
        return pS;
    }
}
