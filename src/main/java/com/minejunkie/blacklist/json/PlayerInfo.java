package com.minejunkie.blacklist.json;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class PlayerInfo {

    Gson gson = new Gson();

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


    public NullPlayer getNullPlayer(String name) throws Exception {
        String json = readUrl("https://api.mojang.com/users/profiles/minecraft/" + name);

        if (json.isEmpty()) return null;

        return gson.fromJson(json, NullPlayer.class);
    }

    /* Asynchronous, takes too long to query.
    public void getNullPlayer(final String name, final Callback<NullPlayer> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final NullPlayer[] result = new NullPlayer[1];
                String json = "";
                try {
                    json = readUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (json.isEmpty()) result[0] = null;
                result[0] = gson.fromJson(json, NullPlayer.class);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(result[0]);
                    }
                }.runTask(Blacklist.getInstance());
            }
        }.runTaskAsynchronously(Blacklist.getInstance());

    }
    */

    public class NullPlayer {
        String id, name;

        public NullPlayer(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

}
