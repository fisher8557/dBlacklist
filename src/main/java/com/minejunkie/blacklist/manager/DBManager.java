package com.minejunkie.blacklist.manager;

import com.minejunkie.blacklist.Blacklist;
import com.minejunkie.blacklist.mysql.MySQL;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DBManager {

    private MySQL m = Blacklist.getInstance().mysql;

    public void isBlacklisted(final String name, final Callback<Boolean> callback) {
        new BukkitRunnable() {
            /*    ASYNC    */

            @Override
            public void run() {

                try {
                    final boolean result = m.check("SELECT * FROM blacklists WHERE UPPER(name) LIKE '" + name.toUpperCase() + "';");
                    callback.onSuccess(result);
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }.runTaskAsynchronously(Blacklist.getInstance());
    }

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
