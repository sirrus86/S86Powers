package me.sirrus86.s86powers.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.users.PowerUser;

public class PowerUseEvent extends UserEvent implements Cancellable {

	private final Block block;
	private boolean cancelled = false;
	private final BlockFace face;
	private final EquipmentSlot hand;
	private final ItemStack item;
	private final Power power;
	
	public PowerUseEvent(final PowerUser user, final Power power, final ItemStack item, final EquipmentSlot hand, final Block block, final BlockFace face) {
		super(user);
		this.power = power;
		this.block = block;
		this.face = face;
		this.hand = hand;
		this.item = item;
	}
	
	public void consumeItem() {
		getUser().getEquipment(hand).setAmount(getUser().getEquipment(hand).getAmount() - 1);
	}
	
	public BlockFace getBlockFace() {
		return face;
	}
	
	public Block getClickedBlock() {
		return block;
	}
	
	public EquipmentSlot getHand() {
		return hand;
	}
	
	public ItemStack getItem() {
		return item;
	}

	public Power getPower() {
		return power;
	}
	
	public boolean hasBlock() {
		return block != null;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		cancelled = arg0;
	}

}
