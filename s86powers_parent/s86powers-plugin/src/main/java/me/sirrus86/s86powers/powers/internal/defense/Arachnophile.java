package me.sirrus86.s86powers.powers.internal.defense;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
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
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.events.PowerUseOnEntityEvent;
import me.sirrus86.s86powers.events.UserMaxedStatEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Arachnophile", type = PowerType.DEFENSE, author = "sirrus86", concept = "vashvhexx", icon = Material.COBWEB, usesPackets = true,
	description = "Hostile spiders, cave spiders, silverfish, and endermites will no longer attack you[noPoison], you become immune to poison[/noPoison], and fall damage is reduced by [fallRed]%. [act:item]ing a spider while holding [item] will allow you to tame it. Tamed spiders will follow and defend you.")
public final class Arachnophile extends Power {

	private double fallRed;
	private boolean noPoison;
	private Map<PowerUser, TamedSpider> spiders;
	private PowerStat spiderDmg;
	private long webCooldown, webDur;
	private final ItemStack webItem = new ItemStack(Material.COBWEB, 1);
	
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
	protected void options() {
		fallRed = option("fall-damage-reduction", 50.0D, "Percentage of fall damage to negate.");
		item = option("item", new ItemStack(Material.STICK), "Item used to tame and direct spiders.");
		noPoison = option("poison-immunity", true, "Whether user should be immune to poison.");
		spiderDmg = stat("damage-by-tamed-spiders", 50, "Damage caused by tamed spiders", "Tamed spiders will occassionally shoot webs at targets, slowing them down.");
		webCooldown = option("web-cooldown", PowerTime.toMillis(5, 0), "Amount of time between webs shot by tamed spiders.");
		webDur = option("web-duration", PowerTime.toMillis(3, 0), "How long webs should slow down targets.");
		supplies(new ItemStack(item.getType(), 1));
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onDmg(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			PowerUser user = getUser((Player)event.getEntity());
			if (user.allowPower(this)) {
				if (event.getCause() == DamageCause.FALL) {
					event.setDamage(event.getDamage() * (1.0D - (fallRed / 100.0D)));
				}
				else if (event.getCause() == DamageCause.POISON
						&& noPoison) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void noTarget(EntityTargetLivingEntityEvent event) {
		if (event.getTarget() instanceof Player
				&& (event.getEntity() instanceof Spider
						|| event.getEntity() instanceof Silverfish
						|| event.getEntity() instanceof Endermite)) {
			PowerUser user = getUser((Player) event.getTarget());
			if (user.allowPower(this)) {
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
		private List<Snowball> webs = new ArrayList<Snowball>();
		
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
		
		private Runnable shootWeb = new BukkitRunnable() {

			@Override
			public void run() {
				if (spider.getTarget() != null) {
					Snowball web = spider.launchProjectile(Snowball.class);
					PowerTools.addDisguise(web, webItem);
					webs.add(web);
				}
				task = getInstance().runTaskLater(shootWeb, PowerTime.toTicks(webCooldown)).getTaskId();
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
			else if (webs.contains(event.getEntity())) {
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
			if (webs.contains(event.getEntity())) {
				if (event.getHitEntity() != null
						&& event.getHitEntity() instanceof LivingEntity) {
					LivingEntity entity = (LivingEntity) event.getHitEntity();
					entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) PowerTime.toTicks(webDur), 1), true);
				}
				webs.remove(event.getEntity());
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
