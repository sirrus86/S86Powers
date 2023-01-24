package me.sirrus86.s86powers.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;

public class GUIPowerList extends GUIAbstractList<Power> {

	static Map<Power, ItemStack> powerItems = new HashMap<>();
	
	public GUIPowerList(int page, Collection<Power> list) {
		super(page, list);
	}
	
	private void setItem(int slot, Power power, GUIAction action) {
		if (!powerItems.containsKey(power)) {
			String powerName = ChatColor.RESET + "" + power.getType().getColor() + power.getName();
			String powerDesc = PowerTools.getFilteredText(power, power.getDescription());
			List<String> lore = PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), powerDesc, 30);
			powerItems.put(power, createItem(power.getIcon(), powerName, lore));
		}
		setItem(slot, powerItems.get(power), action);
	}

	@Override
	void setItems() {
		if (page > 0) {
			int index = page * 45 - 45;
			for (int i = index; i < Math.min(list.size(), index + 45); i ++) {
				Power power = list.get(i);
				setItem(i - index, power, player -> {
					if (selectedGroup.containsKey(player.getUniqueId())) {
						PowerGroup group = selectedGroup.get(player.getUniqueId());
						if (group.hasPower(power)) {
							player.closeInventory();
							player.performCommand("powers group " + group.getName() + " remove " + power.getTag());
						}
						else {
							player.closeInventory();
							player.performCommand("powers group " + group.getName() + " add " + power.getTag());
						}
					}
					else if (selectedUser.containsKey(player.getUniqueId())) {
						PowerUser user = selectedUser.get(player.getUniqueId());
						if (user.hasPower(power)) {
							player.closeInventory();
							player.performCommand("powers player " + (user.getName() != null ? user.getName() : "!NULL") + " remove " + power.getTag());
						}
						else {
							player.closeInventory();
							player.performCommand("powers player " + (user.getName() != null ? user.getName() : "!NULL") + " add " + power.getTag());
						}
					}
					else {
						selectedPower.put(player.getUniqueId(), power);
						openNext(player, GUIBase.POWER_GUI);
					}
				});
			}
			setItem(48, BACK, this::openLast);
			if (page > 1) {
				setItem(49, PAGE1, LocaleString.PAGE + " " + (page - 1), null, player -> openGUI(player, sourceList.get(page - 2)));
			}
			if (page < sourceList.size()) {
				setItem(50, PAGE2, LocaleString.PAGE + " " + (page + 1), null, player -> openGUI(player, sourceList.get(page)));
			}
		}
	}

}
