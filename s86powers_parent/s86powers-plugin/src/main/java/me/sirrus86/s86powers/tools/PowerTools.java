package me.sirrus86.s86powers.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Predicate;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.powers.Power;
import me.sirrus86.s86powers.powers.PowerOption;
import me.sirrus86.s86powers.tools.nms.NMSLibrary;
import me.sirrus86.s86powers.tools.packets.PacketManager;
import me.sirrus86.s86powers.tools.packets.PacketManagerNull;
import me.sirrus86.s86powers.tools.packets.PacketManagerPLib;
import me.sirrus86.s86powers.tools.version.MCMetadata;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.tools.version.VersionTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Art;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Class which contains an assortment of tools for use within power classes.
 * @author sirrus86
 */
public final class PowerTools {
	
	private final static Map<UUID, UUID> tamed = new HashMap<>();
	
	private final static Map<Double, Set<Vector>> auraCoords = new HashMap<>();
	private final static TreeMap<Integer, String> romanNums = new TreeMap<>();
	
	private final static BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	private final static BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

	private final static NMSLibrary nms = resolveNMS();
	private final static PacketManager pm = S86Powers.getProtocolLib() != null ? new PacketManagerPLib() : new PacketManagerNull();
	private final static Random random = new Random();
	private final static VersionTools vTools = resolveVTools();
	
	/**
	 * Adds a disguise to an entity, making it look like a different kind of entity.
	 * <p>
	 * Uses packets to maintain the disguise. The disguise will remain until {@link PowerTools#removeDisguise(Entity)} is called, or if the entity dies or despawns.
	 * <p>
	 * Disguises are not maintained after the server restarts or reloads.
	 * @param entity - The entity which needs to be disguised
	 * @param type - The {@link EntityType} to disguise the entity as
	 */
	@PacketManaged
	public static void addDisguise(Entity entity, EntityType type) {
		pm.addDisguise(entity, type);
	}
	
	/**
	 * Adds a disguise to an entity, making it look like a different kind of entity.
	 * <p>
	 * Uses packets to maintain the disguise. The disguise will remain until {@link PowerTools#removeDisguise(Entity)} is called, or if the entity dies or despawns.
	 * <p>
	 * Disguises are not maintained after the server restarts or reloads.
	 * @param entity - The entity which needs to be disguised
	 * @param type - The {@link EntityType} to disguise the entity as
	 * @param meta - Metadata to be supplied with the disguise packet. May be {@code null}
	 */
	@PacketManaged
	public static void addDisguise(Entity entity, EntityType type, MCMetadata meta) {
		pm.addDisguise(entity, type, meta.getMap());
	}
	
	/**
	 * Adds a disguise to an entity, making it look like a different kind of entity.
	 * <p>
	 * Uses packets to maintain the disguise. The disguise will remain until {@link PowerTools#removeDisguise(Entity)} is called, or if the entity dies or despawns.
	 * <p>
	 * Disguises are not maintained after the server restarts or reloads.
	 * @param entity - The entity which needs to be disguised
	 * @param type - The {@link EntityType} to disguise the entity as
	 * @param meta - Metadata to be supplied with the disguise packet. May be {@code null}
	 */
	@PacketManaged
	public static void addDisguise(Entity entity, EntityType type, Map<Integer, Object> meta) {
		pm.addDisguise(entity, type, meta);
	}
	
	/**
	 * Adds a disguise to an entity, making it look like a different kind of entity.
	 * <p>
	 * Uses packets to maintain the disguise. The disguise will remain until {@link PowerTools#removeDisguise(Entity)} is called, or if the entity dies or despawns.
	 * <p>
	 * Disguises are not maintained after the server restarts or reloads.
	 * @param entity - The entity which needs to be disguised
	 * @param type - The {@link EntityType} to disguise the entity as
	 * @param meta - Metadata to be supplied with the disguise packet. May be {@code null}
	 * @param data - Extra object data, such as {@link Art} for paintings
	 */
	@PacketManaged
	public static void addDisguise(Entity entity, EntityType type, Map<Integer, Object> meta, Object data) {
		pm.addDisguise(entity, type, meta, data);
	}
	
