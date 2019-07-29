package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Adaptability", type = PowerType.DEFENSE, author = "sirrus86", concept = "diamondmario", icon=Material.LEATHER_CHESTPLATE,
	description = "When damaged, develop resistance to that damage type, reducing all following damage of the same type starting at [initAmt]%, increasing up to [maxAmt]%. While resistant to one type, damage from all other types increased to [dmgIncr]%. [cooldown] cooldown.[noArmor] Power prevents you from wearing armor.[/noArmor]")
public class Adaptability extends Power {

	private Map<PowerUser, AdaptUser> aUsers;
	
	private boolean noArmor;
	private double dmgIncr, incrAmt, initAmt, maxAmt;
	private int steps;
	
	@Override
	protected void onEnable() {
		aUsers = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		aUsers.remove(user);
	}

	@Override
	protected void options() {
		cooldown = option("adapt-cooldown", PowerTime.toMillis(3, 0), "Minimum time between incoming damage that user may further adapt.");
		dmgIncr = option("damage-increase", 200.0D, "Percentage of damage done from adaptable sources that aren't being adapted to.");
		initAmt = option("initial-adapt", 25.0D, "Initial percent of damage mitigation after user changes adapt type.");
		maxAmt = option("maximum-adapt", 100.0D, "Maximum percent of damage mitigation for a given adapt type.");
		noArmor = option("prevent-armor", true, "Prevents users from wearing armor.");
		steps = option("adapt-increment-steps", 10, "Number of increments it takes to reach maximum adapt from initial.");
		incrAmt = (maxAmt - initAmt) / steps;
	}
	
	private double adapt(PowerUser user, AdaptType type) {
		double amt = 1.0D;
		if (aUsers.get(user).getType() != type) {
			amt = dmgIncr;
			if (user.getCooldown(this) == 0) {
				user.sendMessage(ChatColor.YELLOW + "Now adapting to " + type.toString().toLowerCase() + " damage.");
				aUsers.get(user).setType(type).setAmount(initAmt);
				user.setCooldown(this, cooldown);
			}
		}
		else {
			amt = aUsers.get(user).getAmount();
			if (user.getCooldown(this) == 0
					&& aUsers.get(user).getAmount() < maxAmt) {
				aUsers.get(user).increaseAmount(incrAmt);
				user.sendMessage(ChatColor.YELLOW + "Resistance to " + type.toString().toLowerCase() + " increased to " + aUsers.get(user).getAmount() + "%.");
				user.setCooldown(this, cooldown);
			}
		}
		return (amt <= 100.0D ? (100.0D - amt) : amt) / 100.0D;
	}
	
	@EventHandler
	private void onClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			PowerUser user = getUser((Player) event.getPlayer());
			if (user.allowPower(this)
					&& noArmor
					&& user.getPlayer().getInventory().getArmorContents() != null) {
				boolean hadArmor = false;
				for (ItemStack armor : user.getPlayer().getInventory().getArmorContents()) {
					if (armor != null
							&& armor.getType() != Material.AIR) {
						user.getPlayer().getWorld().dropItem(user.getPlayer().getLocation(), armor);
						hadArmor = true;
					}
				}
				if (hadArmor) {
					user.sendMessage(ChatColor.RED + "Your power prevents you from wearing armor.");
				}
				user.getPlayer().getInventory().setArmorContents(null);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& AdaptType.getByDamageCause(event.getCause()) != null) {
				AdaptType type = AdaptType.getByDamageCause(event.getCause());
				if (!aUsers.containsKey(user)) {
					aUsers.put(user, new AdaptUser(null, 0.0D));
				}
				double dmg = adapt(user, type);
				event.setDamage(event.getDamage() * dmg);
			}
		}
	}
	
	private enum AdaptType {
		
		EXPLOSION, FIRE, LIGHTNING, MAGIC, NATURE, PHYSICAL, POISON, PROJECTILE, SHADOW, WATER;
		
		public static AdaptType getByDamageCause(DamageCause cause) {
			switch(cause) {
				case BLOCK_EXPLOSION: case ENTITY_EXPLOSION: return EXPLOSION;
				case CONTACT: case THORNS: return NATURE;
				case DROWNING: return WATER;
				case ENTITY_ATTACK: case ENTITY_SWEEP_ATTACK: case FALLING_BLOCK: return PHYSICAL;
				case FIRE: case FIRE_TICK: case HOT_FLOOR: case LAVA: return FIRE;
				case LIGHTNING: return LIGHTNING;
				case MAGIC: return MAGIC;
				case POISON: case WITHER: return POISON;
				case PROJECTILE: return PROJECTILE;
				case DRAGON_BREATH: case VOID: return SHADOW;
				default: return null;
			}
		}
		
	}
	
	private class AdaptUser {
		
		private double amount;
		private AdaptType type;
		
		public AdaptUser(AdaptType type, double amount) {
			this.type = type;
			this.amount = amount;
		}
		
		public double getAmount() {
			return amount;
		}
		
		public AdaptType getType() {
			return type;
		}
		
		public void increaseAmount(double amount) {
			this.amount += amount;
		}
		
		public AdaptUser setAmount(double amount) {
			this.amount = amount;
			return this;
		}
		
		public AdaptUser setType(AdaptType type) {
			this.type = type;
			return this;
		}
		
	}

}
