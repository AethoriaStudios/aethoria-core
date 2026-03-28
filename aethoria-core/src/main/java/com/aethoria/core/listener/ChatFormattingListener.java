package com.aethoria.core.listener;

import com.aethoria.core.chat.RankStyle;
import com.aethoria.core.chat.RankStyleService;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class ChatFormattingListener implements Listener {
    private final RankStyleService rankStyleService;

    public ChatFormattingListener(RankStyleService rankStyleService) {
        this.rankStyleService = rankStyleService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        event.renderer((source, sourceDisplayName, message, viewer) -> render(source, message));
    }

    private Component render(Player source, Component message) {
        return rankStyleService.formattedName(source)
            .append(Component.text(":", NamedTextColor.WHITE))
            .append(RankStyle.CHAT_SEPARATOR)
            .append(Component.text().color(NamedTextColor.WHITE).append(message).build());
    }
}
