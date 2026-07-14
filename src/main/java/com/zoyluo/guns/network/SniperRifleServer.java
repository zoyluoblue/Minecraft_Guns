package com.zoyluo.guns.network;

import com.zoyluo.guns.Guns;
import com.zoyluo.guns.ammo.AmmoService;
import com.zoyluo.guns.ammo.AmmoType;
import com.zoyluo.guns.entity.GrenadeProjectileEntity;
import com.zoyluo.guns.item.GrenadeLauncherItem;
import com.zoyluo.guns.item.ShotgunItem;
import com.zoyluo.guns.item.SniperRifleItem;
import com.zoyluo.guns.upgrade.GunUpgradeService;
import com.zoyluo.guns.visual.BallisticsVisuals;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
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
import java.util.Map;
import java.util.UUID;

public final class SniperRifleServer {
	private static final Map<UUID, Integer> ZOOM_LEVELS = new HashMap<>();
	private static final Map<UUID, Integer> FIRE_COOLDOWNS = new HashMap<>();
	private static final Map<UUID, Integer> SHOTGUN_COOLDOWNS = new HashMap<>();
	private static final Map<UUID, Integer> GRENADE_LAUNCHER_COOLDOWNS = new HashMap<>();
	private static final Map<String, Integer> ADVANCED_COOLDOWNS = new HashMap<>();
	private static final int FIRE_COOLDOWN_TICKS = 16;
	private static final int SHOTGUN_COOLDOWN_TICKS = 22;
	private static final int GRENADE_LAUNCHER_COOLDOWN_TICKS = 30;
	private static final int SMG_COOLDOWN_TICKS = 2;
	private static final int FLAMETHROWER_COOLDOWN_TICKS = 2;
	private static final int RAILGUN_COOLDOWN_TICKS = 60;
	private static final int FLAMETHROWER_SOUND_INTERVAL_TICKS = 3;

