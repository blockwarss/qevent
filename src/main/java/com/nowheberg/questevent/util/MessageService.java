package com.nowheberg.questevent.util;

import com.nowheberg.questevent.QuestEventPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public class MessageService {

    private final QuestEventPlugin plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessageService(QuestEventPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File f = new File(plugin.getDataFolder(), "messages.yml");
        if (!f.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(f);
        prefix = messages.getString("prefix", "&a&lꜱᴜʀᴠɪᴇ&c&lᴛɪᴋᴛᴏᴋ&f | ");
    }

    public String get(String key) {
        return messages.getString(key, key);
    }

    public String prefixed(String raw) {
        return prefix + raw;
    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String format(String key, Map<String, String> placeholders) {
        String val = get(key);
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            val = val.replace("{" + e.getKey() + "}", e.getValue());
        }
        return color(prefixed(val));
    }
}
