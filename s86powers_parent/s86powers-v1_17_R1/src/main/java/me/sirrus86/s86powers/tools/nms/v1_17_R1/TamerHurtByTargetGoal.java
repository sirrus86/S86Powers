package me.sirrus86.s86powers.tools.nms.v1_17_R1;

import java.util.EnumSet;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import org.bukkit.event.entity.EntityTargetEvent;

public class TamerHurtByTargetGoal extends TargetGoal {

	private LivingEntity ownerLastHurtBy;
	private int timestamp;
	private Player owner;

	public TamerHurtByTargetGoal(PathfinderMob entitytameableanimal, Player owner) {
		super(entitytameableanimal, false);
		this.owner = owner;
		setFlags(EnumSet.of(Goal.Flag.TARGET));
	}

	public boolean canUse() {
		LivingEntity entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		this.ownerLastHurtBy = entityliving.getLastHurtByMob();
		int i = entityliving.getLastHurtByMobTimestamp();
		return (i != this.timestamp && canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT));
	}

	public void start() {
		this.mob.setGoalTarget(this.ownerLastHurtBy, EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER, true);
		LivingEntity entityliving = this.owner;
		if (entityliving != null)
			this.timestamp = entityliving.getLastHurtByMobTimestamp(); 
		super.start();
	}
}
