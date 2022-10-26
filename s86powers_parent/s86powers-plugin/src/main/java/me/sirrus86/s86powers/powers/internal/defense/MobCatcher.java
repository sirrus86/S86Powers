package me.sirrus86.s86powers.powers.internal.defense;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Steerable;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Colorable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.sirrus86.s86powers.events.PowerUseEvent;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerManifest;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.powers.PowerStat;
import me.sirrus86.s86powers.powers.PowerType;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.users.PowerUser;

@PowerManifest(name = "Mob Catcher", type = PowerType.DEFENSE, author = "sirrus86", concept = "kamyarm007", version = MCVersion.v1_14, icon = Material.ZOMBIE_SPAWN_EGG, usesPackets = true,
	description = "[act:item]ing while holding [item] throws it. If it hits a capturable entity, it will be stored in the [item] which is returned to you."
			+ " [act:item]ing a [item] with a stored entity will thorw it, causing it to expel the stored entity on contact.")
public final class MobCatcher extends Power {

	private final Set<EntityType> capturable = EnumSet.of(EntityType.BAT, EntityType.BLAZE, EntityType.CAT, EntityType.CAVE_SPIDER, EntityType.CHICKEN,
			EntityType.COW, EntityType.CREEPER, EntityType.DOLPHIN, EntityType.DROWNED, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.FOX,
			EntityType.GHAST, EntityType.GUARDIAN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.MUSHROOM_COW, EntityType.OCELOT, EntityType.PANDA,
			EntityType.PARROT, EntityType.PHANTOM, EntityType.PIG, PowerTools.resolveEntityType("PIGZOMBIE"), EntityType.POLAR_BEAR, EntityType.RABBIT, EntityType.SHEEP,
			EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.SQUID, EntityType.STRAY,
			EntityType.TURTLE, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.WOLF, EntityType.ZOMBIE);
	
	private final NamespacedKey ageableAge = createNamespacedKey("ageable-age"),
			axolotlVariant = createNamespacedKey("axolotl-variant"),
			beeHasNectar = createNamespacedKey("bee-has-nectar"),
			beeHasStung = createNamespacedKey("bee-has-stung"),
			catType = createNamespacedKey("cat-type"),
			collarColor = createNamespacedKey("collar-color"),
			colorableColor = createNamespacedKey("colorable-color"),
			creeperPowered = createNamespacedKey("creeper-powered"),
			customName = createNamespacedKey("custom-name"),
			entityHealth = createNamespacedKey("entity-health"),
			entityType = createNamespacedKey("entity-type"),
			foxType = createNamespacedKey("fox-type"),
			goatScreaming = createNamespacedKey("goat-screaming"),
			mooshroomVariant = createNamespacedKey("mooshroom-variant"),
			ownerUUID = createNamespacedKey("owner-uuid"),
			pandaHiddenGene = createNamespacedKey("panda-hidden-gene"),
			pandaMainGene = createNamespacedKey("panda-main-gene"),
			parrotVariant = createNamespacedKey("parrot-variant"),
			phantomSize = createNamespacedKey("phantom-size"),
			rabbitType = createNamespacedKey("rabbit-type"),
			sheepSheared = createNamespacedKey("sheep-sheared"),
			slimeSize = createNamespacedKey("slime-size"),
			steerableSaddle = createNamespacedKey("steerable-saddle");
	
	private Set<EntityType> allowCapture;
	private Map<Snowball, ItemStack> eggs;
	private Map<Snowball, PowerUser> eggOwners;
	
	private String cantCapTamed;
	private PowerOption<Boolean> captureTamed;
	private PowerStat eggsThrown;
	
	@Override
	protected void onEnable() {
		allowCapture = EnumSet.noneOf(EntityType.class);
		eggOwners = new HashMap<>();
		eggs = new HashMap<>();
	}

	@Override
	protected void config() {
		captureTamed = option("capture-tamed", false, "Whether entities tamed by other players (wolves, cats, etc) can be captured.");
		eggsThrown = stat("eggs-thrown", 50, "Entities relocated", "[item] is now refunded after expelling a stored entity.");
		item = option("item", new ItemStack(Material.ENDER_EYE, 1), "Item used to catch and store mobs.");
		for (EntityType eType : capturable) {
			PowerOption<Boolean> capturable = option("capturable." + eType.toString().replaceAll("_", "-").toLowerCase(), true, "Whether " + WordUtils.capitalizeFully(eType.toString().replaceAll("_", " ")) + " should be capturable.");
			if (getOption(capturable)) {
				allowCapture.add(eType);
			}
		}
		cantCapTamed = locale("message.cant-capture-tamed", ChatColor.RED + "You can't capture entities tamed by other players.");
		supplies(new ItemStack(getRequiredItem().getType(), getRequiredItem().getMaxStackSize()));
	}
	
