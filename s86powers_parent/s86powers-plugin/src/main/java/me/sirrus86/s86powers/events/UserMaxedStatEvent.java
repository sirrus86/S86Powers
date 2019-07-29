package me.sirrus86.s86powers.events;

import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.users.PowerUser;

public class UserMaxedStatEvent extends UserEvent {

	private final PowerStat stat;
	
	public UserMaxedStatEvent(PowerUser user, PowerStat stat) {
		super(user);
		this.stat = stat;
	}
	
	public PowerStat getStat() {
		return this.stat;
	}

}
