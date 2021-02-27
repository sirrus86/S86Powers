package me.sirrus86.s86powers.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.users.PowerGroup;

public class GUIGroupList extends GUIAbstractList<PowerGroup> {

	static Map<PowerGroup, ItemStack> groupItems = new HashMap<>();
	
	public GUIGroupList(int page, Collection<PowerGroup> list) {
		super(page, list);
	}
	
	@Override
	protected void setItems() {
		if (page > 0) {
			int index = page * 45 - 45;
			for (int i = index; i < Math.min(list.size(), index + 45); i ++) {
				PowerGroup group = list.get(i);
				if (!groupItems.containsKey(group)) {
					groupItems.put(group, createItem(Material.TOTEM_OF_UNDYING, ChatColor.RESET + group.getName(), (List<String>) null));
				}
				setItem(i - index, groupItems.get(group), player -> {
					selectedGroup.put(player.getUniqueId(), group);
					openNext(player, GUIBase.GROUP_GUI);
				});
			}
			setItem(48, BACK, player -> {
				openLast(player);
			});
			if (page > 1) {
				setItem(49, PAGE1, LocaleString.PAGE.toString() + " " + Integer.toString(page - 1), (List<String>) null, player -> {
					openGUI(player, sourceList.get(page - 2));
				});
			}
			if (page < sourceList.size()) {
				setItem(50, PAGE2, LocaleString.PAGE.toString() + " " + Integer.toString(page + 1), (List<String>) null, player -> {
					openGUI(player, sourceList.get(page));
				});
			}
		}
	}
	
}