	private ItemStack createEgg(LivingEntity entity) {
		ItemStack egg = new ItemStack(getRequiredItem().getType(), 1);
		ItemMeta meta = egg.hasItemMeta() ? getRequiredItem().getItemMeta() : entity.getServer().getItemFactory().getItemMeta(getRequiredItem().getType());
		PersistentDataContainer iData = meta.getPersistentDataContainer();
		List<String> stats = new ArrayList<String>();
		iData.set(entityType, PersistentDataType.STRING, entity.getType().toString());
		iData.set(entityHealth, PersistentDataType.DOUBLE, entity.getHealth());
		stats.add("Health: " + entity.getHealth() + "/" + entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		if (entity instanceof Ageable) {
			if (!((Ageable)entity).isAdult()) {
				stats.add("Juvenile");
			}
			iData.set(ageableAge, PersistentDataType.INTEGER, ((Ageable) entity).getAge());
		}
		if (entity instanceof Tameable) {
			if (((Tameable)entity).isTamed()) {
				iData.set(ownerUUID, PersistentDataType.STRING, ((Tameable)entity).getOwner().getUniqueId().toString());
			}
		}
		switch (entity.getType().name()) {
			case "AXOLOTL": {
				stats.add("Type: " + WordUtils.capitalize(((Axolotl)entity).getVariant().toString().replace("_", " ").toLowerCase()));
				iData.set(axolotlVariant, PersistentDataType.INTEGER, ((Axolotl)entity).getVariant().ordinal());
				break;
			}
			case "BEE": {
				iData.set(beeHasNectar, PersistentDataType.BYTE, ((Bee)entity).hasNectar() ? (byte) 1 : (byte) 0);
				iData.set(beeHasStung, PersistentDataType.BYTE, ((Bee)entity).hasStung() ? (byte) 1 : (byte) 0);
				break;
			}
			case "CAT": {
				stats.add("Type: " + WordUtils.capitalize(((Cat)entity).getCatType().toString().replace("_", " ").toLowerCase()));
				iData.set(catType, PersistentDataType.STRING, ((Cat)entity).getCatType().toString());
				iData.set(collarColor, PersistentDataType.STRING, ((Cat)entity).getCollarColor().toString());
				break;
			}
			case "CREEPER": {
				if (((Creeper)entity).isPowered()) {
					stats.add("Powered");
				}
				iData.set(creeperPowered, PersistentDataType.BYTE, ((Creeper)entity).isPowered() ? (byte) 1 : (byte) 0);
				break;
			}
			case "FOX": {
				stats.add("Type: " + WordUtils.capitalize(((Fox)entity).getFoxType().toString().replace("_", " ").toLowerCase()));
				iData.set(foxType, PersistentDataType.STRING, ((Fox)entity).getFoxType().toString());
				break;
			}
			case "GOAT": {
				iData.set(goatScreaming, PersistentDataType.BYTE, ((Goat)entity).isScreaming() ? (byte) 1 : (byte) 0);
				break;
			}
			case "MAGMA_CUBE": case "SLIME": {
				stats.add("Size: " + ((Slime)entity).getSize());
				iData.set(slimeSize, PersistentDataType.INTEGER, ((Slime)entity).getSize());
				break;
			}
			case "MUSHROOM_COW": {
				stats.add("Color: " + WordUtils.capitalize(((MushroomCow)entity).getVariant().toString().replace("_", " ").toLowerCase()));
				iData.set(foxType, PersistentDataType.STRING, ((MushroomCow)entity).getVariant().toString());
				break;
			}
			case "PANDA": {
				iData.set(pandaHiddenGene, PersistentDataType.STRING, ((Panda)entity).getHiddenGene().toString());
				iData.set(pandaMainGene, PersistentDataType.STRING, ((Panda)entity).getMainGene().toString());
				break;
			}
			case "PARROT": {
				stats.add("Color: " + WordUtils.capitalize(((Parrot)entity).getVariant().toString().replace("_", " ").toLowerCase()));
				iData.set(parrotVariant, PersistentDataType.STRING, ((Parrot)entity).getVariant().toString());
				break;
			}
			case "PHANTOM": {
				iData.set(phantomSize, PersistentDataType.INTEGER, ((Phantom)entity).getSize());
				break;
			}
			case "PIG": {
				if (((Pig)entity).hasSaddle()) {
					stats.add("Saddled");
				}
				iData.set(steerableSaddle, PersistentDataType.BYTE, ((Pig)entity).hasSaddle() ? (byte) 1 : (byte) 0);
				break;
			}
			case "RABBIT": {
				stats.add("Type: " + WordUtils.capitalize(((Rabbit)entity).getRabbitType().toString().replace("_", " ").toLowerCase()));
				iData.set(rabbitType, PersistentDataType.STRING, ((Rabbit)entity).getRabbitType().toString());
				break;
			}
			case "SHEEP": {
				stats.add("Color: " + WordUtils.capitalize(((Sheep)entity).getColor().toString().replace("_", " ").toLowerCase()));
				iData.set(colorableColor, PersistentDataType.STRING, ((Sheep)entity).getColor().toString());
				iData.set(sheepSheared, PersistentDataType.BYTE, ((Sheep)entity).isSheared() ? (byte) 1 : (byte) 0);
				break;
			}
			case "SHULKER": {
				stats.add("Color: " + WordUtils.capitalize(((Shulker)entity).getColor().toString().replace("_", " ").toLowerCase()));
				iData.set(colorableColor, PersistentDataType.STRING, ((Shulker)entity).getColor().toString());
				break;
			}
			case "STRIDER": {
				if (((Strider)entity).hasSaddle()) {
					stats.add("Saddled");
				}
				iData.set(steerableSaddle, PersistentDataType.BYTE, ((Strider)entity).hasSaddle() ? (byte) 1 : (byte) 0);
				break;
			}
			case "WOLF": {
				iData.set(collarColor, PersistentDataType.STRING, ((Wolf)entity).getCollarColor().toString());
				break;
			}
		}
		meta.setDisplayName(WordUtils.capitalize(entity.getType().name().replaceAll("_", " ").toLowerCase()));
		if (entity.getCustomName() != null) {
			meta.setDisplayName(entity.getCustomName());
			iData.set(customName, PersistentDataType.STRING, entity.getCustomName());
		}
		meta.setLore(stats);
		egg.setItemMeta(meta);
		return egg;
	}
	
	// TODO
	private LivingEntity createEntity(ItemStack egg, Location loc) {
		ItemMeta meta = egg.getItemMeta();
		PersistentDataContainer iData = meta.getPersistentDataContainer();
		LivingEntity entity = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.valueOf(meta.getPersistentDataContainer().get(entityType, PersistentDataType.STRING)));
		if (entity instanceof Ageable) {
			((Ageable)entity).setAge(iData.get(ageableAge, PersistentDataType.INTEGER));
		}
		if (entity instanceof Cat) {
			((Cat)entity).setCatType(Cat.Type.valueOf(iData.get(catType, PersistentDataType.STRING)));
			((Cat)entity).setCollarColor(DyeColor.valueOf(iData.get(collarColor, PersistentDataType.STRING)));
		}
		if (entity instanceof Colorable) {
			((Colorable)entity).setColor(DyeColor.valueOf(iData.get(colorableColor, PersistentDataType.STRING)));
		}
		if (entity instanceof Creeper) {
			((Creeper)entity).setPowered(iData.get(creeperPowered, PersistentDataType.BYTE) == 1);
		}
		if (entity instanceof Fox) {
			((Fox)entity).setFoxType(Fox.Type.valueOf(iData.get(foxType, PersistentDataType.STRING)));
		}
		if (entity instanceof Goat) {
			((Goat)entity).setScreaming(iData.get(goatScreaming, PersistentDataType.BYTE) == 1);
		}
		if (entity instanceof MushroomCow) {
			((MushroomCow)entity).setVariant(MushroomCow.Variant.valueOf(iData.get(mooshroomVariant, PersistentDataType.STRING)));
		}
		if (entity instanceof Panda) {
			((Panda)entity).setHiddenGene(Panda.Gene.valueOf(iData.get(pandaHiddenGene, PersistentDataType.STRING)));
			((Panda)entity).setMainGene(Panda.Gene.valueOf(iData.get(pandaMainGene, PersistentDataType.STRING)));
		}
		if (entity instanceof Parrot) {
			((Parrot)entity).setVariant(Parrot.Variant.valueOf(iData.get(parrotVariant, PersistentDataType.STRING)));
		}
		if (entity instanceof Phantom) {
			((Phantom)entity).setSize(iData.get(phantomSize, PersistentDataType.INTEGER));
		}
		if (entity instanceof Rabbit) {
			((Rabbit)entity).setRabbitType(Rabbit.Type.valueOf(iData.get(rabbitType, PersistentDataType.STRING)));
		}
		if (entity instanceof Sheep) {
			((Sheep)entity).setSheared(iData.get(sheepSheared, PersistentDataType.BYTE) == 1);
		}
		if (entity instanceof Slime) {
			((Slime)entity).setSize(iData.get(slimeSize, PersistentDataType.INTEGER));
		}
		if (entity instanceof Steerable) {
			((Steerable)entity).setSaddle(iData.get(steerableSaddle, PersistentDataType.BYTE) == 1);
		}
		if (entity instanceof Tameable) {
			if (iData.has(ownerUUID, PersistentDataType.STRING)) {
				((Tameable)entity).setOwner(Bukkit.getServer().getOfflinePlayer(UUID.fromString(iData.get(ownerUUID, PersistentDataType.STRING))));
			}
		}
		if (entity instanceof Wolf) {
			((Wolf)entity).setCollarColor(DyeColor.valueOf(iData.get(collarColor, PersistentDataType.STRING)));
		}
		entity.setHealth(meta.getPersistentDataContainer().get(entityHealth, PersistentDataType.DOUBLE));
		return entity;
	}
	
