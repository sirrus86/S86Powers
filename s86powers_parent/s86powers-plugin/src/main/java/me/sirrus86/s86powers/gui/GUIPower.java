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
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.tools.PowerTools;

public final class GUIPower extends GUIAbstract {
	
	private static Map<Power, List<GUIOptionList>> powerOptionList = new HashMap<>();
	
	static List<GUIPowerList> POWER_LIST_GUI = new ArrayList<>();
	
	final static ItemStack INFO = createItem(Material.FILLED_MAP, LocaleString.INFO.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.POWER_INFO_HELP.toString(), 30)),
			OPTIONS = createItem(Material.FILLED_MAP, LocaleString.OPTIONS.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.POWER_OPTION_HELP.toString(), 30)),
			ENABLE = createItem(Material.REDSTONE_TORCH, LocaleString.ENABLE.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.POWER_ENABLE_HELP.toString(), 30)),
			DISABLE = createItem(Material.BARRIER, LocaleString.DISABLE.toString(), PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), LocaleString.POWER_DISABLE_HELP.toString(), 30));

	public GUIPower() {
		super(2, LocaleString.POWER.toString());
		refresh();
	}
	
	@Override
	void refresh() {
		POWER_LIST_GUI = GUIAbstractList.createLists(GUIPowerList.class, S86Powers.getConfigManager().getPowers());
		for (int i = 0; i < POWER_LIST_GUI.size(); i ++) {
			POWER_LIST_GUI.get(i).setSourceList(POWER_LIST_GUI);
		}
	}

	@Override
	void setItems() {
		setItem(0, INFO, player -> {
			Power power = selectedPower.get(player.getUniqueId());
			player.closeInventory();
			player.performCommand("powers power " + power.getTag() + " info");
		});
		setItem(2, OPTIONS, player -> {
			Power power = selectedPower.get(player.getUniqueId());
			if (player.hasPermission(S86Permission.POWER_OPTION)) {
				powerOptionList.put(power, GUIAbstractList.createLists(GUIOptionList.class, power.getOptions().keySet()));
				for (int i = 0; i < powerOptionList.get(power).size(); i ++) {
					powerOptionList.get(power).get(i).setSourceList(powerOptionList.get(power));
				}
				openNext(player, powerOptionList.get(power).get(0));
			}
			else {
				player.closeInventory();
				player.performCommand("powers power " + power.getTag() + " option");
			}
		});
		setItem(7, ENABLE, player -> {
			Power power = selectedPower.get(player.getUniqueId());
			player.closeInventory();
			player.performCommand("powers power " + power.getTag() + " enable");
		});
		setItem(8, DISABLE, player -> {
			Power power = selectedPower.get(player.getUniqueId());
			player.closeInventory();
			player.performCommand("powers power " + power.getTag() + " disable");
		});
		setItem(12, BACK, player -> {
			openLast(player);
		});
	}

}
