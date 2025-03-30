package net.kinoko2k.bowAttack.holograms;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.kinoko2k.bowAttack.database.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;

public class HologramManager {
    private final Plugin plugin;
    private final MySQL mysql;
    private final HashMap<String, Hologram> holograms = new HashMap<>();

    private final Location winsLocation = new Location(Bukkit.getWorld("world"), 119, -56, 75);  // 勝利数ランキング
    private final Location gamesLocation = new Location(Bukkit.getWorld("world"), 115, -56, 75); // 総試合数ランキング

    public HologramManager(Plugin plugin, MySQL mysql) {
        this.plugin = plugin;
        this.mysql = mysql;
    }

    public void createOrUpdateHologram(String category) {
        if (!Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            Bukkit.getLogger().warning("DecentHolograms が有効ではありません。");
            return;
        }

        String hologramName = "ranking_" + category;
        Location hologramLocation = category.equals("wins") ? winsLocation : gamesLocation;

        if (holograms.containsKey(category)) {
            holograms.get(category).delete();
        }

        Hologram hologram = DHAPI.createHologram(hologramName, hologramLocation);
        holograms.put(category, hologram);

        String title = category.equals("wins") ? "🏆 勝利数ランキング 🏆" : "🎮 総試合数ランキング 🎮";
        DHAPI.addHologramLine(hologram, title);

        List<String> topPlayers = mysql.getTopPlayers(category);

        if (topPlayers.isEmpty()) {
            DHAPI.addHologramLine(hologram, "データがありません");
        } else {
            for (String playerInfo : topPlayers) {
                DHAPI.addHologramLine(hologram, playerInfo);
            }
        }
    }
}