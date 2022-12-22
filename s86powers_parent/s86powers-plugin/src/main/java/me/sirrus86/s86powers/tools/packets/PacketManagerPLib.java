package me.sirrus86.s86powers.tools.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.google.common.collect.Lists;

import me.sirrus86.s86powers.config.ConfigOption;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.version.MCMetadata;
import me.sirrus86.s86powers.tools.version.MCVersion;
import me.sirrus86.s86powers.utils.PowerTime;

public final class PacketManagerPLib extends PacketManager {

	private Map<BlockPosition, PacketContainer> blocks = new HashMap<>();
	private Map<UUID, PacketContainer> disguises = new HashMap<>(),
			metadata = new HashMap<>();
	private Map<UUID, Set<PacketContainer>> equipment = new HashMap<>();
	
	private final ProtocolManager pm;
	
	public PacketManagerPLib() {
		pm = ProtocolLibrary.getProtocolManager();
		pm.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.SPAWN_ENTITY,
				PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.BLOCK_CHANGE, PacketType.Play.Client.STEER_VEHICLE,
				PacketType.Play.Client.USE_ENTITY, PacketType.Play.Client.BLOCK_DIG) {
			
			@Override
			public void onPacketSending(PacketEvent event) {
				Player viewer = event.getPlayer();
				Entity entity = null;
				try {
					entity = event.getPacket().getEntityModifier(event).readSafely(0);
				} catch (Exception e) {
					// ProtocolLib can't pinpoint the entity, so leave the field null
					if (ConfigOption.Plugin.SHOW_PACKET_ERRORS) {
						e.printStackTrace();
					}
				}
				if (event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
					BlockPosition bPos = event.getPacket().getBlockPositionModifier().readSafely(0);
					if (bPos != null
							&& blocks.containsKey(bPos)) {
						event.setPacket(blocks.get(bPos));
					}
				}
				else if (event.getPacketType() == PacketType.Play.Server.SPAWN_ENTITY
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
								WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity);
								watcher.setObject(MCMetadata.EntityMeta.ENTITY_CUSTOM_NAME.getIndex(), Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromText(health).getHandle()));
								watcher.setObject(MCMetadata.EntityMeta.ENTITY_IS_CUSTOM_NAME_VISIBLE.getIndex(), Registry.get(Boolean.class), (Object) true);
								PacketContainer packet = createEntityMetadataPacket(entity.getEntityId(), watcher);
								event.setPacket(packet);
							}
						}
					}
				}
			}
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Client.USE_ENTITY) {
					if (event.getPlayer().getEntityId() == event.getPacket().getIntegers().read(0)
							&& control.containsKey(event.getPlayer().getUniqueId())) {
						event.setCancelled(true);
					}
				}
				if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE) {
					if (control.containsKey(event.getPlayer().getUniqueId())) {
						if (event.getPacket().getBooleans().read(1)) {
							event.setCancelled(true);
						}
						plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
							@Override
							public void run() {
								Player controller = event.getPlayer();
								if (control.containsKey(controller.getUniqueId())
										&& control.get(controller.getUniqueId()) != null) {
									float forward = 0.0F;
									if (event.getPacket().getBooleans().read(1)) {
										forward = 1.0F;
									}
									LivingEntity entity = control.get(controller.getUniqueId());
									nms.controlWASD(controller, entity, forward, 0.0F, false);
								}
							}
						});
					}
				}
				plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

					@Override
					public void run() {
						if (event.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
							if (event.getPacket().getPlayerDigTypes().read(0) == PlayerDigType.DROP_ITEM) {
								Player player = event.getPlayer();
								if (player.isInsideVehicle()
										&& player.getVehicle() instanceof Creature
										&& control.containsKey(player.getUniqueId())) {
									Creature vehicle = (Creature) player.getVehicle();
									control.remove(player.getUniqueId());
									PowerTools.removeControl(player, vehicle);
								}
							}
						}
					}
						
				});
			}
			
		});
	}
	
	@Override
	public void addDisguise(Entity entity, EntityType type) {
		createEntityPacket(entity, type, (WrappedDataWatcher) null, null);
	}

	@Override
	public void addDisguise(Entity entity, EntityType type, Map<Integer, Object> meta) {
		createEntityPacket(entity, type, meta != null ? createWrappedDataWatcher(null, meta) : null, null);
	}

	@Override
	public void addDisguise(Entity entity, EntityType type, Map<Integer, Object> meta, Object data) {
		createEntityPacket(entity.getEntityId(), entity.getUniqueId(), entity.getLocation(), entity.getVelocity(), type,
				meta != null ? createWrappedDataWatcher(null, meta) : null, data, null);
	}

	private void createEntityPacket(Entity entity, EntityType type, WrappedDataWatcher watcher, Object data) {
		createEntityPacket(entity.getEntityId(), entity.getUniqueId(), entity.getLocation(), entity.getVelocity(), type, watcher, data, null);
	}

	private void createEntityPacket(int id, UUID uuid, Location loc, Vector velocity, EntityType type, WrappedDataWatcher watcher, Object data, Player viewer) {
		PacketContainer entityPacket = null, metaPacket = null;
		if (type == EntityType.PLAYER) {
			entityPacket = pm.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN, true);
			entityPacket.getIntegers().write(0, id);
			entityPacket.getUUIDs().write(0, (UUID) data);
			entityPacket.getDoubles().write(0, loc.getX());
			entityPacket.getDoubles().write(1, loc.getY());
			entityPacket.getDoubles().write(2, loc.getZ());
			entityPacket.getBytes().write(0, (byte) (loc.getYaw() * 256.0F / 360.0F));
			entityPacket.getBytes().write(1, (byte) (loc.getPitch() * 256.0F / 360.0F));
		}
		else if (type == EntityType.EXPERIENCE_ORB) {
			entityPacket = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB, true);
			entityPacket.getIntegers().write(0, id);
			entityPacket.getDoubles().write(0, loc.getX());
			entityPacket.getDoubles().write(1, loc.getY());
			entityPacket.getDoubles().write(2, loc.getZ());
			entityPacket.getIntegers().write(1, 0);
		}
		else {
			entityPacket = pm.createPacket(PacketType.Play.Server.SPAWN_ENTITY, true);
			entityPacket.getIntegers().write(0, id);
			entityPacket.getUUIDs().write(0, uuid);
			entityPacket.getDoubles().write(0, loc.getX());
			entityPacket.getDoubles().write(1, loc.getY());
			entityPacket.getDoubles().write(2, loc.getZ());
			entityPacket.getIntegers().write(1, velocity.getBlockX());
			entityPacket.getIntegers().write(2, velocity.getBlockY());
			entityPacket.getIntegers().write(3, velocity.getBlockZ());
			if (MCVersion.isLessThan(MCVersion.v1_19)) {
				entityPacket.getIntegers().write(4, (int) (loc.getYaw() * 256.0F / 360.0F));
				entityPacket.getIntegers().write(5, (int) (loc.getPitch() * 256.0F / 360.0F));
				if (data != null
						&& data instanceof Integer) {
					entityPacket.getIntegers().write(6, (int) data);
				}
			}
			else {
				entityPacket.getBytes().write(0, (byte) (loc.getYaw() * 256.0F / 360.0F));
				entityPacket.getBytes().write(1, (byte) (loc.getPitch() * 256.0F / 360.0F));
				entityPacket.getBytes().write(2, (byte) (loc.getYaw() * 256.0F / 360.0F));
				if (data != null
						&& data instanceof Integer) {
					entityPacket.getIntegers().write(4, (int) data);
				}
			}
			entityPacket.getEntityTypeModifier().write(0, type);
		}
		Entity entity = Bukkit.getEntity(uuid);
		if (entityPacket != null) {
			if (viewer != null) {
				sendServerPacket(viewer, entityPacket);
			}
			if (entity != null) {
				if (entity instanceof Player) {
					Player player = (Player) entity;
					PacketContainer infoPacket = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
					infoPacket.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
					List<PlayerInfoData> pInfo = new ArrayList<>();
					WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player).withId(Integer.toString(id));
					NativeGameMode mode = NativeGameMode.fromBukkit(player.getGameMode());
					WrappedChatComponent name = WrappedChatComponent.fromText(player.getDisplayName());
					pInfo.add(new PlayerInfoData(profile, 0, mode, name));
					infoPacket.getPlayerInfoDataLists().write(0, pInfo);
					pm.broadcastServerPacket(infoPacket, entity, false);
				}
				pm.broadcastServerPacket(entityPacket, entity, false);
			}
			disguises.put(uuid, entityPacket);
		}
		if (watcher != null) {
			metaPacket = createEntityMetadataPacket(id, watcher);
			if (viewer != null) {
				sendServerPacket(viewer, metaPacket);
			}
			else if (entity != null) {
				pm.broadcastServerPacket(metaPacket, entity, false);
			}
			metadata.put(uuid, metaPacket);
		}
	}

	@Override
	public void addDisguise(Entity entity, ItemStack item) {
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(MCMetadata.EntityMeta.THROWABLE_ITEM.getIndex(), Registry.getItemStackSerializer(false), item, true);
		createEntityPacket(entity, EntityType.SNOWBALL, watcher, (int) 1);
	}

	@Override
	public void addDisguise(Entity entity, Entity target) {
		Object data = null;
		switch (target.getType()) {
			case PAINTING: {
				data = ((Painting)target).getArt();
				break;
			}
			case PLAYER: {
				data = target.getUniqueId();
				break;
			}
			default: {
				break;
			}
		}
		createEntityPacket(entity, target.getType(), WrappedDataWatcher.getEntityWatcher(target), data);
		if (target instanceof LivingEntity) {
			addEquipmentDisguise(entity, (LivingEntity) target);
		}
	}

	@Override
	public void addEquipmentDisguise(Entity entity, LivingEntity target) {
		if (equipment != null) {
			Set<PacketContainer> eqPackets = createEquipmentPackets(entity.getEntityId(), (LivingEntity) target);
			for (PacketContainer eqPacket : eqPackets) {
				pm.broadcastServerPacket(eqPacket, entity, false);
			}
			equipment.put(entity.getUniqueId(), eqPackets);
		}
	}

	@Override
	public void addGhost(Player player) {
		if (ghosts.add(player.getUniqueId())) {
			showAsGhost(player, player);
			for (Player viewer : pm.getEntityTrackers(player)) {
				showAsGhost(viewer, player);
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false, false));
		}
	}
	
	@Override
	public void addSpectralBlock(Player viewer, Block block, ChatColor color) {
		if (!spectralBlocks.containsKey(viewer.getUniqueId())) {
			spectralBlocks.put(viewer.getUniqueId(), new HashMap<>());
		}
		if (spectralBlocks.get(viewer.getUniqueId()).containsKey(block)) {
			removeSpectralBlock(viewer, block);
		}
		int id = nms.generateEntityID();
		UUID uuid = UUID.randomUUID();
		Location loc = block.getLocation().clone().add(0.5D, 0.0D, 0.5D);
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(MCMetadata.EntityMeta.ENTITY_STATE.getIndex(), Registry.get(Byte.class), (byte) 0x60);
		watcher.setObject(MCMetadata.EntityMeta.ENTITY_IS_SILENT.getIndex(), Registry.get(Boolean.class), (Object) true);
		watcher.setObject(MCMetadata.EntityMeta.ENTITY_NO_GRAVITY.getIndex(), Registry.get(Boolean.class), (Object) true);
		watcher.setObject(MCMetadata.EntityMeta.MOB_STATE.getIndex(), Registry.get(Byte.class), (byte) 0x01);
		createEntityPacket(id, uuid, loc, new Vector(), EntityType.SHULKER, watcher, null, viewer);
		PacketContainer teamPacket = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
		teamPacket.getStrings().write(0, viewer.getEntityId() + "." + id);
		teamPacket.getIntegers().write(0, 0);
		if (MCVersion.isLessThan(MCVersion.v1_17)) {
			teamPacket.getIntegers().write(1, 2);
			teamPacket.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color);
			teamPacket.getModifier().write(7, Lists.newArrayList(viewer.getName(), uuid.toString()));
		}
		else {
			Optional<InternalStructure> optStruct = teamPacket.getOptionalStructures().read(0);
			if (optStruct.isPresent()) {
				InternalStructure struct = optStruct.get();
				struct.getChatComponents().write(0, WrappedChatComponent.fromText(""));
				struct.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color);
				teamPacket.getOptionalStructures().write(0, Optional.of(struct));
			}
			teamPacket.getModifier().write(2, Lists.newArrayList(viewer.getName(), uuid.toString()));
		}
		sendServerPacket(viewer, teamPacket);
		spectralBlocks.get(viewer.getUniqueId()).put(block, id);
	}

	@Override
	public void addSpectralEntity(Player viewer, Entity entity, ChatColor color) {
		WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
		watcher.setObject(MCMetadata.EntityMeta.ENTITY_STATE.getIndex(), Registry.get(Byte.class), (byte) (watcher.getByte(0) + 0x40));
		PacketContainer packet = createEntityMetadataPacket(entity.getEntityId(), watcher);
		sendServerPacket(viewer, packet);
		PacketContainer teamPacket = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
		teamPacket.getStrings().write(0, viewer.getEntityId() + "." + entity.getEntityId());
		teamPacket.getIntegers().write(0, 0);
		if (MCVersion.isLessThan(MCVersion.v1_17)) {
			teamPacket.getIntegers().write(1, 2);
			teamPacket.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color);
			teamPacket.getModifier().write(7, Lists.newArrayList(viewer.getName(), entity.getUniqueId().toString()));
		}
		else {
			Optional<InternalStructure> optStruct = teamPacket.getOptionalStructures().read(0);
			if (optStruct != null) {
				InternalStructure struct = optStruct.get();
				struct.getChatComponents().write(0, WrappedChatComponent.fromText(""));
				struct.getEnumModifier(ChatColor.class, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0, color);
				teamPacket.getOptionalStructures().write(0, Optional.of(struct));
			}
			teamPacket.getModifier().write(2, Lists.newArrayList(viewer.getName(), entity.getUniqueId().toString()));
		}
		sendServerPacket(viewer, teamPacket);
	}

	@Override
	public void blockDisguise(Block block, Material material) {
		BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);
		packet.getBlockPositionModifier().write(0, bPos);
		packet.getBlockData().write(0, WrappedBlockData.createData(material));
		pm.broadcastServerPacket(packet);
		blocks.put(bPos, packet);
	}

	@Override
	public void blockDisguise(Collection<Block> blocks, Material material, BlockData data) {
		if (MCVersion.isLessThan(MCVersion.v1_16_2)) {
			Map<Chunk, List<Block>> chunks = new HashMap<>();
			for (Block block : blocks) {
				blockTemporary(block, material);
				if (!chunks.containsKey(block.getChunk())) {
					chunks.put(block.getChunk(), new ArrayList<Block>());
				}
				chunks.get(block.getChunk()).add(block);
			}
			for (Chunk chunk : chunks.keySet()) {
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE, true);
				Block[] cBlocks = chunks.get(chunk).toArray(new Block[chunks.get(chunk).size()]);
				packet.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(chunk.getX(), chunk.getZ()));
				MultiBlockChangeInfo[] changes = new MultiBlockChangeInfo[cBlocks.length];
				for (int i = 0; i < cBlocks.length; i ++) {
					changes[i] = new MultiBlockChangeInfo(cBlocks[i].getLocation(), WrappedBlockData.createData(data));
				}
				packet.getMultiBlockChangeInfoArrays().write(0, changes);
				pm.broadcastServerPacket(packet);
			}
		}
		else {
			Map<BlockPosition, List<Block>> bPosMap = new HashMap<>();
			for (Block block : blocks) {
				blockTemporary(block, material);
				int x = block.getX() >> 4,
						y = block.getY() >> 4,
						z = block.getZ() >> 4;
				BlockPosition bPos = null;
				for (BlockPosition bPosEntry : bPosMap.keySet()) {
					if (bPosEntry.getX() == x
							&& bPosEntry.getY() == y
							&& bPosEntry.getZ() == z) {
						bPos = bPosEntry;
					}
				}
				if (bPos == null) {
					bPos = new BlockPosition(x, y, z);
					bPosMap.put(bPos, new ArrayList<>());
				}
				List<Block> bList = bPosMap.get(bPos);
				bList.add(block);
			}
			for (BlockPosition bPos : bPosMap.keySet()) {
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE, true);
				packet.getSectionPositions().write(0, bPos);
				Block[] cBlocks = bPosMap.get(bPos).toArray(new Block[bPosMap.get(bPos).size()]);
				short[] locs = new short[cBlocks.length];
				WrappedBlockData[] bDatas = new WrappedBlockData[cBlocks.length];
				for (int i = 0; i < cBlocks.length; i ++) {
					int x = cBlocks[i].getX() & 0xF,
							y = cBlocks[i].getY() & 0xF,
							z = cBlocks[i].getZ() & 0xF;
					locs[i] = (short) (x << 8 | z << 4 | y << 0);
					bDatas[i] = WrappedBlockData.createData(data);
				}
				packet.getShortArrays().write(0, locs);
				packet.getBlockDataArrays().write(0, bDatas);
				pm.broadcastServerPacket(packet);
			}
		}
	}
	
	private void blockTemporary(Block block, Material material) {
		BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);
		packet.getBlockPositionModifier().write(0, bPos);
		packet.getBlockData().write(0, WrappedBlockData.createData(material));
		blocks.put(bPos, packet);
	}

	@Override
	public void blockUpdate(Block block) {
		BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.BLOCK_CHANGE, true);
		packet.getBlockPositionModifier().write(0, bPos);
		packet.getBlockData().write(0, WrappedBlockData.createData(block.getType()));
		pm.broadcastServerPacket(packet);
		blocks.remove(bPos);
	}
	
	@Override
	public void blockUpdate(Collection<Block> blocks) {
		if (MCVersion.isLessThan(MCVersion.v1_16_2)) {
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
		else {
			Map<BlockPosition, List<Block>> bPosMap = new HashMap<>();
			for (Block block : blocks) {
				int x = block.getX() >> 4,
						y = block.getY() >> 4,
						z = block.getZ() >> 4;
				BlockPosition bPos = null;
				for (BlockPosition bPosEntry : bPosMap.keySet()) {
					if (bPosEntry.getX() == x
							&& bPosEntry.getY() == y
							&& bPosEntry.getZ() == z) {
						bPos = bPosEntry;
					}
				}
				if (bPos == null) {
					bPos = new BlockPosition(x, y, z);
					bPosMap.put(bPos, new ArrayList<>());
				}
				List<Block> bList = bPosMap.get(bPos);
				bList.add(block);
			}
			for (BlockPosition bPos : bPosMap.keySet()) {
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE, true);
				packet.getSectionPositions().write(0, bPos);
				Block[] cBlocks = bPosMap.get(bPos).toArray(new Block[bPosMap.get(bPos).size()]);
				short[] locs = new short[cBlocks.length];
				WrappedBlockData[] bDatas = new WrappedBlockData[cBlocks.length];
				for (int i = 0; i < cBlocks.length; i ++) {
					int x = cBlocks[i].getX() & 0xF,
							y = cBlocks[i].getY() & 0xF,
							z = cBlocks[i].getZ() & 0xF;
					locs[i] = (short) (x << 8 | z << 4 | y << 0);
					bDatas[i] = WrappedBlockData.createData(cBlocks[i].getType());
				}
				packet.getShortArrays().write(0, locs);
				packet.getBlockDataArrays().write(0, bDatas);
				pm.broadcastServerPacket(packet);
			}
		}
	}
	
	private Set<PacketContainer> createEquipmentPackets(int id, LivingEntity entity) {
		Set<PacketContainer> packets = new HashSet<>();
		if (MCVersion.isLessThan(MCVersion.v1_16_1)) {
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT, true);
				packet.getIntegers().write(0, id);
				packet.getItemSlots().write(0, getItemSlot(slot));
				packet.getItemModifier().write(0, PowerTools.getEquipment(entity, slot));
				packets.add(packet);
			}
		}
		else {
			EntityEquipment equip = entity.getEquipment();
			if (equip != null) {
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT, true);
				packet.getIntegers().write(0, id);
				List<Pair<EnumWrappers.ItemSlot, ItemStack>> slotList = new ArrayList<>();
		        slotList.add(new Pair<>(EnumWrappers.ItemSlot.CHEST, equip.getChestplate()));
		        slotList.add(new Pair<>(EnumWrappers.ItemSlot.FEET, equip.getBoots()));
		        slotList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD, equip.getHelmet()));
		        slotList.add(new Pair<>(EnumWrappers.ItemSlot.LEGS, equip.getLeggings()));
		        slotList.add(new Pair<>(EnumWrappers.ItemSlot.MAINHAND, equip.getItemInMainHand()));
		        slotList.add(new Pair<>(EnumWrappers.ItemSlot.OFFHAND, equip.getItemInOffHand()));
		        packet.getSlotStackPairLists().write(0, slotList);
				packets = Set.of(packet);
			}
		}
		return packets;
	}

    private PacketContainer createEntityMetadataPacket(int id, WrappedDataWatcher watcher) {
        PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_METADATA, true);
        packet.getIntegers().write(0, id);
        if (MCVersion.isLessThan(MCVersion.v1_19_3)) {
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        }
        else {
            List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();
            for (WrappedWatchableObject entry : watcher.getWatchableObjects()) {
                if (entry == null) {
                    continue;
                }
                WrappedDataWatcher.WrappedDataWatcherObject watcherObject = entry.getWatcherObject();
                wrappedDataValueList.add(new WrappedDataValue(watcherObject.getIndex(), watcherObject.getSerializer(),
                        entry.getRawValue()));
            }
            packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
        }
        return packet;
    }
	
	private WrappedDataWatcher createWrappedDataWatcher(Entity entity, Map<Integer, Object> map) {
		WrappedDataWatcher watcher = entity != null ? WrappedDataWatcher.getEntityWatcher(entity) : new WrappedDataWatcher();
		for (Integer i : map.keySet()) {
			if (watcher.hasIndex(i)
					&& watcher.getObject(i) instanceof Byte) {
				watcher.setObject(i, Registry.get(Byte.class), (byte) (watcher.getByte(i) + (byte) map.get(i)), true);
			}
			else {
				watcher.setObject(i, Registry.get(map.get(i).getClass()), (Object) map.get(i), true);
			}
		}
		return watcher;
	}

	@Override
	public void fakeCollect(Entity entity, Item item) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.COLLECT);
		packet.getIntegers().write(0, item.getEntityId());
		packet.getIntegers().write(1, entity.getEntityId());
		packet.getIntegers().write(2, item.getItemStack().getAmount());
		pm.broadcastServerPacket(packet, entity, true);
	}

	@Override
	public void fakeExplosion(Location loc, float radius) {
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

	@Override
	public boolean hasDisguise(Block block) {
		BlockPosition bPos = new BlockPosition(block.getX(), block.getY(), block.getZ());
		return blocks.containsKey(bPos);
	}

	@Override
	public boolean hasDisguise(Entity entity) {
		return disguises.containsKey(entity.getUniqueId());
	}

	@Override
	public void hide(Entity entity) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY, true);
		if (MCVersion.isLessThan(MCVersion.v1_17)) {
			packet.getIntegerArrays().write(0, new int[] {entity.getEntityId()});
		}
		else if (MCVersion.isLessThan(MCVersion.v1_17_1)) {
			packet.getIntegers().write(0, entity.getEntityId());
		}
		else {
			packet.getIntLists().write(0, Lists.newArrayList(entity.getEntityId()));
		}
		for (Player player : pm.getEntityTrackers(entity)) {
			sendServerPacket(player, packet);
		}
		hidden.add(entity.getUniqueId());
	}

	@Override
	public void removeDisguise(Entity entity) {
		if (disguises.containsKey(entity.getUniqueId())
				|| metadata.containsKey(entity.getUniqueId())
				|| equipment.containsKey(entity.getUniqueId())) {
			disguises.remove(entity.getUniqueId());
			equipment.remove(entity.getUniqueId());
			metadata.remove(entity.getUniqueId());
			updateEntity(entity);
		}
	}

	@Override
	public void removeGhost(Player player) {
		if (ghosts.remove(player.getUniqueId())) {
			player.removePotionEffect(PotionEffectType.INVISIBILITY);
			for (Player players : pm.getEntityTrackers(player)) {
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
				packet.getStrings().write(0, players.getEntityId() + "." + player.getEntityId());
				packet.getIntegers().write(0, 1);
				sendServerPacket(players, packet);
			}
			ghosts.remove(player.getUniqueId());
		}
	}

	@Override
	public void removeSpectralBlock(Player viewer, Block block) {
		if (spectralBlocks.containsKey(viewer.getUniqueId())) {
			Map<Block, Integer> map = spectralBlocks.get(viewer.getUniqueId());
			if (map.containsKey(block)) {
				int id = map.get(block);
				PacketContainer packet = pm.createPacket(PacketType.Play.Server.ENTITY_DESTROY, true);
				if (MCVersion.isLessThan(MCVersion.v1_17)) {
					packet.getIntegerArrays().write(0, new int[] {id});
				}
				else if (MCVersion.isLessThan(MCVersion.v1_17_1)) {
					packet.getIntegers().write(0, id);
				}
				else {
					packet.getIntLists().write(0, Lists.newArrayList(id));
				}
				sendServerPacket(viewer, packet);
				map.remove(block);
			}
		}
	}

	@Override
	public void removeSpectralEntity(Player viewer, Entity entity) {
		PacketContainer packet = createEntityMetadataPacket(entity.getEntityId(), WrappedDataWatcher.getEntityWatcher(entity));
		sendServerPacket(viewer, packet);
	}
	
	private void sendServerPacket(Player player, PacketContainer packet) {
		if (player != null
				&& packet != null) {
			pm.sendServerPacket(player, packet);
		}
	}

	@Override
	public void setCamera(Player player, Entity entity) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.CAMERA);
		packet.getIntegers().write(0, entity.getEntityId());
		sendServerPacket(player, packet);
	}

	@Override
	public void setLook(Player player, Location loc) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.LOOK_AT);
		packet.getEnumModifier(Anchor.class, 4).write(0, Anchor.EYES);
		packet.getDoubles().write(0, loc.getX());
		packet.getDoubles().write(1, loc.getY());
		packet.getDoubles().write(2, loc.getZ());
		sendServerPacket(player, packet);
	}

	@Override
	public void showActionBarMessage(Player player, String message) {
		PacketContainer packet;
		if (MCVersion.isLessThan(MCVersion.v1_17)) {
			packet = pm.createPacket(PacketType.Play.Server.CHAT);
			packet.getChatComponents().write(0, WrappedChatComponent.fromText(message));
			packet.getChatTypes().write(0, ChatType.GAME_INFO);
		}
		else {
			packet = pm.createPacket(PacketType.Play.Server.SET_ACTION_BAR_TEXT);
			packet.getChatComponents().write(0, WrappedChatComponent.fromText(message));
		}
		sendServerPacket(player, packet);
	}

	private void showAsGhost(Player viewer, Player player) {
		PacketContainer packet = pm.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM, true);
		packet.getStrings().write(0, viewer.getEntityId() + "." + player.getEntityId());
		packet.getIntegers().write(0, 0);
		if (MCVersion.isLessThan(MCVersion.v1_17)) {
			packet.getStrings().write(1, "hideForOwnTeam");
			packet.getIntegers().write(1, 2);
			packet.getModifier().write(7, Lists.newArrayList(viewer.getName(), player.getName()));
		}
		else {
			packet.getModifier().write(2, Lists.newArrayList(viewer.getName(), player.getName()));
			Optional<InternalStructure> optStruct = packet.getOptionalStructures().read(0);
			if (optStruct.isPresent()) {
				InternalStructure struct = optStruct.get();
				struct.getChatComponents().write(0, WrappedChatComponent.fromText(""));
				struct.getIntegers().write(0, 2);
				struct.getStrings().write(0, "hideForOwnTeam");
//				struct.getStrings().write(1, "always");
				packet.getOptionalStructures().write(0, Optional.of(struct));
			}
		}
		sendServerPacket(viewer, packet);
	}

	@Override
	public void showHearts(LivingEntity entity, Player player) {
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
		watcher.setObject(MCMetadata.EntityMeta.ENTITY_CUSTOM_NAME.getIndex(), Registry.getChatComponentSerializer(true), Optional.of(WrappedChatComponent.fromText(health).getHandle()));
		watcher.setObject(MCMetadata.EntityMeta.ENTITY_IS_CUSTOM_NAME_VISIBLE.getIndex(), Registry.get(Boolean.class), (Object) true);
		PacketContainer packet = createEntityMetadataPacket(entity.getEntityId(), watcher);
		sendServerPacket(player, packet);
	}

	@Override
	public void showItemCooldown(Player player, ItemStack item, long cooldown) {
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
			PacketContainer packet1 = null, packet2 = null;
			if (entity instanceof Player) {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.NAMED_ENTITY_SPAWN, (Player) entity).createPacket(entity);
				Player player = (Player) entity;
				packet2 = pm.createPacket(PacketType.Play.Server.PLAYER_INFO);
				packet2.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
				List<PlayerInfoData> pInfo = new ArrayList<>();
				WrappedGameProfile profile = WrappedGameProfile.fromPlayer(player).withId(Integer.toString(entity.getEntityId()));
				NativeGameMode mode = NativeGameMode.fromBukkit(player.getGameMode());
				WrappedChatComponent name = WrappedChatComponent.fromText(player.getDisplayName());
				pInfo.add(new PlayerInfoData(profile, 0, mode, name));
				packet2.getPlayerInfoDataLists().write(0, pInfo);
			}
			else if (entity instanceof ExperienceOrb) {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_EXPERIENCE_ORB, (ExperienceOrb) entity).createPacket(entity);
			}
			else {
				packet1 = pm.createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY, entity).createPacket(entity);
			}
			PacketContainer packet3 = createEntityMetadataPacket(entity.getEntityId(), WrappedDataWatcher.getEntityWatcher(entity));
			if (packet2 != null) {
				pm.broadcastServerPacket(packet2, entity, sendToEntity);
			}
			pm.broadcastServerPacket(packet1, entity, sendToEntity);
			pm.broadcastServerPacket(packet3, entity, sendToEntity);
		}
	}
	
	private enum Anchor {
		
		FEET,
		EYES;
		
	}
	
}
