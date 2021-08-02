package me.sirrus86.s86powers.tools.nms.v1_17;

import java.util.EnumSet;

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
import net.minecraft.world.level.pathfinder.PathfinderNormal;
import net.minecraft.world.level.pathfinder.PathType;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityTeleportEvent;

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
		this.g = (IWorldReader)entitytameableanimal.t;
		this.h = d0;
		this.i = entitytameableanimal.getNavigation();
		this.l = f;
		this.k = f1;
		this.n = flag;
		a(EnumSet.of(PathfinderGoal.Type.a, PathfinderGoal.Type.b));
		this.owner = owner;
		if (!(entitytameableanimal.getNavigation() instanceof Navigation) && !(entitytameableanimal.getNavigation() instanceof NavigationFlying)) {
			throw new IllegalArgumentException("Unsupported mob type for PathfinderGoalFollowTamer");
		}
	}

	public boolean a() {
		EntityLiving entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		if (entityliving.isSpectator()) {
			return false;
		}
		if (this.e.f((Entity)entityliving) < (this.l * this.l)) {
			return false;
		}
		this.f = entityliving;
		return true;
	}

	public boolean b() {
		return this.i.m() ? false : ((this.e.f((Entity)this.f) > (this.k * this.k)));
	}

	public void c() {
		this.j = 0;
		this.m = this.e.a(PathType.i);
		this.e.a(PathType.i, 0.0F);
	}

	public void d() {
		this.f = null;
		this.i.o();
		this.e.a(PathType.i, this.m);
	}

	public void e() {
		this.e.getControllerLook().a((Entity)this.f, 10.0F, this.e.eY());
		if (--this.j <= 0) {
			this.j = 10;
			if (!this.e.isLeashed() && !this.e.isPassenger()) {
				if (this.e.f((Entity)this.owner) >= 144.0D) {
					g();
				}
				else {
					this.i.a((Entity)this.f, this.h);
				}
			}
		}
	}

	private void g() {
		BlockPosition blockposition = this.f.getChunkCoordinates();
		for (int i = 0; i < 10; i++) {
			int j = a(-3, 3);
			int k = a(-1, 1);
			int l = a(-3, 3);
			boolean flag = a(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l);
			if (flag) {
				return;
			}
		} 
	}

	private boolean a(int i, int j, int k) {
		if (Math.abs(i - this.f.locX()) < 2.0D && Math.abs(k - this.f.locZ()) < 2.0D) {
			return false;
		}
		if (!a(new BlockPosition(i, j, k))) {
			return false;
		}
		CraftEntity entity = this.e.getBukkitEntity();
		Location to = new Location(entity.getWorld(), i + 0.5D, j, k + 0.5D, this.e.getYRot(), this.e.getXRot());
		EntityTeleportEvent event = new EntityTeleportEvent((org.bukkit.entity.Entity)entity, entity.getLocation(), to);
		this.e.t.getCraftServer().getPluginManager().callEvent((Event)event);
		if (event.isCancelled()) {
			return false;
		}
		to = event.getTo();
		this.e.setPositionRotation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
		this.i.o();
		return true;
	}

	private boolean a(BlockPosition blockposition) {
		PathType pathtype = PathfinderNormal.a((IBlockAccess)this.g, blockposition.i());
		if (pathtype != PathType.c) {
			return false;
		}
		IBlockData iblockdata = this.g.getType(blockposition.down());
		if (!this.n && iblockdata.getBlock() instanceof BlockLeaves) {
			return false;
		}
		BlockPosition blockposition1 = blockposition.e((BaseBlockPosition)this.e.getChunkCoordinates());
		return this.g.getCubes((Entity)this.e, this.e.getBoundingBox().a(blockposition1));
	}

	private int a(int i, int j) {
		return this.e.getRandom().nextInt(j - i + 1) + i;
	}
}
