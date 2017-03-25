package com.minejunkie.blacklist.manager;

import com.google.gson.Gson;
import com.minejunkie.blacklist.Blacklist;
import com.minejunkie.blacklist.mysql.MySQL;
import com.minejunkie.blacklist.util.CommonUtils;
import com.sk89q.minecraft.util.commands.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBManager {

    private final MySQL m = Blacklist.getInstance().mysql;
    private final CommonUtils util = new CommonUtils();
    private final Gson gson = new Gson();

    /**
     * Updating a player's blacklist asynchronously
     *
     * @param reason Reason for blacklist to be updated.
     * @param name   Name to update reasoning for.
     */

    public void makeAsync(final String reason, final String name) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    m.updateSQL("UPDATE blacklists SET reason = '" + reason + "' WHERE UPPER(name) LIKE '" + name.toUpperCase() + "';");
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Blacklist.getInstance());
    }

    /**
     * Creating a blacklist asynchronously.
     *
     * @param pS PreparedStatement to be executed.
     */

    public void makeAsync(final PreparedStatement pS) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    pS.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Blacklist.getInstance());
    }

    /**
     * Asynchronously bans a player who is not online.
     *
     * @param sender    Player sending the command.
     * @param toFind    Name used to find the UUID.
     * @param reason    Reason for the blacklist.
     * @param edit      True if editing a player's blacklist who is not online.
     * @param broadcast True if the blacklist should be broadcasted to the server.
     */

    public void blacklistAsync(final Player sender, final String toFind, final String reason, final boolean edit, final boolean broadcast) {
        util.sendIfNotNull(sender, ChatColor.GREEN + "User is not online, attempting to blacklist async.");

        final long time = System.currentTimeMillis();

        new BukkitRunnable() {
            @Override
            public void run() {
                NullPlayer nullPlayer;
                String json = "";
                try {
                    json = util.readUrl("https://api.mojang.com/users/profiles/minecraft/" + toFind);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                if (json.isEmpty()) nullPlayer = null;
                nullPlayer = gson.fromJson(json, NullPlayer.class);

                String uuid = "", name = "";
                if (nullPlayer != null) {
                    uuid = nullPlayer.getId();
                    name = nullPlayer.getName();
                }

                if (uuid.isEmpty()) {
                    util.sendIfNotNull(sender, ChatColor.RED + "Invalid player name or could not reach uuid.");
                    return;
                }

                boolean blacklisted = Blacklist.getInstance().blacklisted.contains(uuid);

                if (edit) {
                    if (blacklisted) {
                        makeAsync(reason, name);
                        util.sendIfNotNull(sender, ChatColor.GREEN + name + (name.endsWith("s") ? "'" : "'s") + " reason has been updated.");
                    } else {
                        util.sendIfNotNull(sender, ChatColor.RED + (name.isEmpty() ? name : "") + " is not blacklisted. The '-e' flag is used to edit.");
                    }
                } else {
                    if (blacklisted) { util.sendIfNotNull(sender, name + " is already blacklisted. Use flag '-e' to update their reason."); return; }
                    try {
                        makeAsync(util.getInsertStatement(name, uuid, "undefined", sender != null ? sender.getName() : "CONSOLE", reason));
                    } catch (SQLException e) { e.printStackTrace(); }
                    util.sendIfNotNull(sender, ChatColor.GREEN + "You have successfully blacklisted " + name + ".");
                    Blacklist.getInstance().blacklisted.add(uuid);
                    if (broadcast) {
                        Bukkit.getServer().broadcastMessage(ChatColor.GREEN + name + " has been blacklisted" + (sender != null ? " by " + sender.getName() + "." : "."));
                    }
                }

            }
        }.runTaskAsynchronously(Blacklist.getInstance());
    }

    /**
     * Unblacklists a player asyncronously.
     *
     * @param name Player's name to check.
     * @param sender Player sending the unblacklist command.
     */

    public void unblacklistAsync(final String name, final Player sender) {
        final BukkitTask bukkitTask = new BukkitRunnable() {
            /*    ASYNC    */

            @Override
            public void run() {
                NullPlayer nullPlayer = null;
                String json = "";
                try {
                    json = util.readUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (json.isEmpty()) {
                    util.sendIfNotNull(sender, ChatColor.RED + "User is not blacklisted!");
                    return;
                }

                nullPlayer = gson.fromJson(json, NullPlayer.class);

                String uuid = nullPlayer.getId();

                if (Blacklist.getInstance().blacklisted.contains(uuid)) {
                    Blacklist.getInstance().blacklisted.remove(uuid);
                    try {
                        m.updateSQL("DELETE FROM blacklists WHERE uuid='" + uuid + "'");
                        util.sendIfNotNull(sender, ChatColor.GREEN + "User has successfully been unblacklisted.");
                    } catch (SQLException | ClassNotFoundException e) { util.sendIfNotNull(sender, ChatColor.RED + "User was not found in the database!"); }
                } else util.sendIfNotNull(sender, ChatColor.RED + "User is not blacklisted!");

            }

        }.runTaskAsynchronously(Blacklist.getInstance());
    }

    /**
     * Generates a list of blacklisted UUIDs.
     *
     * @param callback list of T objects -> UUIDs
     */

    public void getBlacklisted(final Callback<ArrayList> callback) {
        new BukkitRunnable() {
            /*    ASYNC    */

            @Override
            public void run() {
                final ArrayList<String> result = new ArrayList<>();
                try {
                    ResultSet rs = m.querySQL("SELECT uuid from blacklists;");
                    while (rs.next()) {
                        result.add(rs.getString(1));
                    }

                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                new BukkitRunnable() {
                    /*    SYNC    */

                    @Override
                    public void run() {
                        callback.onSuccess(result);
                    }

                }.runTask(Blacklist.getInstance());
            }
        }.runTaskAsynchronously(Blacklist.getInstance());
    }

    public interface Callback<T> {
        void onSuccess(T done);
        void onFailure(Throwable cause);
    }

    public class NullPlayer {
        String id, name;

        public NullPlayer(String id, String name) {
            this.id = id;
            this.name = name;
        }

        String getId() {
            return id;
        }

        String getName() {
            return name;
        }
    }
}
