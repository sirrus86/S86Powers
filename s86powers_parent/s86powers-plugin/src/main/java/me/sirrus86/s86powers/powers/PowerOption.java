package me.sirrus86.s86powers.powers;

public class PowerOption<T> implements Comparable<PowerOption<?>> {

	private final String desc, path;
	private final T defValue;
	private final boolean locked;
	private final Power power;
	
	public PowerOption(Power power, String path, T defValue, String desc, boolean locked) {
		this.defValue = defValue;
		this.desc = desc;
		this.locked = locked;
		this.path = path;
		this.power = power;
	}
	
	public final T getDefaultValue() {
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

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public final boolean isLocked() {
		return locked;
	}

	@Override
	public int compareTo(PowerOption<?> option) {
		return getPath().compareTo(option.getPath());
	}
	
}
