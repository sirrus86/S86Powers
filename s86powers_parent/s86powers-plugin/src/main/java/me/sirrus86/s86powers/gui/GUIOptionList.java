package me.sirrus86.s86powers.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.tools.PowerTools;

public class GUIOptionList extends GUIAbstractList<PowerOption> {
	
	static Map<PowerOption, ItemStack> optionItems = new HashMap<>();

	public GUIOptionList(int page, Collection<PowerOption> list) {
		super(page, list);
	}

	@Override
	void setItems() {
		if (page > 0) {
			int index = page * 45 - 45;
			for (int i = index; i < Math.min(list.size(), index + 45); i ++) {
				PowerOption option = list.get(i);
				if (!optionItems.containsKey(option)) {
					Power power = option.getPower();
					String optionName = option.getPath();
					String optionDesc = option.getDescription();
					String optionValue = ChatColor.RESET + LocaleString.VALUE.toString() + ": " + ChatColor.GRAY + power.getOptionValue(option).toString();
					List<String> lore = PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), optionDesc, 30);
					lore.add(optionValue);
					optionItems.put(option, createItem(Material.PAPER, ChatColor.RESET + optionName, lore));
				}
				setItem(i - index, optionItems.get(option), null);
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
