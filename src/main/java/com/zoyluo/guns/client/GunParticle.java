package com.zoyluo.guns.client;

import com.zoyluo.guns.visual.BallisticsVisuals;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public final class GunParticle extends SpriteBillboardParticle {
	private final SpriteProvider sprites;
	private final float startScale;
	private final float endScale;
	private final float startAlpha;
	private final float spin;
	private final ParticleTextureSheet textureSheet;

	private GunParticle(
			ClientWorld world,
			double x,
			double y,
			double z,
			double velocityX,
			double velocityY,
			double velocityZ,
			SpriteProvider sprites,
			Behavior behavior
	) {
		super(world, x, y, z, velocityX, velocityY, velocityZ);
		this.sprites = sprites;
		this.velocityX = velocityX;
		this.velocityY = velocityY;
		this.velocityZ = velocityZ;
		this.startScale = behavior.startScale * (0.9F + random.nextFloat() * 0.2F);
		this.endScale = behavior.endScale * (0.9F + random.nextFloat() * 0.2F);
		this.startAlpha = behavior.alpha;
		this.spin = behavior.spin * (random.nextBoolean() ? 1.0F : -1.0F);
		this.textureSheet = behavior.textureSheet;
		this.maxAge = behavior.minAge + random.nextInt(behavior.ageVariance + 1);
		this.scale = startScale;
		this.alpha = startAlpha;
		this.gravityStrength = behavior.gravity;
		this.velocityMultiplier = behavior.drag;
		this.collidesWithWorld = behavior.collides;
		this.angle = behavior.randomStartAngle() ? random.nextFloat() * (float) (Math.PI * 2.0D) : 0.0F;
		this.prevAngle = angle;
		setColor(behavior.red, behavior.green, behavior.blue);
		setSpriteForAge(sprites);
	}

	@Override
	public void tick() {
		prevAngle = angle;
		super.tick();
		if (dead) {
			return;
		}

		float progress = Math.min(1.0F, age / (float) maxAge);
		float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
		scale = startScale + (endScale - startScale) * eased;
		alpha = startAlpha * (1.0F - progress) * (1.0F - progress);
		angle += spin;
		setSpriteForAge(sprites);
	}

	@Override
	public ParticleTextureSheet getType() {
		return textureSheet;
	}

	public static ParticleFactory<SimpleParticleType> factory(SpriteProvider sprites, Behavior behavior) {
		return (parameters, world, x, y, z, velocityX, velocityY, velocityZ) ->
				new GunParticle(world, x, y, z, velocityX, velocityY, velocityZ, sprites, behavior);
	}

	public enum Behavior {
		TRACER(3, 1, 0.22F, 0.08F, 1.0F, 1.00F, 0.78F, 0.24F, 0.0F, 0.92F, 0.0F, false, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT),
		MUZZLE_FLASH(3, 1, 0.70F, 0.18F, 0.95F, 1.00F, 0.52F, 0.12F, 0.0F, 0.70F, 0.10F, false, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT),
		IMPACT_SPARK(7, 3, 0.28F, 0.05F, 1.0F, 1.00F, 0.72F, 0.20F, 0.10F, 0.84F, 0.22F, true, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT),
		FLAME_CORE(8, 4, 0.36F, 0.72F, 0.90F, 1.00F, 0.44F, 0.08F, -0.02F, 0.90F, 0.05F, false, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT),
		ENERGY_ARC(5, 3, 0.30F, 0.12F, 0.95F, 0.15F, 0.92F, 1.00F, 0.0F, 0.86F, 0.28F, false, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT),
		BLAST_WAVE(7, 2, 0.42F, 1.55F, 0.84F, 1.00F, 0.48F, 0.08F, 0.0F, 1.00F, 0.0F, false, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT),
		SHOCKWAVE(7, 2, 0.45F, 1.65F, 0.82F, 0.20F, 0.92F, 1.00F, 0.0F, 1.00F, 0.0F, false, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT),
		GRAY_ROUND(4, 1, 0.13F, 0.09F, 1.0F, 1.00F, 1.00F, 1.00F, 0.0F, 0.96F, 0.0F, false, ParticleTextureSheet.PARTICLE_SHEET_OPAQUE),
		GRAY_RANGE(5, 1, 0.16F, 0.07F, 0.72F, 1.00F, 1.00F, 1.00F, 0.0F, 0.90F, 0.04F, false, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT),
		BLACK_ROUND(3, 1, 0.12F, 0.08F, 1.0F, 1.00F, 1.00F, 1.00F, 0.0F, 0.94F, 0.0F, false, ParticleTextureSheet.PARTICLE_SHEET_OPAQUE),
		FLAME_JET(14, 4, 0.18F, 0.46F, 1.0F, 1.00F, 1.00F, 1.00F, -0.015F, 0.96F, 0.0F, false, ParticleTextureSheet.PARTICLE_SHEET_OPAQUE),
		WHITE_BEAM(BallisticsVisuals.RAILGUN_FADE_TICKS, 0, 0.12F, 0.035F, 0.92F, 1.00F, 1.00F, 1.00F, 0.0F, 1.00F, 0.0F, false, ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT);

		private final int minAge;
		private final int ageVariance;
		private final float startScale;
		private final float endScale;
		private final float alpha;
		private final float red;
		private final float green;
		private final float blue;
		private final float gravity;
		private final float drag;
		private final float spin;
		private final boolean collides;
		private final ParticleTextureSheet textureSheet;

		Behavior(
				int minAge,
				int ageVariance,
				float startScale,
				float endScale,
				float alpha,
				float red,
				float green,
				float blue,
				float gravity,
				float drag,
				float spin,
				boolean collides,
				ParticleTextureSheet textureSheet
		) {
			this.minAge = minAge;
			this.ageVariance = ageVariance;
			this.startScale = startScale;
			this.endScale = endScale;
			this.alpha = alpha;
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.gravity = gravity;
			this.drag = drag;
			this.spin = spin;
			this.collides = collides;
			this.textureSheet = textureSheet;
		}

		private boolean randomStartAngle() {
			return this != GRAY_ROUND && this != BLACK_ROUND && this != FLAME_JET && this != WHITE_BEAM;
		}
	}
}
