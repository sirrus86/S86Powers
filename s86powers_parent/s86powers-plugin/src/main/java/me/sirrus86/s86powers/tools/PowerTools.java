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
import me.sirrus86.s86powers.powers.PowerAdapter;
import me.sirrus86.s86powers.tools.nms.NMSLibrary;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.tools.version.VersionTools;
import me.sirrus86.s86powers.users.PowerUser;
import me.sirrus86.s86powers.utils.PowerTime;

import org.apache.commons.lang.WordUtils;

import org.bukkit.Art;
import org.bukkit.Axis;
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
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Class which contains an assortment of tools for use within power classes.
 * @author sirrus86
 */
public final class PowerTools {
	
	private static final Map<UUID, UUID> tamed = new HashMap<>();
	private static final Map<Double, Set<Vector>> auraCoords = new HashMap<>();
	private static final TreeMap<Integer, String> romanNums = new TreeMap<>();
	
	private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

	private static final NMSLibrary nms = resolveNMS();
	private static final PacketManager pm = new PacketManager();
	private static final Random random = new Random();
	private static final VersionTools vTools = resolveVTools();
	
	/**
	 * Adds a disguise to an entity, making it look like a different kind of entity.
	 * <p>
	 * Uses packets to maintain the disguise. The disguise will remain until {@link PowerTools#removeDisguise(Entity)} is called, or if the entity dies or despawns.
	 * <p>
	 * Disguises are not maintained after the server restarts or reloads.
	 * @param entity - The entity which needs to be disguised
	 * @param type - The {@link EntityType} to disguise the entity as
	 */
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
	public static void addDisguise(Entity entity, Entity target) {
		pm.addDisguise(entity, target);
	}
	
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
	public static void addGhost(Player player) {
		pm.addGhost(player);
	}
	
	/**
	 * Simulates as though the spectral potion effect were applied to a block.
	 * Essentially spawns an invisible Shulker with the spectral effect,
	 * then places it on a team inhabited only by it and the viewer with the specified team color.
	 * The team is created via packets only and shouldn't disrupt existing teams.
	 * @param viewer - The player viewing the block
	 * @param block - The block to apply the effect to
	 * @param color - The color to apply to the block
	 * @return An instance of the resulting Shulker.
	 */
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
	public static void blockDisguise(Block block, Material material) {
		pm.blockDisguise(block, material);
	}
	
	/**
	 * Makes a collection of blocks appear as a different material.
	 * <p>
	 * Uses packets to maintain the appearance. The disguise will remain until {@link PowerTools#blockUpdate(Collection<Block>)} is called.
	 * <p>
	 * Block disguises are not maintained after the server restarts or reloads.
	 * @param blocks - Blocks to disguise
	 * @param material - Material to disguise the blocks as. Using non-block materials may kick any players who can see it
	 * @param meta - Metadata to be applied to the packet. Useful for materials that appear different with metadata (e.g. Wool)
	 */
	public static void blockDisguise(Collection<Block> blocks, Material material, BlockData data) {
		pm.blockDisguise(blocks, material, data);
	}	

	/**
	 * Removes any disguises from a block, making it appear as it should again.
	 * @param block - Block to update
	 */
	public static void blockUpdate(Block block) {
		pm.blockUpdate(block);
	}
	
	/**
	 * Removes any disguises from a collection of blocks, making them appear as they should again.
	 * @param blocks - Blocks to update
	 */
	public static void blockUpdate(Collection<Block> blocks) {
		pm.blockUpdate(blocks);
	}

	/**
	 * Capitalizes the first letter for a given line of text.
	 * @param line - Text to capitalize
	 * @return The capitalized text
	 */
	public static String capitalize(String line) {
		if (line != null) {
			return line.length() > 1 ? line.substring(0, 1).toUpperCase() + line.substring(1).toLowerCase() : line.toUpperCase();
		}
		else {
			return "";
		}
	}
	
