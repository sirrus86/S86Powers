package me.sirrus86.s86powers.tools.packets;

import java.util.Collection;
import java.util.Map;

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

/**
 * <p>Packet Manager used when no packet management plugin is detected.</p>
 * <p>Methods are intentionally left blank to prevent errors.</p>
 *
 */
public class PacketManagerNull extends PacketManager {
	
	@Override
	public void addDisguise(Entity entity, EntityType type) {
	
	}

	@Override
	public void addDisguise(Entity entity, EntityType type, Map<Integer, Object> meta) {
		
	}

	@Override
	public void addDisguise(Entity entity, EntityType type, Map<Integer, Object> meta, Object data) {
		
	}

	@Override
	public void addDisguise(Entity entity, ItemStack item) {
		
	}

	@Override
	public void addDisguise(Entity entity, Entity target) {
		
	}

	@Override
	public void addEquipmentDisguise(Entity entity, LivingEntity target) {
		
	}

	@Override
	public void addGhost(Player player) {
		
	}

	@Override
	public void addSpectralBlock(Player viewer, Block block, ChatColor color) {
		
	}

	@Override
	public void addSpectralEntity(Player viewer, Entity entity, ChatColor color) {
		
	}

	@Override
	public void blockDisguise(Block block, Material material) {
		
	}

	@Override
	public void blockDisguise(Collection<Block> blocks, Material material, BlockData data) {
		
	}

	@Override
	public void blockUpdate(Block block) {
		
	}

	@Override
	public void blockUpdate(Collection<Block> blocks) {
		
	}

	@Override
	public void fakeCollect(Entity entity, Item item) {
		
	}

	@Override
	public void fakeExplosion(Location loc, float radius) {
		
	}

	@Override
	public boolean hasDisguise(Block block) {
		return false;
	}

	@Override
	public void hide(Entity entity) {
		
	}

	@Override
	public void removeDisguise(Entity entity) {
		
	}

	@Override
	public void removeGhost(Player player) {
		
	}

	@Override
	public void removeSpectralBlock(Player viewer, Block block) {
		
	}

	@Override
	public void removeSpectralEntity(Player viewer, Entity entity) {
		
	}

	@Override
	public void setCamera(Player player, Entity entity) {
		
	}

	@Override
	public void setLook(Player player, Location loc) {
		
	}

	@Override
	public void showActionBarMessage(Player player, String message) {
		
	}

	@Override
	public void showHearts(LivingEntity entity, Player player) {
		
	}

	@Override
	public void showItemCooldown(Player player, ItemStack item, long cooldown) {
		
	}

}
