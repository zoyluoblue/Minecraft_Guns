package com.zoyluo.guns.network;

import com.zoyluo.guns.Guns;
import com.zoyluo.guns.entity.GrenadeProjectileEntity;
import com.zoyluo.guns.item.GrenadeLauncherItem;
import com.zoyluo.guns.item.ShotgunItem;
import com.zoyluo.guns.item.SniperRifleItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
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
	private static final List<BulletTrace> BULLET_TRACES = new ArrayList<>();
	private static final int FIRE_COOLDOWN_TICKS = 16;
	private static final int SHOTGUN_COOLDOWN_TICKS = 22;
	private static final int GRENADE_LAUNCHER_COOLDOWN_TICKS = 30;
	private static final int BULLET_TRACE_TICKS = 6;
	private static final int SHOTGUN_PELLETS = 12;

	private SniperRifleServer() {
	}

	public static void register() {
		PayloadTypeRegistry.playC2S().register(SniperZoomPayload.ID, SniperZoomPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SniperFirePayload.ID, SniperFirePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ShotgunFirePayload.ID, ShotgunFirePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(GrenadeLauncherFirePayload.ID, GrenadeLauncherFirePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SniperZoomPayload.ID, (payload, context) -> setZoom(context.player(), payload.zoomLevel()));
		ServerPlayNetworking.registerGlobalReceiver(SniperFirePayload.ID, (payload, context) -> fire(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(ShotgunFirePayload.ID, (payload, context) -> fireShotgun(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(GrenadeLauncherFirePayload.ID, (payload, context) -> fireGrenadeLauncher(context.player()));
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
	}

	private record BulletTrace(ServerWorld world, Vec3d start, Vec3d end, int age) {
		private BulletTrace withAge(int age) {
			return new BulletTrace(world, start, end, age);
		}
	}
}
