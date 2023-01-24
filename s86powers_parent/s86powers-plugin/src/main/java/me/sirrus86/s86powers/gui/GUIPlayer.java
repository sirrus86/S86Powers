package me.sirrus86.s86powers.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;

public class GUIPlayer extends GUIAbstract {
	
	private static final Map<PowerUser, List<GUIPowerList>> userAddPowerList = new HashMap<>();
	private static final Map<PowerUser, List<GUIPowerList>> userRemovePowerList = new HashMap<>();
	
	static List<GUIPlayerList> PLAYER_LIST_GUI = new ArrayList<>();
	
	final static ItemStack ADD_POWER = createItem(Material.BLAZE_POWDER, LocaleString.ADD_POWER.toString(), PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), LocaleString.PLAYER_ADD_HELP.toString(), 30)),
			REMOVE_POWER = createItem(Material.BLAZE_POWDER, LocaleString.REMOVE_POWER.toString(), PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), LocaleString.PLAYER_REMOVE_HELP.toString(), 30)),
			INFO = createItem(Material.FILLED_MAP, LocaleString.INFO.toString(), PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), LocaleString.PLAYER_INFO_HELP.toString(), 30));
	
	public GUIPlayer() {
		super(2, LocaleString.PLAYER.toString());
		refresh();
	}
	
	@Override
	void refresh() {
		PLAYER_LIST_GUI = GUIAbstractList.createLists(GUIPlayerList.class, S86Powers.getConfigManager().getUserList());
		for (int i = 0; i < PLAYER_LIST_GUI.size(); i ++) {
			PLAYER_LIST_GUI.get(i).setSourceList(PLAYER_LIST_GUI);
		}
	}

	@Override
	void setItems() {
		setItem(0, ADD_POWER, player -> {
			PowerUser user = selectedUser.get(player.getUniqueId());
			if (player.hasPermission(S86Permission.PLAYER_ADD)) {
				userAddPowerList.put(user, GUIAbstractList.createLists(GUIPowerList.class, S86Powers.getConfigManager().getPowers(),
						S86Powers.getConfigManager().getPowersByType(PowerType.UTILITY), user.getPowers()));
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
		setItem(1, REMOVE_POWER, player -> {
			PowerUser user = selectedUser.get(player.getUniqueId());
			if (player.hasPermission(S86Permission.PLAYER_REMOVE)) {
				userRemovePowerList.put(user, GUIAbstractList.createLists(GUIPowerList.class, user.getPowers()));
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
		setItem(3, INFO, player -> {
			PowerUser user = selectedUser.get(player.getUniqueId());
			player.closeInventory();
			player.performCommand("powers player " + (user.getName() != null ? user.getName() : "!NULL") + " info");
		});
		setItem(12, BACK, this::openLast);
	}

}