	private boolean hasEntityStored(ItemStack item) {
		return item.hasItemMeta()
				&& item.getItemMeta().getPersistentDataContainer().has(entityType, PersistentDataType.STRING);
	}
	
	@EventHandler(ignoreCancelled = true)
	private void onCapture(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Snowball
				&& eggs.containsKey(event.getDamager())) {
			Snowball egg = (Snowball) event.getDamager();
			ItemStack spawnEgg = eggs.get(egg);
			if (egg.getShooter() instanceof Player) {
				PowerUser user = getUser((Player) egg.getShooter());
				if (user.allowPower(this)
						&& capturable.contains(event.getEntity().getType())
						&& !hasEntityStored(spawnEgg)) {
					LivingEntity entity = (LivingEntity) event.getEntity();
					if (user.getOption(captureTamed)
							|| !(entity instanceof Tameable)
							|| (entity instanceof Tameable &&
									(((Tameable)entity).getOwner() == user.getPlayer() || ((Tameable)entity).getOwner() == null))) {
						ItemStack newSpawnEgg = createEgg(entity);
						Item droppedEgg = entity.getWorld().dropItemNaturally(entity.getLocation(), newSpawnEgg);
						droppedEgg.setOwner(user.getUUID());
						entity.playEffect(EntityEffect.ENTITY_POOF);
						entity.remove();
						if (!user.getPlayer().getInventory().addItem(newSpawnEgg).containsValue(newSpawnEgg)) {
							runTask(new Runnable() {

								@Override
								public void run() {
									PowerTools.fakeCollect(user.getPlayer(), droppedEgg);
									droppedEgg.remove();
								}
								
							});
						}
						eggs.remove(egg);
					}
					else if (!user.getOption(captureTamed)
							&& entity instanceof Tameable
							&& ((Tameable)entity).getOwner() != null
							&& ((Tameable)entity).getOwner() != user.getPlayer()) {
						user.getPlayer().sendMessage(cantCapTamed);
					}
				}
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	private void onUse(PowerUseEvent event) {
		if (event.getPower() == this) {
			PowerUser user = event.getUser();
			if (user.allowPower(this)) {
				ItemStack item = event.getItem().clone();
				item.setAmount(1);
				Snowball snowball = user.getPlayer().launchProjectile(Snowball.class);
				PowerTools.addDisguise(snowball, item);
				eggs.put(snowball, item);
				if (user.hasStatMaxed(eggsThrown)) {
					eggOwners.put(snowball, user);
				}
				if (event.getItem().hasItemMeta()
						&& event.getItem().getItemMeta().getPersistentDataContainer().has(entityType, PersistentDataType.STRING)) {
					user.increaseStat(eggsThrown, 1);
				}
				event.consumeItem();
			}
		}
	}
	
	@EventHandler
	private void onEggHit(ProjectileHitEvent event) {
		if (eggs.containsKey(event.getEntity())) {
			Snowball egg = (Snowball) event.getEntity();
			ItemStack spawnEgg = eggs.get(egg);
			if (hasEntityStored(spawnEgg)) {
				createEntity(spawnEgg, egg.getLocation());
				eggs.remove(egg);
				if (eggOwners.containsKey(egg)) {
					Item droppedEgg = egg.getWorld().dropItemNaturally(egg.getLocation(), getRequiredItem());
					PowerUser user = eggOwners.get(egg);
					droppedEgg.setOwner(user.getUUID());
					if (!user.getPlayer().getInventory().addItem(getRequiredItem()).containsValue(getRequiredItem())) {
						runTask(new Runnable() {

							@Override
							public void run() {
								PowerTools.fakeCollect(user.getPlayer(), droppedEgg);
								droppedEgg.remove();
							}
							
						});
					}
				}
			}
			else if (event.getHitEntity() == null
					|| !capturable.contains(event.getHitEntity().getType())) {
				egg.getWorld().dropItemNaturally(egg.getLocation(), spawnEgg);
			}
		}
	}

}
