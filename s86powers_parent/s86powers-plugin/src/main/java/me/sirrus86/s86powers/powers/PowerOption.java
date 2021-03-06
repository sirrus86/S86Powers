package me.sirrus86.s86powers.powers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PowerOption<O> implements Comparable<PowerOption<?>> {

	private final String desc, path;
	private final Object defValue;
	private final boolean locked;
	private final Power power;
	
	public PowerOption(Power power, String path, Object defValue, String desc, boolean locked) {
		this.defValue = defValue;
		this.desc = desc;
		this.locked = locked;
		this.path = path;
		this.power = power;
	}
	
	public final Object getDefaultValue() {
		return defValue;
	}
	
	public final String getDescription() {
		return desc;
	}
	
	public final String getPath() {
		return path;
	}
	
	public final Power getPower() {
		return power;
	}
	
	public final boolean isLocked() {
		return locked;
	}

	@Override
	public int compareTo(PowerOption<?> o) {
		String o1Str = getPath(),
				o2Str = o.getPath();
		List<String> tmp = Arrays.asList(o1Str, o2Str);
		Collections.sort(tmp);
		if (tmp.get(0).equalsIgnoreCase(getPath())) {
			return -1;
		}
		else {
			return 1;
		}
	}
	
}
