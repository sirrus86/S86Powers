package me.sirrus86.s86powers.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigManager;
import me.sirrus86.s86powers.localization.LocaleString;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.regions.NeutralRegion;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerGroup;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class ComAbstract {
	
	protected static final String ERROR = ChatColor.WHITE + "[" + ChatColor.RED + LocaleString.ERROR_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET,
			HELP = ChatColor.WHITE + "[" + ChatColor.YELLOW + LocaleString.HELP_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET,
			INFO = ChatColor.WHITE + "[" + ChatColor.YELLOW + LocaleString.INFO_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET,
			LIST = ChatColor.WHITE + "[" + ChatColor.YELLOW + LocaleString.LIST_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET,
			SUCCESS = ChatColor.WHITE + "[" + ChatColor.GREEN + LocaleString.SUCCESS_CAPS + ChatColor.WHITE + "] " + ChatColor.RESET;

	protected static final ConfigManager config = S86Powers.getConfigManager();
	protected final CommandSender sender;
	protected final PowerUser sUser;

	@SuppressWarnings("unused")
	protected ComAbstract(CommandSender sender, String... args) {
		this.sender = sender;
		sUser = sender instanceof Player ? S86Powers.getConfigManager().getUser(((Player) sender).getUniqueId()) : null;
	}

	protected final ItemStack createItemStack(String value) {
		return createItemStack(value, 1);
	}

	protected final ItemStack createItemStack(String value, int qty) {
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
	
	private PotionEffect createPotionEffect(String value) {
		PotionEffect effect = null;
		if (value != null) {
			String[] values = value.split(",");
			if (values.length > 2) {
				PotionEffectType eType = PotionEffectType.getByName(values[0]);
				int eDur = Integer.parseInt(values[1]);
				int eAmp = Integer.parseInt(values[2]);
				boolean eAmbient = true,
						eParticles = true,
						eIcon = true;
				if (values.length > 3) {
					eAmbient = Boolean.parseBoolean(values[3]);
					if (values.length > 4) {
						eParticles = Boolean.parseBoolean(values[4]);
						if (values.length > 5) {
							eIcon = Boolean.parseBoolean(values[5]);
						}
					}
				}
				if (eType != null) {
					effect = new PotionEffect(eType, eDur, eAmp, eAmbient, eParticles, eIcon);
				}
			}
		}
		return effect;
	}

	protected final String getGroups() {
		List<PowerGroup> groups = Lists.newArrayList(S86Powers.getConfigManager().getGroups());
		Collections.sort(groups);
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < groups.size(); i ++) {
			PowerGroup group = groups.get(i);
			tmp.append(ChatColor.GREEN).append(group.getName()).append(ChatColor.GRAY);
			if (i < groups.size() - 1) {
				tmp.append(", ");
			}
		}
		return tmp.toString().equals("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getGroups(PowerUser user) {
		StringBuilder tmp = new StringBuilder();
		List<PowerGroup> pList = Lists.newArrayList(user.getGroups());
		Collections.sort(pList);
		for (int i = 0; i < pList.size(); i ++) {
			PowerGroup group = pList.get(i);
			tmp.append(ChatColor.RESET).append(group.getName()).append(ChatColor.RESET);
			if (!user.getAssignedGroups().contains(group)) {
				tmp.append("(").append(ChatColor.GRAY).append("P").append(ChatColor.RESET).append(")");
			}
			if (i < pList.size() - 1) {
				tmp.append(ChatColor.GRAY).append(", ");
			}
		}
		return tmp.toString().equalsIgnoreCase("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getOptions(Power power) {
		List<PowerOption<?>> options = Lists.newArrayList(power.getOptions().keySet());
		Collections.sort(options);
		StringBuilder tmp = new StringBuilder();
		for (PowerOption<?> option : options) {
			StringBuilder valueStr = new StringBuilder(power.getOption(option).toString());
			if (power.getOption(option) instanceof ItemStack) {
				valueStr = new StringBuilder(PowerTools.getItemName((ItemStack) power.getOption(option)));
			} else if (power.getOption(option) instanceof List<?> optList) {
				valueStr = new StringBuilder(LocaleString.LIST + " {");
				for (int j = 0; j < optList.size(); j++) {
					valueStr.append(optList.get(j).toString()).append(j < optList.size() ? ", " : "");
				}
				valueStr.append("}");
			}
			tmp.append(ChatColor.GREEN).append(option.getPath()).append(ChatColor.GRAY).append(": ").append(ChatColor.WHITE).append(valueStr).append("\n");
		}
		if (tmp.toString().endsWith("\n")) {
			tmp = new StringBuilder(tmp.substring(0, tmp.lastIndexOf("\n")));
		}
//		tmp.toString().split("\n");
		return tmp.toString().equals("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getOptions(Power power, PowerUser user) {
		List<PowerOption<?>> options = new ArrayList<>();
		for (PowerOption<?> option : user.getOptions().keySet()) {
			if (option.getPower() == power) {
				options.add(option);
			}
		}
		Collections.sort(options);
		StringBuilder tmp = new StringBuilder();
		for (PowerOption<?> option : options) {
			StringBuilder valueStr = new StringBuilder(user.getOption(option).toString());
			if (user.getOption(option) instanceof ItemStack) {
				valueStr = new StringBuilder(PowerTools.getItemName((ItemStack) user.getOption(option)));
			} else if (user.getOption(option) instanceof List<?> optList) {
				valueStr = new StringBuilder(LocaleString.LIST + " {");
				for (int j = 0; j < optList.size(); j++) {
					valueStr.append(optList.get(j).toString()).append(j < optList.size() ? ", " : "");
				}
				valueStr.append("}");
			}
			tmp.append(ChatColor.GREEN).append(option.getPath()).append(ChatColor.GRAY).append(": ").append(ChatColor.WHITE).append(valueStr).append("\n");
		}
		if (tmp.toString().endsWith("\n")) {
			tmp = new StringBuilder(tmp.substring(0, tmp.lastIndexOf("\n")));
		}
//		tmp.toString().split("\n");
		return tmp.toString().equals("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getPowerDesc(Power power) {
		power.refreshOptions();
		return PowerTools.getFilteredText(power, power.getDescription());
	}

	protected final String getPowerList(Set<Power> list) {
		StringBuilder tmp = new StringBuilder("(" + ChatColor.GRAY + list.size() + ChatColor.RESET + ") ");
		List<Power> powers = Lists.newArrayList(list);
		Collections.sort(powers);
		for (int i = 0; i < powers.size(); i ++) {
			Power power = powers.get(i);
			tmp.append(power.getType().getColor()).append(power.getTag()).append(ChatColor.GRAY);
			if (i < powers.size() - 1) {
				tmp.append(", ");
			}
		}
		return list.isEmpty() ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getPowers(PowerGroup group) {
		StringBuilder tmp = new StringBuilder();
		List<Power> pList = Lists.newArrayList(group.getPowers());
		Collections.sort(pList);
		for (int i = 0; i < pList.size(); i ++) {
			Power power = pList.get(i);
			tmp.append(power.getType().getColor()).append(power.getTag()).append(ChatColor.RESET);
			if (i < pList.size() - 1) {
				tmp.append(ChatColor.GRAY).append(", ");
			}
		}
		return tmp.toString().equalsIgnoreCase("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getPowers(PowerUser user) {
		StringBuilder tmp = new StringBuilder();
		List<Power> pList = Lists.newArrayList(user.getPowers());
		Collections.sort(pList);
		for (int i = 0; i < pList.size(); i ++) {
			Power power = pList.get(i);
			tmp.append(power.getType().getColor()).append(power.getTag()).append(ChatColor.RESET);
			if (!user.getAssignedPowers().contains(pList.get(i))) {
				if (user.getGroupPowers().contains(pList.get(i))) {
					tmp.append("(").append(ChatColor.GRAY).append("G").append(ChatColor.RESET).append(")");
				}
				else if (user.getPermissiblePowers().contains(pList.get(i))) {
					tmp.append("(").append(ChatColor.GRAY).append("P").append(ChatColor.RESET).append(")");
				}
			}
			if (i < pList.size() - 1) {
				tmp.append(ChatColor.GRAY).append(", ");
			}
		}
		return tmp.toString().equalsIgnoreCase("") ? LocaleString.NONE.toString() : tmp.toString();
	}
	
	protected final Power getRandomPower(PowerGroup group) {
		List<Power> powers = Lists.newArrayList(config.getPowers());
		powers.removeAll(config.getPowersByType(PowerType.UTILITY));
		powers.removeAll(group.getPowers());
		Collections.shuffle(powers);
		return !powers.isEmpty() ? powers.get(0) : null;
	}
	
	protected final Power getRandomPower(PowerUser user) {
		List<Power> powers = Lists.newArrayList(config.getPowers());
		powers.removeAll(config.getPowersByType(PowerType.UTILITY));
		powers.removeAll(user.getAssignedPowers());
		Collections.shuffle(powers);
		return !powers.isEmpty() ? powers.get(0) : null;
	}

	protected final String getRegions() {
		List<NeutralRegion> regions = Lists.newArrayList(S86Powers.getConfigManager().getRegions());
		Collections.sort(regions);
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < regions.size(); i ++) {
			NeutralRegion region = regions.get(i);
			tmp.append(ChatColor.GREEN).append(region.getName()).append(ChatColor.GRAY);
			if (i < regions.size() - 1) {
				tmp.append(", ");
			}
		}
		return tmp.toString().equals("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getStats(Power power) {
		List<PowerStat> stats = Lists.newArrayList(power.getStats().keySet());
		Collections.sort(stats);
		StringBuilder tmp = new StringBuilder();
		for (PowerStat stat : stats) {
			tmp.append(ChatColor.GREEN).append(stat.getPath()).append(ChatColor.GRAY).append(": ").append(ChatColor.WHITE).append(power.getStatValue(stat)).append("\n");
		}
		if (tmp.toString().endsWith("\n")) {
			tmp = new StringBuilder(tmp.substring(0, tmp.lastIndexOf("\n")));
		}
//		tmp.toString().split("\n");
		return tmp.toString().equals("") ? LocaleString.NONE.toString() : tmp.toString();
	}
	
	protected final String getSupplies(Power power) {
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < power.getSupplies().size(); i ++) {
			tmp.append(ChatColor.GREEN).append(i).append(ChatColor.RESET).append(": ").append(ChatColor.GRAY).append(PowerTools.getItemName(power.getSupplies().get(i))).append(" x").append(power.getSupplies().get(i).getAmount()).append("\n");
		}
		if (tmp.toString().equalsIgnoreCase("")) {
			return LocaleString.NONE.toString();
		}
//		tmp.toString().split("\n");
		return tmp.toString();
	}

	protected final String getUserName(PowerUser user) {
		ChatColor color = ChatColor.GREEN;
		if (user.isAdmin()) {
			color = ChatColor.GOLD;
		}
		else if (user.getPowers().isEmpty()) {
			color = ChatColor.GRAY;
		}
		return color + (user.getName() != null ? user.getName() : "NULL") + ChatColor.RESET;
	}

	protected final String getUsers() {
		List<PowerUser> users = Lists.newArrayList(S86Powers.getConfigManager().getUserList());
		Collections.sort(users);
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < users.size(); i ++) {
			PowerUser user = users.get(i);
			tmp.append(getUserName(user)).append(ChatColor.GRAY);
			if (i < users.size() - 1) {
				tmp.append(", ");
			}
		}
		return tmp.toString().equals("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getUsers(Power power) {
		List<PowerUser> users = Lists.newArrayList(power.getUsers());
		Collections.sort(users);
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < users.size(); i ++) {
			PowerUser user = users.get(i);
			tmp.append(getUserName(user)).append(ChatColor.GRAY);
			if (i < users.size() - 1) {
				tmp.append(", ");
			}
		}
		return tmp.toString().equals("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final String getUsers(PowerGroup group) {
		List<PowerUser> users = Lists.newArrayList(group.getMembers());
		Collections.sort(users);
		StringBuilder tmp = new StringBuilder();
		for (int i = 0; i < users.size(); i ++) {
			PowerUser user = users.get(i);
			tmp.append(getUserName(user)).append(ChatColor.GRAY);
			if (i < users.size() - 1) {
				tmp.append(", ");
			}
		}
		return tmp.toString().equals("") ? LocaleString.NONE.toString() : tmp.toString();
	}

	protected final void killPower(Power power) {
		Set<Field> fields = Sets.newHashSet(Power.class.getDeclaredFields());
		fields.addAll(Sets.newHashSet(power.getClass().getDeclaredFields()));
		for (Field field : fields) {
			field.setAccessible(true);
			try {
				field.set(this, null);
			} catch (Exception ignored) {
			}
		}
		System.gc();
	}

	protected final String optList() {
		StringBuilder tmp = new StringBuilder();
		List<String> options = Lists.newArrayList(S86Powers.getConfigManager().getConfigOptions().keySet());
		Collections.sort(options);
		for (String option : options) {
			Object value = S86Powers.getConfigManager().getConfigValue(option);
			tmp.append(ChatColor.GREEN).append(option).append(ChatColor.RESET).append(": ").append(value.toString()).append("\n");
		}
		if (tmp.toString().endsWith("\n")) {
			tmp = new StringBuilder(tmp.substring(0, tmp.lastIndexOf("\n")));
		}
		return tmp.toString();
	}

	protected final void supply(PowerUser user, Power power) {
		user.supply(power);
	}

	protected final Object validate(PowerOption<?> option, String value) {
		Object defValue = option.getDefaultValue() instanceof List<?> ? ((List<?>)option.getDefaultValue()).get(0) : option.getDefaultValue();
		if (defValue instanceof Boolean
				&& (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("true"))) {
			return Boolean.parseBoolean(value);
		}
		else if (defValue instanceof Double) {
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		else if (defValue instanceof Float) {
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		else if (defValue instanceof Long) {
			try {
				return PowerTime.toMillis(value);
			} catch (Exception e) {
				return null;
			}
		}
		else if (defValue instanceof Integer) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		else if (defValue instanceof ItemStack) {
			return createItemStack(value);
		}
		else if (defValue instanceof PotionEffect) {
			return createPotionEffect(value);
		}
		else if (defValue instanceof String) {
			return value;
		}
		return null;
	}
	
	protected final int validate(PowerStat stat, String value) {
		try {
			return Math.min(Integer.parseInt(value), stat.getDefaultValue());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

}
