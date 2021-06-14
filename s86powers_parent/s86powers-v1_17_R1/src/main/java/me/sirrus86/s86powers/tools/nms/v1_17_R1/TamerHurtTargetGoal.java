package me.sirrus86.s86powers.tools.nms.v1_17_R1;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import org.bukkit.event.entity.EntityTargetEvent;

public class TamerHurtTargetGoal  extends TargetGoal {
	
	private LivingEntity ownerLastHurt;
	private int timestamp;
	private Player owner;

	public TamerHurtTargetGoal(PathfinderMob entitytameableanimal, Player owner) {
		super(entitytameableanimal, false);
		this.owner = owner;
		setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	public boolean canUse() {
		LivingEntity entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		this.ownerLastHurt = entityliving.getLastHurtMob();
		int i = entityliving.getLastHurtMobTimestamp();
		return (i != this.timestamp && canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT));
	}

	public void start() {
		this.mob.setGoalTarget(this.ownerLastHurt, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true);
		LivingEntity entityliving = this.owner;
		if (entityliving != null) {
			this.timestamp = entityliving.getLastHurtMobTimestamp();
		}
		super.start();
	}
}
