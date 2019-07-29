package me.sirrus86.s86powers.gui;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;

public class GUIPlayerList extends GUIAbstractList<PowerUser> {
	
	public GUIPlayerList(int page, Collection<PowerUser> list) {
		super(page, list);
	}
	
	private void setItem(int slot, OfflinePlayer player, String name, GUIAction action) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(Material.PLAYER_HEAD);
		meta.setDisplayName(ChatColor.RESET + name);
		meta.addItemFlags(ItemFlag.values());
		if (meta instanceof SkullMeta) {
			SkullMeta skullMeta = (SkullMeta) meta;
			skullMeta.setOwningPlayer(player);
		}
		item.setItemMeta(meta);
		guiInv.setItem(slot, item);
		if (action != null) {
			actions.put(slot, action);
		}
	}

	@Override
	protected void setItems() {
		if (page > 0) {
			int index = page * 45 - 45;
			for (int i = index; i < Math.min(list.size(), index + 45); i ++) {
				PowerUser user = list.get(i);
				String userName = user.getName();
				setItem(i - index, user.getOfflinePlayer(), ChatColor.RESET + userName, player -> {
					if (selectedGroup.containsKey(player.getUniqueId())) {
						PowerGroup group = selectedGroup.get(player.getUniqueId());
						if (group.hasMember(user)) {
							player.closeInventory();
							player.performCommand("powers group " + group.getName() + " kick " + (user.getName() != null ? user.getName() : "!NULL"));
						}
						else {
							player.closeInventory();
							player.performCommand("powers group " + group.getName() + " assign " + (user.getName() != null ? user.getName() : "!NULL"));
						}
					}
					else {
						selectedUser.put(player.getUniqueId(), user);
						openNext(player, GUIBase.PLAYER_GUI);
					}
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
