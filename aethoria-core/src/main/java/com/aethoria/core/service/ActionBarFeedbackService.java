package com.aethoria.core.service;

import com.aethoria.core.AethoriaCorePlugin;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public final class ActionBarFeedbackService {
    private final AethoriaCorePlugin plugin;
    private final Map<UUID, EnumMap<FeedbackChannel, TimedMessage>> activeMessages = new java.util.HashMap<>();

    public ActionBarFeedbackService(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, FeedbackChannel channel, String message, long durationTicks) {
        long expiresAt = System.currentTimeMillis() + Math.max(1L, durationTicks) * 50L;
        activeMessages.computeIfAbsent(player.getUniqueId(), ignored -> new EnumMap<>(FeedbackChannel.class))
            .put(channel, new TimedMessage(message, expiresAt));
        render(player);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            cleanup(player);
            render(player);
        }, Math.max(1L, durationTicks));
    }

    private void cleanup(Player player) {
        EnumMap<FeedbackChannel, TimedMessage> messages = activeMessages.get(player.getUniqueId());
        if (messages == null) {
            return;
        }

        long now = System.currentTimeMillis();
        messages.entrySet().removeIf(entry -> entry.getValue().expiresAtMillis() <= now);
        if (messages.isEmpty()) {
            activeMessages.remove(player.getUniqueId());
        }
    }

    private void render(Player player) {
        EnumMap<FeedbackChannel, TimedMessage> messages = activeMessages.get(player.getUniqueId());
        if (messages == null || messages.isEmpty()) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        appendIfPresent(builder, messages.get(FeedbackChannel.COOLDOWN));
        appendIfPresent(builder, messages.get(FeedbackChannel.XP_GAIN));
        appendIfPresent(builder, messages.get(FeedbackChannel.SYSTEM));
        if (builder.isEmpty()) {
            return;
        }

        Component component = LegacyComponentSerializer.legacySection().deserialize(builder.toString());
        player.sendActionBar(component);
    }

    private void appendIfPresent(StringBuilder builder, TimedMessage message) {
        if (message == null || message.expiresAtMillis() <= System.currentTimeMillis()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(" §7• ");
        }
        builder.append(message.message());
    }

    public enum FeedbackChannel {
        COOLDOWN,
        XP_GAIN,
        SYSTEM
    }

    private record TimedMessage(String message, long expiresAtMillis) {
    }
}
