package me.sirrus86.s86powers.tools.version;

import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pose;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Particle;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MCMetadata {
	
	private final Map<Integer, Object> map = new HashMap<>();
	
	public Map<Integer, Object> getMap() {
		return map;
	}
	
	public void setEntry(EntityMeta meta, Object value) {
		if (meta.getIndex() >= 0
				&& meta.getType().isInstance(value)) {
			map.put(meta.getIndex(), value);
		}
	}
	
	public enum EntityMeta {
		
		ENTITY_STATE(0,0,0,0,0,0,0,Byte.class),
		ENTITY_AIR(1,1,1,1,1,1,1,Integer.class),
		ENTITY_CUSTOM_NAME(2,2,2,2,2,2,2,String.class), //TODO OptChat
		ENTITY_IS_CUSTOM_NAME_VISIBLE(3,3,3,3,3,3,3,Boolean.class),
		ENTITY_IS_SILENT(4,4,4,4,4,4,4,Boolean.class),
		ENTITY_NO_GRAVITY(5,5,5,5,5,5,5,Boolean.class),
		ENTITY_POSE(-1,6,6,6,6,6,6,Pose.class),
		ENTITY_FROZEN_IN_SNOW(-1,-1,-1,-1,7,7,7,Integer.class),
		
		THROWABLE_ITEM(-1,7,7,7,8,8,8,ItemStack.class),
		
		THROWABLE_POTION_ITEM(6,7,7,7,8,8,8,ItemStack.class),
		
		FALLINGBLOCK_POSITION(6,7,7,7,8,8,8,BlockVector.class),
		
		AREAEFFECTCLOUD_RADIUS(6,7,7,7,8,8,8,Float.class),
		AREAEFFECTCLOUD_COLOR(7,8,8,8,9,9,9,Integer.class),
		AREAEFFECTCLOUD_IGNORE_RADIUS(8,9,9,9,10,10,10,Boolean.class),
		AREAEFFECTCLOUD_PARTICLE(9,10,10,10,11,11,11,Particle.class), //TODO Particle data

		FISHINGHOOK_HOOKED_ENTITY_ID(6,7,7,7,8,8,8,Integer.class),
		FISHINGHOOK_IS_CATCHABLE(-1,-1,-1,8,9,9,9,Boolean.class),
		
		ARROW_OPTIONS(6,7,7,7,8,8,8,Byte.class),
		ARROW_SHOOTER_UUID(7,8,8,-1,-1,-1,-1,UUID.class), //TODO OptUUID
		ARROW_PIERCING_LEVEL(-1,9,9,8,9,9,9,Byte.class),

		TIPPEDARROW_COLOR(8,10,10,9,10,10,10,Integer.class),

		TRIDENT_LOYALTY_LEVEL(8,10,10,9,10,10,10,Integer.class),
		TRIDENT_IS_ENCHANTED(-1,-1,11,10,11,11,11,Boolean.class),
		
		BOAT_TIME_SINCE_LAST_HIT(6,7,7,7,8,8,8,Integer.class),
		BOAT_FORWARD_DIRECTION(7,8,8,8,9,9,9,Integer.class),
		BOAT_DAMAGE_TAKEN(8,9,9,9,10,10,10,Float.class),
		BOAT_TYPE(9,10,10,10,11,11,11,Integer.class),
		BOAT_LEFT_PADDLE_TURNING(10,11,11,11,12,12,12,Boolean.class),
		BOAT_RIGHT_PADDLE_TURNING(11,12,12,12,13,13,13,Boolean.class),
		BOAT_SPLASH_TIMER(12,13,13,13,14,14,14,Integer.class),

		ENDERCRYSTAL_BEAM_TARGET(6,7,7,7,8,8,8,BlockVector.class), //TODO OptBlockPos
		ENDERCRYSTAL_SHOW_BOTTOM(7,8,8,8,9,9,9,Boolean.class),
		
		FIREBALL_ITEM(-1,7,7,7,8,8,8,ItemStack.class),

		WITHERSKULL_INVULNERABLE(6,7,7,7,8,8,8,Boolean.class),

		FIREWORK_ITEM(6,7,7,7,8,8,8,ItemStack.class),
		FIREWORK_SHOOTER_ENTITY_ID(7,8,8,8,9,9,9,Integer.class), //TODO OptVarInt
		FIREWORK_SHOT_AT_ANGLE(-1,9,9,9,10,10,10,Boolean.class),

		ITEMFRAME_ITEM(6,7,7,7,8,8,8,ItemStack.class),
		ITEMFRAME_ROTATION(7,8,8,8,9,9,9,Integer.class),
		
		PAINTING_TYPE(-1,-1,-1,-1,-1,-1,8,Object.class),
		
		DROPPEDITEM_ITEM(6,7,7,7,8,8,8,ItemStack.class),
		
		LIVINGENTITY_HAND_STATE(6,7,7,7,8,8,8,Byte.class),
		LIVINGENTITY_HEALTH(7,8,8,8,9,9,9,Float.class),
		LIVINGENTITY_POTION_EFFECT_COLOR(8,9,9,9,10,10,10,Integer.class),
		LIVINGENTITY_IS_POTION_EFFECT_AMBIENT(9,10,10,10,11,11,11,Boolean.class),
		LIVINGENTITY_ARROWS_IN_ENTITY(10,11,11,11,12,12,12,Integer.class),
		LIVINGENTITY_BEE_STINGERS_IN_ENTITY(-1,-1,-1,-1,13,13,13,Integer.class),
		LIVINGENTITY_ABSORPTION_AMOUNT(-1,-1,12,12,-1,-1,-1,Integer.class),
		LIVINGENTITY_BED_LOCATION(-1,12,13,13,14,14,14,BlockVector.class), //TODO OptBlockPos

		PLAYER_ADDITIONAL_HEARTS(11,13,14,14,15,15,15,Float.class),
		PLAYER_SCORE(12,14,15,15,16,16,16,Integer.class),
		PLAYER_DISPLAYED_SKIN_PARTS(13,15,16,16,17,17,17,Byte.class),
		PLAYER_MAIN_HAND(14,16,17,17,18,18,18,Byte.class),
		PLAYER_LEFT_SHOULDER_ENTITY(15,17,18,18,19,19,19,Entity.class), //TODO NBT
		PLAYER_RIGHT_SHOULDER_ENTITY(16,18,19,19,20,20,20,Entity.class), //TODO NBT

		ARMORSTAND_OPTIONS(11,13,14,14,15,15,15,Byte.class),
		ARMORSTAND_HEAD_ROTATION(12,14,15,15,16,16,16,Vector.class), //TODO Rotation
		ARMORSTAND_BODY_ROTATION(13,15,16,16,17,17,17,Vector.class), //TODO Rotation
		ARMORSTAND_LEFT_ARM_ROTATION(14,16,17,17,18,18,18,Vector.class), //TODO Rotation
		ARMORSTAND_RIGHT_ARM_ROTATION(15,17,18,18,19,19,19,Vector.class), //TODO Rotation
		ARMORSTAND_LEFT_LEG_ROTATION(16,18,19,19,20,20,20,Vector.class), //TODO Rotation
		ARMORSTAND_RIGHT_LEG_ROTATION(17,19,20,20,21,21,21,Vector.class), //TODO Rotation

		MOB_STATE(11,13,14,14,15,15,15,Byte.class),

		BAT_IS_HANGING(12,14,15,15,16,16,16,Byte.class),

		DOLPHIN_TREASURE_POSITION(12,14,15,15,16,16,16,Vector.class), //TODO Position
		DOLPHIN_CAN_FIND_TREASURE(13,15,16,16,17,17,17,Boolean.class),
		DOLPHIN_HAS_FISH(14,16,17,17,18,18,18,Boolean.class),

		FISH_FROM_BUCKET(12,14,15,15,16,16,16,Boolean.class),

		PUFFERFISH_PUFF_STATE(13,15,16,16,17,17,17,Integer.class),

		TROPICALFISH_VARIANT(13,15,16,16,17,17,17,Integer.class),

		AGEABLE_IS_BABY(12,14,15,15,16,16,16,Boolean.class),

		HORSE_OPTIONS(13,15,16,16,17,17,17,Byte.class),
		HORSE_OWNER_UUID(14,16,17,17,18,18,18,UUID.class), //TODO: OptUUID

		HORSE_VARIANT(15,17,18,18,19,19,19,Integer.class),
		HORSE_ARMOR(16,-1,-1,-1,-1,-1,-1,Integer.class),
		HORSE_ARMOR_ITEM(17,-1,-1,-1,-1,-1,-1,ItemStack.class),
		
		CHESTEDHORSE_HAS_CHEST(15,17,18,18,19,19,19,Boolean.class),
		
		LLAMA_STRENGTH(16,18,19,19,20,20,20,Integer.class),
		LLAMA_CARPET_COLOR(17,19,20,20,21,21,21,Integer.class),
		LLAMA_VARIANT(18,20,21,21,22,22,22,Integer.class),
		
		AXOLOTL_VARIANT(-1,-1,-1,-1,17,17,17,Integer.class),
		AXOLOTL_PLAYING_DEAD(-1,-1,-1,-1,18,18,18,Boolean.class),
		AXOLOTL_SPAWNED_FROM_BUCKET(-1,-1,-1,-1,19,19,19,Boolean.class),

		BEE_STATE(-1,-1,16,16,17,17,17,Byte.class),
		BEE_ANGER_TIME(-1,-1,17,17,18,18,18,Integer.class),

		FOX_TYPE(-1,15,16,16,17,17,17,Integer.class),
		FOX_STATE(-1,16,17,17,18,18,18,Byte.class),
		FOX_FIRST_UUID(-1,17,18,18,19,19,19,UUID.class), //TODO: OptUUID
		FOX_SECOND_UUID(-1,18,19,19,20,20,20,UUID.class), //TODO: OptUUID
		
		OCELOT_IS_TRUSTING(-1,15,16,16,17,17,17,Boolean.class),

		PANDA_BREED_TIMER(-1,15,16,16,17,17,17,Integer.class),
		PANDA_SNEEZE_TIMER(-1,16,17,17,18,18,18,Integer.class),
		PANDA_EAT_TIMER(-1,17,18,18,19,19,19,Integer.class),
		PANDA_MAIN_GENE(-1,18,19,19,20,20,20,Byte.class),
		PANDA_HIDDEN_GENE(-1,19,20,20,21,21,21,Byte.class),
		PANDA_STATE(-1,20,21,21,22,22,22,Byte.class),

		PIG_HAS_SADDLE(13,15,16,16,17,17,17,Boolean.class),
		PIG_CARROT_BOOST_TIME(14,16,17,17,18,18,18,Integer.class),
		
		RABBIT_TYPE(13,15,16,16,17,17,17,Integer.class),
		
		TURTLE_HOME_POSITION(13,15,16,16,17,17,17,BlockVector.class),
		TURTLE_HAS_EGG(14,16,17,17,18,18,18,Boolean.class),
		TURTLE_IS_LAYING_EGG(15,17,18,18,19,19,19,Boolean.class),
		TURTLE_TRAVEL_POSITION(16,18,19,19,20,20,20,BlockVector.class),
		TURTLE_IS_GOING_HOME(17,19,20,20,21,21,21,Boolean.class),
		TURTLE_IS_TRAVELING(18,20,21,21,22,22,22,Boolean.class),
		
		POLARBEAR_STANDING_UP(13,15,16,16,17,17,17,Boolean.class),
		
		HOGLIN_IMMUNE_TO_ZOMBIFICATION(-1,-1,-1,16,17,17,17,Boolean.class),

		MOOSHROOM_VARIANT(-1,15,16,16,17,17,17,String.class),
		
		SHEEP_OPTIONS(13,15,16,16,17,17,17,Byte.class),

		STRIDER_BOOST_TIME(-1,-1,-1,16,17,17,17,Integer.class),
		STRIDER_IS_SHAKING(-1,-1,-1,17,18,18,18,Boolean.class),
		STRIDER_HAS_SADDLE(-1,-1,-1,18,19,19,19,Boolean.class),
		
		TAMEABLE_STATE(13,15,16,16,17,17,17,Byte.class),
		TAMEABLE_OWNER_UUID(14,16,17,17,18,18,18,UUID.class), //TODO: OptUUID

		CAT_TYPE(15,17,18,18,19,19,19,Integer.class),
		CAT_IS_LYING(-1,18,19,19,20,20,20,Boolean.class),
		CAT_IS_RELAXED(-1,19,20,20,21,21,21,Boolean.class),
		CAT_COLLAR_COLOR(-1,20,21,21,22,22,22,Integer.class),

		WOLF_DAMAGE_TAKEN(15,17,-1,-1,-1,-1,-1,Float.class),
		WOLF_IS_BEGGING(16,18,18,18,19,19,19,Boolean.class),
		WOLF_COLLAR_COLOR(17,19,19,19,20,20,20,Integer.class),
		WOLF_ANGER_TIME(-1,-1,-1,20,21,21,21,Integer.class),

		PARROT_VARIANT(15,17,18,18,19,19,19,Integer.class),

		VILLAGER_HEAD_SHAKE_TIMER(-1,15,16,16,17,17,17,Integer.class),
		VILLAGER_PROFESSION(13,-1,-1,-1,-1,-1,-1,Integer.class),
		VILLAGER_DATA(-1,16,17,17,18,18,18,Villager.class), //TODO VillagerData

		IRONGOLEM_IS_PLAYER_CREATED(12,14,15,15,16,16,16,Byte.class),

		SNOWGOLEM_STATE(12,14,15,15,16,16,16,Byte.class),

		SHULKER_ATTACH_FACE(12,14,15,15,16,16,16,BlockFace.class),
		SHULKER_ATTACH_POSITION(13,15,16,16,17,17,17,BlockVector.class), //TODO: OptPosition
		SHULKER_SHIELD_HEIGHT(14,16,17,17,18,18,18,Byte.class),
		SHULKER_COLOR(15,17,18,18,19,19,19,Byte.class),
		
		PIGLIN_IMMUNE_TO_ZOMBIFICATION(-1,-1,-1,15,16,16,16,Boolean.class),
		PIGLIN_IS_BABY(-1,-1,-1,16,17,17,17,Boolean.class),
		PIGLIN_IS_CHARGING_CROSSBOW(-1,-1,-1,17,18,18,18,Boolean.class),
		PIGLIN_IS_DANCING(-1,-1,-1,18,19,19,19,Boolean.class),

		BLAZE_IS_ON_FIRE(12,14,15,15,16,16,16,Byte.class),

		CREEPER_STATE(12,14,15,15,16,16,16,Integer.class),
		CREEPER_IS_CHARGED(13,15,16,16,17,17,17,Boolean.class),
		CREEPER_IS_IGNITED(14,16,17,17,18,18,18,Boolean.class),
		
		GOAT_IS_SCREAMING_GOAT(-1,-1,-1,-1,-1,-1,17,Boolean.class),
        GOAT_HAS_LEFT_HORN(-1,-1,-1,-1,-1,-1,18,Boolean.class),
        GOAT_HAS_RIGHT_HORN(-1,-1,-1,-1,-1,-1,19,Boolean.class),

		GUARDIAN_IS_RETRACTING_SPIKES(12,14,15,15,16,16,16,Boolean.class),
		GUARDIAN_TARGET_ENTITY_ID(13,15,16,16,17,17,17,Integer.class),

		RAIDER_IS_CELEBRATING(-1,14,15,15,16,16,16,Boolean.class),

		ILLAGER_HAS_TARGET(12,-1,-1,-1,-1,-1,-1,Byte.class),
		
		PILLAGER_IS_CHARGING(-1,-1,-1,-1,17,17,17,Boolean.class),

		SPELLCASTER_ILLAGER_SPELL(13,15,16,16,17,17,17,Byte.class),

		WITCH_IS_DRINKING_POTION(12,15,16,16,17,17,17,Boolean.class),

		VEX_IS_IN_ATTACK_MODE(12,14,15,15,16,16,16,Byte.class),

		SKELETON_IS_SWINGING_ARMS(12,-1,-1,-1,-1,-1,-1,Boolean.class),

		SPIDER_IS_CLIMBING(12,14,15,15,16,16,16,Byte.class),

		WITHER_CENTER_HEAD_TARGET_ENTITY_ID(12,14,15,15,16,16,16,Integer.class),
		WITHER_LEFT_HEAD_TARGET_ENTITY_ID(13,15,16,16,17,17,17,Integer.class),
		WITHER_RIGHT_HEAD_TARGET_ENTITY_ID(14,16,17,17,18,18,18,Integer.class),
		WITHER_INVULNERABLE_TIME(15,17,18,18,19,19,19,Integer.class),
		
		ZOGLIN_IS_BABY(-1,-1,-1,15,16,16,16,Boolean.class),

		ZOMBIE_IS_BABY(12,14,15,15,16,16,16,Boolean.class),
		ZOMBIE_TYPE(13,15,16,16,17,17,17,Integer.class),
		ZOMBIE_HAS_HANDS_UP(14,-1,-1,-1,-1,-1,-1,Boolean.class),
		ZOMBIE_IS_DROWNING(15,16,17,17,18,18,18,Boolean.class),
		
		ZOMBIE_VILLAGER_IS_CONVERTING(16,17,18,18,19,19,19,Boolean.class),
		ZOMBIE_VILLAGER_PROFESSION(17,18,-1,-1,-1,-1,-1,Integer.class),
		ZOMBIE_VILLAGER_DATA(-1,-1,19,19,20,20,20,Villager.class), //TODO VillagerData

		ENDERMAN_CARRIED_BLOCK(12,14,15,15,16,16,16,BlockState.class), //TODO: OptBlock
		ENDERMAN_IS_SCREAMING(13,15,16,16,17,17,17,Boolean.class),
		ENDERMAN_IS_STARING(-1,-1,17,17,18,18,18,Boolean.class),

		ENDERDRAGON_PHASE(12,14,15,15,16,16,16,Integer.class),

		GHAST_IS_ATTACKING(12,14,15,15,16,16,16,Boolean.class),

		PHANTOM_SIZE(12,14,15,15,16,16,16,Integer.class),

		SLIME_SIZE(12,14,15,15,16,16,16,Integer.class),

		MINECART_SHAKING_POWER(6,7,7,7,8,8,8,Integer.class),
		MINECART_SHAKING_DIRECTION(7,8,8,8,9,9,9,Integer.class),
		MINECART_SHAKING_MULTIPLIER(8,9,9,9,10,10,10,Float.class),
		MINECART_BLOCK_ID(9,10,10,10,11,11,11,Integer.class),
		MINECART_BLOCK_Y_POSITION(10,11,11,11,12,12,12,Integer.class),
		MINECART_SHOW_CUSOM_BLOCK(11,12,12,12,13,13,13,Boolean.class),
		
		MINECARTFURNACE_HAS_FUEL(12,13,13,13,14,14,14,Boolean.class),
		
		MINECARTCOMMANDBLOCK_COMMAND(12,13,13,13,14,14,14,String.class),
		MINECARTCOMMANDBLOCK_LAST_OUTPUT(12,14,14,14,15,15,15,String.class), //TODO: Chat

		TNTPRIMED_FUSE_TIME(6,7,7,7,8,8,8,Integer.class);
		
		private final int v1_13, v1_14, v1_15, v1_16, v1_17, v1_18, v1_19;
		private final Class<?> type;
		
		private EntityMeta(int v1_13, int v1_14, int v1_15, int v1_16, int v1_17, int v1_18, int v1_19, Class<?> type) {
			this.v1_13 = v1_13;
			this.v1_14 = v1_14;
			this.v1_15 = v1_15;
			this.v1_16 = v1_16;
			this.v1_17 = v1_17;
			this.v1_18 = v1_18;
			this.v1_19 = v1_19;
			this.type = type;
		}
		
		public int getIndex() {
			switch(MCVersion.CURRENT_VERSION) {
				case v1_13: case v1_13_1: case v1_13_2:
					return this.v1_13;
				case v1_14: case v1_14_1: case v1_14_2: case v1_14_3: case v1_14_4:
					return this.v1_14;
				case v1_15: case v1_15_1: case v1_15_2:
					return this.v1_15;
				case v1_16_1: case v1_16_2: case v1_16_3: case v1_16_4: case v1_16_5:
					return this.v1_16;
				case v1_17: case v1_17_1:
					return this.v1_17;
				case v1_18: case v1_18_1: case v1_18_2:
					return this.v1_18;
				case v1_19: case v1_19_1: case v1_19_2: default:
					return this.v1_19;
			}
		}
		
		public Class<?> getType() {
			return this.type;
		}
		
	}
	
}
