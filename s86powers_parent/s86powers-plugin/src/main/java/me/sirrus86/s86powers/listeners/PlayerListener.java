package me.sirrus86.s86powers.listeners;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerAdapter;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.PowerUserAdapter;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerLoadEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class PlayerListener implements Listener {

	private final S86Powers plugin;
	private double pLibVer;
	
	public PlayerListener(S86Powers plugin) {
		this.plugin = plugin;
		pLibVer = getPLibVer();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	private void onDeath(PlayerDeathEvent event) {
		if (S86Powers.getProtocolLib() != null) {
			PowerTools.removeDisguise(event.getEntity());
			PowerTools.removeGhost(event.getEntity());
		}
		if (ConfigOption.Users.REMOVE_POWERS_ON_DEATH) {
			PowerUser user = S86Powers.getConfigManager().getUser(event.getEntity().getUniqueId());
			for (Power power : Sets.newHashSet(user.getAssignedPowers())) {
				PowerUserAdapter.getAdapter(user).removePower(power);
			}
		}
	}
	
	private final double getPLibVer() {
		try {
			if (S86Powers.getProtocolLib() != null) {
				return Double.parseDouble(S86Powers.getProtocolLib().getDescription().getVersion().substring(0, 3));
			}
		} catch (Exception e) { }
		return 0.0D;
	}
	
	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		if (event.getPlayer().isOp()
				|| event.getPlayer().hasPermission(S86Permission.ADMIN)) {
			if (MCVersion.CURRENT_VERSION.getRequiredProtocolLib() > pLibVer) {
				event.getPlayer().sendMessage(ChatColor.RED + LocaleString.BAD_PROTOCOLLIB_VERSION.build(MCVersion.CURRENT_VERSION.getRequiredProtocolLib(),
						S86Powers.getProtocolLib() != null ? S86Powers.getProtocolLib().getDescription().getVersion() : "N/A"));
			}
		}
		if (!S86Powers.getConfigManager().hasUser(event.getPlayer().getUniqueId())) {
			PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
			if (user != null) {
				PowerUserAdapter uCont = PowerUserAdapter.getAdapter(user);
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
			PowerUserAdapter uCont = PowerUserAdapter.getAdapter(user);
			for (Power power : uCont.getPowers(true)) {
				if (uCont.hasPowerEnabled(power)) {
					power.enable(user);
				}
			}
		}
	}
	
	@EventHandler
	private void onRespawn(PlayerRespawnEvent event) {
		PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
		PowerUserAdapter uCont = PowerUserAdapter.getAdapter(user);
		for (Power power : uCont.getPowers(true)) {
			if (uCont.hasPowerEnabled(power)) {
				power.enable(user);
			}
		}
	}
	
	@EventHandler
	private void onQuit(PlayerQuitEvent event) {
		PowerUser user = S86Powers.getConfigManager().getUser(event.getPlayer().getUniqueId());
		PowerUserAdapter uCont = PowerUserAdapter.getAdapter(user);
		uCont.save();
		for (Power power : uCont.getPowers(true)) {
			if (uCont.hasPowerEnabled(power)) {
				PowerAdapter pCont = PowerAdapter.getAdapter(power);
				pCont.disable(user);
			}
		}
	}
	
	@EventHandler
	private void onStart(ServerLoadEvent event) {
		if (S86Powers.getProtocolLib() != null
				&& MCVersion.CURRENT_VERSION.getRequiredProtocolLib() > pLibVer) {
			plugin.log(Level.SEVERE, ChatColor.RED + LocaleString.BAD_PROTOCOLLIB_VERSION.build(MCVersion.CURRENT_VERSION.getRequiredProtocolLib(),
					S86Powers.getProtocolLib() != null ? S86Powers.getProtocolLib().getDescription().getVersion() : "N/A"));
		}
	}
	
}
