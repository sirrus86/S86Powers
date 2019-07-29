package me.sirrus86.s86powers.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.google.common.collect.Lists;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.tools.nms.NMSLibrary;
import me.sirrus86.s86powers.utils.PowerTime;

public class PacketManager {

	private Map<BlockPosition, PacketContainer> blocks = new HashMap<>();
	private Map<UUID, LivingEntity> control = new HashMap<>();
	private Map<UUID, PacketContainer> disguises = new HashMap<>(),
			metadata = new HashMap<>();
	private Map<UUID, Set<PacketContainer>> equipment = new HashMap<>();
	private Map<UUID, Map<Block, Integer>> spectralBlocks = new HashMap<>();
	private Map<UUID, UUID> visibleToOne = new HashMap<>();
	private Set<UUID> ghosts = new HashSet<>(),
			hidden = new HashSet<>();
	
	private final NMSLibrary nms;
	protected final S86Powers plugin;
	private final ProtocolManager pm;
	
	public PacketManager() {
		plugin = JavaPlugin.getPlugin(S86Powers.class);
		nms = PowerTools.getNMSLibrary();
		pm = ProtocolLibrary.getProtocolManager();
		pm.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.SPAWN_ENTITY,
				PacketType.Play.Server.SPAWN_ENTITY_LIVING, PacketType.Play.Server.NAMED_ENTITY_SPAWN,
				PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Client.STEER_VEHICLE) {
			
			@Override
			public void onPacketSending(PacketEvent event) {
				Player viewer = event.getPlayer();
				Entity entity = event.getPacket().getEntityModifier(event).readSafely(0);
				if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
					BlockPosition bPos = event.getPacket().getBlockPositionModifier().readSafely(0);
					if (bPos != null
							&& blocks.containsKey(bPos)) {
						event.setPacket(blocks.get(bPos));
					}
				}
				else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY
						|| event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY_LIVING
						|| event.getPacketType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
					if (entity != null) {
						if (disguises.containsKey(entity.getUniqueId())) {
							PacketContainer packet = disguises.get(entity.getUniqueId());
							if (packet.getType() == event.getPacketType()) {
								event.setPacket(packet);
							}
							else {
								event.setCancelled(true);
								sendServerPacket(viewer, packet);
							}
							if (equipment.containsKey(entity.getUniqueId())) {
								for (PacketContainer eqPacket : equipment.get(entity.getUniqueId())) {
									sendServerPacket(viewer, eqPacket);
								}
							}
						}
						if (ghosts.contains(entity.getUniqueId())
								&& entity instanceof Player) {
							showAsGhost(event.getPlayer(), (Player) entity);
						}
						if (hidden.contains(entity.getUniqueId())) {
							event.setCancelled(true);
						}
						if (visibleToOne.containsKey(entity.getUniqueId())
								&& visibleToOne.get(entity.getUniqueId()) != event.getPlayer().getUniqueId()) {
							event.setCancelled(true);
						}
					}
				}
				else if (event.getPacketType() == PacketType.Play.Server.ENTITY_METADATA) {
					if (entity != null) {
						if (metadata.containsKey(entity.getUniqueId())
								&& entity != viewer) {
							event.setPacket(metadata.get(entity.getUniqueId()));
						}
						if (PowerTools.isTamed(entity)
								&& ConfigOption.Powers.SHOW_HEARTS_ON_TAMED
								&& entity instanceof LivingEntity) {
							Player owner = PowerTools.getTamedOwner(entity).getPlayer();
							if (viewer == owner) {
								double i = ((LivingEntity) entity).getHealth() / 2,
										j = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2;
								String health = "";
								for (int k = 0; k < j; k ++) {
									if (i > 0) {
										health = health + ChatColor.RED + "\u2665";
										i --;
									}
									else {
										health = health + ChatColor.GRAY + "\u2665";
									}
								}
								PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA, true);
								WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity);
								watcher.setObject(2, Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromText(health).getHandle()));
								watcher.setObject(3, Registry.get(Boolean.class), (Object) true);
								packet.getIntegers().write(0, entity.getEntityId());
								packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
								event.setPacket(packet);
							}
						}
					}
				}
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

					@Override
					public void run() {
						if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
							Player controller = event.getPlayer();
							if (control.containsKey(controller.getUniqueId())
									&& control.get(controller.getUniqueId()) != null) {
								LivingEntity entity = control.get(controller.getUniqueId());
								float forward = event.getPacket().getFloat().read(1);
								float strafe = event.getPacket().getFloat().read(0);
								boolean jumping = event.getPacket().getBooleans().read(0);
								nms.controlWASD(controller, entity, forward, strafe, jumping);
							}
						}
					}
						
				});
			}
			
		});
	}
	
	protected void addDisguise(LivingEntity entity, EntityType type, WrappedDataWatcher meta) {
		PacketContainer packet1 = pm.createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_LIVING, entity).createPacket(entity);
		packet1.getIntegers().write(1, nms.getEntityTypeID(type));
		if (meta != null) {
			packet1.getDataWatcherModifier().write(0, meta);
			PacketContainer packet2 = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA, true);
			packet2.getIntegers().write(0, entity.getEntityId());
			packet2.getWatchableCollectionModifier().write(0, meta.getWatchableObjects());
			pm.broadcastServerPacket(packet2, entity, false);
			metadata.put(entity.getUniqueId(), packet2);
		}
		pm.broadcastServerPacket(packet1, entity, false);
		disguises.put(entity.getUniqueId(), packet1);
	}
	
	protected void addDisguise(Entity entity, ItemStack item) {
		PacketContainer packet1 = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY, true);
		packet1.getIntegers().write(0, entity.getEntityId());
		packet1.getDoubles().write(0, entity.getLocation().getX());
		packet1.getDoubles().write(1, entity.getLocation().getY());
		packet1.getDoubles().write(2, entity.getLocation().getZ());
		packet1.getIntegers().write(6, 2);
		PacketContainer packet2 = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA, true);
		WrappedDataWatcher watcher = new WrappedDataWatcher(nms.getDataWatcher(nms.createItem(entity.getLocation(), item)));
		packet2.getIntegers().write(0, entity.getEntityId());
		packet2.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
		pm.broadcastServerPacket(packet1, entity, false);
		pm.broadcastServerPacket(packet2, entity, false);
		disguises.put(entity.getUniqueId(), packet1);
		metadata.put(entity.getUniqueId(), packet2);
	}
	
	protected void addDisguise(Entity entity, Player player) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN, true);
		packet.getIntegers().write(0, entity.getEntityId());
		packet.getUUIDs().write(0, player.getUniqueId());
		packet.getDoubles().write(0, entity.getLocation().getX());
		packet.getDoubles().write(1, entity.getLocation().getY());
		packet.getDoubles().write(2, entity.getLocation().getZ());
		packet.getBytes().write(0, (byte) (entity.getLocation().getYaw() * 256.0F / 360.0F));
		packet.getBytes().write(1, (byte) (entity.getLocation().getPitch() * 256.0F / 360.0F));
