package me.anonlalani.tablistcustomizer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public class TablistService {

    private final TablistCustomizer plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacySection();
    private LuckPerms luckPerms;

    public TablistService(TablistCustomizer plugin) {
        this.plugin = plugin;
        refreshLuckPerms();
    }

    public void refreshLuckPerms() {
        luckPerms = null;
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
                luckPerms = LuckPermsProvider.get();
            }
        } catch (IllegalStateException ignored) {
            plugin.getLogger().warning("LuckPerms API not ready yet - running with fallback mode.");
        }
    }

    public void updatePlayerTab(Player player) {
        PlayerContext ctx = buildContext(player);

        String format = resolveTabFormat(ctx.groupName());
        String headerText = String.join("\n", plugin.getConfig().getStringList("header"));
        String footerText = String.join("\n", plugin.getConfig().getStringList("footer"));

        Component listName = parse(formatPlaceholders(format, ctx));
        Component header = parse(formatPlaceholders(headerText, ctx));
        Component footer = parse(formatPlaceholders(footerText, ctx));

        player.playerListName(listName);
        player.sendPlayerListHeaderAndFooter(header, footer);

        applySortOrderIfSupported(player, resolveSortOrder(ctx));
    }

    private Component parse(String text) {
        try {
            if (text.contains("§")) {
                return legacy.deserialize(text);
            }
            return miniMessage.deserialize(text);
        } catch (Exception ex) {
            return Component.text(text);
        }
    }

    private String resolveTabFormat(String groupName) {
        ConfigurationSection groupSection = plugin.getConfig().getConfigurationSection("groups." + groupName);
        if (groupSection != null) {
            String groupFormat = groupSection.getString("tab-format");
            if (groupFormat != null && !groupFormat.isBlank()) {
                return groupFormat;
            }
        }

        return plugin.getConfig().getString("default.tab-format", "<gray>%player%");
    }

    private void applySortOrderIfSupported(Player player, int order) {
        try {
            player.getClass().getMethod("setPlayerListOrder", int.class).invoke(player, order);
        } catch (Exception ignored) {
            // API not available on this Paper build.
        }
    }

    private int resolveSortOrder(PlayerContext ctx) {
        int explicitPriority = plugin.getConfig().getInt("default.priority", 0);

        ConfigurationSection groupSection = plugin.getConfig().getConfigurationSection("groups." + ctx.groupName());
        if (groupSection != null) {
            explicitPriority = groupSection.getInt("priority", explicitPriority);
        }

        boolean byWeight = plugin.getConfig().getBoolean("sort-by-group-weight", true);
        int base = byWeight ? ctx.groupWeight() : explicitPriority;

        int normalized = Math.max(-100000, Math.min(100000, base));
        return 100000 - normalized;
    }

    private PlayerContext buildContext(Player player) {
        String group = "default";
        String lpPrefix = "";
        String lpSuffix = "";
        int weight = 0;

        if (luckPerms != null) {
            UUID uuid = player.getUniqueId();
            User user = luckPerms.getUserManager().getUser(uuid);
            if (user != null) {
                group = user.getPrimaryGroup() == null ? "default" : user.getPrimaryGroup();

                var meta = user.getCachedData().getMetaData();
                lpPrefix = meta.getPrefix() == null ? "" : meta.getPrefix();
                lpSuffix = meta.getSuffix() == null ? "" : meta.getSuffix();

                var groupObj = luckPerms.getGroupManager().getGroup(group);
                if (groupObj != null) {
                    weight = groupObj.getWeight().orElse(0);
                }
            }
        }

        return new PlayerContext(player, group, lpPrefix, lpSuffix, weight);
    }

    private String formatPlaceholders(String input, PlayerContext ctx) {
        String output = input
                .replace("%player%", ctx.player().getName())
                .replace("%displayname%", ctx.player().getDisplayName())
                .replace("%world%", ctx.player().getWorld().getName())
                .replace("%group%", ctx.groupName())
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%lp_prefix%", ctx.lpPrefix())
                .replace("%lp_suffix%", ctx.lpSuffix())
                .replace("%prefix%", ctx.lpPrefix())
                .replace("%suffix%", ctx.lpSuffix());

        Plugin placeholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderApi != null && placeholderApi.isEnabled()) {
            try {
                output = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(ctx.player(), output);
            } catch (Throwable ignored) {
                // PlaceholderAPI present but not healthy -> fallback silently.
            }
        }

        return output;
    }

    private record PlayerContext(Player player, String groupName, String lpPrefix, String lpSuffix, int groupWeight) {
    }
}