	private SniperRifleServer() {
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(SniperZoomPayload.ID, (payload, context) -> setZoom(context.player(), payload.zoomLevel()));
		ServerPlayNetworking.registerGlobalReceiver(SniperFirePayload.ID, (payload, context) -> fire(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(ShotgunFirePayload.ID, (payload, context) -> fireShotgun(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(GrenadeLauncherFirePayload.ID, (payload, context) -> fireGrenadeLauncher(context.player()));
		ServerPlayNetworking.registerGlobalReceiver(AdvancedWeaponFirePayload.ID, (payload, context) -> fireAdvancedWeapon(context.player(), payload.weapon()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> clear(handler.player));
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearAll());
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCooldowns();
			AmmoService.tick();
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
		if (!AmmoService.tryConsume(player, AmmoType.RIFLE_ROUND)) {
			return;
		}
		double range = GunUpgradeService.range(stack, SniperRifleItem.RANGE);
		Vec3d start = player.getEyePos();
		Vec3d forward = player.getRotationVec(1.0F).normalize();
		Vec3d end = start.add(forward.multiply(range));
		BlockHitResult blockHit = world.raycast(new RaycastContext(
				start,
				end,
				RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE,
				player
		));
		double maxDistance = blockHit.getType() == HitResult.Type.BLOCK ? start.squaredDistanceTo(blockHit.getPos()) : range * range;
		Box searchBox = player.getBoundingBox().stretch(forward.multiply(range)).expand(1.0D);
		EntityHitResult entityHit = ProjectileUtil.raycast(player, start, end, searchBox, entity -> canHit(player, entity), maxDistance);

		Vec3d hitPos = blockHit.getType() == HitResult.Type.BLOCK ? blockHit.getPos() : end;
		boolean impacted = blockHit.getType() == HitResult.Type.BLOCK;
		if (entityHit != null) {
			Entity target = entityHit.getEntity();
			hitPos = entityHit.getPos();
			impacted = true;
			float damage = EnchantmentHelper.getDamage(world, stack, target, player.getDamageSources().playerAttack(player), GunUpgradeService.damage(stack, SniperRifleItem.BASE_DAMAGE));
			target.damage(world, player.getDamageSources().playerAttack(player), damage);
			EnchantmentHelper.onTargetDamaged(world, target, player.getDamageSources().playerAttack(player), stack);
		}

		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.45F, 1.8F);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 0.85F, 0.55F);
		BallisticsVisuals.sniper(world, start.add(forward.multiply(1.35D)), hitPos, impacted);
		stack.damage(1, player, EquipmentSlot.MAINHAND);
		FIRE_COOLDOWNS.put(player.getUuid(), GunUpgradeService.cooldown(stack, FIRE_COOLDOWN_TICKS));
	}

	private static void fireShotgun(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world) || !player.getMainHandStack().isOf(Guns.SHOTGUN)) {
			return;
		}
		if (SHOTGUN_COOLDOWNS.getOrDefault(player.getUuid(), 0) > 0) {
			return;
		}

		ItemStack stack = player.getMainHandStack();
		if (!AmmoService.tryConsume(player, AmmoType.SHOTGUN_SHELL)) {
			return;
		}
		Vec3d start = player.getEyePos();
		Vec3d forward = player.getRotationVec(1.0F).normalize();
		double range = GunUpgradeService.range(stack, ShotgunItem.RANGE);
		double coneDegrees = GunUpgradeService.cone(stack, ShotgunItem.CONE_DEGREES);
		Box searchBox = player.getBoundingBox().stretch(forward.multiply(range)).expand(range);
		double coneDot = Math.cos(Math.toRadians(coneDegrees / 2.0D));
		for (Entity target : world.getOtherEntities(player, searchBox, entity -> canHit(player, entity))) {
			Vec3d targetPos = target.getBoundingBox().getCenter();
			Vec3d toTarget = targetPos.subtract(start);
			double distance = toTarget.length();
			if (distance <= 0.0D || distance > range || toTarget.normalize().dotProduct(forward) < coneDot || isBlocked(world, player, start, targetPos)) {
				continue;
			}

			int blockDistance = Math.max(1, (int) Math.ceil(distance));
			float damage = Math.max(ShotgunItem.MIN_DAMAGE, GunUpgradeService.damage(stack, ShotgunItem.BASE_DAMAGE) - (blockDistance - 1) * ShotgunItem.DAMAGE_FALLOFF_PER_BLOCK);
			damage = EnchantmentHelper.getDamage(world, stack, target, player.getDamageSources().playerAttack(player), damage);
			target.damage(world, player.getDamageSources().playerAttack(player), damage);
			EnchantmentHelper.onTargetDamaged(world, target, player.getDamageSources().playerAttack(player), stack);
			applyShotgunKnockback(target, forward, blockDistance);
			BallisticsVisuals.kineticHit(world, targetPos, 2);
		}

		BallisticsVisuals.shotgun(
				world,
				player,
				start.add(forward.multiply(0.9D)),
				forward,
				player.getYaw(),
				player.getPitch(),
				coneDegrees,
				range
		);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.55F, 1.15F);
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 0.9F, 0.65F);
		stack.damage(1, player, EquipmentSlot.MAINHAND);
		SHOTGUN_COOLDOWNS.put(player.getUuid(), GunUpgradeService.cooldown(stack, SHOTGUN_COOLDOWN_TICKS));
	}

	private static void fireGrenadeLauncher(ServerPlayerEntity player) {
		if (!(player.getWorld() instanceof ServerWorld world) || !player.getMainHandStack().isOf(Guns.GRENADE_LAUNCHER)) {
			return;
		}
		if (GRENADE_LAUNCHER_COOLDOWNS.getOrDefault(player.getUuid(), 0) > 0) {
			return;
		}

		ItemStack stack = player.getMainHandStack();
		if (!AmmoService.tryConsume(player, AmmoType.GRENADE_ROUND)) {
			return;
		}
		GrenadeProjectileEntity grenade = new GrenadeProjectileEntity(
				world,
				player,
				new ItemStack(Guns.GRENADE_ROUND),
				GunUpgradeService.damage(stack, GrenadeLauncherItem.EXPLOSION_POWER)
		);
		Vec3d forward = player.getRotationVec(1.0F).normalize();
		grenade.setPosition(player.getEyePos().add(forward.multiply(0.85D)));
		grenade.setVelocity(forward.multiply(GunUpgradeService.projectileSpeed(stack, GrenadeLauncherItem.PROJECTILE_SPEED)));
		world.spawnEntity(grenade);
		world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE.value(), SoundCategory.PLAYERS, 0.35F, 1.55F);
		world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 0.8F, 0.45F);
		BallisticsVisuals.grenadeMuzzle(world, grenade.getPos(), forward);
		stack.damage(1, player, EquipmentSlot.MAINHAND);
		GRENADE_LAUNCHER_COOLDOWNS.put(player.getUuid(), GunUpgradeService.cooldown(stack, GRENADE_LAUNCHER_COOLDOWN_TICKS));
	}

	private static void fireAdvancedWeapon(ServerPlayerEntity player, AdvancedWeaponFirePayload.Weapon weapon) {
		if (!(player.getWorld() instanceof ServerWorld world) || !isHoldingAdvancedWeapon(player, weapon)) {
			return;
		}
		String cooldownKey = player.getUuid() + ":" + weapon.name();
		if (ADVANCED_COOLDOWNS.getOrDefault(cooldownKey, 0) > 0) {
			return;
		}
		if (!AmmoService.tryConsume(player, ammoType(weapon))) {
			return;
		}
		ItemStack stack = player.getMainHandStack();

		switch (weapon) {
			case SMG -> {
				fireHitscan(
						player,
						GunUpgradeService.range(stack, 32.0D),
						GunUpgradeService.damage(stack, 2.0F),
						GunUpgradeService.spread(stack, 4.0D),
						1
				);
				setAdvancedCooldown(cooldownKey, GunUpgradeService.cooldown(stack, SMG_COOLDOWN_TICKS));
			}
			case FLAMETHROWER -> {
				fireFlamethrower(player);
				setAdvancedCooldown(cooldownKey, GunUpgradeService.cooldown(stack, FLAMETHROWER_COOLDOWN_TICKS));
			}
			case RAILGUN -> {
				fireRailgun(player);
				setAdvancedCooldown(cooldownKey, GunUpgradeService.cooldown(stack, RAILGUN_COOLDOWN_TICKS));
			}
		}
	}

	private static AmmoType ammoType(AdvancedWeaponFirePayload.Weapon weapon) {
		return switch (weapon) {
			case SMG -> AmmoType.RIFLE_ROUND;
			case FLAMETHROWER -> AmmoType.FUEL_CELL;
			case RAILGUN -> AmmoType.RAILGUN_CELL;
		};
	}

	private static void setAdvancedCooldown(String cooldownKey, int ticks) {
		ADVANCED_COOLDOWNS.put(cooldownKey, ticks);
	}

	private static void fireHitscan(ServerPlayerEntity player, double range, float baseDamage, double spreadDegrees, int pierce) {
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
		boolean impacted = blockHit.getType() == HitResult.Type.BLOCK || hitCount > 0;
		BallisticsVisuals.smg(world, start.add(direction.multiply(1.05D)), visualEnd, impacted);
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
		double range = GunUpgradeService.range(stack, 160.0D);
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
			float damage = EnchantmentHelper.getDamage(world, stack, target, player.getDamageSources().playerAttack(player), GunUpgradeService.damage(stack, 35.0F));
			target.damage(world, player.getDamageSources().playerAttack(player), damage);
			EnchantmentHelper.onTargetDamaged(world, target, player.getDamageSources().playerAttack(player), stack);
		}
		Vec3d muzzle = start.add(direction.multiply(1.40D));
		BallisticsVisuals.railgun(
				world,
				muzzle,
				visualEnd,
				blockHit.getType() == HitResult.Type.BLOCK,
				hits.stream().limit(BallisticsVisuals.RAILGUN_VISIBLE_ENTITY_HITS).map(EntityHitResult::getPos).toList()
		);
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
		double range = GunUpgradeService.range(stack, 8.0D);
		double coneDegrees = GunUpgradeService.cone(stack, 24.0D);
		double coneDot = Math.cos(Math.toRadians(coneDegrees));
		BallisticsVisuals.flamethrower(
				world,
				start.add(forward.multiply(1.25D)),
				forward,
				range,
				GunUpgradeService.spread(stack, 22.0D),
				player.age
		);
		Box searchBox = player.getBoundingBox().stretch(forward.multiply(range)).expand(range * 0.5D);
		for (Entity target : world.getOtherEntities(player, searchBox, entity -> canHit(player, entity))) {
			Vec3d targetPos = target.getBoundingBox().getCenter();
			Vec3d toTarget = targetPos.subtract(start);
			if (toTarget.length() > range || toTarget.normalize().dotProduct(forward) < coneDot || isBlocked(world, player, start, targetPos)) {
				continue;
			}
			target.damage(world, player.getDamageSources().playerAttack(player), GunUpgradeService.damage(stack, 2.0F));
			target.setOnFireFor(3.0F);
		}
		if (player.age % FLAMETHROWER_SOUND_INTERVAL_TICKS == 0) {
			world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 0.48F, 0.72F);
		}
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

	private static void clear(ServerPlayerEntity player) {
		AmmoService.clear(player);
		ZOOM_LEVELS.remove(player.getUuid());
		FIRE_COOLDOWNS.remove(player.getUuid());
		SHOTGUN_COOLDOWNS.remove(player.getUuid());
		GRENADE_LAUNCHER_COOLDOWNS.remove(player.getUuid());
		ADVANCED_COOLDOWNS.keySet().removeIf(key -> key.startsWith(player.getUuid().toString()));
	}

	private static void clearAll() {
		ZOOM_LEVELS.clear();
		FIRE_COOLDOWNS.clear();
		SHOTGUN_COOLDOWNS.clear();
		GRENADE_LAUNCHER_COOLDOWNS.clear();
		ADVANCED_COOLDOWNS.clear();
		AmmoService.clearAll();
	}
}
