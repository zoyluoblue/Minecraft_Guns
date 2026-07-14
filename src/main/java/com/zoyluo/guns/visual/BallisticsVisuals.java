package com.zoyluo.guns.visual;

import com.zoyluo.guns.registry.ModParticles;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.List;

/**
 * Server-authoritative, bounded visual composition for every Guns projectile.
 * Gameplay collision and damage remain in their owning weapon classes.
 */
public final class BallisticsVisuals {
	public static final int SNIPER_MAX_SAMPLES = 80;
	public static final double SNIPER_VISUAL_SPEED = 0.110D;
	public static final int SHOTGUN_PELLETS = 13;
	public static final int SHOTGUN_MAX_SAMPLES_PER_PELLET = 5;
	public static final int SHOTGUN_MAX_CALLS = 96;
	public static final int SMG_MAX_SAMPLES = 12;
	public static final int FLAMETHROWER_MAX_PARTICLES = 6;
	public static final int GRENADE_TRAIL_MAX_PARTICLES = 3;
	public static final int RAILGUN_MAX_CALLS = 208;
	public static final int RAILGUN_VISIBLE_ENTITY_HITS = 4;
	public static final int RAILGUN_FADE_TICKS = 60;

	private static final double TWO_PI = Math.PI * 2.0D;
	private static final int SHOTGUN_RING_PELLETS = 6;
	private static final double SHOTGUN_TRAIL_SPEED = 0.160D;
	private static final double SHOTGUN_PELLET_SPEED = 0.520D;
	private static final int RAILGUN_BEAM_MAX_SAMPLES = 192;
	private static final int RAILGUN_ENTITY_HIT_PARTICLES = 2;
	private static final int RAILGUN_BLOCK_IMPACT_PARTICLES = 4;

	private BallisticsVisuals() {
	}

	public static void sniper(ServerWorld world, Vec3d muzzle, Vec3d end, boolean impacted) {
		Vec3d direction = direction(muzzle, end);
		spawnLine(world, muzzle, end, ModParticles.GRAY_ROUND, 1.60D, SNIPER_MAX_SAMPLES, SNIPER_VISUAL_SPEED);
		if (impacted) {
			solidImpact(world, end, direction, ModParticles.GRAY_ROUND, 3, true);
		}
	}

	public static void shotgun(
			ServerWorld world,
			ServerPlayerEntity shooter,
			Vec3d muzzle,
			Vec3d forward,
			float yaw,
			float pitch,
			double coneDegrees,
			double range
	) {
		double yawRadius = Math.min(54.0D, coneDegrees * 0.36D);
		double pitchRadius = Math.min(28.0D, coneDegrees * 0.19D);
		double patternRotation = world.random.nextDouble() * TWO_PI;
		for (int pellet = 0; pellet < SHOTGUN_PELLETS; pellet++) {
			int ring = pellet == 0 ? 0 : 1 + (pellet - 1) / SHOTGUN_RING_PELLETS;
			int ringIndex = pellet == 0 ? 0 : (pellet - 1) % SHOTGUN_RING_PELLETS;
			double ringScale = ring == 0 ? 0.0D : ring == 1 ? 0.52D : 1.0D;
			double ringPhase = ring == 2 ? Math.PI / SHOTGUN_RING_PELLETS : 0.0D;
			double ringAngle = patternRotation + ringIndex * TWO_PI / SHOTGUN_RING_PELLETS + ringPhase;
			double yawOffset = Math.cos(ringAngle) * yawRadius * ringScale;
			double pitchOffset = Math.sin(ringAngle) * pitchRadius * ringScale;
			Vec3d pelletDirection = Vec3d.fromPolar((float) (pitch + pitchOffset), (float) (yaw + yawOffset)).normalize();
			if (pelletDirection.dotProduct(forward) < 0.1D) {
				pelletDirection = forward;
			}
			Vec3d intendedEnd = muzzle.add(pelletDirection.multiply(range));
			BlockHitResult blockHit = world.raycast(new RaycastContext(
					muzzle,
					intendedEnd,
					RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.NONE,
					shooter
			));
			boolean impacted = blockHit.getType() == HitResult.Type.BLOCK;
			Vec3d pelletEnd = impacted ? blockHit.getPos() : intendedEnd;
			double visualOffset = Math.min(0.32D, muzzle.distanceTo(pelletEnd) * 0.20D);
			Vec3d visualStart = muzzle.add(pelletDirection.multiply(visualOffset));
			spawnGrayRoundOrBubble(world, visualStart, pelletDirection.multiply(SHOTGUN_PELLET_SPEED));
			spawnLine(world, visualStart, pelletEnd, ModParticles.GRAY_RANGE, 1.55D, SHOTGUN_MAX_SAMPLES_PER_PELLET, SHOTGUN_TRAIL_SPEED);
			if (impacted) {
				solidImpact(world, pelletEnd, pelletDirection, ModParticles.GRAY_RANGE, 1, false);
			}
		}
	}

