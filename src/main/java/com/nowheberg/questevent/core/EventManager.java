package com.nowheberg.questevent.core;

import com.nowheberg.questevent.QuestEventPlugin;
import com.nowheberg.questevent.model.ItemEntry;
import com.nowheberg.questevent.util.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EventManager {

    private final QuestEventPlugin plugin;
    private final MessageService msg;

    private boolean running = false;
    private Material currentMaterial = null;
    private int required = 0;
    private Instant endAt = null;

    private final Map<UUID, Integer> counts = new HashMap<>();
    private final List<UUID> winners = new ArrayList<>(3);

    private BukkitRunnable endTask = null;
    private BukkitRunnable announceTask = null;

    public EventManager(QuestEventPlugin plugin) {
        this.plugin = plugin;
        this.msg = plugin.messages();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean start(Duration duration) {
        ItemEntry entry = pickRandomItem();
        if (entry == null) return false;

        this.currentMaterial = entry.material();
        this.required = entry.required();
        this.endAt = Instant.now().plus(duration);
        this.running = true;

        counts.clear();
        winners.clear();

        // Schedule end
        long ticks = duration.toMillis() / 50L;
        endTask = new BukkitRunnable() {
            @Override public void run() { forceEnd(true); }
        };
        endTask.runTaskLater(plugin, Math.max(1L, ticks));

        // Announcements
        int intervalSec = plugin.getConfig().getInt("announcements.intervalSeconds", 120);
        if (intervalSec > 0) {
            announceTask = new BukkitRunnable() {
                @Override public void run() {
                    if (!running) return;
                    long left = Math.max(0L, java.time.Duration.between(Instant.now(), endAt).toSeconds());
                    String leftStr = humanDuration(left);
                    Bukkit.broadcastMessage(msg.color(msg.prefixed(msg.get("time_left_broadcast")
                            .replace("{left}", leftStr)
                            .replace("{item}", currentMaterial.name())
                            .replace("{required}", String.valueOf(required))
                    )));
                }
            };
            announceTask.runTaskTimer(plugin, 20L * intervalSec, 20L * intervalSec);
        }

        String durStr = humanDuration(duration.getSeconds());
        Bukkit.broadcastMessage(msg.color(msg.prefixed(msg.get("started")
                .replace("{duration}", durStr)
                .replace("{item}", currentMaterial.name())
                .replace("{required}", String.valueOf(required))
        )));
        return true;
    }

    public void onPickup(Player player, Material material, int amount) {
        if (!running) return;
        if (material != currentMaterial) return;
        if (Instant.now().isAfter(endAt)) return;

        UUID id = player.getUniqueId();
        int before = counts.getOrDefault(id, 0);
        int after = Math.min(required, before + Math.max(0, amount));
        if (after == before) return;
        counts.put(id, after);

        // Check completion
        if (before < required && after >= required && winners.size() < 3 && !winners.contains(id)) {
            winners.add(id);
            int place = winners.size();
            String name = player.getName();

            // Rewards
            List<String> cmds = switch (place) {
                case 1 -> plugin.getConfig().getStringList("rewards.first");
                case 2 -> plugin.getConfig().getStringList("rewards.second");
                case 3 -> plugin.getConfig().getStringList("rewards.third");
                default -> Collections.emptyList();
            };
            for (String raw : cmds) {
                String cmd = raw.replace("{player}", name);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }

            Bukkit.broadcastMessage(msg.color(msg.prefixed(msg.get("completed_rank")
                    .replace("{player}", name)
                    .replace("{place}", String.valueOf(place))
                    .replace("{required}", String.valueOf(required))
                    .replace("{item}", currentMaterial.name())
            )));

            if (winners.size() >= 3) {
                forceEnd(true);
            }
        }
    }

    public void forceEnd(boolean announce) {
        if (!running) return;
        running = false;

        if (endTask != null) { endTask.cancel(); endTask = null; }
        if (announceTask != null) { announceTask.cancel(); announceTask = null; }

        if (announce) {
            String first = nameOrDash(0);
            String second = nameOrDash(1);
            String third = nameOrDash(2);
            Bukkit.broadcastMessage(msg.color(msg.prefixed(msg.get("ended_top3")
                    .replace("{first}", first)
                    .replace("{second}", second)
                    .replace("{third}", third)
                    .replace("{item}", currentMaterial != null ? currentMaterial.name() : "?")
            )));
        }

        currentMaterial = null;
        required = 0;
        endAt = null;
        counts.clear();
        winners.clear();
    }

    private String nameOrDash(int idx) {
        if (idx < winners.size()) {
            UUID id = winners.get(idx);
            Player p = Bukkit.getPlayer(id);
            if (p != null) return p.getName();
            return java.util.Optional.ofNullable(Bukkit.getOfflinePlayer(id).getName()).orElse("—");
        }
        return "—";
    }

    private ItemEntry pickRandomItem() {
        FileConfiguration cfg = plugin.getConfig();
        java.util.List<?> list = cfg.getList("items");
        if (list == null || list.isEmpty()) return null;

        java.util.List<ItemEntry> entries = new java.util.ArrayList<>();
        for (Object o : list) {
            if (o instanceof java.util.Map<?, ?> m) {
                Object mat = m.get("material");
                Object req = m.get("required");
                if (mat == null || req == null) continue;
                try {
                    Material material = Material.valueOf(String.valueOf(mat).toUpperCase());
                    int required = Integer.parseInt(String.valueOf(req));
                    if (required > 0) {
                        entries.add(new ItemEntry(material, required));
                    }
                } catch (Exception ignored) {}
            }
        }
        if (entries.isEmpty()) return null;
        return entries.get(ThreadLocalRandom.current().nextInt(entries.size()));
    }

    private String humanDuration(long totalSeconds) {
        long s = totalSeconds % 60;
        long m = (totalSeconds / 60) % 60;
        long h = totalSeconds / 3600;
        StringBuilder sb = new StringBuilder();
        if (h > 0) sb.append(h).append("h");
        if (m > 0) { sb.append(m).append("m"); }
        if (h == 0 && m == 0) sb.append(s).append("s");
        return sb.toString();
    }
}
