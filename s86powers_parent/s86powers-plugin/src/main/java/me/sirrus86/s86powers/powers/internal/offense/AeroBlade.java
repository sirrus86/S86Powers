package me.sirrus86.s86powers.powers.internal.offense;

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

@PowerManifest(name = "Aero Blade", type = PowerType.OFFENSE, author = "sirrus86", concept = "repete8", icon = Material.IRON_SWORD, usesPackets = true,
//	description = "[wItem][act:item]ing while holding [item][/wItem][wMany] or [/wMany][wSword]swinging any sword[/wSword] will create a gust of wind in front of you, propelling entities away from you.")
	description = "[use-with-specific-item][act:item]ing while holding [item][/use-with-specific-item][wMany] or [/wMany][use-any-sword]swinging any sword[/use-any-sword] will create a gust of wind in"
			+ " front of you, propelling entities away from you.")
public final class AeroBlade extends Power {

	private Map<Snowball, PowerUser> sList;
	
	private PowerOption<Double> fSpread, fVel;
	@SuppressWarnings("unused")
	private boolean wMany;
	
	@Override
	protected void onEnable() {
		sList = new WeakHashMap<>();
	}
	
	@Override
	protected void config() {
		cooldown = option("cooldown", PowerTime.toMillis(1, 0), "Amount of time before power can be used again.");
		fSpread = option("feather-spread", 0.1D, "Spread of feathers. Higher values create greater spreads.");
		fVel = option("feather-velocity", 2.0D, "Velocity of feathers.");
		item = option("item", new ItemStack(Material.IRON_SWORD), "Item used to trigger power.");
		wItem = option("use-with-specific-item", false, "Whether power will work with the specified item.");
		wSword = option("use-any-sword", true, "Whether power will work with any sword.");
		wMany = getOption(wItem) && getOption(wSword);
		supplies(getRequiredItem());
	}
	
	@EventHandler
	private void onDmg(EntityDamageByEntityEvent event) {
		if (sList.containsKey(event.getDamager())) {
			Snowball snowball = (Snowball) event.getDamager();
			event.getEntity().setVelocity(snowball.getVelocity());
			sList.remove(snowball);
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.getCooldown(this) <= 0) {
				user.getPlayer().getWorld().playEffect(user.getPlayer().getEyeLocation(), Effect.BLAZE_SHOOT, 0);
				for (int i = 0; i < 10; i ++) {
					Snowball snowball = user.getPlayer().launchProjectile(Snowball.class);
					Vector sVel = snowball.getVelocity().clone();
					snowball.setVelocity(sVel.add(new Vector(random.nextGaussian() * user.getOption(fSpread), 0, random.nextGaussian() * user.getOption(fSpread))).multiply(user.getOption(fVel)));
					PowerTools.addDisguise(snowball, new ItemStack(Material.FEATHER, 1));
					sList.put(snowball, user);
				}
				user.setCooldown(this, user.getOption(cooldown));
			}
		}
	}

}
