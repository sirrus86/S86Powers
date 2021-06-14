package me.sirrus86.s86powers.tools.nms.v1_17_R1;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTeleportEvent;

public class FollowTamerGoal extends Goal {

	public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
//	private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
//	private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
//	private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
	private final PathfinderMob tamable;
	private LivingEntity owner;
	private final LevelReader level;
	private final double speedModifier;
	private final PathNavigation navigation;
	private int timeToRecalcPath;
	private final float stopDistance;
	private final float startDistance;
	private float oldWaterCost;
	private final boolean canFly;

	public FollowTamerGoal(PathfinderMob entitytameableanimal, Player owner, double d0, float f, float f1, boolean flag) {
		this.tamable = entitytameableanimal;
		this.level = (LevelReader)entitytameableanimal.level;
		this.speedModifier = d0;
		this.navigation = entitytameableanimal.getNavigation();
		this.startDistance = f;
		this.stopDistance = f1;
		this.canFly = flag;
		setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		if (!(entitytameableanimal.getNavigation() instanceof GroundPathNavigation) && !(entitytameableanimal.getNavigation() instanceof FlyingPathNavigation)) {
			throw new IllegalArgumentException("Unsupported mob type for FollowTamerGoal");
		}
	}

	public boolean canUse() {
		LivingEntity entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		if (entityliving.isSpectator()) {
			return false;
		}
		if (this.tamable.distanceToSqr((Entity)entityliving) < (this.startDistance * this.startDistance)) {
			return false;
		}
		return true;
	}

	public boolean canContinueToUse() {
		return this.navigation.isDone() ? false : ((this.tamable.distanceToSqr((Entity)this.owner) > (this.stopDistance * this.stopDistance)));
	}

	public void start() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.tamable.getPathfindingMalus(BlockPathTypes.WATER);
		this.tamable.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
	}

	public void stop() {
		this.owner = null;
		this.navigation.stop();
		this.tamable.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
	}

	public void tick() {
		this.tamable.getLookControl().setLookAt((Entity)this.owner, 10.0F, this.tamable.getMaxHeadXRot());
		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = 10;
			if (!this.tamable.isLeashed() && !this.tamable.isPassenger()) {
				if (this.tamable.distanceToSqr((Entity)this.owner) >= 144.0D) {
					teleportToOwner();
				}
				else {
					this.navigation.moveTo((Entity)this.owner, this.speedModifier);
				}
			}
		}
	}

	private void teleportToOwner() {
		BlockPos blockposition = this.owner.blockPosition();
		for (int i = 0; i < 10; i++) {
			int j = randomIntInclusive(-3, 3);
			int k = randomIntInclusive(-1, 1);
			int l = randomIntInclusive(-3, 3);
			boolean flag = maybeTeleportTo(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l);
			if (flag) {
				return;
			}
		} 
	}

	private boolean maybeTeleportTo(int i, int j, int k) {
		if (Math.abs(i - this.owner.getX()) < 2.0D && Math.abs(k - this.owner.getZ()) < 2.0D) {
			return false;
		}
		if (!canTeleportTo(new BlockPos(i, j, k))) {
			return false;
		}
		CraftEntity entity = this.tamable.getBukkitEntity();
		Location to = new Location(entity.getWorld(), i + 0.5D, j, k + 0.5D, this.tamable.getYRot(), this.tamable.getXRot());
		EntityTeleportEvent event = new EntityTeleportEvent((org.bukkit.entity.Entity)entity, entity.getLocation(), to);
		this.tamable.level.getCraftServer().getPluginManager().callEvent((Event)event);
		if (event.isCancelled()) {
			return false;
		}
		to = event.getTo();
		this.tamable.moveTo(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
		this.navigation.stop();
		return true;
	}

	private boolean canTeleportTo(BlockPos blockposition) {
		BlockPathTypes pathtype = WalkNodeEvaluator.getBlockPathTypeStatic((BlockGetter)this.level, blockposition.mutable());
		if (pathtype != BlockPathTypes.WALKABLE) {
			return false;
		}
		BlockState iblockdata = this.level.getBlockState(blockposition.down());
		if (!this.canFly && iblockdata.getBlock() instanceof LeavesBlock) {
			return false;
		}
		BlockPos blockposition1 = blockposition.e((Vec3i)this.tamable.blockPosition());
		return this.level.noCollision((Entity)this.tamable, this.tamable.getBoundingBox().move(blockposition1));
	}

	private int randomIntInclusive(int i, int j) {
		return this.tamable.getRandom().nextInt(j - i + 1) + i;
	}
}