	public static void copyEquipment(LivingEntity from, LivingEntity to) {
		to.getEquipment().setArmorContents(from.getEquipment().getArmorContents());
		to.getEquipment().setItemInMainHand(from.getEquipment().getItemInMainHand());
		to.getEquipment().setItemInOffHand(from.getEquipment().getItemInOffHand());
	}
	
	@Deprecated
	public static void createBeam(Location from, Location to) {
		if (from.getWorld() == to.getWorld()) {
			EnderCrystal crystal = from.getWorld().spawn(from, EnderCrystal.class);
			pm.hide(crystal);
			crystal.setBeamTarget(to);
		}
	}
	
	/**
	 * Makes it appear as though an item is being picked up by an entity.
	 * @param entity - Entity which will be picking up the item
	 * @param item - Item to be picked up
	 */
	public static void fakeCollect(Entity entity, Item item) {
		pm.fakeCollect(entity, item);
	}
	
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
			case NORTH: case SOUTH: return Axis.X;
			case UP: case DOWN: return Axis.Y;
			case EAST: case WEST: return Axis.Z;
			default: return null;
		}
	}
	
	public static Block getClosestBlock(Location loc, Vector vector) {
		for (Location newLoc = loc.clone(); newLoc.getBlockY() > 0 && newLoc.getBlockY() <= loc.getWorld().getMaxHeight(); newLoc.add(vector)) {
			if (newLoc.getBlock() != null
					&& newLoc.getBlock().getType().isSolid()) {
				return newLoc.getBlock();
			}
		}
		return null;
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
	 * @param useSubCardinalDirections
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

	public static String getEntityType(Entity entity) {
		return entity.getType().name();
	}
	
	public static ItemStack getEquipment(LivingEntity entity, EquipmentSlot slot) {
		switch(slot) {
			case CHEST: return entity.getEquipment().getChestplate();
			case FEET: return entity.getEquipment().getBoots();
			case HAND: return entity.getEquipment().getItemInMainHand();
			case HEAD: return entity.getEquipment().getHelmet();
			case LEGS: return entity.getEquipment().getLeggings();
			case OFF_HAND: return entity.getEquipment().getItemInOffHand();
		}
		return null;
	}
	
	public static String getFilteredText(Power power, String text) {
		PowerAdapter adapter = PowerAdapter.getAdapter(power);
		String tmp = text;
		while(tmp.indexOf("[") != -1 && tmp.indexOf("]") != -1) {
			int i = tmp.indexOf("["),
					j = tmp.indexOf("]");
			String tag = tmp.substring(i, j + 1);
			String field = tmp.substring(i + 1, j);
			if (field.startsWith("act:")) {
				ItemStack item = (ItemStack) adapter.getFieldValue(field.substring(4));
				if (item != null) {
					tmp = tmp.replace(tag, PowerTools.getActionString(item));
				}
			}
			else {
				Object object = adapter.getFieldValue(field);
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
			}
		}
		char[] chars = tmp.toCharArray();
		Character.toUpperCase(chars[0]);
		for (int i = 2; i < chars.length; i ++) {
			if (chars[i - 2] == '.') Character.toUpperCase(chars[i]);
		}
		tmp = new String(chars);
		return tmp;
	}

	public static Block getHighestAirBlock(Location loc, int range) {
		Location newLoc = loc.clone();
		for (newLoc = loc.clone(); newLoc.distanceSquared(loc) < range * range; newLoc.add(0, 1, 0)) {
			if (!newLoc.getBlock().getType().isSolid()
					&& newLoc.getBlock().getRelative(BlockFace.UP).getType().isSolid()) {
				return newLoc.getBlock();
			}
		}
		return newLoc.getBlock();
	}

	public static String getItemName(ItemStack item) {
		return nms.getItemName(item);
	}

	public static Set<Block> getNearbyBlocks(Block block, BlockFace... faces) {
		Set<Block> blocks = new HashSet<Block>();
		for (BlockFace face : faces) {
			blocks.add(block.getRelative(face));
		}
		return blocks;
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
					Block block = loc.getWorld().getBlockAt(loc);
					loc.subtract(x, y, z);
					if (loc.distanceSquared(block.getLocation()) <= radius * radius
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
	
	public static Set<Material> getNearbyMaterials(Block block, BlockFace... faces) {
		Set<Material> mats = new HashSet<Material>();
		for (BlockFace face : faces) {
			mats.add(block.getRelative(face).getType());
		}
		return mats;
	}
	
	protected static final NMSLibrary getNMSLibrary() {
		return nms != null ? nms : resolveNMS();
	}
	
	public static LivingEntity getRandomEntity(Entity entity, double radius, LivingEntity... ignore) {
		return getRandomEntity(entity, radius, ignore != null ? Sets.newHashSet(ignore) : null);
	}
	
	public static LivingEntity getRandomEntity(Entity entity, double radius, Set<LivingEntity> ignore) {
		List<Entity> eList = entity.getNearbyEntities(radius, radius, radius);
		Collections.shuffle(eList);
		for (int i = 0; i < eList.size(); i ++) {
			if (eList.get(i) instanceof LivingEntity) {
				LivingEntity target = (LivingEntity) eList.get(i);
				if (ignore == null
						|| !ignore.contains(target)) {
					return target;
				}
			}
			continue;
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
	
	public static Material getSpawnEgg(EntityType type) {
		return Material.getMaterial(type.toString() + "_SPAWN_EGG");
	}
	
	public static Set<Vector> getSphereCoords(double radius) {
		if (!auraCoords.containsKey(radius)
				|| auraCoords.get(radius) == null
				|| auraCoords.get(radius).isEmpty()) {
			Set<Vector> coords = new HashSet<>();
			for (double i = 0; i <= Math.PI; i += Math.PI / (radius * 2.0D)) {
				double rad = Math.sin(i) * radius;
				double y = Math.cos(i) * radius;
				for (double j = 0; j < Math.PI * 2; j += Math.PI / Math.abs(rad)) {
					double x = Math.cos(j) * rad;
					double z = Math.sin(j) * rad;
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
		return vTools.getTargetEntity(clazz, location, direction, maxDistance, filter);
	}
	
	public static boolean hasDisguise(Block block) {
		return pm.hasDisguise(block);
	}

	public static boolean hasDisguise(Entity entity) {
		return pm.hasDisguise(entity);
	}
	
	public static boolean hasDurability(ItemStack item) {
		return isSword(item)
				|| isTool(item);
	}

	public static void hide(Entity entity) {
		pm.hide(entity);
	}

	public static boolean inSunlight(Location loc) {
		return loc.getBlock().getLightFromSky() == 15
				&& loc.getBlock().getLightLevel() > loc.getBlock().getLightFromBlocks()
				&& loc.getBlock().getLightLevel() > 10;
	}

	public static final boolean isArmor(ItemStack item) {
		return isBoots(item)
				|| isChestplate(item)
				|| isHelmet(item)
				|| isLeggings(item);
	}

	public static final boolean isAxe(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_AXE");
	}

	public static final boolean isBoots(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_BOOTS");
	}

	public static final boolean isChestplate(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_CHESTPLATE");
	}

	public static boolean isGhost(Player player) {
		return pm.isGhost(player);
	}

	public static final boolean isHelmet(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_HELMET");
	}

	public static final boolean isHoe(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_HOE");
	}

	public static final boolean isLeggings(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_LEGGINGS");
	}

	public static boolean isOutside(Location loc) {
		return loc.getBlockY() > loc.getWorld().getHighestBlockYAt(loc);
	}

	public static final boolean isPickaxe(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_PICKAXE");
	}

	public static final boolean isProjectile(ItemStack item) {
		return item.getType() == Material.ENDER_PEARL
				|| item.getType() == Material.EGG
				|| item.getType() == Material.SNOWBALL;
	}

	public static final boolean isShovel(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_SPADE");
	}

	public static final boolean isSword(ItemStack item) {
		return item != null
				&& item.getType().name().endsWith("_SWORD");
	}
	
	public static boolean isTamed(Entity entity) {
		return tamed.containsKey(entity.getUniqueId());
	}

	public static final boolean isTool(ItemStack item) {
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
		loc.getWorld().spawnParticle(particle, loc.getX(), loc.getY(), loc.getZ(), count, offset.getX(), offset.getY(), offset.getZ());
	}
	
	public static void playRedstoneEffect(Location loc, Vector offset, int count, DustOptions dustOptions) {
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc.getX(), loc.getY(), loc.getZ(), count, offset.getX(), offset.getY(), offset.getZ(), 1.0D, dustOptions);
	}
	
	public static void removeControl(Player player, Creature creature) {
		creature.eject();
		nms.unTame(creature);
		setCamera(player, player);
	}
	
	public static void removeDisguise(Entity entity) {
		pm.removeDisguise(entity);
	}
	
	public static void removeGhost(Player player) {
		pm.removeGhost(player);
	}
	
	public static void removeSpectralBlock(Player player, Block block) {
		pm.removeSpectralBlock(player, block);
	}
	
	@SuppressWarnings("deprecation")
	public static EntityType resolveEntityType(String typeString) {
		for (EntityType type : EntityType.values()) {
			if (type.getName().equalsIgnoreCase(typeString)) {
				return type;
			}
		}
		return null;
	}
	
	private static NMSLibrary resolveNMS() {
		try {
			return nms != null ? nms : (NMSLibrary) Class.forName("me.sirrus86.s86powers.tools.nms." + MCVersion.CURRENT_VERSION.getPath() + ".NMSLibrary").newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static VersionTools resolveVTools() {
		if (vTools == null) {
			try {
				switch(MCVersion.CURRENT_VERSION) {
					case v1_13: case v1_13_1: case v1_13_2: {
						return (VersionTools) Class.forName("me.sirrus86.s86powers.tools.version.v1_13.VersionTools").newInstance();
					}
					default: {
						return (VersionTools) Class.forName("me.sirrus86.s86powers.tools.version.v1_14.VersionTools").newInstance();
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
	
	public static void setCamera(Player player, Entity entity) {
		pm.setCamera(player, entity);
	}
	
	public static void setControlling(Player player, LivingEntity entity) {
		pm.setControlling(player, entity);
	}
	
	public static void setDirection(Fireball entity, Vector dir) {
		nms.setDirection(entity, dir);
	}
	
	public static ItemStack setItemGlow(ItemStack item) {
		return nms.setItemGlow(item);
	}
	
	public static void setLook(Player player, Location loc) {
		pm.setLook(player, loc);
	}
	
	public static void setRotation(Entity entity, float yaw, float pitch) {
		nms.setRotation(entity, yaw, pitch);
	}
	
	public static void setTamed(Creature entity, PowerUser owner) {
		if (owner != null) {
			nms.setTamed(entity, owner.getPlayer());
			tamed.put(entity.getUniqueId(), owner.getUUID());
			if (ConfigOption.Powers.SHOW_HEARTS_ON_TAMED) {
				pm.showHearts(entity, owner.getPlayer());
			}
		}
		else {
			nms.unTame(entity);
			if (tamed.containsKey(entity.getUniqueId())) {
				tamed.remove(entity.getUniqueId());
			}
		}
	}
	
	public static void showActionBarMessage(Player player, String message) {
		pm.showActionBarMessage(player, message);
	}
	
	public static void showAsSpectral(Player player, Entity entity, ChatColor color, boolean spectral) {
		if (spectral) {
			pm.addSpectralEntity(player, entity, color);
		}
		else {
			pm.removeSpectralEntity(player, entity);
		}
	}
	
	public static void showItemCooldown(Player player, ItemStack item, long cooldown) {
		pm.showItemCooldown(player, item, cooldown);
	}
	
	public static void spawnEntity(Entity entity, Location loc) {
		nms.spawnEntity(entity, loc);
	}
	
	public static void takeControl(Player player, Creature creature) {
		creature.addPassenger(player);
		nms.removePathfinding(creature);
		setControlling(player, creature);
		setCamera(player, creature);
	}
	
	public static final boolean usesDurability(ItemStack item) {
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
