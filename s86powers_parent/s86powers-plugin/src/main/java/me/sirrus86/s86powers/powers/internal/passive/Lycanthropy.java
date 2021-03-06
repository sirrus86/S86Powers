package me.sirrus86.s86powers.powers.internal.passive;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCMetadata;
import me.sirrus86.s86powers.tools.version.MCMetadata.EntityMeta;
//import me.sirrus86.s86powers.tools.version.MCMetadata;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Lycanthropy", type = PowerType.PASSIVE, author = "sirrus86", concept = "vashvhexx", icon = Material.RABBIT_HIDE, usesPackets = true,
	description = "[control-transformation]By [act:item]ing while holding [item][/control-transformation][noControl]At night during a full moon [/noControl]you change into a wolf."
			+ " As a wolf[increase-speed] sprinting speed increases,[/increase-speed][night-vision] you gain night vision,[/night-vision]"
			+ "[either] and[/either] unarmed damage increases by [damage-multiplier]%, but you take [iron-multiplier]% damage from iron tools and weapons,"
			+ " and are unable to wear any armor.[noControl] Effect ends at sunrise.[/noControl]")
public final class Lycanthropy extends Power {

	private Set<PowerUser> isWolf;
	
	private final MCMetadata angryMeta = new MCMetadata();
	
	private PowerOption<Double> dmgIncr, infectChance, ironDmg;
	private PowerOption<Boolean> control, infect, nv, speed;
	private PowerOption<Integer> moonEnd, moonStart, spdIncr;
	private PowerStat transforms;
	private String infected, noArmor, turnToHuman, turnToWolf;
	@SuppressWarnings("unused")
	private boolean either, noControl;
	
	@Override
	protected void onEnable() {
		angryMeta.setEntry(EntityMeta.TAMEABLE_STATE, (byte) 0x02);
		isWolf = new HashSet<>();
		runTaskTimer(manage, 0L, 0L);
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (isWolf.contains(user)) {
			revert(user);
		}
	}

	@Override
	protected void config() {
		control = option("control-transformation", false, "Whether users should always be able to willfully transform.");
		dmgIncr = option("damage-multiplier", 400.0D, "Percentage increase of damage done while transformed and barehanded.");
		infect = option("infect-other-players", false, "Whether barehanded attacks from werewolves should infect players without the power.");
		infectChance = option("infect-chance", 15.0D, "Percent chance that other players will be infected.");
		ironDmg = option("iron-multiplier", 200.0D, "Percent of damage done by iron weapons against transformed werewolves.");
		item = option("item", new ItemStack(Material.ROTTEN_FLESH), "Item used to manually transform into a werewolf.");
		moonEnd = option("full-moon-end", 22000, "Time (in game ticks) moon sets.");
		moonStart = option("full-moon-start", 13000, "Time (in game ticks) when the moon rises.");
		nv = option("night-vision", true, "Whether users should get night vision while transformed.");
		spdIncr = option("speed-amplifier", 3, "Amplifier for increased speed potion effect.");
		speed = option("increase-speed", true, "Whether speed should increase while transformed.");
		transforms = stat("transformations", 10, "Transformations", "[act:item]ing while holding [item] at night allows you to transform into a werewolf at will.");
		infected = locale("message.been-infected", ChatColor.RED + "You've been infected with [power]!");
		noArmor = locale("message.cant-wear-armor", ChatColor.RED + "Your power prevents you from wearing armor.");
		turnToHuman = locale("message.turn-to-human", ChatColor.YELLOW + "You return to human form.");
		turnToWolf = locale("message.turn-to-wolf", ChatColor.GREEN + "You transform into a wolf.");
		noControl = !getOption(control);
		either = getOption(nv) || getOption(speed);
	}
	
