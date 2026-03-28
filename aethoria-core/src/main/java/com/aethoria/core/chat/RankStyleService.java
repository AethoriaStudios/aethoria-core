package com.aethoria.core.chat;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public final class RankStyleService {
    private final List<RankStyle> styles = List.of(
        RankStyle.aethorian(),
        RankStyle.vipd(),
        RankStyle.vipc(),
        RankStyle.vipb(),
        RankStyle.vipa()
    );

    public RankStyle resolve(Player player) {
        for (RankStyle style : styles) {
            if (player.hasPermission(style.permission())) {
                return style;
            }
        }

        return RankStyle.defaultStyle();
    }

    public Component formattedName(Player player) {
        RankStyle style = resolve(player);
        return style.prefix().append(Component.text(player.getName(), NamedTextColor.WHITE));
    }
}