	public static void smg(ServerWorld world, Vec3d muzzle, Vec3d end, boolean impacted) {
		Vec3d direction = direction(muzzle, end);
		spawnLine(world, muzzle.add(direction.multiply(0.28D)), end, ModParticles.BLACK_ROUND, 2.80D, SMG_MAX_SAMPLES, 0.075D);
		if (impacted) {
			solidImpact(world, end, direction, ModParticles.BLACK_ROUND, 1, false);
		}
	}

	public static void grenadeMuzzle(ServerWorld world, Vec3d muzzle, Vec3d direction) {
		if (isUnderwater(world, muzzle)) {
			world.spawnParticles(ParticleTypes.BUBBLE, muzzle.x, muzzle.y, muzzle.z, 5, 0.07D, 0.07D, 0.07D, 0.035D);
			return;
		}
		spawnStatic(world, ModParticles.BLAST_WAVE, muzzle.add(direction.multiply(0.08D)));
		spawnStatic(world, ModParticles.MUZZLE_FLASH, muzzle);
		world.spawnParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 3, 0.045D, 0.045D, 0.045D, 0.01D);
	}

	public static void grenadeTrail(ServerWorld world, Vec3d position, Vec3d velocity, int age) {
		Vec3d back = position.subtract(velocity.multiply(0.32D));
		if (isUnderwater(world, back)) {
			spawnDirected(world, ParticleTypes.BUBBLE, back, velocity.multiply(-0.08D));
			spawnDirected(world, ParticleTypes.BUBBLE, back.subtract(velocity.multiply(0.12D)), velocity.multiply(-0.04D));
			return;
		}

		spawnDirected(world, ModParticles.FLAME_CORE, back, velocity.multiply(-0.025D));
		spawnDirected(world, ModParticles.IMPACT_SPARK, back.subtract(velocity.multiply(0.12D)), velocity.multiply(-0.04D));
		if ((age & 1) == 0) {
			world.spawnParticles(ParticleTypes.SMOKE, back.x, back.y, back.z, 1, 0.018D, 0.018D, 0.018D, 0.003D);
		}
	}

	public static void grenadeImpact(ServerWorld world, Vec3d position) {
		if (isUnderwater(world, position)) {
			world.spawnParticles(ParticleTypes.BUBBLE, position.x, position.y, position.z, 18, 0.35D, 0.28D, 0.35D, 0.08D);
			for (int i = 0; i < 8; i++) {
				spawnDirected(world, ModParticles.ENERGY_ARC, position, randomUnit(world).multiply(0.08D));
			}
			return;
		}
		spawnStatic(world, ModParticles.BLAST_WAVE, position);
		spawnStatic(world, ModParticles.MUZZLE_FLASH, position);
		for (int i = 0; i < 14; i++) {
			Vec3d velocity = randomUnit(world).multiply(0.12D + world.random.nextDouble() * 0.16D).add(0.0D, 0.04D, 0.0D);
			spawnDirected(world, ModParticles.IMPACT_SPARK, position, velocity);
		}
		world.spawnParticles(ParticleTypes.LARGE_SMOKE, position.x, position.y, position.z, 8, 0.28D, 0.22D, 0.28D, 0.025D);
	}

	public static void flamethrower(ServerWorld world, Vec3d muzzle, Vec3d forward, double range, double coneDegrees, int phase) {
		Vec3d side = perpendicular(forward);
		Vec3d up = side.crossProduct(forward).normalize();
		double baseSpeed = Math.max(0.38D, Math.min(0.78D, range / 12.0D));
		for (int particle = 0; particle < FLAMETHROWER_MAX_PARTICLES; particle++) {
			double angle = (particle - 1) * TWO_PI / (FLAMETHROWER_MAX_PARTICLES - 1) + phase * 0.41D;
			double spread = particle == 0
					? 0.0D
					: Math.tan(Math.toRadians(coneDegrees * (0.28D + world.random.nextDouble() * 0.28D)));
			Vec3d radial = side.multiply(Math.cos(angle)).add(up.multiply(Math.sin(angle)));
			Vec3d jetDirection = forward.add(radial.multiply(spread)).normalize();
			double speed = baseSpeed * (0.88D + world.random.nextDouble() * 0.24D);
			Vec3d position = muzzle.add(radial.multiply(particle == 0 ? 0.0D : 0.025D));
			spawnFlameJetOrBubble(world, position, jetDirection.multiply(speed));
		}
	}

	public static void railgun(ServerWorld world, Vec3d muzzle, Vec3d end, boolean blocked, List<Vec3d> entityHits) {
		Vec3d delta = end.subtract(muzzle);
		double length = delta.length();
		if (length <= 0.001D) {
			return;
		}
		int calls = 0;

		int beamSamples = sampleCount(length, 0.82D, RAILGUN_BEAM_MAX_SAMPLES);
		for (int i = 0; i < beamSamples && calls < RAILGUN_MAX_CALLS; i++) {
			double progress = beamSamples == 1 ? 0.0D : i / (double) (beamSamples - 1);
			Vec3d position = muzzle.lerp(end, progress);
			spawnWhiteBeamOrBubble(world, position, Vec3d.ZERO);
			calls++;
		}

		int visibleEntityHits = Math.min(RAILGUN_VISIBLE_ENTITY_HITS, entityHits.size());
		for (int hitIndex = 0; hitIndex < visibleEntityHits && calls < RAILGUN_MAX_CALLS; hitIndex++) {
			Vec3d hitPosition = entityHits.get(hitIndex);
			for (int spark = 0; spark < RAILGUN_ENTITY_HIT_PARTICLES && calls < RAILGUN_MAX_CALLS; spark++) {
				spawnRailgunImpactOrBubble(world, hitPosition, randomUnit(world).multiply(0.018D));
				calls++;
			}
		}

		if (blocked && calls < RAILGUN_MAX_CALLS) {
			for (int i = 0; i < RAILGUN_BLOCK_IMPACT_PARTICLES && calls < RAILGUN_MAX_CALLS; i++) {
				Vec3d velocity = randomUnit(world).multiply(0.012D + world.random.nextDouble() * 0.016D);
				spawnRailgunImpactOrBubble(world, end, velocity);
				calls++;
			}
		}
	}

	public static void kineticHit(ServerWorld world, Vec3d position, int sparkCount) {
		solidImpact(world, position, new Vec3d(0.0D, 1.0D, 0.0D), ModParticles.GRAY_RANGE, Math.max(1, Math.min(3, sparkCount)), false);
	}

	/** Returns a bounded number of evenly distributed samples including both endpoints. */
	public static int sampleCount(double length, double preferredSpacing, int maximum) {
		if (!Double.isFinite(length) || !Double.isFinite(preferredSpacing) || length <= 0.001D || preferredSpacing <= 0.0D || maximum <= 0) {
			return 0;
		}
		return Math.min(maximum, Math.max(2, (int) Math.ceil(length / preferredSpacing) + 1));
	}

	public static int railgunWorstCaseCalls() {
		return RAILGUN_BEAM_MAX_SAMPLES
				+ RAILGUN_BLOCK_IMPACT_PARTICLES
				+ RAILGUN_VISIBLE_ENTITY_HITS * RAILGUN_ENTITY_HIT_PARTICLES;
	}

	public static int shotgunWorstCaseCalls() {
		return SHOTGUN_PELLETS * (SHOTGUN_MAX_SAMPLES_PER_PELLET + 2);
	}

	private static void spawnLine(
			ServerWorld world,
			Vec3d start,
			Vec3d end,
			ParticleEffect particle,
			double preferredSpacing,
			int maximum,
			double speed
	) {
		Vec3d delta = end.subtract(start);
		double length = delta.length();
		int samples = sampleCount(length, preferredSpacing, maximum);
		if (samples == 0) {
			return;
		}
		Vec3d velocity = delta.normalize().multiply(speed);
		for (int i = 0; i < samples; i++) {
			double progress = i / (double) (samples - 1);
			Vec3d position = start.lerp(end, progress);
			if (isUnderwater(world, position)) {
				spawnDirected(world, ParticleTypes.BUBBLE, position, velocity.multiply(0.4D));
			} else {
				spawnDirected(world, particle, position, velocity);
			}
		}
	}

	private static void solidImpact(
			ServerWorld world,
			Vec3d position,
			Vec3d incoming,
			ParticleEffect particle,
			int particleCount,
			boolean smoke
	) {
		if (isUnderwater(world, position)) {
			world.spawnParticles(ParticleTypes.BUBBLE, position.x, position.y, position.z, Math.max(2, particleCount), 0.08D, 0.08D, 0.08D, 0.025D);
			return;
		}
		Vec3d rebound = incoming.lengthSquared() > 1.0E-6D ? incoming.normalize().multiply(-1.0D) : new Vec3d(0.0D, 1.0D, 0.0D);
		for (int i = 0; i < particleCount; i++) {
			Vec3d velocity = rebound.multiply(0.018D + world.random.nextDouble() * 0.025D)
					.add(randomUnit(world).multiply(0.012D + world.random.nextDouble() * 0.018D));
			spawnDirected(world, particle, position, velocity);
		}
		if (smoke) {
			world.spawnParticles(ParticleTypes.SMOKE, position.x, position.y, position.z, 3, 0.055D, 0.055D, 0.055D, 0.008D);
		}
	}

	private static void spawnFlameJetOrBubble(ServerWorld world, Vec3d position, Vec3d velocity) {
		spawnDirected(world, isUnderwater(world, position) ? ParticleTypes.BUBBLE : ModParticles.FLAME_JET, position, velocity);
	}

	private static void spawnGrayRoundOrBubble(ServerWorld world, Vec3d position, Vec3d velocity) {
		spawnDirected(world, isUnderwater(world, position) ? ParticleTypes.BUBBLE : ModParticles.GRAY_ROUND, position, velocity);
	}

	private static void spawnWhiteBeamOrBubble(ServerWorld world, Vec3d position, Vec3d velocity) {
		spawnDirected(world, isUnderwater(world, position) ? ParticleTypes.BUBBLE : ModParticles.WHITE_BEAM, position, velocity);
	}

	private static void spawnRailgunImpactOrBubble(ServerWorld world, Vec3d position, Vec3d velocity) {
		spawnDirected(world, isUnderwater(world, position) ? ParticleTypes.BUBBLE : ParticleTypes.END_ROD, position, velocity);
	}

	private static void spawnMuzzleOrBubble(ServerWorld world, Vec3d position, Vec3d velocity) {
		spawnDirected(world, isUnderwater(world, position) ? ParticleTypes.BUBBLE : ModParticles.MUZZLE_FLASH, position, velocity);
	}

	private static boolean isUnderwater(ServerWorld world, Vec3d position) {
		BlockPos blockPos = BlockPos.ofFloored(position);
		if (!world.getChunkManager().isChunkLoaded(blockPos.getX() >> 4, blockPos.getZ() >> 4)) {
			return false;
		}
		return world.getFluidState(blockPos).isIn(FluidTags.WATER);
	}

	private static Vec3d direction(Vec3d start, Vec3d end) {
		Vec3d delta = end.subtract(start);
		return delta.lengthSquared() <= 1.0E-6D ? new Vec3d(0.0D, 0.0D, 1.0D) : delta.normalize();
	}

	private static Vec3d perpendicular(Vec3d direction) {
		Vec3d side = direction.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
		if (side.lengthSquared() < 1.0E-4D) {
			side = direction.crossProduct(new Vec3d(1.0D, 0.0D, 0.0D));
		}
		return side.normalize();
	}

	private static Vec3d randomUnit(ServerWorld world) {
		Vec3d vector;
		do {
			vector = new Vec3d(
					world.random.nextDouble() * 2.0D - 1.0D,
					world.random.nextDouble() * 2.0D - 1.0D,
					world.random.nextDouble() * 2.0D - 1.0D
			);
		} while (vector.lengthSquared() < 1.0E-6D);
		return vector.normalize();
	}

	private static void spawnStatic(ServerWorld world, ParticleEffect particle, Vec3d position) {
		world.spawnParticles(particle, position.x, position.y, position.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
	}

	private static void spawnDirected(ServerWorld world, ParticleEffect particle, Vec3d position, Vec3d velocity) {
		world.spawnParticles(particle, position.x, position.y, position.z, 0, velocity.x, velocity.y, velocity.z, 1.0D);
	}
}