	private Runnable manage = new BukkitRunnable() {
		@Override
		public void run() {
			for (PowerUser user : getUsers()) {
				if (user.allowPower(getInstance())) {
					if (isWolf.contains(user)) {
						if (user.isOnline()
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
								user.sendMessage(noArmor);
							}
							user.getPlayer().getInventory().setArmorContents(null);
						}
						if (user.isOnline()
								&& !isFullMoon(user.getPlayer().getWorld())
								&& !canControl(user)) {
							revert(user);
						}
					}
					else if (user.isOnline()
								&& isFullMoon(user.getPlayer().getWorld())
								&& !canControl(user)) {
						transform(user);
					}
				}
			}
		}
	};
	
	private boolean canControl(PowerUser user) {
		return user.getOption(control)
				|| user.hasStatMaxed(transforms);
	}
	
	private boolean isFullMoon(World world) {
		if (world.getEnvironment() == Environment.NORMAL) {
			double days = world.getFullTime() / 24000L;
			int phase = (int) (days % 8);
			return phase == 0
					&& world.getTime() < getOption(moonEnd)
					&& world.getTime() > getOption(moonStart);
		}
		return false;
	}
	
	private void revert(PowerUser user) {
		if (user.isOnline()) {
			user.removePotionEffect(PotionEffectType.NIGHT_VISION);
			user.removePotionEffect(PotionEffectType.SPEED);
			user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
			PowerTools.removeDisguise(user.getPlayer());
			user.sendMessage(turnToHuman);
			isWolf.remove(user);
		}
	}
	
	private void transform(PowerUser user) {
		if (user.isOnline()) {
			if (user.getOption(nv)) {
				user.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Short.MAX_VALUE, 0));
			}
			user.getPlayer().getWorld().playSound(user.getPlayer().getEyeLocation(), Sound.ENTITY_WOLF_HOWL, 1.0F, 1.0F);
			user.getPlayer().playEffect(EntityEffect.ENTITY_POOF);
			PowerTools.addDisguise(user.getPlayer(), EntityType.WOLF, angryMeta);
			user.sendMessage(turnToWolf);
			if (noControl) {
				user.increaseStat(transforms, 1);
			}
			isWolf.add(user);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player
				&& event.getDamager() instanceof LivingEntity) {
			PowerUser user = getUser((Player) event.getEntity());
			LivingEntity attacker = (LivingEntity) event.getDamager();
			if (isWolf.contains(user)
					&& attacker.getEquipment() != null
					&& attacker.getEquipment().getItemInMainHand() != null
					&& attacker.getEquipment().getItemInMainHand().getType().toString().startsWith("IRON_")) {
				event.setDamage(event.getDamage() * (user.getOption(ironDmg) / 100));
			}
		}
		if (event.getDamager() instanceof Player) {
			PowerUser user = getUser((Player) event.getDamager());
			if (user.allowPower(this)
					&& isWolf.contains(user)
					&& user.getEquipment(EquipmentSlot.HAND).getType() == Material.AIR) {
				event.setDamage(event.getDamage() * (user.getOption(dmgIncr) / 100));
				PowerTools.playParticleEffect(event.getEntity().getLocation(), Particle.CRIT, 5);
				if (event.getEntity() instanceof Player
						&& user.getOption(infect)) {
					PowerUser victim = getUser((Player) event.getEntity());
					if (!victim.hasPower(this)
							&& !victim.hasPower("Vampirism")
							&& random.nextDouble() < (user.getOption(infectChance) / 100.0D)) {
						victim.sendMessage(infected.replace("[power]", this.getName()));
						victim.addPower(this, true);
					}
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onSprint(PlayerToggleSprintEvent event) {
		PowerUser user = getUser(event.getPlayer());
		if (user.allowPower(this)
				&& isWolf.contains(user)) {
			if (event.isSprinting()) {
				if (user.getOption(speed)) {
					user.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Short.MAX_VALUE, user.getOption(spdIncr)));
				}
			}
			else {
				if (user.getOption(speed)) {
					user.getPlayer().removePotionEffect(PotionEffectType.SPEED);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (canControl(user)) {
				if (isWolf.contains(user)) {
					revert(user);
				}
				else {
					transform(user);
				}
			}
		}
	}

}
