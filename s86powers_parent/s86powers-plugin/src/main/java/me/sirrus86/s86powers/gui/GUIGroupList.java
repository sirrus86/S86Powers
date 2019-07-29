package me.sirrus86.s86powers.gui;

import java.util.Collection;

import org.bukkit.ChatColor;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.users.PowerGroup;

public class GUIGroupList extends GUIAbstractList<PowerGroup> {
	
	public GUIGroupList(int page, Collection<PowerGroup> list) {
		super(page, list);
	}
	
	@Override
	protected void setItems() {
		if (page > 0) {
			int index = page * 45 - 45;
			for (int i = index; i < Math.min(list.size(), index + 45); i ++) {
				PowerGroup group = list.get(i);
				String groupName = group.getName();
				setItem(i - index, GROUP, ChatColor.RESET + groupName, (String) null, player -> {
					selectedGroup.put(player.getUniqueId(), group);
					openNext(player, GUIBase.GROUP_GUI);
				});
			}
			setItem(48, BACK, LocaleString.BACK.toString(), (String) null, player -> {
				openLast(player);
			});
			if (page > 1) {
				setItem(49, PAGE, LocaleString.PAGE.toString() + " " + Integer.toString(page - 1), (String) null, player -> {
					openGUI(player, sourceList.get(page - 2));
				});
			}
			if (page < sourceList.size()) {
				setItem(50, PAGE, LocaleString.PAGE.toString() + " " + Integer.toString(page + 1), (String) null, player -> {
					openGUI(player, sourceList.get(page));
				});
			}
		}
	}
	
}
