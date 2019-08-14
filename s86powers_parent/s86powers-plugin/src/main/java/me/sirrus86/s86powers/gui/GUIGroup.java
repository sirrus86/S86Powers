package me.sirrus86.s86powers.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.permissions.S86Permission;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerGroup;

public class GUIGroup extends GUIAbstract {

	private static Map<PowerGroup, List<GUIPlayerList>> groupAddPlayerList = new HashMap<>(),
			groupRemovePlayerList = new HashMap<>();
	private static Map<PowerGroup, List<GUIPowerList>> groupAddPowerList = new HashMap<>(),
			groupRemovePowerList = new HashMap<>();
	
	static List<GUIGroupList> GROUP_LIST_GUI = new ArrayList<>();
	
	public GUIGroup() {
		super(2, LocaleString.GROUPS.toString());
		refresh();
	}
	
	@Override
	void refresh() {
		GROUP_LIST_GUI = GUIAbstractList.createLists(GUIGroupList.class, S86Powers.getConfigManager().getGroups());
		for (int i = 0; i < GROUP_LIST_GUI.size(); i ++) {
			GROUP_LIST_GUI.get(i).setSourceList(GROUP_LIST_GUI);
		}
	}

	@Override
	void setItems() {
		setItem(0, PLAYER, LocaleString.ADD_PLAYER.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.GROUP_ASSIGN_HELP.toString(), 30), player -> {
			if (player.hasPermission(S86Permission.GROUP_ASSIGN)) {
				PowerGroup group = selectedGroup.get(player.getUniqueId());
				groupAddPlayerList.put(group, GUIAbstractList.createLists(GUIPlayerList.class, S86Powers.getConfigManager().getUserList(), group.getMembers()));
				for (int i = 0; i < groupAddPlayerList.get(group).size(); i ++) {
					groupAddPlayerList.get(group).get(i).setSourceList(groupAddPlayerList.get(group));
				}
				openNext(player, groupAddPlayerList.get(group).get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers group assign");
			}
		});
		setItem(1, PLAYER, LocaleString.REMOVE_PLAYER.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.GROUP_KICK_HELP.toString(), 30), player -> {
			if (player.hasPermission(S86Permission.GROUP_KICK)) {
				PowerGroup group = selectedGroup.get(player.getUniqueId());
				groupRemovePlayerList.put(group, GUIAbstractList.createLists(GUIPlayerList.class, group.getMembers()));
				for (int i = 0; i < groupRemovePlayerList.get(group).size(); i ++) {
					groupRemovePlayerList.get(group).get(i).setSourceList(groupRemovePlayerList.get(group));
				}
				openNext(player, groupRemovePlayerList.get(group).get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers group kick");
			}
		});
		setItem(3, POWER, LocaleString.ADD_POWER.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.GROUP_ADD_HELP.toString(), 30), player -> {
			if (player.hasPermission(S86Permission.GROUP_ADD)) {
				PowerGroup group = selectedGroup.get(player.getUniqueId());
				groupAddPowerList.put(group, GUIAbstractList.createLists(GUIPowerList.class, S86Powers.getConfigManager().getPowers(),
						S86Powers.getConfigManager().getPowersByType(PowerType.UTILITY), group.getPowers()));
				for (int i = 0; i < groupAddPowerList.get(group).size(); i ++) {
					groupAddPowerList.get(group).get(i).setSourceList(groupAddPowerList.get(group));
				}
				openNext(player, groupAddPowerList.get(group).get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers group add");
			}
		});
		setItem(4, POWER, LocaleString.REMOVE_POWER.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.GROUP_REMOVE_HELP.toString(), 30), player -> {
			if (player.hasPermission(S86Permission.GROUP_REMOVE)) {
				PowerGroup group = selectedGroup.get(player.getUniqueId());
				groupRemovePowerList.put(group, GUIAbstractList.createLists(GUIPowerList.class, group.getPowers()));
				for (int i = 0; i < groupRemovePowerList.get(group).size(); i ++) {
					groupRemovePowerList.get(group).get(i).setSourceList(groupRemovePowerList.get(group));
				}
				openNext(player, groupRemovePowerList.get(group).get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers group remove");
			}
		});
		setItem(6, LIST, LocaleString.INFO.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.GROUP_INFO_HELP.toString(), 30), player -> {
			PowerGroup group = selectedGroup.get(player.getUniqueId());
			player.closeInventory();
			player.performCommand("powers group " + group.getName() + " info");
		});
		setItem(8, DELETE, LocaleString.DELETE.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.GROUP_DELETE_HELP.toString(), 30), player -> {
			PowerGroup group = selectedGroup.get(player.getUniqueId());
			player.closeInventory();
			player.performCommand("powers group " + group.getName() + " delete");
		});
		setItem(12, BACK, LocaleString.BACK.toString(), (String) null, player -> {
			openLast(player);
		});
	}

}
