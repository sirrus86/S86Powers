package me.sirrus86.s86powers.powers.internal.defense;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vindicator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Diversion", type = PowerType.DEFENSE, author = "sirrus86", concept = "blacknova777", icon=Material.ARMOR_STAND,
	description = "Upon taking damage from another entity, become invisible while summoning an exact copy of yourself as a diversion to attack the damager. Remain invisible until the diversion dies or despawns [lifespan] later. [cooldown] cooldown.")
public class Diversion extends Power {

	private Map<PowerUser, Set<Decoy>> decoys;
	
	private PowerStat decoysMade;
	private long lifespan;
	private int maxDecoys;
	
	@Override
	protected void onEnable() {
		decoys = new HashMap<>();
	}
	
	@Override
	protected void onDisable(PowerUser user) {
		if (decoys.get(user) != null) {
			for (Decoy decoy : decoys.get(user)) {
				decoy.kill();
			}
		}
	}

	@Override
	protected void options() {
		cooldown = option("cooldown", PowerTime.toMillis(1, 0, 0), "How long before another diversion can be created.");
		decoysMade = stat("diversions-made", 30, "Diversions created", "You now create [maxDecoys] diversions when damaged instead of 1.");
		lifespan = option("diversion-lifespan", PowerTime.toMillis(10, 0), "How long diversions last before despawning.");
		maxDecoys = option("maximum-diversions", 3, "Number of diversions created when player has fulfilled stat.");
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onDamage(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player
				&& event.getDamage() > 0.0D) {
			PowerUser user = getUser((Player) event.getEntity());
			if (user.allowPower(this)
					&& user.getCooldown(this) <= 0L) {
				LivingEntity damager = PowerTools.getEntitySource(event.getDamager());
				if (damager != null) {
					if (!decoys.containsKey(user)
							|| decoys.get(user) == null) {
						decoys.put(user, new HashSet<>());
					}
					if (decoys.get(user).size() < (user.hasStatMaxed(decoysMade) ? maxDecoys : 1)) {
						for (int i = 0; i < (user.hasStatMaxed(decoysMade) ? maxDecoys : 1); i ++) {
							Vindicator pz = user.getPlayer().getWorld().spawn(user.getPlayer().getLocation(), Vindicator.class);
							PowerTools.copyEquipment(user.getPlayer(), pz);
							runTask(new BukkitRunnable() {
								@Override
								public void run() {
									PowerTools.addDisguise(pz, user.getPlayer());
									PowerTools.addGhost(user.getPlayer());
								}
							});
							if (damager instanceof Mob) {
								((Mob) damager).setTarget(pz);
							}
							pz.setSilent(true);
							pz.setTarget(damager);
							Decoy decoy = new Decoy(user, pz);
							decoys.get(user).add(decoy);
							user.increaseStat(decoysMade, 1);
						}
					}
				}
			}
		}
	}
	
	private class Decoy implements Listener {
		
		private PowerUser owner;
		private int timer;
		private Vindicator zombie;
		
		public Decoy(PowerUser owner, Vindicator zombie) {
			this.owner = owner;
			this.zombie = zombie;
			registerEvents(this);
			timer = runTaskLater(new BukkitRunnable() {

				@Override
				public void run() {
					kill();
				}
				
			}, PowerTime.toTicks(lifespan)).getTaskId();
		}
		
		public void kill() {
			if (zombie != null
					&& zombie.isValid()
					&& !zombie.isDead()) {
				zombie.damage(zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}
			if (this.owner != null
					&& PowerTools.isGhost(this.owner.getPlayer())) {
				PowerTools.removeGhost(this.owner.getPlayer());
			}
			decoys.get(owner).remove(this);
			unregisterEvents(this);
			cancelTask(timer);
		}
		
		@EventHandler
		private void onDeath(EntityDeathEvent event) {
			if (event.getEntity() == this.zombie) {
				event.getDrops().clear();
				event.setDroppedExp(0);
				kill();
			}
		}
		
		@EventHandler (ignoreCancelled = true)
		private void onTarget(EntityTargetEvent event) {
			if (event.getEntity() == this.zombie
					&& event.getTarget() == this.owner.getPlayer()) {
				event.setCancelled(true);
			}
		}
		
	}

}
