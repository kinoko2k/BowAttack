package net.kinoko2k.bowAttack.commands;

import net.kinoko2k.bowAttack.holograms.HologramManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpdateRankingCommand implements CommandExecutor {
    private final HologramManager hologramManager;

    public UpdateRankingCommand(HologramManager hologramManager) {
        this.hologramManager = hologramManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます！");
            return false;
        }

        if (args.length != 1 || (!args[0].equalsIgnoreCase("wins") && !args[0].equalsIgnoreCase("totalgame"))) {
            sender.sendMessage(ChatColor.RED + "使用方法: /updateranking <wins/totalgame>");
            return false;
        }

        String category = args[0].equalsIgnoreCase("wins") ? "wins" : "total_games";
        hologramManager.createOrUpdateHologram(category);
        sender.sendMessage(ChatColor.GREEN + "ランキングを更新しました！");

        return true;
    }
}