package org.example.netheritetracker;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetheriteTracker extends JavaPlugin implements Listener {

    private Map<UUID, Integer> netheriteData = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadData();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("nethcheck").setExecutor(this);
    }

    @Override
    public void onDisable() {
        saveData();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.ANCIENT_DEBRIS) return;
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        int current = netheriteData.getOrDefault(uuid, 0);
        netheriteData.put(uuid, current + 1);
        saveData();

        String msg = "§6⚠ " + player.getName() + " §fmined ancient debris! §7(Total: " + (current + 1) + ")";
        Bukkit.broadcastMessage(msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("nethcheck")) return false;

        if (netheriteData.isEmpty()) {
            sender.sendMessage("§7No netherite data recorded yet.");
            return true;
        }

        sender.sendMessage("§6--- Netherite Leaderboard ---");
        netheriteData.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .forEach(entry -> {
                String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                sender.sendMessage("§f" + name + ": §6" + entry.getValue() + " ancient debris");
            });

        return true;
    }

    private void loadData() {
        FileConfiguration config = getConfig();
        if (config.getConfigurationSection("data") == null) return;
        for (String key : config.getConfigurationSection("data").getKeys(false)) {
            netheriteData.put(UUID.fromString(key), config.getInt("data." + key));
        }
    }

    private void saveData() {
        FileConfiguration config = getConfig();
        for (Map.Entry<UUID, Integer> entry : netheriteData.entrySet()) {
            config.set("data." + entry.getKey().toString(), entry.getValue());
        }
        saveConfig();
    }
}
