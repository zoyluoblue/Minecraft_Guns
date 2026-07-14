package com.zoyluo.guns.registry;

import com.zoyluo.guns.Guns;
import com.zoyluo.guns.entity.GrenadeProjectileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModEntities {
	private static final Identifier GRENADE_PROJECTILE_ID = Guns.id("grenade_projectile");
	private static final RegistryKey<EntityType<?>> GRENADE_PROJECTILE_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, GRENADE_PROJECTILE_ID);

	public static final EntityType<GrenadeProjectileEntity> GRENADE_PROJECTILE = Registry.register(
			Registries.ENTITY_TYPE,
			GRENADE_PROJECTILE_ID,
			EntityType.Builder.<GrenadeProjectileEntity>create(GrenadeProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.35F, 0.35F)
					.maxTrackingRange(64)
					.trackingTickInterval(10)
					.build(GRENADE_PROJECTILE_KEY)
	);

	private ModEntities() {
	}

	public static void initialize() {
		// Loading this class performs the static entity registrations.
	}
}
