package com.zoyluo.guns.entity;

import com.zoyluo.guns.Guns;
import com.zoyluo.guns.item.GrenadeLauncherItem;
import com.zoyluo.guns.visual.BallisticsVisuals;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class GrenadeProjectileEntity extends ThrownItemEntity {
	private float explosionPower;

	public GrenadeProjectileEntity(EntityType<? extends GrenadeProjectileEntity> entityType, World world) {
		super(entityType, world);
		this.explosionPower = GrenadeLauncherItem.EXPLOSION_POWER;
	}

	public GrenadeProjectileEntity(World world, LivingEntity owner, ItemStack stack) {
		this(world, owner, stack, GrenadeLauncherItem.EXPLOSION_POWER);
	}

	public GrenadeProjectileEntity(World world, LivingEntity owner, ItemStack stack, float explosionPower) {
		super(Guns.GRENADE_PROJECTILE, owner, world, stack);
		this.explosionPower = Math.max(0.0F, explosionPower);
	}

	@Override
	protected Item getDefaultItem() {
		return Guns.GRENADE_ROUND;
	}

	@Override
	protected double getGravity() {
		return 0.035D;
	}

	@Override
	public void tick() {
		super.tick();
		if (!isRemoved() && age > 1 && getWorld() instanceof ServerWorld serverWorld) {
			BallisticsVisuals.grenadeTrail(serverWorld, getPos(), getVelocity(), age);
		}
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
		if (!getWorld().isClient()) {
			explode();
		}
	}

	private void explode() {
		if (!(getWorld() instanceof ServerWorld serverWorld)) {
			return;
		}
		BallisticsVisuals.grenadeImpact(serverWorld, getPos());
		serverWorld.createExplosion(this, getX(), getY(), getZ(), explosionPower, false, World.ExplosionSourceType.TNT);
		discard();
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putFloat("guns_explosion_power", explosionPower);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		if (nbt.contains("guns_explosion_power", NbtElement.FLOAT_TYPE)) {
			explosionPower = Math.max(0.0F, Math.min(32.0F, nbt.getFloat("guns_explosion_power")));
		}
	}

}
