package me.sirrus86.s86powers.tools.packets;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.sirrus86.s86powers.S86Powers;
import me.sirrus86.s86powers.tools.PowerTools;
import me.sirrus86.s86powers.tools.nms.NMSLibrary;

public abstract class PacketManager {
	
	Map<UUID, LivingEntity> control = new HashMap<>();
	Map<UUID, Map<Block, Integer>> spectralBlocks = new HashMap<>();
	Set<UUID> ghosts = new HashSet<>(),
			hidden = new HashSet<>();

	final S86Powers plugin = JavaPlugin.getPlugin(S86Powers.class);
	final NMSLibrary nms = PowerTools.getNMSLibrary();

	public abstract void addDisguise(Entity entity, EntityType type);
	
	public abstract void addDisguise(Entity entity, EntityType type, Map<Integer, Object> meta);
	
	public abstract void addDisguise(Entity entity, EntityType type, Map<Integer, Object> meta, Object data);
	
	public abstract void addDisguise(Entity entity, ItemStack item);
	
	public abstract void addDisguise(Entity entity, Entity target);
	
	public abstract void addEquipmentDisguise(Entity entity, LivingEntity target);
	
	public abstract void addGhost(Player player);
	
	public abstract void addSpectralBlock(Player viewer, Block block, ChatColor color);
	
	public abstract void addSpectralEntity(Player viewer, Entity entity, ChatColor color);
	
	public abstract void blockDisguise(Block block, Material material);
	
	public abstract void blockDisguise(Collection<Block> blocks, Material material, BlockData data);
	
	public abstract void blockUpdate(Block block);
	
	public abstract void blockUpdate(Collection<Block> blocks);
	
	public abstract void fakeCollect(Entity entity, Item item);
	
	public abstract void fakeExplosion(Location loc, float radius);
	
	public abstract boolean hasDisguise(Block block);
	
	public abstract boolean hasDisguise(Entity entity);
	
	public abstract void hide(Entity entity);
	
	public boolean isGhost(Player player) {
		return ghosts.contains(player.getUniqueId());
	}
	
	public abstract void removeDisguise(Entity entity);
	
	public abstract void removeGhost(Player player);
	
	public abstract void removeSpectralBlock(Player viewer, Block block);
	
	public abstract void removeSpectralEntity(Player viewer, Entity entity);
	
	public abstract void setCamera(Player player, Entity entity);
	
	public void setControlling(Player player, LivingEntity entity) {
		control.put(player.getUniqueId(), entity);
	}
	
	public abstract void setLook(Player player, Location loc);
	
	public abstract void showActionBarMessage(Player player, String message);
	
	public abstract void showHearts(LivingEntity entity, Player player);
	
	public abstract void showItemCooldown(Player player, ItemStack item, long cooldown);
	
}
