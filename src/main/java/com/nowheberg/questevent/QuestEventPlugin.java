package com.nowheberg.questevent;

import com.nowheberg.questevent.commands.QEventCommand;
import com.nowheberg.questevent.core.EventManager;
import com.nowheberg.questevent.listeners.PickupListener;
import com.nowheberg.questevent.util.MessageService;
import org.bukkit.plugin.java.JavaPlugin;

public final class QuestEventPlugin extends JavaPlugin {

    private static QuestEventPlugin instance;
    private EventManager eventManager;
    private MessageService messageService;

    public static QuestEventPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResourceIfNotExists("messages.yml");

        messageService = new MessageService(this);
        eventManager = new EventManager(this);

        getServer().getPluginManager().registerEvents(new PickupListener(eventManager), this);
        getCommand("qevent").setExecutor(new QEventCommand(eventManager, messageService));
        getCommand("qevent").setTabCompleter(new QEventCommand(eventManager, messageService));
        getLogger().info("questevent enabled.");
    }

    private void saveResourceIfNotExists(String path) {
        if (getDataFolder() == null) return;
        java.io.File out = new java.io.File(getDataFolder(), path);
        if (!out.exists()) {
            saveResource(path, false);
        }
    }

    public EventManager eventManager() {
        return eventManager;
    }

    public MessageService messages() {
        return messageService;
    }

    @Override
    public void onDisable() {
        if (eventManager != null) {
            eventManager.forceEnd(false);
        }
    }
}
