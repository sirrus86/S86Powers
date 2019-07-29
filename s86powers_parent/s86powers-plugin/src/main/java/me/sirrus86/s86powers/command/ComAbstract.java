package me.sirrus86.s86powers.command;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigManager;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerContainer;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.regions.NeutralRegion;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.users.UserContainer;
import me.sirrus86.s86powers.utils.PowerTime;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class ComAbstract {

	private final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	
	protected static final String ERROR = ChatColor.WHITE + "[" + ChatColor.RED + LocaleString.ERROR_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET,
			HELP = ChatColor.WHITE + "[" + ChatColor.YELLOW + LocaleString.HELP_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET,
			INFO = ChatColor.WHITE + "[" + ChatColor.YELLOW + LocaleString.INFO_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET,
			LIST = ChatColor.WHITE + "[" + ChatColor.YELLOW + LocaleString.LIST_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET,
			SUCCESS = ChatColor.WHITE + "[" + ChatColor.GREEN + LocaleString.SUCCESS_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET;

	protected final ConfigManager config;
	protected final CommandSender sender;
	protected final PowerUser sUser;

	protected ComAbstract(CommandSender sender, String... args) {
		this.sender = sender;
		sUser = sender instanceof Player ? plugin.getConfigManager().getUser(((Player) sender).getUniqueId()) : null;
		config = plugin.getConfigManager();
	}

	protected String capitalize(String line) {
		if (line != null) {
			return line.length() > 1 ? line.substring(0, 1).toUpperCase() + line.substring(1).toLowerCase() : line.toUpperCase();
		}
		else {
			return "";
		}
	}

	protected ItemStack createItemStack(String value) {
		return createItemStack(value, 1);
	}

	protected ItemStack createItemStack(String value, int qty) {
		ItemStack item = null;
		String type = "";
		Material mat = Material.getMaterial(type);
		if (mat == null) {
			mat = Material.matchMaterial(value);
		}
		if (mat != null) {
			item = new ItemStack(mat, qty);
		}
		if (item != null) {
			if (item.getType() == Material.AIR) {
				return new ItemStack(Material.AIR, 0);
			}
			else {
				return item;
			}
		}
		return null;
	}

	protected String getGroups() {
		List<PowerGroup> groups = Lists.newArrayList(plugin.getConfigManager().getGroups());
		Collections.sort(groups);
		String tmp = "";
		for (int i = 0; i < groups.size(); i ++) {
			PowerGroup group = groups.get(i);
			tmp = tmp + ChatColor.GREEN + group.getName() + ChatColor.GRAY;
			if (i < groups.size() - 1) {
				tmp = tmp + ", ";
			}
		}
		return tmp == "" ? LocaleString.NONE.toString() : tmp;
	}

	protected String getGroups(UserContainer user) {
		String tmp = "";
		List<PowerGroup> pList = Lists.newArrayList(user.getGroups());
		Collections.sort(pList);
		for (int i = 0; i < pList.size(); i ++) {
			PowerGroup group = pList.get(i);
			tmp = tmp + ChatColor.RESET + group.getName() + ChatColor.RESET;
			if (!user.getAssignedGroups().contains(group)) {
				tmp = tmp + "(" + ChatColor.GRAY + "P" + ChatColor.RESET + ")";
			}
			if (i < pList.size() - 1) {
				tmp = tmp + ChatColor.GRAY + ", ";
			}
		}
		return tmp.equalsIgnoreCase("") ? LocaleString.NONE.toString() : tmp;
	}

	protected String getItemName(ItemStack itemStack) {
		return PowerTools.getItemName(itemStack);
	}

	protected String getOptions(PowerContainer pCont) {
		List<PowerOption> options = Lists.newArrayList(pCont.getOptions().keySet());
		Collections.sort(options);
		String tmp = "";
		for (int i = 0; i < options.size(); i ++) {
			PowerOption option = options.get(i);
			tmp = tmp + ChatColor.GREEN + option.getPath() + ChatColor.GRAY + ": " + ChatColor.WHITE + (pCont.getOptionValue(option) instanceof ItemStack ? getItemName((ItemStack) pCont.getOptionValue(option)) : pCont.getOptionValue(option).toString()) + "\n";
		}
		if (tmp.endsWith("\n")) {
			tmp = tmp.substring(0, tmp.lastIndexOf("\n"));
		}
		tmp.split("\n");
		return tmp == "" ? LocaleString.NONE.toString() : tmp;
	}

	protected String getPowerDesc(PowerContainer pCont) {
		pCont.refreshOptions();
		return pCont.getFilteredText(pCont.getDescription());
	}

	protected String getPowerList(Set<Power> list) {
		String tmp = "(" + ChatColor.GRAY + list.size() + ChatColor.RESET + ") ";
		List<Power> powers = Lists.newArrayList(list);
		Collections.sort(powers);
		for (int i = 0; i < powers.size(); i ++) {
			PowerContainer pCont = PowerContainer.getContainer(powers.get(i));
			tmp = tmp + powers.get(i).getType().getColor() + pCont.getTag() + ChatColor.GRAY;
			if (i < powers.size() - 1) {
				tmp = tmp + ", ";
			}
		}
		return list.isEmpty() ? LocaleString.NONE.toString() : tmp;
	}

	protected String getPowers(PowerGroup group) {
		String tmp = "";
		List<Power> pList = Lists.newArrayList(group.getPowers());
		Collections.sort(pList);
		for (int i = 0; i < pList.size(); i ++) {
			PowerContainer pCont = PowerContainer.getContainer(pList.get(i));
			tmp = tmp + pList.get(i).getType().getColor() + pCont.getTag() + ChatColor.RESET;
			if (i < pList.size() - 1) {
				tmp = tmp + ChatColor.GRAY + ", ";
			}
		}
		return tmp.equalsIgnoreCase("") ? LocaleString.NONE.toString() : tmp;
	}

	protected String getPowers(UserContainer user) {
		String tmp = "";
		List<Power> pList = Lists.newArrayList(user.getPowers());
		Collections.sort(pList);
		for (int i = 0; i < pList.size(); i ++) {
			PowerContainer pCont = PowerContainer.getContainer(pList.get(i));
			tmp = tmp + pList.get(i).getType().getColor() + pCont.getTag() + ChatColor.RESET;
			if (!user.getAssignedPowers().contains(pList.get(i))) {
				if (user.getGroupPowers().contains(pList.get(i))) {
					tmp = tmp + "(" + ChatColor.GRAY + "G" + ChatColor.RESET + ")";
				}
				else if (user.getPermissiblePowers().contains(pList.get(i))) {
					tmp = tmp + "(" + ChatColor.GRAY + "P" + ChatColor.RESET + ")";
				}
			}
			if (i < pList.size() - 1) {
				tmp = tmp + ChatColor.GRAY + ", ";
			}
		}
		return tmp.equalsIgnoreCase("") ? LocaleString.NONE.toString() : tmp;
	}

	protected String getRegions() {
		List<NeutralRegion> regions = Lists.newArrayList(plugin.getConfigManager().getRegions());
		Collections.sort(regions);
		String tmp = "";
		for (int i = 0; i < regions.size(); i ++) {
			NeutralRegion region = regions.get(i);
			tmp = tmp + ChatColor.GREEN + region.getName() + ChatColor.GRAY;
			if (i < regions.size() - 1) {
				tmp = tmp + ", ";
			}
		}
		return tmp == "" ? LocaleString.NONE.toString() : tmp;
	}

	protected String getStats(PowerContainer power) {
		List<PowerStat> stats = Lists.newArrayList(power.getStats().keySet());
		Collections.sort(stats);
		String tmp = "";
		for (int i = 0; i < stats.size(); i ++) {
			PowerStat stat = stats.get(i);
			tmp = tmp + ChatColor.GREEN + stat.getPath() + ChatColor.GRAY + ": " + ChatColor.WHITE + power.getStatValue(stat) + "\n";
		}
		if (tmp.endsWith("\n")) {
			tmp = tmp.substring(0, tmp.lastIndexOf("\n"));
		}
		tmp.split("\n");
		return tmp == "" ? LocaleString.NONE.toString() : tmp;
	}
	
	protected String getSupplies(PowerContainer power) {
		String tmp = "";
		for (int i = 0; i < power.getSupplies().size(); i ++) {
			tmp = tmp + ChatColor.GREEN + i + ChatColor.RESET + ": " + ChatColor.GRAY + getItemName(power.getSupplies().get(i)) + " x" + power.getSupplies().get(i).getAmount() + "\n";
		}
		if (tmp.equalsIgnoreCase("")) {
			return LocaleString.NONE.toString();
		}
		tmp.split("\n");
		return tmp;
	}

	protected String getUserName(UserContainer user) {
		ChatColor color = ChatColor.GREEN;
		if (user.isAdmin()) {
			color = ChatColor.GOLD;
		}
		else if (user.getPowers().isEmpty()) {
			color = ChatColor.GRAY;
		}
		return color + (user.getUser().getName() != null ? user.getUser().getName() : "NULL") + ChatColor.RESET;
	}

	protected String getUsers() {
		List<PowerUser> users = Lists.newArrayList(plugin.getConfigManager().getUserList());
		Collections.sort(users);
		String tmp = "";
		for (int i = 0; i < users.size(); i ++) {
			UserContainer user = UserContainer.getContainer(users.get(i));
			tmp = tmp + getUserName(user) + ChatColor.GRAY;
			if (i < users.size() - 1) {
				tmp = tmp + ", ";
			}
		}
		return tmp == "" ? LocaleString.NONE.toString() : tmp;
	}

	protected String getUsers(PowerContainer power) {
		List<PowerUser> users = Lists.newArrayList(power.getUsers());
		Collections.sort(users);
		String tmp = "";
		for (int i = 0; i < users.size(); i ++) {
			UserContainer user = UserContainer.getContainer(users.get(i));
			tmp = tmp + getUserName(user) + ChatColor.GRAY;
			if (i < users.size() - 1) {
				tmp = tmp + ", ";
			}
		}
		return tmp == "" ? LocaleString.NONE.toString() : tmp;
	}

	protected String getUsers(PowerGroup group) {
		List<PowerUser> users = Lists.newArrayList(group.getMembers());
		Collections.sort(users);
		String tmp = "";
		for (int i = 0; i < users.size(); i ++) {
			UserContainer user = UserContainer.getContainer(users.get(i));
			tmp = tmp + getUserName(user) + ChatColor.GRAY;
			if (i < users.size() - 1) {
				tmp = tmp + ", ";
			}
		}
		return tmp == "" ? LocaleString.NONE.toString() : tmp;
	}

	protected void killPower(Power power) {
		Set<Field> fields = Sets.newHashSet(Power.class.getDeclaredFields());
		fields.addAll(Sets.newHashSet(power.getClass().getDeclaredFields()));
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				field.set(this, null);
			} catch (Exception e) {
			}
		}
		System.gc();
	}

	protected final String optList() {
		String tmp = "";
		List<String> options = Lists.newArrayList(plugin.getConfigManager().getOptions().keySet());
		Collections.sort(options);
		for (int i = 0; i < options.size(); i ++) {
			String option = options.get(i);
			Field field = plugin.getConfigManager().getOptions().get(option);
			try {
				tmp = tmp + ChatColor.GREEN + option + ChatColor.RESET + ": " + field.get(null).toString() + "\n";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (tmp.endsWith("\n")) {
			tmp = tmp.substring(0, tmp.lastIndexOf("\n"));
		}
		return tmp;
	}

	protected void supply(UserContainer user, Power power) {
		user.supply(power);
	}

	protected Object validate(PowerOption option, String value) {
		if (option.getDefaultValue() instanceof Boolean
				&& (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("true"))) {
			return Boolean.parseBoolean(value);
		}
		else if (option.getDefaultValue() instanceof Double) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		else if (option.getDefaultValue() instanceof Long) {
			try {
				return PowerTime.toMillis(value);
			} catch (Exception e) {
				return null;
			}
		}
		else if (option.getDefaultValue() instanceof Integer) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		else if (option.getDefaultValue() instanceof ItemStack) {
			return createItemStack(value);
		}
		else if (option.getDefaultValue() instanceof String) {
			return value;
		}
		return null;
	}
	
	protected int validate(PowerStat stat, String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

}