//		packet.getDataWatcherModifier().write(0, WrappedDataWatcher.getEntityWatcher(player));
		pm.broadcastServerPacket(packet, entity, false);
		Set<PacketContainer> eqPackets = createEquipmentPackets(entity.getEntityId(), player);
		for (PacketContainer eqPacket : eqPackets) {
			pm.broadcastServerPacket(eqPacket, entity, false);
		}
		disguises.put(entity.getUniqueId(), packet);
		equipment.put(entity.getUniqueId(), eqPackets);
	}
	
	protected void addGhost(Player player) {
		if (ghosts.add(player.getUniqueId())) {
			showAsGhost(player, player);
			for (Player viewer : pm.getEntityTrackers(player)) {
				showAsGhost(viewer, player);
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false, false), true);
		}
	}
	
	protected void addSpectralBlock(Player viewer, Block block, ChatColor color) {
		int id = nms.generateEntityID();
		UUID uuid = UUID.randomUUID();
		PacketContainer packet1 = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING, true);
		packet1.getIntegers().write(0, id);
		packet1.getUUIDs().write(0, uuid);
		packet1.getIntegers().write(1, nms.getEntityTypeID(EntityType.SHULKER));
		packet1.getDoubles().write(0, (double) block.getX() + 0.5D);
		packet1.getDoubles().write(1, (double) block.getY());
		packet1.getDoubles().write(2, (double) block.getZ() + 0.5D);
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(0, Registry.get(Byte.class), (byte) 0x60);
		watcher.setObject(4, Registry.get(Boolean.class), (Object) true);
		watcher.setObject(5, Registry.get(Boolean.class), (Object) true);
		watcher.setObject(11, Registry.get(Byte.class), (byte) 0x01);
		packet1.getDataWatcherModifier().write(0, watcher);
		PacketContainer packet2 = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
		packet2.getStrings().write(0, viewer.getEntityId() + "." + id);
		packet2.getIntegers().write(0, 0);
		packet2.getIntegers().write(1, 2);
		packet2.getEnumModifier(ChatColor.class, 6).write(0, color);
		packet2.getModifier().write(7, Lists.newArrayList(viewer.getName(), uuid.toString()));
		sendServerPacket(viewer, packet1);
		sendServerPacket(viewer, packet2);
		if (!spectralBlocks.containsKey(viewer.getUniqueId())) {
			spectralBlocks.put(viewer.getUniqueId(), new HashMap<>());
		}
		spectralBlocks.get(viewer.getUniqueId()).put(block, id);
	}
	
	protected void addSpectralEntity(Player viewer, Entity entity) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA, true);
		WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
		watcher.setObject(0, Registry.get(Byte.class), (byte) (watcher.getByte(0) + 0x40));
		packet.getIntegers().write(0, entity.getEntityId());
		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
		sendServerPacket(viewer, packet);
	}
	
	protected void blockDisguise(Block block, Material material) {
		BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);
		packet.getBlockPositionModifier().write(0, bPos);
		packet.getBlockData().write(0, WrappedBlockData.createData(material));
		pm.broadcastServerPacket(packet);
		blocks.put(bPos, packet);
	}
	
	protected void blockDisguise(Collection<Block> blocks, Material material, BlockData data) {
		Map<Chunk, List<Block>> chunks = new HashMap<Chunk, List<Block>>();
		for (Block block : blocks) {
			blockTemporary(block, material);
			if (!chunks.containsKey(block.getChunk())) {
				chunks.put(block.getChunk(), new ArrayList<Block>());
			}
			chunks.get(block.getChunk()).add(block);
		}
		for (Chunk chunk : chunks.keySet()) {
			Block[] cBlocks = chunks.get(chunk).toArray(new Block[chunks.get(chunk).size()]);
			PacketContainer packet = pm.createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE, true);
			packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
			MultiBlockChangeInfo[] changes = new MultiBlockChangeInfo[cBlocks.length];
			for (int i = 0; i < cBlocks.length; i ++) {
				changes[i] = new MultiBlockChangeInfo(cBlocks[i].getLocation(), WrappedBlockData.createData(data));
			}
			packet.getMultiBlockChangeInfoArrays().write(0, changes);
			pm.broadcastServerPacket(packet);
		}
	}
	
	private void blockTemporary(Block block, Material material) {
		BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);
		packet.getBlockPositionModifier().write(0, bPos);
		packet.getBlockData().write(0, WrappedBlockData.createData(material));
		blocks.put(bPos, packet);
	}
	
	protected void blockUpdate(Block block) {
		BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);
		packet.getBlockPositionModifier().write(0, bPos);
		packet.getBlockData().write(0, WrappedBlockData.createData(block.getType()));
		pm.broadcastServerPacket(packet);
		blocks.remove(bPos);
	}
	
	protected void blockUpdate(Collection<Block> blocks) {
		Map<Chunk, List<Block>> chunks = new HashMap<Chunk, List<Block>>();
		for (Block block : blocks) {
			if (!chunks.containsKey(block.getChunk())) {
				chunks.put(block.getChunk(), new ArrayList<Block>());
			}
			chunks.get(block.getChunk()).add(block);
		}
		for (Chunk chunk : chunks.keySet()) {
			Block[] cBlocks = chunks.get(chunk).toArray(new Block[chunks.get(chunk).size()]);
			PacketContainer packet = pm.createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE, true);
			packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
			MultiBlockChangeInfo[] changes = new MultiBlockChangeInfo[cBlocks.length];
			for (int i = 0; i < cBlocks.length; i ++) {
				changes[i] = new MultiBlockChangeInfo(cBlocks[i].getLocation(), WrappedBlockData.createData(cBlocks[i].getType()));
			}
			packet.getMultiBlockChangeInfoArrays().write(0, changes);
			pm.broadcastServerPacket(packet);
		}
	}
	
	private Set<PacketContainer> createEquipmentPackets(int id, LivingEntity entity) {
		Set<PacketContainer> packets = new HashSet<>();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
			packet.getIntegers().write(0, id);
			packet.getItemSlots().write(0, getItemSlot(slot));
			packet.getItemModifier().write(0, PowerTools.getEquipment(entity, slot));
			packets.add(packet);
		}
		return packets;
	}
	
	protected void fakeCollect(Entity entity, Item item) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.COLLECT);
		packet.getIntegers().write(0, item.getEntityId());
		packet.getIntegers().write(1, entity.getEntityId());
		packet.getIntegers().write(2, item.getItemStack().getAmount());
		pm.broadcastServerPacket(packet, entity, true);
	}
	
	protected void fakeExplosion(Location loc, float radius) {
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0F);
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.EXPLOSION);
		packet.getDoubles().write(0, loc.getX());
		packet.getDoubles().write(1, loc.getY());
		packet.getDoubles().write(2, loc.getZ());
		packet.getFloat().write(0, radius);
		packet.getBlockPositionCollectionModifier().writeDefaults();
		packet.getFloat().write(1, 0.0F);
		packet.getFloat().write(2, 0.0F);
		packet.getFloat().write(3, 0.0F);
		pm.broadcastServerPacket(packet, loc, plugin.getServer().getViewDistance());
	}
	
	private ItemSlot getItemSlot(EquipmentSlot slot) {
		switch (slot) {
			case HAND: return ItemSlot.MAINHAND;
			case OFF_HAND: return ItemSlot.OFFHAND;
			default: return ItemSlot.valueOf(slot.name());
		}
	}
	
	protected boolean hasDisguise(Block block) {
		BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		return blocks.containsKey(bPos);
	}
	
	protected boolean hasDisguise(Entity entity) {
		return disguises.containsKey(entity.getUniqueId());
	}
	
	protected void hide(Entity entity) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY, true);
		packet.getIntegerArrays().write(0, new int[] {entity.getEntityId()});
		for (Player player : pm.getEntityTrackers(entity)) {
			sendServerPacket(player, packet);
		}
		hidden.add(entity.getUniqueId());
	}
	
	protected boolean isGhost(Player player) {
		return ghosts.contains(player.getUniqueId());
	}
	
	protected void removeDisguise(Entity entity) {
		if (disguises.containsKey(entity.getUniqueId())
				|| metadata.containsKey(entity.getUniqueId())) {
			disguises.remove(entity.getUniqueId());
			metadata.remove(entity.getUniqueId());
			updateEntity(entity);
		}
	}
	
	protected void removeGhost(Player player) {
		if (ghosts.remove(player.getUniqueId())) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			for (Player players : pm.getEntityTrackers(player)) {
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
				packet.getStrings().write(0, players.getEntityId() + "." + player.getEntityId());
				packet.getIntegers().write(1, 1);
				sendServerPacket(players, packet);
			}
		}
	}
	
	protected void removeSpectralBlock(Player viewer, Block block) {
		if (spectralBlocks.containsKey(viewer.getUniqueId())) {
			Map<Block, Integer> map = spectralBlocks.get(viewer.getUniqueId());
			if (map.containsKey(block)) {
				int id = map.get(block);
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY, true);
				packet.getIntegerArrays().write(0, new int[] {id});
				sendServerPacket(viewer, packet);
				map.remove(block);
			}
		}
	}
	
	protected void removeSpectralEntity(Player viewer, Entity entity) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
		packet.getIntegers().write(0, entity.getEntityId());
		packet.getWatchableCollectionModifier().write(0, WrappedDataWatcher.getEntityWatcher(entity).getWatchableObjects());
		sendServerPacket(viewer, packet);
	}
	
	private void sendServerPacket(Player player, PacketContainer packet) {
		try {
			pm.sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			if (ConfigOption.Plugin.SHOW_PACKET_ERRORS) {
				e.printStackTrace();
			}
			else {
				return;
			}
		}
	}
	
	protected void setControlling(Player player, LivingEntity entity) {
		control.put(player.getUniqueId(), entity);
	}
	
	protected void setPointOfView(Entity entity, Player player) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.CAMERA);
		packet.getIntegers().write(0, entity.getEntityId());
		sendServerPacket(player, packet);
	}
	
	protected void showActionBarMessage(Player player, String message) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.CHAT);
		packet.getChatComponents().write(0, WrappedChatComponent.fromText(message));
		packet.getChatTypes().write(0, ChatType.GAME_INFO);
		sendServerPacket(player, packet);
	}
	
	private void showAsGhost(Player viewer, Player player) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
		packet.getStrings().write(0, viewer.getEntityId() + "." + player.getEntityId());
		packet.getIntegers().write(0, 0);
		packet.getIntegers().write(1, 2);
		packet.getModifier().write(7, Lists.newArrayList(viewer.getName(), player.getName()));
		sendServerPacket(viewer, packet);
	}
	
	protected void showHearts(LivingEntity entity, Player player) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA, true);
		WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity);
		double i = ((LivingEntity) entity).getHealth() / 2,
				j = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 2;
		String health = "";
		for (int k = 0; k < j; k ++) {
			if (i > 0) {
				health = health + ChatColor.RED + "\u2665";
				i --;
			}
			else {
				health = health + ChatColor.GRAY + "\u2665";
			}
		}
		watcher.setObject(2, Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromText(health).getHandle()));
		watcher.setObject(3, Registry.get(Boolean.class), (Object) true);
		packet.getIntegers().write(0, entity.getEntityId());
		packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
		sendServerPacket(player, packet);
	}
	
	protected void showItemCooldown(Player player, ItemStack item, long cooldown) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.SET_COOLDOWN);
		packet.getModifier().write(0, nms.getNMSItem(item));
		packet.getIntegers().write(0, (int) PowerTime.toTicks(cooldown));
		sendServerPacket(player, packet);
	}
	
	private void updateEntity(Entity entity) {
		updateEntity(entity, false);
	}
	
	private void updateEntity(Entity entity, boolean sendToEntity) {
		try {
			pm.updateEntity(entity, pm.getEntityTrackers(entity));
		} catch (Exception e) {
			PacketContainer packet1 = null;
			if (entity instanceof Player) {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.NAMED_ENTITY_SPAWN, (Player) entity).createPacket(entity);
			}
			else if (entity instanceof LivingEntity) {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_LIVING, (LivingEntity) entity).createPacket(entity);
			}
			else if (entity instanceof ExperienceOrb) {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB, (ExperienceOrb) entity).createPacket(entity);
			}
			else if (entity instanceof LightningStrike) {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_WEATHER, (LightningStrike) entity).createPacket(entity);
			}
			else if (entity instanceof Painting) {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_PAINTING, (Painting) entity).createPacket(entity);
			}
			else {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY, entity).createPacket(entity);
			}
			PacketContainer packet2 = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA);
			packet2.getIntegers().write(0, entity.getEntityId());
			packet2.getWatchableCollectionModifier().write(0, WrappedDataWatcher.getEntityWatcher(entity).getWatchableObjects());
			if (packet1 != null) {
				pm.broadcastServerPacket(packet1, entity, sendToEntity);
			}
			pm.broadcastServerPacket(packet2, entity, sendToEntity);
		}
	}
	
}
