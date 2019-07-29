package me.sirrus86.s86powers.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.UserContainer;

public class GUIPlayer extends GUIAbstract {
	
	private static Map<PowerUser, List<GUIPowerList>> userAddPowerList = new HashMap<>(),
			userRemovePowerList = new HashMap<>();
	
	static List<GUIPlayerList> PLAYER_LIST_GUI = new ArrayList<>();
	
	public GUIPlayer() {
		super(2, LocaleString.PLAYER.toString());
		refresh();
	}
	
	@Override
	void refresh() {
		PLAYER_LIST_GUI = GUIAbstractList.createLists(GUIPlayerList.class, plugin.getConfigManager().getUserList());
		for (int i = 0; i < PLAYER_LIST_GUI.size(); i ++) {
			PLAYER_LIST_GUI.get(i).setSourceList(PLAYER_LIST_GUI);
		}
	}

	@Override
	void setItems() {
		setItem(0, POWER, LocaleString.ADD_POWER.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.PLAYER_ADD_HELP.toString(), 30), player -> {
			PowerUser user = selectedUser.get(player.getUniqueId());
			UserContainer uCont = UserContainer.getContainer(user);
			if (player.hasPermission(S86Permission.PLAYER_ADD)) {
				userAddPowerList.put(user, GUIAbstractList.createLists(GUIPowerList.class, plugin.getConfigManager().getPowers(),
						plugin.getConfigManager().getPowersByType(PowerType.UTILITY), uCont.getPowers()));
				for (int i = 0; i < userAddPowerList.get(user).size(); i ++) {
					userAddPowerList.get(user).get(i).setSourceList(userAddPowerList.get(user));
				}
				openNext(player, userAddPowerList.get(user).get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers player " + (user.getName() != null ? user.getName() : "!NULL") + " add");
			}
		});
		setItem(1, POWER, LocaleString.REMOVE_POWER.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.PLAYER_REMOVE_HELP.toString(), 30), player -> {
			PowerUser user = selectedUser.get(player.getUniqueId());
			UserContainer uCont = UserContainer.getContainer(user);
			if (player.hasPermission(S86Permission.PLAYER_REMOVE)) {
				userRemovePowerList.put(user, GUIAbstractList.createLists(GUIPowerList.class, uCont.getPowers()));
				for (int i = 0; i < userRemovePowerList.get(user).size(); i ++) {
					userRemovePowerList.get(user).get(i).setSourceList(userRemovePowerList.get(user));
				}
				openNext(player, userRemovePowerList.get(user).get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers player " + (user.getName() != null ? user.getName() : "!NULL") + " remove");
			}
		});
		setItem(3, LIST, LocaleString.INFO.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.PLAYER_INFO_HELP.toString(), 30), player -> {
			PowerUser user = selectedUser.get(player.getUniqueId());
			player.closeInventory();
			player.performCommand("powers player " + (user.getName() != null ? user.getName() : "!NULL") + " info");
		});
		setItem(12, BACK, LocaleString.BACK.toString(), (String) null, player -> {
			openLast(player);
		});
	}

}
