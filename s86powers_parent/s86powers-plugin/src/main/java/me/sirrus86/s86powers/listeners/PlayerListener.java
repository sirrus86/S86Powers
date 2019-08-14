package me.sirrus86.s86powers.listeners;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerContainer;
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

	
	public PlayerListener(S86Powers plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	private void onDeath(PlayerDeathEvent event) {
		PowerTools.removeDisguise(event.getEntity());
		PowerTools.removeGhost(event.getEntity());
	}
	
	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		if (!S86Powers.getConfigManager().hasUser(event.getPlayer().getUniqueId())) {
			PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
			if (user != null) {
				UserContainer uCont = UserContainer.getContainer(user);
				if (ConfigOption.Users.AUTO_ASSIGN) {
					for (PowerType type : PowerType.values()) {
						if (type != PowerType.UTILITY
								&& uCont.getAssignedPowersByType(type).isEmpty()) {
							List<Power> tempList = Lists.newArrayList(S86Powers.getConfigManager().getPowersByType(type));
							Collections.shuffle(tempList); 
							uCont.addPower(tempList.get(0));
						}
					}
				}
			}
		}
		PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
		if (user != null) {
			UserContainer uCont = UserContainer.getContainer(user);
			for (Power power : uCont.getPowers(true)) {
				if (uCont.hasPowerEnabled(power)) {
					PowerContainer pCont = PowerContainer.getContainer(power);
					pCont.enable(user);
				}
			}
		}
	}
	
	@EventHandler
	private void onQuit(PlayerQuitEvent event) {
		PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
		UserContainer uCont = UserContainer.getContainer(user);
		uCont.save();
		for (Power power : uCont.getPowers(true)) {
			if (uCont.hasPowerEnabled(power)) {
				PowerContainer pCont = PowerContainer.getContainer(power);
				pCont.disable(user);
			}
		}
	}
	
}
