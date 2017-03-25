package com.minejunkie.blacklist.command;

import com.minejunkie.blacklist.manager.DBManager;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class UnblacklistCMD {

    @Command(
            aliases = "unblacklist",
            desc = "Unblacklist a player from joining the network.",
            usage = "<name>",
            min = 1,
            max = 1
    )
    @CommandPermissions("dblacklist.unblacklist")

    public static void onUnblacklistCMD(CommandContext args, CommandSender sender) throws SQLException {
        DBManager manager = new DBManager();
        String name = args.getString(0);
        Player pSender = null;
        if (sender instanceof Player) pSender = (Player) sender;
        manager.unblacklistAsync(name, pSender);
    }

}
