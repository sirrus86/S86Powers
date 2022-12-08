package me.sirrus86.s86powers.tools.nms.v1_19_3;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTeleportEvent;

import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.navigation.Navigation;
import net.minecraft.world.entity.ai.navigation.NavigationAbstract;
import net.minecraft.world.entity.ai.navigation.NavigationFlying;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.IWorldReader;
import net.minecraft.world.level.block.BlockLeaves;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfinderNormal;

public class PathfinderGoalFollowTamer extends PathfinderGoal {

	public static final int a = 12;
//	private static final int b = 2;
//	private static final int c = 3;
//	private static final int d = 1;
	private final EntityInsentient e;
	private EntityLiving f;
	private final IWorldReader g;
	private final double h;
	private final NavigationAbstract i;
	private int j;
	private final float k;
	private final float l;
	private float m;
	private final boolean n;
	private EntityHuman owner;

	public PathfinderGoalFollowTamer(EntityInsentient entitytameableanimal, EntityHuman owner, double d0, float f, float f1, boolean flag) {
		this.e = entitytameableanimal;
		this.g = (IWorldReader)entitytameableanimal.s;
		this.h = d0;
		this.i = entitytameableanimal.E();
		this.l = f;
		this.k = f1;
		this.n = flag;
		a(EnumSet.of(PathfinderGoal.Type.a, PathfinderGoal.Type.b));
		this.owner = owner;
		if (!(entitytameableanimal.E() instanceof Navigation) && !(entitytameableanimal.E() instanceof NavigationFlying)) {
			throw new IllegalArgumentException("Unsupported mob type for PathfinderGoalFollowTamer");
		}
	}

	public boolean a() {
		EntityLiving entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		if (entityliving.B_()) {
			return false;
		}
		if (this.e.f((Entity)entityliving) < (this.l * this.l)) {
			return false;
		}
		this.f = entityliving;
		return true;
	}

	public boolean b() {
		return this.i.l() ? false : ((this.e.f((Entity)this.f) > (this.k * this.k)));
	}

	public void c() {
		this.j = 0;
		this.m = this.e.a(PathType.j);
		this.e.a(PathType.j, 0.0F);
	}

	public void d() {
		this.f = null;
		this.i.n();
		this.e.a(PathType.j, this.m);
	}

	public void e() {
		this.e.A().a((Entity)this.f, 10.0F, this.e.V());
		if (--this.j <= 0) {
			this.j = a(10);
			if (!this.e.fC() && !this.e.bL()) {
				if (this.e.f((Entity)this.owner) >= 144.0D) {
					h();
				}
				else {
					this.i.a((Entity)this.f, this.h);
				}
			}
		}
	}

	private void h() {
		BlockPosition blockposition = this.f.df();
		for (int i = 0; i < 10; i++) {
			int j = a(-3, 3);
			int k = a(-1, 1);
			int l = a(-3, 3);
			boolean flag = a(blockposition.u() + j, blockposition.v() + k, blockposition.w() + l);
			if (flag) {
				return;
			}
		} 
	}

	private boolean a(int i, int j, int k) {
		if (Math.abs(i - this.f.dk()) < 2.0D && Math.abs(k - this.f.dq()) < 2.0D) {
			return false;
		}
		if (!a(new BlockPosition(i, j, k))) {
			return false;
		}
		CraftEntity entity = this.e.getBukkitEntity();
		Location to = new Location(entity.getWorld(), i + 0.5D, j, k + 0.5D, this.e.dv(), this.e.dx());
		EntityTeleportEvent event = new EntityTeleportEvent((org.bukkit.entity.Entity)entity, entity.getLocation(), to);
		this.e.s.getCraftServer().getPluginManager().callEvent((Event)event);
		if (event.isCancelled()) {
			return false;
		}
		to = event.getTo();
		this.e.b(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
		this.i.n();
		return true;
	}

	private boolean a(BlockPosition blockposition) {
		PathType pathtype = PathfinderNormal.a((IBlockAccess)this.g, blockposition.j());
		if (pathtype != PathType.c) {
			return false;
		}
		IBlockData iblockdata = this.g.a_(blockposition.d());
		if (!this.n && iblockdata.b() instanceof BlockLeaves) {
			return false;
		}
		BlockPosition blockposition1 = blockposition.b((BaseBlockPosition)this.e.df());
		return this.g.a((Entity)this.e, this.e.cD().a(blockposition1));
	}

	private int a(int i, int j) {
		return this.e.dV().a(j - i + 1) + i;
	}
}