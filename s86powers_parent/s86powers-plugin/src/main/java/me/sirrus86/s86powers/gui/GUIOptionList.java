package me.sirrus86.s86powers.gui;

import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.PowerAdapter;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.tools.PowerTools;

public class GUIOptionList extends GUIAbstractList<PowerOption> {

	public GUIOptionList(int page, Collection<PowerOption> list) {
		super(page, list);
	}

	@Override
	void setItems() {
		if (page > 0) {
			int index = page * 45 - 45;
			for (int i = index; i < Math.min(list.size(), index + 45); i ++) {
				PowerOption option = list.get(i);
				PowerAdapter pCont = PowerAdapter.getAdapter(option.getPower());
				String optionName = option.getPath();
				String optionDesc = option.getDescription();
				String optionValue = ChatColor.RESET + LocaleString.VALUE.toString() + ": " + ChatColor.GRAY + pCont.getOptionValue(option).toString();
				List<String> lore = PowerTools.wordSplit(ChatColor.RESET.toString() + ChatColor.GRAY.toString(), optionDesc, 30);
				lore.add(optionValue);
				setItem(i - index, LIST, ChatColor.RESET + optionName, lore, null);
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
