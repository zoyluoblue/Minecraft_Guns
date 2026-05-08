package com.zoyluo.guns;

import com.zoyluo.guns.entity.GrenadeProjectileEntity;
import com.zoyluo.guns.item.AdvancedWeaponItem;
import com.zoyluo.guns.item.GrenadeLauncherItem;
import com.zoyluo.guns.item.SniperRifleItem;
import com.zoyluo.guns.item.ShotgunItem;
import com.zoyluo.guns.network.SniperRifleServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class Guns implements ModInitializer {
	public static final String MOD_ID = "guns";
	private static final Identifier SNIPER_RIFLE_ID = id("sniper_rifle");
	private static final Identifier SHOTGUN_ID = id("shotgun");
	private static final Identifier GRENADE_LAUNCHER_ID = id("grenade_launcher");
	private static final Identifier SMG_ID = id("smg");
	private static final Identifier FLAMETHROWER_ID = id("flamethrower");
	private static final Identifier RAILGUN_ID = id("railgun");
	private static final Identifier GRENADE_ROUND_ID = id("grenade_round");
	private static final Identifier GRENADE_PROJECTILE_ID = id("grenade_projectile");
	private static final Identifier ZYGUNS_ID = id("zyguns");
	private static final RegistryKey<Item> SNIPER_RIFLE_KEY = RegistryKey.of(RegistryKeys.ITEM, SNIPER_RIFLE_ID);
	private static final RegistryKey<Item> SHOTGUN_KEY = RegistryKey.of(RegistryKeys.ITEM, SHOTGUN_ID);
	private static final RegistryKey<Item> GRENADE_LAUNCHER_KEY = RegistryKey.of(RegistryKeys.ITEM, GRENADE_LAUNCHER_ID);
	private static final RegistryKey<Item> SMG_KEY = RegistryKey.of(RegistryKeys.ITEM, SMG_ID);
	private static final RegistryKey<Item> FLAMETHROWER_KEY = RegistryKey.of(RegistryKeys.ITEM, FLAMETHROWER_ID);
	private static final RegistryKey<Item> RAILGUN_KEY = RegistryKey.of(RegistryKeys.ITEM, RAILGUN_ID);
	private static final RegistryKey<Item> GRENADE_ROUND_KEY = RegistryKey.of(RegistryKeys.ITEM, GRENADE_ROUND_ID);
	private static final RegistryKey<EntityType<?>> GRENADE_PROJECTILE_KEY = RegistryKey.of(RegistryKeys.ENTITY_TYPE, GRENADE_PROJECTILE_ID);
	private static final RegistryKey<ItemGroup> ZYGUNS_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, ZYGUNS_ID);

	public static final Item SNIPER_RIFLE = Registry.register(
			Registries.ITEM,
			SNIPER_RIFLE_ID,
			new SniperRifleItem(new Item.Settings()
					.registryKey(SNIPER_RIFLE_KEY)
					.maxCount(1)
					.maxDamage(512)
					.enchantable(10)
					.rarity(Rarity.UNCOMMON))
	);

	public static final Item SHOTGUN = Registry.register(
			Registries.ITEM,
			SHOTGUN_ID,
			new ShotgunItem(new Item.Settings()
					.registryKey(SHOTGUN_KEY)
					.maxCount(1)
					.maxDamage(384)
					.enchantable(8)
					.rarity(Rarity.UNCOMMON))
	);

	public static final Item GRENADE_LAUNCHER = Registry.register(
			Registries.ITEM,
			GRENADE_LAUNCHER_ID,
			new GrenadeLauncherItem(new Item.Settings()
					.registryKey(GRENADE_LAUNCHER_KEY)
					.maxCount(1)
					.maxDamage(256)
					.enchantable(6)
					.rarity(Rarity.UNCOMMON))
	);

	public static final Item SMG = registerAdvancedWeapon(SMG_ID, SMG_KEY, "tooltip.guns.smg", 420, 6);
	public static final Item FLAMETHROWER = registerAdvancedWeapon(FLAMETHROWER_ID, FLAMETHROWER_KEY, "tooltip.guns.flamethrower", 640, 4);
	public static final Item RAILGUN = registerAdvancedWeapon(RAILGUN_ID, RAILGUN_KEY, "tooltip.guns.railgun", 180, 15);

	public static final Item GRENADE_ROUND = Registry.register(
			Registries.ITEM,
			GRENADE_ROUND_ID,
			new Item(new Item.Settings()
					.registryKey(GRENADE_ROUND_KEY)
					.maxCount(16)
					.rarity(Rarity.UNCOMMON))
	);

	public static final EntityType<GrenadeProjectileEntity> GRENADE_PROJECTILE = Registry.register(
			Registries.ENTITY_TYPE,
			GRENADE_PROJECTILE_ID,
			EntityType.Builder.<GrenadeProjectileEntity>create(GrenadeProjectileEntity::new, SpawnGroup.MISC)
					.dimensions(0.35F, 0.35F)
					.maxTrackingRange(64)
					.trackingTickInterval(10)
					.build(GRENADE_PROJECTILE_KEY)
	);

	public static final ItemGroup ZYGUNS = Registry.register(
			Registries.ITEM_GROUP,
			ZYGUNS_GROUP_KEY,
			FabricItemGroup.builder()
					.icon(() -> new ItemStack(SNIPER_RIFLE))
					.displayName(Text.translatable("itemGroup.guns.zyguns"))
					.entries((displayContext, entries) -> {
						entries.add(SNIPER_RIFLE);
						entries.add(SHOTGUN);
						entries.add(GRENADE_LAUNCHER);
						entries.add(SMG);
						entries.add(FLAMETHROWER);
						entries.add(RAILGUN);
					})
					.build()
	);

	@Override
	public void onInitialize() {
		SniperRifleServer.register();
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	private static Item registerAdvancedWeapon(Identifier id, RegistryKey<Item> key, String tooltipKey, int maxDamage, int enchantability) {
		return Registry.register(
				Registries.ITEM,
				id,
				new AdvancedWeaponItem(new Item.Settings()
						.registryKey(key)
						.maxCount(1)
						.maxDamage(maxDamage)
						.enchantable(enchantability)
						.rarity(Rarity.UNCOMMON), tooltipKey)
		);
	}
}
