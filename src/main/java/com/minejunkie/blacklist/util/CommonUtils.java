package com.minejunkie.blacklist.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class CommonUtils {

    public String colorize(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    public ItemStack createItemStack(Material material, int amount, String name, String... lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        if(im.hasLore()) {
            if(!im.getLore().isEmpty()) {
                im.getLore().clear();
            }
            for(String l : lore) {
                im.getLore().add(l);
            }
        } else {
            List<String> loreList = new ArrayList<>();
            for(String l : lore) {
                loreList.add(l);
            }
            im.setLore(loreList);
        }
        item.setItemMeta(im);
        return item;
    }

    public boolean isFull(Player player) {
        return (player.getInventory().firstEmpty() == -1);
    }

    public boolean isVowel(String s) {
        return (s.startsWith("a") || s.startsWith("e") || s.startsWith("i") || s.startsWith("o") || s.startsWith("u"));
    }

    public boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }

        return true;
    }

    public double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
