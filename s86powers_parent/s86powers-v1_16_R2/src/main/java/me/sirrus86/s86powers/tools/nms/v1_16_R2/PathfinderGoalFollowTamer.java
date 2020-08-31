package me.sirrus86.s86powers.tools.nms.v1_16_R2;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import net.minecraft.server.v1_16_R2.BlockLeaves;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.EntityHuman;
import net.minecraft.server.v1_16_R2.EntityInsentient;
import net.minecraft.server.v1_16_R2.EntityLiving;
import net.minecraft.server.v1_16_R2.IBlockData;
import net.minecraft.server.v1_16_R2.IWorldReader;
import net.minecraft.server.v1_16_R2.Navigation;
import net.minecraft.server.v1_16_R2.NavigationAbstract;
import net.minecraft.server.v1_16_R2.NavigationFlying;
import net.minecraft.server.v1_16_R2.PathType;
import net.minecraft.server.v1_16_R2.PathfinderGoal;
import net.minecraft.server.v1_16_R2.PathfinderNormal;

public class PathfinderGoalFollowTamer extends PathfinderGoal {

	private final EntityInsentient a;
	private EntityLiving b;
	protected final IWorldReader c;
	private final double d;
	private final NavigationAbstract e;
	private int f;
	private final float g;
	private final float h;
	private float i;
	private final boolean j;
	private EntityHuman owner;

	public PathfinderGoalFollowTamer(EntityInsentient entitytameableanimal, EntityHuman owner, double d0, float f, float f1, boolean flag) {
		this.a = entitytameableanimal;
		this.c = entitytameableanimal.world;
		this.d = d0;
		this.e = entitytameableanimal.getNavigation();
		this.h = f;
		this.g = f1;
		this.j = flag;
		a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
		this.owner = owner;
		if (!(entitytameableanimal.getNavigation() instanceof Navigation) && !(entitytameableanimal.getNavigation() instanceof NavigationFlying))
			throw new IllegalArgumentException("Unsupported mob type for FollowTamerGoal");
	}

	@Override
	public boolean a() {
		final EntityLiving entityliving = this.owner;
		if (entityliving == null) {
			return false;
		}
		if (entityliving.isSpectator()) {
			return false;
		}
		if (this.a.h(entityliving) < this.h * this.h) {
			return false;
		}
		this.b = entityliving;
		return true;
	}

	@Override
	public boolean b() {
		return !this.e.m() && this.a.h(this.b) > this.g * this.g;
	}

	@Override
	public void c() {
		this.f = 0;
		this.i = this.a.a(PathType.WATER);
		this.a.a(PathType.WATER, 0.0F);
	}

	@Override
	public void d() {
		this.b = null;
		this.e.o();
		this.a.a(PathType.WATER, this.i);
	}

	@Override
	public void e() {
		this.a.getControllerLook().a(this.b, 10.0F, (float) this.a.eo()); 
		final int f = this.f - 1;
		this.f = f;
		if (f <= 0) {
			this.f = 10;
			if (!this.a.isLeashed() && !this.a.isPassenger()) {
				if (this.a.h(this.b) >= 144.0) {
					this.g();
				}
				else {
					this.e.a(this.b, this.d);
				}
			}
		}
	}

	private void g() {
		BlockPosition blockposition = this.b.getChunkCoordinates();
		for (int i = 0; i < 10; i ++) {
			int j = a(-3, 3);
			int k = a(-1, 1);
			int l = a(-3, 3);
			boolean flag = a(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l);
			if (flag) {
				return;
			}
		}
	}

	private boolean a(final int i, final int j, final int k) {
		if (Math.abs(i - this.b.locX()) < 2.0 && Math.abs(k - this.b.locZ()) < 2.0) {
			return false;
		}
		if (!a(new BlockPosition(i, j, k))) {
			return false;
		}
		CraftEntity entity = this.a.getBukkitEntity();
		Location to = new Location(entity.getWorld(), i + 0.5f, j, k + 0.5f, this.a.yaw, this.a.pitch);
		EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
		this.a.world.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}
		to = event.getTo();
		this.a.setPositionRotation(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
		this.e.o();
		return true;
	}

	private boolean a(final BlockPosition blockposition) {
		final PathType pathtype = PathfinderNormal.a(this.c, blockposition.i());
		if (pathtype != PathType.WALKABLE) {
			return false;
		}
		final IBlockData iblockdata = this.c.getType(blockposition.down());
		if (!this.j && iblockdata.getBlock() instanceof BlockLeaves) {
			return false;
		}
		final BlockPosition blockposition1 = blockposition.b(this.a.getChunkCoordinates());
		return this.c.getCubes(this.a, this.a.getBoundingBox().a(blockposition1));
	}

	private int a(final int i, final int j) {
		return this.a.getRandom().nextInt(j - i + 1) + i;
	}

}