package me.sirrus86.s86powers.powers;

import me.sirrus86.s86powers.users.PowerUser;

public class PowerFire {
	
	private final Power power;
	private final PowerUser user;
	
	public PowerFire(Power power, PowerUser user) {
		this.power = power;
		this.user = user;
	}
	
	public final Power getPower() {
		return power;
	}
	
	public final PowerUser getUser() {
		return user;
	}
	
}
