package net.kinoko2k.bowAttack.listeners;

import net.kinoko2k.bowAttack.managers.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (GameManager.isInGame(player)) {
            event.setDeathMessage(ChatColor.RED + player.getName() + " が倒れました！");
            player.getInventory().clear();
            GameManager.endGame(player);
        }
    }
}