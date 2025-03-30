package net.kinoko2k.bowAttack.managers;

import net.kinoko2k.bowAttack.BowAttack;
import net.kinoko2k.bowAttack.database.MySQL;
import net.kinoko2k.bowAttack.holograms.HologramManager;
import net.kinoko2k.bowAttack.utils.DiscordWebhook;
import net.kinoko2k.bowAttack.utils.LocationUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class GameManager {
    private static final BowAttack plugin = JavaPlugin.getPlugin(BowAttack.class);
    private static final MySQL mysql = plugin.getMySQL();
    private static final HashMap<UUID, UUID> activeGames = new HashMap<>();
    private static final HashMap<UUID, BossBar> bossBars = new HashMap<>();
    private static final HashMap<UUID, Boolean> gameStarted = new HashMap<>();
    private static final HashMap<UUID, String> gameModes = new HashMap<>();
    private static final int GAME_TIME = 60; // 試合時間（秒）
    private static final String DISCORD_WEBHOOK_URL = plugin.getDiscordWebhookUrl();
    private static HologramManager hologramManager;

    public static void setHologramManager(HologramManager manager) {
        hologramManager = manager;
    }

    public static void startGame(Player player1, Player player2, String mode) {
        if (activeGames.containsKey(player1.getUniqueId()) || activeGames.containsKey(player2.getUniqueId())) {
            player1.sendMessage(ChatColor.RED + "あなたか相手はすでに試合中です！");
            return;
        }

        if (gameStarted.getOrDefault(player1.getUniqueId(), false) || gameStarted.getOrDefault(player2.getUniqueId(), false)) {
            return;
        }

        gameStarted.put(player1.getUniqueId(), true);
        gameStarted.put(player2.getUniqueId(), true);

        activeGames.put(player1.getUniqueId(), player2.getUniqueId());
        activeGames.put(player2.getUniqueId(), player1.getUniqueId());

        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown == 0) {
                    player1.sendTitle(ChatColor.GREEN + "開始！", "", 5, 20, 5);
                    player2.sendTitle(ChatColor.GREEN + "開始！", "", 5, 20, 5);
                    startMatch(player1, player2, mode);
                    cancel();
                    return;
                }

                player1.sendTitle(ChatColor.RED + "試合開始まで", countdown + "秒", 5, 20, 5);
                player2.sendTitle(ChatColor.RED + "試合開始まで", countdown + "秒", 5, 20, 5);
                countdown--;
            }
        }.runTaskTimer(BowAttack.getInstance(), 0L, 20L);
    }

    private static void startMatch(Player player1, Player player2, String mode) {
        Location start1 = LocationUtils.getStartLocation1();
        Location start2 = LocationUtils.getStartLocation2();

        if (start1 == null || start2 == null) {
            player1.sendMessage(ChatColor.RED + "ゲーム開始地点が設定されていません。");
            player2.sendMessage(ChatColor.RED + "ゲーム開始地点が設定されていません。");
            return;
        }

        player1.teleport(start1);
        player2.teleport(start2);

        player1.setGameMode(GameMode.ADVENTURE);
        player2.setGameMode(GameMode.ADVENTURE);

        giveBattleItems(player1, mode);
        giveBattleItems(player2, mode);

        gameModes.put(player1.getUniqueId(), mode);
        gameModes.put(player2.getUniqueId(), mode);

        mysql.incrementTotalGames(player1.getName(), player1.getUniqueId().toString());
        mysql.incrementTotalGames(player2.getName(), player2.getUniqueId().toString());

        Bukkit.broadcastMessage(ChatColor.GREEN + player1.getName() + " vs " + player2.getName() + " の試合が開始されました！");

        sendDiscordMatchStart(player1.getName(), player2.getName(), mode);

        BossBar bossBar = Bukkit.createBossBar("試合時間", BarColor.RED, BarStyle.SEGMENTED_20);
        bossBar.setProgress(1.0);
        bossBar.addPlayer(player1);
        bossBar.addPlayer(player2);
        bossBars.put(player1.getUniqueId(), bossBar);
        bossBars.put(player2.getUniqueId(), bossBar);

        new BukkitRunnable() {
            int timeLeft = GAME_TIME;

            @Override
            public void run() {
                if (!activeGames.containsKey(player1.getUniqueId()) || !activeGames.containsKey(player2.getUniqueId())) {
                    bossBar.removeAll();
                    cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    bossBar.removeAll();
                    player1.setHealth(0);
                    player2.setHealth(0);
                    Bukkit.broadcastMessage(ChatColor.GOLD + "同点わろたw");
                    endGame(player1);
                    cancel();
                    return;
                }

                bossBar.setProgress((double) timeLeft / GAME_TIME);
                timeLeft--;
            }
        }.runTaskTimer(BowAttack.getInstance(), 0L, 20L);
    }

    private static void giveBattleItems(Player player, String mode) {
        player.getInventory().clear();
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();

        switch (mode.toLowerCase()) {
            case "1hp":
                meta.setDisplayName(ChatColor.RED + "1HP弓");
                bow.setItemMeta(meta);
                bow.addUnsafeEnchantment(Enchantment.POWER, 255);
                break;
            case "knockback":
                meta.setDisplayName(ChatColor.BLUE + "ノックバック弓");
                bow.setItemMeta(meta);
                bow.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.KNOCKBACK, 5);
                break;
            case "normal":
            default:
                meta.setDisplayName(ChatColor.GREEN + "通常弓");
                bow.setItemMeta(meta);
                break;
        }

        player.getInventory().addItem(bow);
        player.getInventory().addItem(new ItemStack(Material.ARROW, 64));
        player.setGameMode(GameMode.ADVENTURE);
    }

    public static void endGame(Player loser) {
        UUID loserUUID = loser.getUniqueId();
        UUID winnerUUID = activeGames.get(loserUUID);
        if (winnerUUID == null) return;

        Player winner = Bukkit.getPlayer(winnerUUID);
        Bukkit.broadcastMessage(ChatColor.GOLD + winner.getName() + " が勝利しました！");

        loser.getInventory().clear();
        winner.getInventory().clear();
        loser.teleport(LocationUtils.getLobbyLocation());
        winner.teleport(LocationUtils.getLobbyLocation());

        if (hologramManager != null) {
            hologramManager.createOrUpdateHologram("wins");
            hologramManager.createOrUpdateHologram("total_games");
        }
        mysql.updatePlayerStats(winner.getName(), winner.getUniqueId().toString());

        String mode = gameModes.getOrDefault(loserUUID, "normal");
        sendDiscordMessage(winner.getName(), loser.getName(), mode);

        if (bossBars.containsKey(loserUUID)) {
            bossBars.get(loserUUID).removeAll();
            bossBars.remove(loserUUID);
        }
        if (bossBars.containsKey(winnerUUID)) {
            bossBars.get(winnerUUID).removeAll();
            bossBars.remove(winnerUUID);
        }

        activeGames.remove(loserUUID);
        activeGames.remove(winnerUUID);

        gameStarted.put(loserUUID, false);
        gameStarted.put(winnerUUID, false);

    }

    public static boolean isInGame(Player player) {
        return activeGames.containsKey(player.getUniqueId());
    }

    private static void sendDiscordMatchStart(String player1, String player2, String mode) {
        DiscordWebhook webhook = new DiscordWebhook(DISCORD_WEBHOOK_URL);
        webhook.setUsername("BowAttack");

        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("BowAttack")
                .setDescription(player1 + " vs " + player2 + " の戦いが " + mode + " モードで開始されました！")
                .setColor(0x00FF00)
        );

        try {
            webhook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendDiscordMessage(String winner, String loser, String mode) {
        DiscordWebhook webhook = new DiscordWebhook(DISCORD_WEBHOOK_URL);

        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("BowAttack")
                .setDescription(winner + " vs " + loser + " の末、" + winner + " さんが " + mode + " モードで勝利しました！")
                .setColor(0xFFD700)
        );

        try {
            webhook.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}