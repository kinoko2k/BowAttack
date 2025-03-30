package net.kinoko2k.bowAttack.listeners;

import net.kinoko2k.bowAttack.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (GameManager.isInGame(player)) {
            GameManager.endGame(player);
            player.sendMessage(ChatColor.RED + "ログアウトしたため、試合は無効となりました。");
        }
    }
}