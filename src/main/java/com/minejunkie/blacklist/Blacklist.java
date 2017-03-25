package com.minejunkie.blacklist;

import com.minejunkie.blacklist.command.BlacklistCMD;
import com.minejunkie.blacklist.command.UnblacklistCMD;
import com.minejunkie.blacklist.listener.PlayerListener;
import com.minejunkie.blacklist.manager.DBManager;
import com.minejunkie.blacklist.mysql.MySQL;
import com.sk89q.bukkit.util.BukkitCommandsManager;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

public class Blacklist extends JavaPlugin {

    private static Blacklist instance;

    private CommandsManagerRegistration cmdRegister;
    private CommandsManager<CommandSender> commands;

    public DBManager dbManager;

    public Connection c;
    public MySQL mysql;

    public final ArrayList<String> blacklisted = new ArrayList<>();

    /*
     * TODO
     * Unblacklist
     * Configurable Messages
     * Clean
     */


    public void onEnable() {
        instance = this;

        getConfig().options().copyHeader(true);
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        if (initDB()) getLogger().log(Level.INFO, "[dBlacklist] Database attached!");

        new PlayerListener(this);

        setupCommands();

        dbManager = new DBManager();

        getBlacklisted();
    }

    public void onDisable()  {

        try {
            mysql.closeConnection();
        } catch (SQLException e) {
            getLogger().log(Level.INFO, "Error: " + e);
        }

        cmdRegister = null;
        commands = null;
        instance = null;
    }

    /*
     * Creates ArrayList of blacklisted UUIDs.
     */
    private void getBlacklisted() {

        dbManager.getBlacklisted(new DBManager.Callback<ArrayList>() {
            @Override
            public void onSuccess(ArrayList done) {
                for (Object s : done) {
                    blacklisted.add(s.toString().replaceAll("-", ""));
                }
            }

            @Override
            public void onFailure(Throwable cause) {}
        });
    }

    /**
     * Initializes the SQL database
     * Synchronized because it is run during server start. (A small delay is acceptable).
     * @return true if connection has been established.
     */

    private boolean initDB() {
        String hostname = getConfig().getString("mysql.hostname"), database = getConfig().getString("mysql.database"), username = getConfig().getString("mysql.username"), password = getConfig().getString("mysql.password");
        int port = getConfig().getInt("mysql.port");

        getLogger().log(Level.INFO, "[MYSQL] ATTEMPTING TO ATTACH TO SERVER @ HOSTNAME: " + hostname
                + " PORT: " + port + " DATABASE: " + database + " USERNAME:" + username + " PASSWORD:" + password);

        mysql = new MySQL(hostname, String.valueOf(port), database, username, password);

        try {
            c = mysql.openConnection();
            mysql.updateSQL("CREATE TABLE IF NOT EXISTS blacklists (id INT NOT NULL AUTO_INCREMENT, name VARCHAR(20) NOT NULL, uuid VARCHAR(36) NOT NULL, ip VARCHAR(15) NOT NULL, banned_by VARCHAR(20) NOT NULL, reason TEXT NOT NULL, PRIMARY KEY (id));");
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error: " + e);
        }

        return (c != null);
    }

    public static Blacklist getInstance() {
        return instance;
    }

    private void setupCommands() {
        commands = new BukkitCommandsManager();
        cmdRegister = new CommandsManagerRegistration(this, this.commands);
        cmdRegister.register(BlacklistCMD.class);
        cmdRegister.register(UnblacklistCMD.class);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        try {
            this.commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
        } catch (MissingNestedCommandException e) {
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
            sender.sendMessage(ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                sender.sendMessage(ChatColor.RED + "Number expected, string received instead.");
            } else {
                sender.sendMessage(ChatColor.RED + "An error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            sender.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }
}
