package net.kinoko2k.bowAttack.commands;

import net.kinoko2k.bowAttack.managers.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StartCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーのみ実行できます！");
            return false;
        }

        Player player1 = (Player) sender;

        if (args.length != 2) {
            player1.sendMessage("使用方法: /start <mcid> <normal/1hp/knockback>");
            return false;
        }

        String mcid = args[0];
        String mode = args[1].toLowerCase();

        if (player1.getName().equalsIgnoreCase(mcid)) {
            player1.sendMessage("自分自身とは試合を開始できません！");
            return false;
        }

        if (!mode.equals("normal") && !mode.equals("1hp") && !mode.equals("knockback")) {
            player1.sendMessage("無効なモードです。使用できるモードは: normal, 1hp, knockback");
            return false;
        }

        Player player2 = player1.getServer().getPlayer(mcid);

        if (player2 == null || !player2.isOnline()) {
            player1.sendMessage("指定されたプレイヤーがオンラインではありません！");
            return false;
        }

        GameManager.startGame(player1, player2, mode);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                for (Player p : player.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }

        if (args.length == 2) {
            String[] modes = {"normal", "1hp", "knockback"};
            for (String mode : modes) {
                if (mode.startsWith(args[1].toLowerCase())) {
                    completions.add(mode);
                }
            }
        }

        return completions;
    }
}