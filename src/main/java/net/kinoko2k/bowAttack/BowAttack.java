package net.kinoko2k.bowAttack;

import net.kinoko2k.bowAttack.commands.RankingCommand;
import net.kinoko2k.bowAttack.commands.StartCommand;
import net.kinoko2k.bowAttack.commands.UpdateRankingCommand;
import net.kinoko2k.bowAttack.database.MySQL;
import net.kinoko2k.bowAttack.holograms.HologramManager;
import net.kinoko2k.bowAttack.listeners.DamageListener;
import net.kinoko2k.bowAttack.listeners.DeathListener;
import net.kinoko2k.bowAttack.listeners.QuitListener;
import net.kinoko2k.bowAttack.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class BowAttack extends JavaPlugin {

    private static BowAttack instance;
    private MySQL mysql;
    private String discordWebhookUrl;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("DecentHolograms") == null) {
            getLogger().severe("DecentHolograms が見つかりません！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        mysql = new MySQL(this);
        discordWebhookUrl = config.getString("Discord.webhook_url", "");
        HologramManager hologramManager = new HologramManager(this, mysql);

        Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(getCommand("start")))))).setExecutor(new StartCommand());
        getCommand("ranking").setExecutor(new RankingCommand(mysql));
        getCommand("updateranking").setExecutor(new UpdateRankingCommand(hologramManager));

        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);


        hologramManager.createOrUpdateHologram("wins");
        hologramManager.createOrUpdateHologram("total_games");

        GameManager.setHologramManager(hologramManager);

        Bukkit.getScheduler().runTaskLater(this, () -> {
            hologramManager.createOrUpdateHologram("wins");
            hologramManager.createOrUpdateHologram("total_games");
        }, 20L);
    }

    @Override
    public void onDisable() {
        if (mysql != null) {
            mysql.closeConnection();
        }
        getLogger().info("BowAttackはむこうやねん");

    }

    public MySQL getMySQL() { return mysql; }

    public String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }

    public static BowAttack getInstance() {
        return instance;
    }
}