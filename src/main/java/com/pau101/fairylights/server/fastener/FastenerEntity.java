package com.pau101.fairylights.server.fastener;

import com.pau101.fairylights.server.fastener.accessor.FastenerAccessorEntity;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class FastenerEntity<E extends Entity> extends FastenerDefault<FastenerAccessorEntity<E>> {
	protected final E entity;

	public FastenerEntity(E entity) {
		this.entity = entity;
		bounds = new AxisAlignedBB(entity.getPosition());
		setWorld(entity.world);
	}

	@Override
	public EnumFacing getFacing() {
		return EnumFacing.UP;
	}

	public E getEntity() {
		return entity;
	}

	@Override
	public BlockPos getPos() {
		return new BlockPos(entity);
	}

	@Override
	public Vec3d getConnectionPoint() {
		return new Vec3d(entity.posX, entity.posY, entity.posZ);
	}
}
