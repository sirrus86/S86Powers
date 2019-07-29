package me.sirrus86.s86powers.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.users.PowerUser;

public class PowerIgniteEvent extends PowerEvent {

	private Block block;
	private Entity entity;
	private BlockFace face;
	private final PowerUser user;
	
	public PowerIgniteEvent(final Power power, final PowerUser user, final Block block, final BlockFace face) {
		super(power);
		this.user = user;
		this.block = block;
		this.face = face;
	}
	
	public PowerIgniteEvent(final Power power, final PowerUser user, final Entity entity) {
		super(power);
		this.user = user;
		this.entity = entity;
	}
	
	public Block getBlock() {
		return block;
	}
	
	public BlockFace getBlockFace() {
		return face;
	}
	
	public Entity getEntity() {
		return entity;
	}

	public final PowerUser getUser() {
		return user;
	}
	
}
