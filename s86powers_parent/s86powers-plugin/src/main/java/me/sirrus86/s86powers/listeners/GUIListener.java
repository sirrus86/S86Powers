package me.sirrus86.s86powers.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.gui.GUIAbstract;
import me.sirrus86.s86powers.gui.GUIAction;

public class GUIListener implements Listener {

	public GUIListener(S86Powers plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	private void onClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			GUIAbstract gui = GUIAbstract.getGUI(player.getUniqueId());
			if (gui != null) {
				event.setCancelled(true);
				GUIAction action = gui.getAction(event.getSlot());
				if (action != null) {
					action.click(player);
				}
			}
		}
	}
	
	@EventHandler
	private void onClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			GUIAbstract gui = GUIAbstract.getGUI(player.getUniqueId());
			if (gui != null) {
				gui.removeViewer(player.getUniqueId());
			}
		}
	}
	
	@EventHandler
	private void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		GUIAbstract gui = GUIAbstract.getGUI(player.getUniqueId());
		if (gui != null) {
			gui.removeViewer(player.getUniqueId());
		}
	}
	
}
