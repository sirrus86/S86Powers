package me.sirrus86.s86powers.gui;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;

import org.bukkit.entity.Player;

public final class GUIBase extends GUIAbstract {
	
	static GUIConfig CONFIG_GUI = new GUIConfig();
	static GUIGroup GROUP_GUI = new GUIGroup();
	static GUIPlayer PLAYER_GUI = new GUIPlayer();
	static GUIPower POWER_GUI = new GUIPower();

	public GUIBase() {
		super(1, "Main Menu");
		setItems();
	}
	
	@Override
	public void open(Player player) {
		clearHistory(player);
		super.open(player);
	}
	
	@Override
	void setItems() {
		setItem(0, PLAYER, LocaleString.PLAYERS.toString(), (String) null, player -> {
			PLAYER_GUI.refresh();
			if (player.hasPermission(S86Permission.PLAYER)
					&& !GUIPlayer.PLAYER_LIST_GUI.isEmpty()) {
				openNext(player, GUIPlayer.PLAYER_LIST_GUI.get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers player list");
			}
		});
		setItem(1, GROUP, LocaleString.GROUPS.toString(), (String) null, player -> {
			GROUP_GUI.refresh();
			if (player.hasPermission(S86Permission.GROUP)
					&& !GUIGroup.GROUP_LIST_GUI.isEmpty()) {
				openNext(player, GUIGroup.GROUP_LIST_GUI.get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers group list");
			}
		});
		setItem(2, POWER, LocaleString.POWERS.toString(), (String) null, player -> {
			POWER_GUI.refresh();
			if (player.hasPermission(S86Permission.POWER)
					&& !GUIPower.POWER_LIST_GUI.isEmpty()) {
				openNext(player, GUIPower.POWER_LIST_GUI.get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers power list");
			}
		});
		setItem(8, CONFIG, LocaleString.CONFIG.toString(), (String) null, player -> {
			if (player.hasPermission(S86Permission.CONFIG)) {
				openNext(player, CONFIG_GUI);
			}
			else {
				player.closeInventory();
				player.performCommand("powers config list");
			}
		});
	}
	
}
