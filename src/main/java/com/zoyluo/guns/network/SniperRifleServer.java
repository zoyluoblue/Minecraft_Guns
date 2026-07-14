package com.zoyluo.guns.network;

import com.zoyluo.guns.Guns;
import com.zoyluo.guns.entity.GrenadeProjectileEntity;
import com.zoyluo.guns.item.GrenadeLauncherItem;
import com.zoyluo.guns.item.ShotgunItem;
import com.zoyluo.guns.item.SniperRifleItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

public final class SniperRifleServer {
	private static final Map<UUID, Integer> ZOOM_LEVELS = new HashMap<>();
	private static final Map<UUID, Integer> FIRE_COOLDOWNS = new HashMap<>();
	private static final Map<UUID, Integer> SHOTGUN_COOLDOWNS = new HashMap<>();
	private static final Map<UUID, Integer> GRENADE_LAUNCHER_COOLDOWNS = new HashMap<>();
	private static final Map<String, Integer> ADVANCED_COOLDOWNS = new HashMap<>();
	private static final List<BulletTrace> BULLET_TRACES = new ArrayList<>();
	private static final int FIRE_COOLDOWN_TICKS = 16;
	private static final int SHOTGUN_COOLDOWN_TICKS = 22;
	private static final int GRENADE_LAUNCHER_COOLDOWN_TICKS = 30;
	private static final int BULLET_TRACE_TICKS = 6;
	private static final int SHOTGUN_PELLETS = 12;