	/**
	 * Adds a disguise to an entity, making it look like a dropped item.
	 * <p>
	 * Uses packets to maintain the disguise. The disguise will remain until {@link PowerTools#removeDisguise(Entity)} is called, or if the entity dies or despawns.
	 * <p>
	 * Disguises are not maintained after the server restarts or reloads.
	 * @param entity - The entity which needs to be disguised
	 * @param item - An {@link ItemStack} that matches this entity's disguise
	 */
	@PacketManaged
	public static void addDisguise(Entity entity, ItemStack item) {
		pm.addDisguise(entity, item);
	}
	
	/**
	 * Adds a disguise to an entity, making it look like another entity.
	 * <p>
	 * Uses packets to maintain the disguise. The disguise will remain until {@link PowerTools#removeDisguise(Entity)} is called, or if the entity dies or despawns.
	 * <p>
	 * Disguises are not maintained after the server restarts or reloads.
	 * @param entity - The entity which needs to be disguised
	 * @param target - The entity to disguise the original entity as
	 */
	@PacketManaged
	public static void addDisguise(Entity entity, Entity target) {
		pm.addDisguise(entity, target);
	}

	@PacketManaged
	public static void addEquipmentDisguise(Entity entity, LivingEntity target) {
		pm.addEquipmentDisguise(entity, target);
	}
	
	/**
	 * Makes a player appear translucent. Note that this forcibly adds the invisibility potion effect to the player.
	 * <p>
	 * Uses packets to maintain the appearance. The appearance will remain until {@link PowerTools#removeGhost(Player)} is called, or if the player dies or despawns.
	 * <p>
	 * Ghost effects are not maintained after the server restarts or reloads.
	 * @param player - Player to apply ghost appearance to
	 */
	@PacketManaged
	public static void addGhost(Player player) {
		pm.addGhost(player);
	}
	
	/**
	 * Simulates as though the spectral potion effect were applied to a block.
	 * Essentially spawns an invisible Shulker with the spectral effect,
	 * then places it on a team inhabited only by it and the viewer with the specified team color.
	 * The team is created via packets only and shouldn't disrupt existing teams.
	 * @param viewer - The player viewing the block
	 * @param color - The color to apply to the block
	 */
	@PacketManaged
	public static void addSpectralBlock(Player viewer, Block block, ChatColor color) {
		pm.addSpectralBlock(viewer, block, color);
	}
	
	/**
	 * Makes a block appear as a block of a different material.
	 * <p>
	 * Uses packets to maintain the appearance. The disguise will remain until {@link PowerTools#blockUpdate(Block)} is called.
	 * <p>
	 * Block disguises are not maintained after the server restarts or reloads.
	 * @param block - Block to disguise
	 * @param material - Material to disguise the block as. Using non-block materials may kick any players who can see it
	 */
	@PacketManaged
	public static void blockDisguise(Block block, Material material) {
		pm.blockDisguise(block, material);
	}
	
	/**
	 * Makes a collection of blocks appear as a different material.
	 * <p>
	 * Uses packets to maintain the appearance. The disguise will remain until {@link PowerTools#blockUpdate(Collection)} is called.
	 * <p>
	 * Block disguises are not maintained after the server restarts or reloads.
	 * @param blocks - Blocks to disguise
	 * @param material - Material to disguise the blocks as. Using non-block materials may kick any players who can see it
	 */
	@PacketManaged
	public static void blockDisguise(Collection<Block> blocks, Material material, BlockData data) {
		pm.blockDisguise(blocks, material, data);
	}	

	/**
	 * Removes any disguises from a block, making it appear as it should again.
	 * @param block - Block to update
	 */
	@PacketManaged
	public static void blockUpdate(Block block) {
		pm.blockUpdate(block);
	}
	
	/**
	 * Removes any disguises from a collection of blocks, making them appear as they should again.
	 * @param blocks - Blocks to update
	 */
	@PacketManaged
	public static void blockUpdate(Collection<Block> blocks) {
		pm.blockUpdate(blocks);
	}

