package me.sirrus86.s86powers.powers.internal.defense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.sirrus86.s86powers.events.PowerUseOnEntityEvent;
import me.sirrus86.s86powers.events.UserMaxedStatEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Arachnophile", type = PowerType.DEFENSE, author = "sirrus86", concept = "vashvhexx", icon = Material.COBWEB, usesPackets = true,
	description = "Hostile spiders, cave spiders, silverfish, and endermites will no longer attack you[poison-immunity], you become immune to poison[/poison-immunity],"
			+ " and fall damage is reduced by [fall-damage-reduction]%. [act:item]ing a spider while holding [item] will allow you to tame it. Tamed spiders will follow and defend you.")
public final class Arachnophile extends Power {

	private PowerOption<Double> fallRed;
	private PowerOption<List<String>> ignoreTypes;
	private PowerOption<Boolean> noPoison;
	private Map<PowerUser, TamedSpider> spiders;
	private PowerStat spiderDmg;
	private PowerOption<Long> webCooldown;
	private PowerOption<List<PotionEffect>> webEffects;
	private PowerOption<ItemStack> webItem;
	
	@Override
	protected void onEnable() {
		spiders = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (spiders.containsKey(user)) {
			spiders.get(user).unTame();
			spiders.remove(user);
		}
	}

	@Override
	protected void config() {
		fallRed = option("fall-damage-reduction", 50.0D, "Percentage of fall damage to negate.");
		ignoreTypes = option("ignoring-entity-types", List.of("CAVE_SPIDER", "ENDERMITE", "SILVERFISH", "SPIDER"), "Entity types which will ignore users.");
		item = option("item", new ItemStack(Material.STICK), "Item used to tame and direct spiders.");
		noPoison = option("poison-immunity", true, "Whether user should be immune to poison.");
		spiderDmg = stat("damage-by-tamed-spiders", 50, "Damage caused by tamed spiders", "Tamed spiders will occassionally shoot webs at targets, slowing them down.");
		webCooldown = option("web-cooldown", PowerTime.toMillis(5, 0), "Amount of time between webs shot by tamed spiders.");
		webEffects = option("web-effects", List.of(new PotionEffect(PotionEffectType.SLOW, (int) PowerTime.toMillis(3, 0), 1)), "Effects caused when spider webs hit targets.");
		webItem = option("web-item", new ItemStack(Material.COBWEB, 1), "Item used to show the projectile shot by tamed spiders.");
		supplies(new ItemStack(getRequiredItem().getType(), 1));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player)event.getEntity());
			if (user.allowPower(this)) {
				if (event.getCause() == DamageCause.FALL) {
					event.setDamage(event.getDamage() * (1.0D - (user.getOption(fallRed) / 100.0D)));
				}
				else if (event.getCause() == DamageCause.POISON
						&& user.getOption(noPoison)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void noTarget(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() instanceof Player) {
			PowerUser user = getUser((Player) event.getTarget());
			if (user.allowPower(this)
					&& user.getOption(ignoreTypes).contains(event.getEntityType().name())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseOnEntityEvent event) {
		if (event.getPower() == this
				&& event.getEntity() instanceof Spider
				&& !(event.getEntity() instanceof CaveSpider)) {
			PowerUser user = event.getUser();
			if (spiders.containsKey(user)) {
				spiders.get(user).unTame();
			}
			TamedSpider spider = new TamedSpider(user, (Spider) event.getEntity());
			spiders.put(user, spider);
		}
	}
	
	private class TamedSpider implements Listener {
		
		private final PowerUser owner;
		private final Spider spider;
		private int task = -1;
		private final List<Snowball> webs = new ArrayList<>();
		
		public TamedSpider(PowerUser owner, Spider spider) {
			getInstance().registerEvents(this);
			this.owner = owner;
			this.spider = spider;
			PowerTools.setTamed(spider, owner);
			spider.playEffect(EntityEffect.WOLF_HEARTS);
			if (owner.hasStatMaxed(spiderDmg)) {
				task = getInstance().runTask(shootWeb).getTaskId();
			}
		}
		
		private final Runnable shootWeb = new Runnable() {

			@Override
			public void run() {
				if (spider.getTarget() != null) {
					Snowball web = spider.launchProjectile(Snowball.class);
					PowerTools.addDisguise(web, owner.getOption(webItem));
					webs.add(web);
				}
				task = getInstance().runTaskLater(shootWeb, PowerTime.toTicks(owner.getOption(webCooldown))).getTaskId();
			}

		};
		
		public void unTame() {
			PowerTools.setTamed(spider, null);
			if (task >= 0) {
				cancelTask(task);
			}
			unregisterEvents(this);
		}
		
		@EventHandler(ignoreCancelled = true)
		private void onDamage(EntityDamageByEntityEvent event) {
			if (event.getDamager() == this.spider) {
				owner.increaseStat(spiderDmg, (int) event.getDamage());
			}
			else if (event.getEntity() instanceof Snowball
					&& webs.contains((Snowball) event.getEntity())) {
				event.setCancelled(true);
			}
		}
		
		@EventHandler
		private void onDeath(EntityDeathEvent event) {
			if (event.getEntity() == this.spider) {
				spiders.remove(this.owner);
			}
		}
		
		@EventHandler
		private void onHit(ProjectileHitEvent event) {
			if (event.getEntity() instanceof Snowball
					&& webs.contains((Snowball) event.getEntity())) {
				if (event.getHitEntity() != null
						&& event.getHitEntity() instanceof LivingEntity entity) {
					entity.addPotionEffects(owner.getOption(webEffects));
				}
				webs.remove((Snowball) event.getEntity());
			}
		}
		
		@EventHandler
		private void onStatMax(UserMaxedStatEvent event) {
			if (event.getUser() == owner
					&& event.getStat() == spiderDmg) {
				task = getInstance().runTask(shootWeb).getTaskId();
			}
		}
		
	}

}