	private SniperRifleServer() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(SniperZoomPayload.ID, (payload, context) -> setZoom(context.player(), payload.zoomLevel()));
		ServerPlayNetworking.registerGlobalReceiver(SniperFirePayload.ID, (payload, context) -> fire(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(ShotgunFirePayload.ID, (payload, context) -> fireShotgun(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(GrenadeLauncherFirePayload.ID, (payload, context) -> fireGrenadeLauncher(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(AdvancedWeaponFirePayload.ID, (payload, context) -> fireAdvancedWeapon(context.player(), payload.weapon()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> clear(handler.player));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCooldowns();
			tickBulletTraces();
		});
	}

	private static void setZoom(ServerPlayerEntity player, int zoomLevel) {
		if (!isHoldingSniper(player)) {
			clear(player);
			return;
		}

		int clamped = Math.max(0, Math.min(SniperRifleItem.MAX_ZOOM_LEVEL, zoomLevel));
		if (clamped == 0) {
			ZOOM_LEVELS.remove(player.getUuid());
		} else {
			ZOOM_LEVELS.put(player.getUuid(), clamped);
		}
	}

	private static void fire(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world) || !isHoldingSniper(player)) {
			clear(player);
			return;
		}
		if (ZOOM_LEVELS.getOrDefault(player.getUuid(), 0) <= 0) {
			player.sendMessage(Text.translatable("message.guns.sniper_rifle.need_scope"), true);
			return;
		}
		if (FIRE_COOLDOWNS.getOrDefault(player.getUuid(), 0) > 0) {
			return;
		}

		ItemStack stack = player.getMainHandStack();
		Vec3d start = player.getEyePos();
		Vec3d end = start.add(player.getRotationVec(1.0F).multiply(SniperRifleItem.RANGE));
		BlockHitResult blockHit = world.raycast(new RaycastContext(
				start,
				end,
				RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE,
				player
		));
		double maxDistance = blockHit.getType() == HitResult.Type.BLOCK ? start.squaredDistanceTo(blockHit.getPos()) : SniperRifleItem.RANGE * SniperRifleItem.RANGE;
		Box searchBox = player.getBoundingBox().stretch(player.getRotationVec(1.0F).multiply(SniperRifleItem.RANGE)).expand(1.0D);
		EntityHitResult entityHit = ProjectileUtil.raycast(player, start, end, searchBox, entity -> canHit(player, entity), maxDistance);

		Vec3d hitPos = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getPos() : end;
		if (entityHit != null) {
			Entity target = entityHit.getEntity();
			hitPos = entityHit.getPos();
			float damage = EnchantmentHelper.getDamage(world, stack, target, player.getDamageSources().playerAttack(player), SniperRifleItem.BASE_DAMAGE);
			target.damage(world, player.getDamageSources().playerAttack(player), damage);
			EnchantmentHelper.onTargetDamaged(world, target, player.getDamageSources().playerAttack(player), stack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.45F, 1.8F);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.85F, 0.55F);
		BULLET_TRACES.add(new BulletTrace(world, start.add(player.getRotationVec(1.0F).multiply(1.35D)), hitPos, 0));
		world.spawnParticles(ParticleTypes.SMOKE, hitPos.x, hitPos.y, hitPos.z, 8, 0.05D, 0.05D, 0.05D, 0.01D);
		stack.damage(1, player, EquipmentSlot.MAINHAND);
		FIRE_COOLDOWNS.put(player.getUuid(), FIRE_COOLDOWN_TICKS);
	}

	private static void fireShotgun(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world) || !player.getMainHandStack().isOf(Guns.SHOTGUN)) {
			return;
		}
		if (SHOTGUN_COOLDOWNS.getOrDefault(player.getUuid(), 0) > 0) {
			return;
		}

		ItemStack stack = player.getMainHandStack();
		Vec3d start = player.getEyePos();
		Vec3d forward = player.getRotationVec(1.0F).normalize();
		Box searchBox = player.getBoundingBox().stretch(forward.multiply(ShotgunItem.RANGE)).expand(ShotgunItem.RANGE);
		double coneDot = Math.cos(Math.toRadians(ShotgunItem.CONE_DEGREES / 2.0D));
		for (Entity target : world.getOtherEntities(player, searchBox, entity -> canHit(player, entity))) {
			Vec3d targetPos = target.getBoundingBox().getCenter();
			Vec3d toTarget = targetPos.subtract(start);
			double distance = toTarget.length();
			if (distance <= 0.0D || distance > ShotgunItem.RANGE || toTarget.normalize().dotProduct(forward) < coneDot || isBlocked(world, player, start, targetPos)) {
				continue;
			}

			int blockDistance = Math.max(1, (int) Math.ceil(distance));
			float damage = Math.max(ShotgunItem.MIN_DAMAGE, ShotgunItem.BASE_DAMAGE - (blockDistance - 1) * ShotgunItem.DAMAGE_FALLOFF_PER_BLOCK);
			damage = EnchantmentHelper.getDamage(world, stack, target, player.getDamageSources().playerAttack(player), damage);
			target.damage(world, player.getDamageSources().playerAttack(player), damage);
			EnchantmentHelper.onTargetDamaged(world, target, player.getDamageSources().playerAttack(player), stack);
			applyShotgunKnockback(target, forward, blockDistance);
			world.spawnParticles(ParticleTypes.SMOKE, targetPos.x, targetPos.y, targetPos.z, 4, 0.16D, 0.16D, 0.16D, 0.01D);
		}

		spawnShotgunPellets(world, start.add(forward.multiply(0.9D)), forward, player.getYaw(), player.getPitch());
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.55F, 1.15F);
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 0.9F, 0.65F);
		world.spawnParticles(ParticleTypes.SMOKE, start.x + forward.x, start.y + forward.y, start.z + forward.z, 8, 0.08D, 0.08D, 0.08D, 0.02D);
		stack.damage(1, player, EquipmentSlot.MAINHAND);
		SHOTGUN_COOLDOWNS.put(player.getUuid(), SHOTGUN_COOLDOWN_TICKS);
	}

	private static void fireGrenadeLauncher(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world) || !player.getMainHandStack().isOf(Guns.GRENADE_LAUNCHER)) {
			return;
		}
		if (GRENADE_LAUNCHER_COOLDOWNS.getOrDefault(player.getUuid(), 0) > 0) {
			return;
		}

		ItemStack stack = player.getMainHandStack();
		GrenadeProjectileEntity grenade = new GrenadeProjectileEntity(world, player, new ItemStack(Guns.GRENADE_ROUND));
		Vec3d forward = player.getRotationVec(1.0F).normalize();
		grenade.setPosition(player.getEyePos().add(forward.multiply(0.85D)));
		grenade.setVelocity(forward.multiply(GrenadeLauncherItem.PROJECTILE_SPEED));
		world.spawnEntity(grenade);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.35F, 1.55F);
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 0.8F, 0.45F);
		world.spawnParticles(ParticleTypes.SMOKE, grenade.getX(), grenade.getY(), grenade.getZ(), 10, 0.08D, 0.08D, 0.08D, 0.02D);
		stack.damage(1, player, EquipmentSlot.MAINHAND);
		GRENADE_LAUNCHER_COOLDOWNS.put(player.getUuid(), GRENADE_LAUNCHER_COOLDOWN_TICKS);
	}

	private static void fireAdvancedWeapon(ServerPlayerEntity player, AdvancedWeaponFirePayload.Weapon weapon) {
		if (!(player.getWorld() instanceof ServerWorld world) || !isHoldingAdvancedWeapon(player, weapon)) {
			return;
		}
		String cooldownKey = player.getUuid() + ":" + weapon.name();
		if (ADVANCED_COOLDOWNS.getOrDefault(cooldownKey, 0) > 0) {
			return;
		}

		switch (weapon) {
			case SMG -> {
				fireHitscan(player, 32.0D, 2.0F, 4.0D, 1, ParticleTypes.SMOKE);
				setAdvancedCooldown(cooldownKey, 4);
			}
			case FLAMETHROWER -> {
				fireFlamethrower(player);
				setAdvancedCooldown(cooldownKey, 2);
			}
			case RAILGUN -> {
				fireRailgun(player);
				setAdvancedCooldown(cooldownKey, 60);
			}
		}
	}

	private static void setAdvancedCooldown(String cooldownKey, int ticks) {
		ADVANCED_COOLDOWNS.put(cooldownKey, ticks);
	}

	private static void fireHitscan(ServerPlayerEntity player, double range, float baseDamage, double spreadDegrees, int pierce, net.minecraft.particle.ParticleEffect particle) {
		if (!(player.getWorld() instanceof ServerWorld world)) {
			return;
		}
		ItemStack stack = player.getMainHandStack();
		Vec3d start = player.getEyePos();
		Vec3d direction = applySpread(player.getRotationVec(1.0F).normalize(), world, spreadDegrees);
		Vec3d end = start.add(direction.multiply(range));
		BlockHitResult blockHit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		Vec3d visualEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getPos() : end;
		double maxDistance = start.squaredDistanceTo(visualEnd);
		Box searchBox = player.getBoundingBox().stretch(direction.multiply(range)).expand(1.0D);
		List<EntityHitResult> hits = new ArrayList<>();
		for (Entity target : world.getOtherEntities(player, searchBox, entity -> canHit(player, entity))) {
			EntityHitResult hit = ProjectileUtil.raycast(player, start, visualEnd, target.getBoundingBox().expand(0.3D), entity -> entity == target, maxDistance);
			if (hit != null) {
				hits.add(hit);
			}
		}
		hits.sort((a, b) -> Double.compare(start.squaredDistanceTo(a.getPos()), start.squaredDistanceTo(b.getPos())));
		int hitCount = 0;
		for (EntityHitResult hit : hits) {
			Entity target = hit.getEntity();
			float damage = EnchantmentHelper.getDamage(world, stack, target, player.getDamageSources().playerAttack(player), baseDamage);
			target.damage(world, player.getDamageSources().playerAttack(player), damage);
			EnchantmentHelper.onTargetDamaged(world, target, player.getDamageSources().playerAttack(player), stack);
			visualEnd = hit.getPos();
			if (++hitCount >= pierce) {
				break;
			}
		}
		spawnLineParticles(world, start.add(direction.multiply(0.7D)), visualEnd, particle, 0.65D);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.55F, spreadDegrees <= 0.0D ? 1.8F : 1.35F);
		stack.damage(1, player, EquipmentSlot.MAINHAND);
	}

	private static void fireRailgun(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world)) {
			return;
		}
		ItemStack stack = player.getMainHandStack();
		Vec3d start = player.getEyePos();
		Vec3d direction = player.getRotationVec(1.0F).normalize();
		double range = 160.0D;
		Vec3d end = start.add(direction.multiply(range));
		BlockHitResult blockHit = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
		Vec3d visualEnd = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getPos() : end;
		double maxDistance = start.squaredDistanceTo(visualEnd);
		Box searchBox = player.getBoundingBox().stretch(direction.multiply(range)).expand(1.2D);
		List<EntityHitResult> hits = new ArrayList<>();
		for (Entity target : world.getOtherEntities(player, searchBox, entity -> canHit(player, entity))) {
			EntityHitResult hit = ProjectileUtil.raycast(player, start, visualEnd, target.getBoundingBox().expand(0.65D), entity -> entity == target, maxDistance);
			if (hit != null) {
				hits.add(hit);
			}
		}
		hits.sort((a, b) -> Double.compare(start.squaredDistanceTo(a.getPos()), start.squaredDistanceTo(b.getPos())));
		for (EntityHitResult hit : hits) {
			Entity target = hit.getEntity();
			float damage = EnchantmentHelper.getDamage(world, stack, target, player.getDamageSources().playerAttack(player), 35.0F);
			target.damage(world, player.getDamageSources().playerAttack(player), damage);
			EnchantmentHelper.onTargetDamaged(world, target, player.getDamageSources().playerAttack(player), stack);
		}
		Vec3d muzzle = start.add(direction.multiply(0.95D));
		spawnRailgunMuzzleRing(world, muzzle, direction);
		spawnRailgunBeam(world, muzzle, visualEnd);
		world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1.0F, 0.6F);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.45F, 1.9F);
		stack.damage(2, player, EquipmentSlot.MAINHAND);
	}

	private static void fireFlamethrower(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world)) {
			return;
		}
		ItemStack stack = player.getMainHandStack();
		Vec3d start = player.getEyePos();
		Vec3d forward = player.getRotationVec(1.0F).normalize();
		double range = 8.0D;
		double coneDot = Math.cos(Math.toRadians(24.0D));
		for (int i = 1; i <= 12; i++) {
			Vec3d direction = applySpread(forward, world, 22.0D);
			Vec3d pos = start.add(direction.multiply(i * range / 12.0D));
			world.spawnParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 2, 0.04D, 0.04D, 0.04D, 0.01D);
			world.spawnParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0.04D, 0.04D, 0.04D, 0.0D);
		}
		Box searchBox = player.getBoundingBox().stretch(forward.multiply(range)).expand(range * 0.5D);
		for (Entity target : world.getOtherEntities(player, searchBox, entity -> canHit(player, entity))) {
			Vec3d targetPos = target.getBoundingBox().getCenter();
			Vec3d toTarget = targetPos.subtract(start);
			if (toTarget.length() > range || toTarget.normalize().dotProduct(forward) < coneDot || isBlocked(world, player, start, targetPos)) {
				continue;
			}
			target.damage(world, player.getDamageSources().playerAttack(player), 2.0F);
			target.setOnFireFor(3.0F);
		}
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.55F, 0.8F);
		stack.damage(1, player, EquipmentSlot.MAINHAND);
	}

	private static boolean isBlocked(ServerWorld world, ServerPlayerEntity player, Vec3d start, Vec3d end) {
		BlockHitResult hit = world.raycast(new RaycastContext(
				start,
				end,
				RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE,
				player
		));
		return hit.getType() == HitResult.Type.BLOCK && start.squaredDistanceTo(hit.getPos()) + 0.09D < start.squaredDistanceTo(end);
	}

	private static void applyShotgunKnockback(Entity target, Vec3d forward, int blockDistance) {
		double knockbackBlocks = Math.max(0.0D, ShotgunItem.BASE_KNOCKBACK_BLOCKS - (blockDistance - 1) * ShotgunItem.KNOCKBACK_FALLOFF_PER_BLOCK);
		if (knockbackBlocks <= 0.0D) {
			return;
		}
		Vec3d velocity = forward.multiply(knockbackBlocks * 0.42D).add(0.0D, Math.min(0.45D, knockbackBlocks * 0.06D), 0.0D);
		target.addVelocity(velocity);
		target.velocityModified = true;
	}

	private static void spawnShotgunPellets(ServerWorld world, Vec3d start, Vec3d forward, float yaw, float pitch) {
		for (int i = 0; i < SHOTGUN_PELLETS; i++) {
			double yawOffset = (world.random.nextDouble() - 0.5D) * ShotgunItem.CONE_DEGREES;
			double pitchOffset = (world.random.nextDouble() - 0.5D) * 28.0D;
			Vec3d direction = Vec3d.fromPolar((float) (pitch + pitchOffset), (float) (yaw + yawOffset)).normalize();
			if (direction.dotProduct(forward) < 0.15D) {
				direction = forward;
			}
			for (int step = 1; step <= 5; step++) {
				Vec3d pos = start.add(direction.multiply(step * 0.85D));
				world.spawnParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
			}
		}
	}

	private static Vec3d applySpread(Vec3d direction, ServerWorld world, double spreadDegrees) {
		if (spreadDegrees <= 0.0D) {
			return direction;
		}
		double spread = Math.tan(Math.toRadians(spreadDegrees));
		Vec3d random = new Vec3d(
				(world.random.nextDouble() - 0.5D) * spread,
				(world.random.nextDouble() - 0.5D) * spread,
				(world.random.nextDouble() - 0.5D) * spread
		);
		return direction.add(random).normalize();
	}

	private static void spawnLineParticles(ServerWorld world, Vec3d start, Vec3d end, net.minecraft.particle.ParticleEffect particle, double spacing) {
		Vec3d delta = end.subtract(start);
		double length = delta.length();
		if (length <= 0.001D) {
			return;
		}
		Vec3d direction = delta.normalize();
		for (double traveled = 0.0D; traveled <= length; traveled += spacing) {
			Vec3d pos = start.add(direction.multiply(traveled));
			world.spawnParticles(particle, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
	}

	private static void spawnRailgunBeam(ServerWorld world, Vec3d start, Vec3d end) {
		Vec3d delta = end.subtract(start);
		double length = delta.length();
		if (length <= 0.001D) {
			return;
		}
		Vec3d direction = delta.normalize();
		Vec3d side = perpendicular(direction);
		Vec3d up = side.crossProduct(direction).normalize();
		for (double traveled = 0.0D; traveled <= length; traveled += 0.28D) {
			Vec3d center = start.add(direction.multiply(traveled));
			world.spawnParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 2, 0.015D, 0.015D, 0.015D, 0.0D);
			for (double radius : new double[]{0.12D, -0.12D, 0.22D, -0.22D}) {
				Vec3d sidePos = center.add(side.multiply(radius));
				Vec3d upPos = center.add(up.multiply(radius));
				world.spawnParticles(ParticleTypes.CLOUD, sidePos.x, sidePos.y, sidePos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
				world.spawnParticles(ParticleTypes.CLOUD, upPos.x, upPos.y, upPos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	private static void spawnRailgunMuzzleRing(ServerWorld world, Vec3d center, Vec3d direction) {
		Vec3d side = perpendicular(direction);
		Vec3d up = side.crossProduct(direction).normalize();
		for (int i = 0; i < 32; i++) {
			double angle = i * Math.PI * 2.0D / 32.0D;
			Vec3d pos = center
					.add(side.multiply(Math.cos(angle) * 0.75D))
					.add(up.multiply(Math.sin(angle) * 0.75D));
			world.spawnParticles(ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 2, 0.01D, 0.01D, 0.01D, 0.0D);
			world.spawnParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
		}
	}

	private static Vec3d perpendicular(Vec3d direction) {
		Vec3d side = direction.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
		if (side.lengthSquared() < 1.0E-4D) {
			side = direction.crossProduct(new Vec3d(1.0D, 0.0D, 0.0D));
		}
		return side.normalize();
	}

	private static boolean isHoldingAdvancedWeapon(ServerPlayerEntity player, AdvancedWeaponFirePayload.Weapon weapon) {
		return switch (weapon) {
			case SMG -> player.getMainHandStack().isOf(Guns.SMG);
			case FLAMETHROWER -> player.getMainHandStack().isOf(Guns.FLAMETHROWER);
			case RAILGUN -> player.getMainHandStack().isOf(Guns.RAILGUN);
		};
	}

	private static boolean canHit(ServerPlayerEntity player, Entity entity) {
		return entity != player && entity.canHit() && entity.isAttackable() && !entity.isSpectator();
	}

	private static boolean isHoldingSniper(ServerPlayerEntity player) {
		return player.getMainHandStack().isOf(Guns.SNIPER_RIFLE);
	}

	private static void tickCooldowns() {
		FIRE_COOLDOWNS.replaceAll((uuid, ticks) -> ticks - 1);
		FIRE_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= 0);
		SHOTGUN_COOLDOWNS.replaceAll((uuid, ticks) -> ticks - 1);
		SHOTGUN_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= 0);
		GRENADE_LAUNCHER_COOLDOWNS.replaceAll((uuid, ticks) -> ticks - 1);
		GRENADE_LAUNCHER_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= 0);
		ADVANCED_COOLDOWNS.replaceAll((key, ticks) -> ticks - 1);
		ADVANCED_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= 0);
	}

	private static void tickBulletTraces() {
		ListIterator<BulletTrace> iterator = BULLET_TRACES.listIterator();
		while (iterator.hasNext()) {
			BulletTrace trace = iterator.next();
			int nextAge = trace.age() + 1;
			double progress = nextAge / (double) BULLET_TRACE_TICKS;
			Vec3d pos = trace.start().lerp(trace.end(), Math.min(1.0D, progress));
			Vec3d back = trace.start().lerp(trace.end(), Math.max(0.0D, progress - 0.12D));
			trace.world().spawnParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
			trace.world().spawnParticles(ParticleTypes.SMOKE, back.x, back.y, back.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
			if (nextAge >= BULLET_TRACE_TICKS) {
				iterator.remove();
			} else {
				iterator.set(trace.withAge(nextAge));
			}
		}
	}

	private static void clear(ServerPlayerEntity player) {
		ZOOM_LEVELS.remove(player.getUuid());
		FIRE_COOLDOWNS.remove(player.getUuid());
		SHOTGUN_COOLDOWNS.remove(player.getUuid());
		GRENADE_LAUNCHER_COOLDOWNS.remove(player.getUuid());
		ADVANCED_COOLDOWNS.keySet().removeIf(key -> key.startsWith(player.getUuid().toString()));
	}

	private record BulletTrace(ServerWorld world, Vec3d start, Vec3d end, int age) {
		private BulletTrace withAge(int age) {
			return new BulletTrace(world, start, end, age);
		}
	}
}
