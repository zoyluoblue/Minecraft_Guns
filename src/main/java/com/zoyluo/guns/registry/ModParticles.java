package com.zoyluo.guns.registry;

import com.zoyluo.guns.Guns;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
	public static final SimpleParticleType TRACER = register("tracer");
	public static final SimpleParticleType MUZZLE_FLASH = register("muzzle_flash");
	public static final SimpleParticleType IMPACT_SPARK = register("impact_spark");
	public static final SimpleParticleType FLAME_CORE = register("flame_core");
	public static final SimpleParticleType ENERGY_ARC = register("energy_arc");
	public static final SimpleParticleType BLAST_WAVE = register("blast_wave");
	public static final SimpleParticleType SHOCKWAVE = register("shockwave");
	public static final SimpleParticleType GRAY_ROUND = register("gray_round");
	public static final SimpleParticleType GRAY_RANGE = register("gray_range");
	public static final SimpleParticleType BLACK_ROUND = register("black_round");
	public static final SimpleParticleType FLAME_JET = register("flame_jet");
	public static final SimpleParticleType WHITE_BEAM = register("white_beam");

	private ModParticles() {
	}

	public static void initialize() {
		// Loading this class performs the stable particle registrations.
	}

	private static SimpleParticleType register(String path) {
		return Registry.register(Registries.PARTICLE_TYPE, Guns.id(path), FabricParticleTypes.simple());
	}
}
