package me.sirrus86.s86powers.powers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PowerStat implements Comparable<PowerStat> {

	private final String desc, path, reward;
	private final int defValue;
	private final Power power;
	
	public PowerStat(final Power power, final String path, final int defValue, final String description, final String reward) {
		this.desc = description;
		this.defValue = defValue;
		this.path = path;
		this.power = power;
		this.reward = reward;
	}
	
	public final String getDescription() {
		return desc;
	}
	
	public final int getDefaultValue() {
		return defValue;
	}
	
	public final String getPath() {
		return path;
	}
	
	public final Power getPower() {
		return power;
	}
	
	public final String getReward() {
		return reward;
	}
	
	@Override
	public int compareTo(PowerStat o) {
		String o1Str = getPath(),
				o2Str = o.getPath();
		List<String> tmp = Arrays.asList(o1Str, o2Str);
		Collections.sort(tmp);
		if (tmp.get(0).equalsIgnoreCase(getPath())) return -1;
		else return 1;
	}
	
}
