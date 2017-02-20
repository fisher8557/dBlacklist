package com.minejunkie.blacklist.manager;

import com.minejunkie.blacklist.Blacklist;
import com.minejunkie.blacklist.mysql.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBManager {

    private MySQL m = Blacklist.getInstance().mysql;

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
     * Checks if a player is blacklisted asynchronously.
     * Not used, as storing a list of UUIDs is more efficient at login.
     *
     * @param uuid Player's uuid to check.
     * @param callback returns true if blacklisted, false otherwise.
     */

    public void isBlacklisted(final String uuid, final Callback<Boolean> callback) {
        new BukkitRunnable() {
            /*    ASYNC    */

            @Override
            public void run() {
                final boolean[] result = new boolean[1];
                try {
                    result[0] = m.check("SELECT * FROM blacklists WHERE uuid = '" + uuid + "';");
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                new BukkitRunnable() {
                    /*    SYNC    */

                    @Override
                    public void run() {
                        Bukkit.getServer().broadcastMessage("Blacklist check has found to be " + result[0]);
                        callback.onSuccess(result[0]);
                    }
                }.runTask(Blacklist.getInstance());
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
}
