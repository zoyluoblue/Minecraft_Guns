package com.zoyluo.guns.client;

import com.zoyluo.guns.registry.ModParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public final class GunParticlesClient {
	private GunParticlesClient() {
	}

	public static void register() {
		ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
		registry.register(ModParticles.TRACER, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.TRACER));
		registry.register(ModParticles.MUZZLE_FLASH, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.MUZZLE_FLASH));
		registry.register(ModParticles.IMPACT_SPARK, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.IMPACT_SPARK));
		registry.register(ModParticles.FLAME_CORE, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.FLAME_CORE));
		registry.register(ModParticles.ENERGY_ARC, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.ENERGY_ARC));
		registry.register(ModParticles.BLAST_WAVE, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.BLAST_WAVE));
		registry.register(ModParticles.SHOCKWAVE, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.SHOCKWAVE));
		registry.register(ModParticles.GRAY_ROUND, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.GRAY_ROUND));
		registry.register(ModParticles.GRAY_RANGE, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.GRAY_RANGE));
		registry.register(ModParticles.BLACK_ROUND, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.BLACK_ROUND));
		registry.register(ModParticles.FLAME_JET, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.FLAME_JET));
		registry.register(ModParticles.WHITE_BEAM, sprites -> GunParticle.factory(sprites, GunParticle.Behavior.WHITE_BEAM));
	}
}
