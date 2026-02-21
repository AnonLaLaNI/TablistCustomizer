package me.anonlalani.tablistcustomizer;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class TablistCustomizer extends JavaPlugin {

    private TablistService tablistService;
    private BukkitTask updateTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        tablistService = new TablistService(this);

        PluginCommand command = getCommand("tablistcustomizer");
        if (command != null) {
            command.setExecutor(new TablistCommand(this));
        }

        startUpdater();
        Bukkit.getOnlinePlayers().forEach(tablistService::updatePlayerTab);

        getLogger().info("TablistCustomizer enabled.");
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        tablistService.refreshLuckPerms();
        startUpdater();
        for (Player player : Bukkit.getOnlinePlayers()) {
            tablistService.updatePlayerTab(player);
        }
    }

    private void startUpdater() {
        if (updateTask != null) {
            updateTask.cancel();
        }

        long interval = Math.max(1L, getConfig().getLong("update-interval-ticks", 40L));
        updateTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                tablistService.updatePlayerTab(player);
            }
        }, 1L, interval);
    }
}
