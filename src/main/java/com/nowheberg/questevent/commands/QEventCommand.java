package com.nowheberg.questevent.commands;

import com.nowheberg.questevent.core.EventManager;
import com.nowheberg.questevent.util.MessageService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class QEventCommand implements TabExecutor {

    private final EventManager manager;
    private final MessageService msg;

    public QEventCommand(EventManager manager, MessageService msg) {
        this.manager = manager;
        this.msg = msg;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("questevent.admin")) {
            sender.sendMessage(msg.color(msg.prefixed("&cPermission insuffisante.")));
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("start")) {
            sender.sendMessage(msg.color(msg.prefixed(msg.get("usage_start"))));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(msg.color(msg.prefixed(msg.get("usage_start"))));
            return true;
        }

        if (manager.isRunning()) {
            sender.sendMessage(msg.color(msg.prefixed(msg.get("already_running"))));
            return true;
        }

        Duration duration = parseDuration(args[1]);
        if (duration.isZero() || duration.isNegative()) {
            sender.sendMessage(msg.color(msg.prefixed(msg.get("invalid_duration"))));
            return true;
        }

        boolean ok = manager.start(duration);
        if (!ok) {
            sender.sendMessage(msg.color(msg.prefixed(msg.get("no_items"))));
        }
        return true;
    }

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(h|m|s)");

    private Duration parseDuration(String input) {
        String in = input.toLowerCase();
        java.util.regex.Matcher m = DURATION_PATTERN.matcher(in);
        long seconds = 0;
        boolean found = false;
        while (m.find()) {
            found = true;
            long val = Long.parseLong(m.group(1));
            String unit = m.group(2);
            switch (unit) {
                case "h" -> seconds += val * 3600L;
                case "m" -> seconds += val * 60L;
                case "s" -> seconds += val;
            }
        }
        if (!found) return Duration.ZERO;
        return Duration.ofSeconds(seconds);
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            if ("start".startsWith(args[0].toLowerCase())) out.add("start");
        } else if (args.length == 2 && "start".equalsIgnoreCase(args[0])) {
            out.add("30m");
            out.add("1h");
            out.add("1h30m");
            out.add("90s");
        }
        return out;
    }
}
