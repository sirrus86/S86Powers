package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.List;
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
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Adaptability", type = PowerType.DEFENSE, author = "sirrus86", concept = "diamondmario", icon = Material.LEATHER_CHESTPLATE,
	description = "When damaged, develop resistance to that damage type, reducing all following damage of the same type starting at [initial-adapt]%,"
			+ " increasing up to [maximum-adapt]%. While resistant to one type, damage from all other types increased to [damage-increase]%."
			+ " [adapt-cooldown] cooldown.[prevent-armor] Power prevents you from wearing armor.[/prevent-armor]")
public final class Adaptability extends Power {

	private Map<PowerUser, AdaptUser> aUsers;
	
	private double incrAmt;
	private PowerOption<List<String>> explosionTypes, fireTypes, iceTypes, lightningTypes, magicTypes, natureTypes, physicalTypes, poisonTypes, projectileTypes,
				shadowTypes, waterTypes;
	private PowerOption<Boolean> noArmor;
	private PowerOption<Double> dmgIncr, initAmt, maxAmt;
	private PowerOption<Integer> steps;
	private String nowAdapting, resistIncrease, preventArmor;
	
	@Override
	protected void onEnable() {
		aUsers = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		aUsers.remove(user);
	}

	@Override
	protected void config() {
		cooldown = option("adapt-cooldown", PowerTime.toMillis(3, 0), "Minimum time between incoming damage that user may further adapt.");
		dmgIncr = option("damage-increase", 200.0D, "Percentage of damage done from adaptable sources that aren't being adapted to.");
		explosionTypes = option("damage-types.explosion", List.of("BLOCK_EXPLOSION", "ENTITY_EXPLOSION"), "Damage causes which should attribute to explosion resistance.");
		fireTypes = option("damage-types.fire", List.of("FIRE", "FIRE_TICK", "HOT_FLOOR", "LAVA"), "Damage causes which should attribute to fire resistance.");
		iceTypes = option("damage-types.ice", List.of("FREEZE"), "Damage causes which should attribute to ice resistance.");
		initAmt = option("initial-adapt", 25.0D, "Initial percent of damage mitigation after user changes adapt type.");
		lightningTypes = option("damage-types.lightning", List.of("LIGHTNING"), "Damage causes which should attribute to lightning resistance.");
		magicTypes = option("damage-types.magic", List.of("MAGIC"), "Damage causes which should attribute to magic resistance.");
		maxAmt = option("maximum-adapt", 100.0D, "Maximum percent of damage mitigation for a given adapt type.");
		natureTypes = option("damage-types.nature", List.of("CONTACT", "THORNS"), "Damage causes which should attribute to nature resistance.");
		noArmor = option("prevent-armor", true, "Prevents users from wearing armor.");
		physicalTypes = option("damage-types.physical", List.of("ENTITY_ATTACK", "ENTITY_SWEEP_ATTACK", "FALLING_BLOCK"), "Damage causes which should attribute to physical resistance.");
		poisonTypes = option("damage-types.poison", List.of("POISON", "WITHER"), "Damage causes which should attribute to poison resistance.");
		projectileTypes = option("damage-types.projectile", List.of("PROJECTILE"), "Damage causes which should attribute to projectile resistance.");
		shadowTypes = option("damage-types.shadow", List.of("DRAGON_BREATH"), "Damage causes which should attribute to shadow resistance.");
		steps = option("adapt-increment-steps", 10, "Number of increments it takes to reach maximum adapt from initial.");
		nowAdapting = locale("message.now-adapting", ChatColor.YELLOW + "Now adapting to [type] damage.");
		preventArmor = locale("message.prevents-armor", ChatColor.RED + "Your power prevents you from wearing armor.");
		resistIncrease = locale("message.resistance-increase", ChatColor.YELLOW + "Resistance to [type] increased to [amount]%.");
	}
	
	private double adapt(PowerUser user, AdaptType type) {
		double amt = 1.0D;
		if (aUsers.get(user).getType() != type) {
			amt = user.getOption(dmgIncr);
			if (user.getCooldown(this) <= 0L) {
				user.sendMessage(nowAdapting.replace("[type]", type.name().toLowerCase()));
				aUsers.get(user).setType(type).setAmount(user.getOption(initAmt));
				user.setCooldown(this, user.getOption(cooldown));
			}
		}
		else {
			amt = aUsers.get(user).getAmount();
			if (user.getCooldown(this) <= 0L
					&& aUsers.get(user).getAmount() < user.getOption(maxAmt)) {
				incrAmt = (user.getOption(maxAmt) - user.getOption(initAmt)) / user.getOption(steps);
				aUsers.get(user).increaseAmount(incrAmt);
				user.sendMessage(resistIncrease.replace("[type]", type.name().toLowerCase()).replace("[amount]", Double.toString(aUsers.get(user).getAmount())));
				user.setCooldown(this, user.getOption(cooldown));
			}
		}
		return (amt <= 100.0D ? (100.0D - amt) : amt) / 100.0D;
	}
	
	private AdaptType getAdaptType(PowerUser user, DamageCause cause) {
		if (user.getOption(explosionTypes).contains(cause.name())) {
			return AdaptType.EXPLOSION;
		}
		else if (user.getOption(fireTypes).contains(cause.name())) {
			return AdaptType.FIRE;
		}
		else if (user.getOption(iceTypes).contains(cause.name())) {
			return AdaptType.ICE;
		}
		else if (user.getOption(lightningTypes).contains(cause.name())) {
			return AdaptType.LIGHTNING;
		}
		else if (user.getOption(magicTypes).contains(cause.name())) {
			return AdaptType.MAGIC;
		}
		else if (user.getOption(natureTypes).contains(cause.name())) {
			return AdaptType.NATURE;
		}
		else if (user.getOption(physicalTypes).contains(cause.name())) {
			return AdaptType.PHYSICAL;
		}
		else if (user.getOption(poisonTypes).contains(cause.name())) {
			return AdaptType.POISON;
		}
		else if (user.getOption(projectileTypes).contains(cause.name())) {
			return AdaptType.PROJECTILE;
		}
		else if (user.getOption(shadowTypes).contains(cause.name())) {
			return AdaptType.SHADOW;
		}
		else if (user.getOption(waterTypes).contains(cause.name())) {
			return AdaptType.WATER;
		}
		else {
			return null;
		}
	}
	
	@EventHandler
	private void onClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			PowerUser user = getUser((Player) event.getPlayer());
			if (user.allowPower(this)
					&& user.getOption(noArmor)
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
					user.sendMessage(preventArmor);
				}
				user.getPlayer().getInventory().setArmorContents(null);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player) event.getEntity());
			AdaptType type = getAdaptType(user, event.getCause());
			if (user.allowPower(this)
					&& type != null) {
				if (!aUsers.containsKey(user)) {
					aUsers.put(user, new AdaptUser(null, 0.0D));
				}
				double dmg = adapt(user, type);
				event.setDamage(event.getDamage() * dmg);
			}
		}
	}
	
	private enum AdaptType {
		
		EXPLOSION, FIRE, ICE, LIGHTNING, MAGIC, NATURE, PHYSICAL, POISON, PROJECTILE, SHADOW, WATER;
				
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
