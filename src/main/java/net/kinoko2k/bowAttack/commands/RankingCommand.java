package net.kinoko2k.bowAttack.commands;

import net.kinoko2k.bowAttack.database.MySQL;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RankingCommand implements CommandExecutor {
    private final MySQL mysql;

    public RankingCommand(MySQL mysql) {
        this.mysql = mysql;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます！");
            return false;
        }

        if (args.length != 1 || (!args[0].equalsIgnoreCase("wins") && !args[0].equalsIgnoreCase("totalgame"))) {
            sender.sendMessage(ChatColor.RED + "使用方法: /ranking <wins/totalgame>");
            return false;
        }

        String category = args[0].equalsIgnoreCase("wins") ? "wins" : "total_games";
        List<String> topPlayers = mysql.getTopPlayers(category);

        if (topPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "ランキングデータがありません！");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "🏆 " + (category.equals("wins") ? "勝利数" : "総ゲーム数") + " ランキング 🏆");
        for (String playerInfo : topPlayers) {
            sender.sendMessage(ChatColor.YELLOW + playerInfo);
        }

        return true;
    }
}