	@PacketManaged
	@Deprecated
	public static void createBeam(Location from, Location to) {
		if (from.getWorld() == to.getWorld()
				&& from.getWorld() != null) {
			EnderCrystal crystal = from.getWorld().spawn(from, EnderCrystal.class);
			pm.hide(crystal);
			crystal.setBeamTarget(to);
		}
	}
	
	public static ItemStack createPowerBook(Power power) {
		ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK, 1);
		ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
		if (meta != null) {
			meta.setDisplayName(ChatColor.RESET.toString() + power.getType().getColor() + power.getName());
			meta.getPersistentDataContainer().set(Power.getCollectorKey(), PersistentDataType.STRING, power.getClass().getSimpleName());
			String powerDesc = PowerTools.getFilteredText(power, power.getDescription());
			List<String> lore = PowerTools.wordSplit(ChatColor.RESET + ChatColor.GRAY.toString(), powerDesc, 30);
			meta.setLore(lore);
			stack.setItemMeta(meta);
		}
		return stack;
	}
	
	/**
	 * Makes it appear as though an item is being picked up by an entity.
	 * @param entity - Entity which will be picking up the item
	 * @param item - Item to be picked up
	 */
	@PacketManaged
	public static void fakeCollect(Entity entity, Item item) {
		pm.fakeCollect(entity, item);
	}

	@PacketManaged
	public static void fakeExplosion(Location loc, float radius) {
		pm.fakeExplosion(loc, radius);
	}
	
	public static String getActionString(ItemStack item) {
		if (item.getType() != Material.AIR) {
			if (item.getType().isBlock()
					|| item.getType().isEdible()
					|| item.getType().isRecord()
					|| item.getType().isInteractable()) {
				return "left-click";
			}
		}
		return "right-click";
	}
	
	public static Axis getAxis(BlockFace face) {
		switch (face) {
			case NORTH, SOUTH -> {
				return Axis.X;
			}
			case UP, DOWN -> {
				return Axis.Y;
			}
			default -> {
				return Axis.Z;
			}
		}
	}
	
	/**
	 * Gets the horizontal Block Face from a given yaw angle<br>
	 * This includes the NORTH_WEST faces
	 * 
	 * @return The Block Face of the angle
	 * @author bergerkiller
	 */
	public static BlockFace getDirection(Location loc) {
		return getDirection(loc, true);
	}
	
	/**
	 * Gets the horizontal Block Face from a given yaw angle
	 * 
	 * @param useSubCardinalDirections Whether to use subcardinal directions
	 * @return The Block Face of the angle
	 * @author bergerkiller
	 */
	public static BlockFace getDirection(Location loc, boolean useSubCardinalDirections) {
		float yaw = loc.getYaw();
		if (useSubCardinalDirections) {
			return radial[Math.round(yaw / 45f) & 0x7];
		}
		else {
			return axis[Math.round(yaw / 90f) & 0x3];
		}
	}
	
	public static Vector getDirection(Location loc1, Location loc2) {
		if (loc1.getWorld() == loc2.getWorld()) {
			return loc2.toVector().subtract(loc1.toVector()).normalize();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Attempts to get the {@link LivingEntity} responsible for the specified entity.
	 * @param entity - The entity of which to find the source
	 * @return The source entity, if there is one, otherwise null
	 */
	public static LivingEntity getEntitySource(Entity entity) {
		if (entity instanceof LivingEntity) {
			return (LivingEntity) entity;
		}
		else if (entity instanceof Projectile
				&& ((Projectile) entity).getShooter() instanceof LivingEntity) {
			return (LivingEntity) ((Projectile) entity).getShooter();
		}
		else if (entity instanceof AreaEffectCloud
				&& ((AreaEffectCloud) entity).getSource() instanceof LivingEntity) {
			return (LivingEntity) ((AreaEffectCloud) entity).getSource();
		}
		else if (entity instanceof EvokerFangs) {
			return ((EvokerFangs) entity).getOwner();
		}
		return null;
	}
	
	public static ItemStack getEquipment(LivingEntity entity, EquipmentSlot slot) {
		if (entity.getEquipment() != null) {
			return switch (slot) {
				case CHEST -> entity.getEquipment().getChestplate();
				case FEET -> entity.getEquipment().getBoots();
				case HAND -> entity.getEquipment().getItemInMainHand();
				case HEAD -> entity.getEquipment().getHelmet();
				case LEGS -> entity.getEquipment().getLeggings();
				case OFF_HAND -> entity.getEquipment().getItemInOffHand();
			};
		}
		return null;
	}
	
	public static String getFilteredText(Power power, String text) {
		String tmp = text;
		while(tmp.contains("[") && tmp.contains("]")) {
			int i = tmp.indexOf("["),
					j = tmp.indexOf("]");
			String tag = tmp.substring(i, j + 1),
					field = tmp.substring(i + 1, j);
			PowerOption<?> option = power.getOptionByName(field);
			if (field.startsWith("act:")) {
				option = power.getOptionByName(field.substring(4));
				Object object;
				if (option != null) {
					object = power.getOption(option);
				}
				else {
					object = power.getFieldValue(field.substring(4));
				}
				ItemStack item = (ItemStack) object;
				if (item != null) {
					tmp = tmp.replace(tag, PowerTools.getActionString(item));
				}
			}
			else {
				Object object;
				if (option != null) {
					object = power.getOption(option);
				}
				else {
					object = power.getFieldValue(field);
				}
				if (object != null) {
					if (object instanceof Boolean) {
						String endTag = "[/" + field + "]";
						if (!Boolean.parseBoolean(object.toString())) {
							tmp = tmp.substring(0, tmp.indexOf(tag)) + tmp.substring(tmp.indexOf(endTag) + endTag.length());
						}
						else {
							tmp = tmp.replace(tag, "").replace(endTag, "");
						}
					}
					else if (object instanceof ItemStack) {
						tmp = tmp.replace(tag, PowerTools.getItemName((ItemStack)object));
					}
					else if (object instanceof Long) {
						tmp = tmp.replace(tag, PowerTime.asLongString((Long)object));
					}
					else {
						tmp = tmp.replace(tag, object.toString());
					}
				}
				else {
					tmp = tmp.replace(tag, "NULL");
				}
			}
		}
		char[] chars = tmp.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		for (int i = 2; i < chars.length; i ++) {
			if (chars[i - 2] == '.') {
				chars[i] = Character.toUpperCase(chars[i]);
			}
		}
		tmp = new String(chars);
		return tmp;
	}
	
	public static String getFriendlyName(Entity entity) {
		String name = entity.getClass().getSimpleName().replace("Craft", "");
		if (entity instanceof Player) {
			name = ((Player)entity).getDisplayName();
		}
		else if (entity.getCustomName() != null) {
			name = entity.getCustomName();
		}
		return name;
	}

	public static Block getHighestAirBlock(Location loc, int range) {
		Location newLoc;
		for (newLoc = loc.clone(); newLoc.distanceSquared(loc) < range * range; newLoc.add(0, 1, 0)) {
			if (!newLoc.getBlock().getType().isSolid()
					&& newLoc.getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
				return newLoc.getBlock();
			}
		}
		return newLoc.getBlock();
	}

	public static String getItemName(ItemStack item) {
		ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : Bukkit.getServer().getItemFactory().getItemMeta(item.getType());
		return WordUtils.capitalize(meta != null && meta.hasLocalizedName() ? meta.getLocalizedName() : item.getType().toString().replace('_', ' ').toLowerCase());
	}

	public static Set<Block> getNearbyBlocks(Location loc, double radius) {
		return getNearbyBlocks(loc, radius, EnumSet.allOf(Material.class));
	}

	public static Set<Block> getNearbyBlocks(Location loc, double radius, Collection<Material> materials) {
		Set<Block> blocks = new HashSet<>();
		for (double x = -radius; x < radius; x ++) {
			for (double y = -radius; y < radius; y ++) {
				for (double z = -radius; z < radius; z ++) {
					loc.add(x, y, z);
					Block block = loc.getWorld() != null ? loc.getWorld().getBlockAt(loc) : null;
					loc.subtract(x, y, z);
					if (block != null
							&& loc.distanceSquared(block.getLocation()) <= radius * radius
							&& materials.contains(block.getType())) {
						blocks.add(block);
					}
				}
			}
		}
		return blocks;
	}
	
	public static <E extends Entity> Set<E> getNearbyEntities(Class<E> clazz, Location loc, double radius) {
		return getNearbyEntities(clazz, loc, radius, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends Entity> Set<E> getNearbyEntities(Class<E> clazz, Location loc, double radius, Entity ignore) {
		Set<E> tmp = new HashSet<>();
		int cRad = radius < 16 ? 1 : (int)((radius - (radius % 16)) / 16);
		for (int x = -cRad; x < cRad; x ++) {
			for (int z = -cRad; z < cRad; z ++) {
				for (Entity entity : new Location(loc.getWorld(), loc.getX() + (x * 16), loc.getY(), loc.getZ() + (z * 16)).getChunk().getEntities()) {
					if (entity.getLocation().distanceSquared(loc) <= radius * radius
							&& clazz.isInstance(entity)
							&& entity != ignore) {
						tmp.add((E) entity);
					}
				}
			}
		}
		return tmp;
	}
	
	public static NMSLibrary getNMSLibrary() {
		return nms != null ? nms : resolveNMS();
	}
	
	public static PotionEffect getPotionEffect(PotionData data) {
		PotionEffectType type = data.getType().getEffectType();
		int amp = data.isUpgraded() ? 1 : 0;
		int dur = 0;
		switch (data.getType()) {
			case FIRE_RESISTANCE, INVISIBILITY, JUMP, NIGHT_VISION, SPEED, STRENGTH, WATER_BREATHING -> dur = data.isExtended() ? PowerTime.toTicks(8, 0, 0) : (data.isUpgraded() ? PowerTime.toTicks(1, 30, 0) : PowerTime.toTicks(3, 0, 0));
			case POISON, REGEN -> dur = data.isExtended() ? PowerTime.toTicks(1, 30, 0) : (data.isUpgraded() ? PowerTime.toTicks(22, 0) : PowerTime.toTicks(45, 0));
			case SLOW_FALLING, SLOWNESS, WEAKNESS -> dur = data.isExtended() ? PowerTime.toTicks(4, 0, 0) : (data.isUpgraded() ? PowerTime.toTicks(20, 0) : PowerTime.toTicks(1, 30, 0));
			case TURTLE_MASTER -> dur = data.isExtended() ? PowerTime.toTicks(40, 0) : PowerTime.toTicks(20, 0);
			default -> {
			}
		}
		return type != null ? new PotionEffect(type, dur, amp) : null;
	}
	
	public static String getPotionEffectName(PotionEffectType type) {
		return WordUtils.capitalizeFully(type.getName().replace("_", " "));
	}
	
	public static LivingEntity getRandomEntity(Entity entity, double radius, LivingEntity... ignore) {
		return getRandomEntity(entity, radius, ignore != null ? Sets.newHashSet(ignore) : null);
	}
	
	public static LivingEntity getRandomEntity(Entity entity, double radius, Set<LivingEntity> ignore) {
		List<Entity> eList = entity.getNearbyEntities(radius, radius, radius);
		Collections.shuffle(eList);
		for (Entity value : eList) {
			if (value instanceof LivingEntity target) {
				if (ignore == null
						|| !ignore.contains(target)) {
					return target;
				}
			}
		}
		return null;
	}
	
	public static String getRomanNumeral(int number) {
		if (romanNums.isEmpty()) {
			romanNums.put(1000, "M");
			romanNums.put(900, "CM");
			romanNums.put(500, "D");
			romanNums.put(400, "CD");
			romanNums.put(100, "C");
			romanNums.put(90, "XC");
			romanNums.put(50, "L");
			romanNums.put(40, "XL");
			romanNums.put(10, "X");
			romanNums.put(9, "IX");
			romanNums.put(5, "V");
			romanNums.put(4, "IV");
			romanNums.put(1, "I");
		}
		int i = romanNums.floorKey(number);
		if (number == i) {
			return romanNums.get(number);
		}
		return romanNums.get(i) + getRomanNumeral(number - i);
	}
	
	public static Set<Vector> getSphereCoords(double radius) {
		if (!auraCoords.containsKey(radius)
				|| auraCoords.get(radius) == null
				|| auraCoords.get(radius).isEmpty()) {
			Set<Vector> coords = new HashSet<>();
			for (double phi = 0; phi <= Math.PI; phi += Math.PI / (radius * 2.0D)) {
				double rad = Math.sin(phi) * radius;
				double y = Math.cos(phi) * radius;
				for (double theta = 0; theta < Math.PI * 2; theta += Math.PI / Math.abs(rad)) {
					double x = Math.cos(theta) * rad;
					double z = Math.sin(theta) * rad;
					coords.add(new Vector(x, y, z));
				}
			}
			auraCoords.put(radius, coords);
		}
		return auraCoords.get(radius);
	}
	
	public static PowerUser getTamedOwner(Entity entity) {
		return tamed.containsKey(entity.getUniqueId()) ? S86Powers.getConfigManager().getUser(tamed.get(entity.getUniqueId())) : null;
	}
	
	public static <T extends Entity> T getTargetEntity(Class<T> clazz, Location location, Vector direction, double maxDistance, Predicate<Entity> filter) {
		return vTools != null ? vTools.getTargetEntity(clazz, location, direction, maxDistance, filter) : null;
	}

	@PacketManaged
	public static boolean hasDisguise(Block block) {
		return pm.hasDisguise(block);
	}
	
	public static boolean hasDurability(ItemStack item) {
		return isSword(item)
				|| isTool(item);
	}

	public static boolean inSunlight(Location loc) {
		return loc.getBlock().getLightFromSky() == 15
				&& loc.getBlock().getLightLevel() > loc.getBlock().getLightFromBlocks()
				&& loc.getBlock().getLightLevel() > 10;
	}

	public static boolean isArmor(ItemStack item) {
		return isBoots(item)
				|| isChestplate(item)
				|| isHelmet(item)
				|| isLeggings(item);
	}

	public static boolean isAxe(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_AXE");
	}

	public static boolean isBoots(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_BOOTS");
	}

	public static boolean isChestplate(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_CHESTPLATE");
	}

	@PacketManaged
	public static boolean isGhost(Player player) {
		return pm.isGhost(player);
	}

	public static boolean isHelmet(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_HELMET");
	}

	public static boolean isHoe(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_HOE");
	}

	public static boolean isLeggings(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_LEGGINGS");
	}

	public static boolean isOutside(Location loc) {
		return loc.getWorld() != null && loc.getBlockY() > loc.getWorld().getHighestBlockYAt(loc);
	}

	public static boolean isPickaxe(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_PICKAXE");
	}

	public static boolean isShovel(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_SPADE");
	}

	public static boolean isSword(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_SWORD");
	}
	
	public static boolean isTamed(Entity entity) {
		return tamed.containsKey(entity.getUniqueId());
	}

	public static boolean isTool(ItemStack item) {
		return item != null
				&& (item.getType() == Material.BOW
						|| item.getType() == Material.FISHING_ROD
						|| item.getType() == Material.FLINT_AND_STEEL
						|| item.getType() == Material.SHEARS
						|| isAxe(item)
						|| isHoe(item)
						|| isPickaxe(item)
						|| isShovel(item));
	}
	
	public static void playParticleEffect(Location loc, Particle particle) {
		playParticleEffect(loc, particle, random.nextInt(6));
	}
	
	public static void playParticleEffect(Location loc, Particle particle, int count) {
		playParticleEffect(loc, particle, Vector.getRandom(), count);
	}

	public static void playParticleEffect(Location loc, Particle particle, Vector offset, int count) {
		if (loc.getWorld() != null) {
			loc.getWorld().spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), count, offset.getX(), offset.getY(), offset.getZ());
		}
	}
	
	public static void playRedstoneEffect(Location loc, Vector offset, int count, DustOptions dustOptions) {
		if (loc.getWorld() != null) {
			loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), count, offset.getX(), offset.getY(), offset.getZ(), 1.0D, dustOptions);
		}
	}
	
	public static void removeControl(Player player, Creature creature) {
		creature.eject();
		if (nms != null) {
			nms.unTame(creature);
		}
		setCamera(player, player);
	}

	@PacketManaged
	public static void removeDisguise(Entity entity) {
		pm.removeDisguise(entity);
	}

	@PacketManaged
	public static void removeGhost(Player player) {
		pm.removeGhost(player);
	}

	@PacketManaged
	public static void removeSpectralBlock(Player player, Block block) {
		pm.removeSpectralBlock(player, block);
	}
	
	public static EntityType resolveEntityType(String name) {
		return vTools != null ? vTools.resolveEntityType(name) : null;
	}
	
	private static NMSLibrary resolveNMS() {
		try {
			return nms != null ? nms : (NMSLibrary) Class.forName("me.sirrus86.s86powers.tools.nms." + MCVersion.CURRENT_VERSION.getPath() + ".NMSLibrary").getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static VersionTools resolveVTools() {
		if (vTools == null) {
			try {
				switch (MCVersion.CURRENT_VERSION) {
					case v1_13, v1_13_1, v1_13_2 -> {
						return (VersionTools) Class.forName("me.sirrus86.s86powers.tools.version.v1_13.VersionTools").getDeclaredConstructor().newInstance();
					}
					case v1_14, v1_14_1, v1_14_2, v1_14_3, v1_14_4, v1_15, v1_15_1, v1_15_2 -> {
						return (VersionTools) Class.forName("me.sirrus86.s86powers.tools.version.v1_14.VersionTools").getDeclaredConstructor().newInstance();
					}
					default -> {
						return (VersionTools) Class.forName("me.sirrus86.s86powers.tools.version.v1_16.VersionTools").getDeclaredConstructor().newInstance();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		else {
			return vTools;
		}
	}

	@PacketManaged
	public static void setCamera(Player player, Entity entity) {
		pm.setCamera(player, entity);
	}

	@PacketManaged
	public static void setLook(Player player, Location loc) {
		pm.setLook(player, loc);
	}
	
	public static void setRotation(Entity entity, float yaw, float pitch) {
		if (nms != null) {
			nms.setRotation(entity, yaw, pitch);
		}
	}

	@PacketManaged
	public static void setTamed(Creature entity, PowerUser owner) {
		if (nms != null) {
			if (owner != null) {
				nms.setTamed(entity, owner.getPlayer());
				tamed.put(entity.getUniqueId(), owner.getUUID());
				if (ConfigOption.Powers.SHOW_HEARTS_ON_TAMED) {
					pm.showHearts(entity, owner.getPlayer());
				}
			}
			else {
				nms.unTame(entity);
				tamed.remove(entity.getUniqueId());
			}
		}
	}

	@PacketManaged
	public static void showActionBarMessage(Player player, String message) {
		pm.showActionBarMessage(player, message);
	}

	@PacketManaged
	public static void showAsSpectral(Player player, Entity entity, ChatColor color, boolean spectral) {
		if (spectral) {
			pm.addSpectralEntity(player, entity, color);
		}
		else {
			pm.removeSpectralEntity(player, entity);
		}
	}

	@PacketManaged
	public static void showItemCooldown(Player player, ItemStack item, long cooldown) {
		pm.showItemCooldown(player, item, cooldown);
	}
	
	public static boolean usesDurability(ItemStack item) {
		return item != null
				&& (item.getType() == Material.BOW
						|| isArmor(item)
						|| isSword(item)
						|| isTool(item));
	}
	
	public static List<String> wordSplit(String text, int wrapLength) {
		return wordSplit("", text, wrapLength);
	}
	
	public static List<String> wordSplit(String prefix, String text, int wrapLength) {
		String wrapped = wordWrap(text, wrapLength);
		String[] split = wrapped.split("\n");
		for (int i = 0; i < split.length; i ++) {
			split[i] = prefix + split[i];
		}
		return Lists.newArrayList(split);
	}
	
	public static String wordWrap(String text, int wrapLength) {
		return WordUtils.wrap(text, wrapLength, "\n", false);
	}
	
}
