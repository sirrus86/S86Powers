package me.sirrus86.s86powers.listeners;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.UserContainer;

import java.util.Collections;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Lists;

public class PlayerListener implements Listener {

	private final S86Powers plugin;
	
	public PlayerListener(S86Powers plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	private void onDeath(PlayerDeathEvent event) {
		PowerTools.removeGhost(event.getEntity());
	}
	
	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		if (!plugin.getConfigManager().hasUser(event.getPlayer().getUniqueId())) {
			PowerUser user = plugin.getConfigManager().getUser(event.getPlayer().getUniqueId());
			if (ConfigOption.Users.AUTO_ASSIGN) {
				UserContainer uCont = UserContainer.getContainer(user);
				for (PowerType type : PowerType.values()) {
					if (type != PowerType.UTILITY
							&& uCont.getAssignedPowersByType(type).isEmpty()) {
						List<Power> tempList = Lists.newArrayList(plugin.getConfigManager().getPowersByType(type));
						Collections.shuffle(tempList); 
						uCont.addPower(tempList.get(0));
					}
				}
			}
		}
	}
	
	@EventHandler
	private void onQuit(PlayerQuitEvent event) {
		PowerUser user = plugin.getConfigManager().getUser(event.getPlayer().getUniqueId());
		UserContainer.getContainer(user).save();
		PowerTools.removeGhost(event.getPlayer());
		PowerTools.removeDisguise(event.getPlayer());
	}
	
}
