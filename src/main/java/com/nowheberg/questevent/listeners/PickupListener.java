package com.nowheberg.questevent.listeners;

import com.nowheberg.questevent.core.EventManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class PickupListener implements Listener {

    private final EventManager manager;

    public PickupListener(EventManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        ItemStack stack = event.getItem().getItemStack();
        Material mat = stack.getType();
        int amount = stack.getAmount();
        manager.onPickup(player, mat, amount);
    }
}
