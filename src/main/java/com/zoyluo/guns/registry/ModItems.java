package com.zoyluo.guns.registry;

import com.zoyluo.guns.Guns;
import com.zoyluo.guns.item.AdvancedWeaponItem;
import com.zoyluo.guns.item.GrenadeLauncherItem;
import com.zoyluo.guns.item.UpgradeModuleItem;
import com.zoyluo.guns.item.UpgradeTemplateItem;
import com.zoyluo.guns.item.ShotgunItem;
import com.zoyluo.guns.item.SniperRifleItem;
import com.zoyluo.guns.upgrade.GunUpgrade;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ModItems {
	private static final Identifier SNIPER_RIFLE_ID = Guns.id("sniper_rifle");
	private static final Identifier SHOTGUN_ID = Guns.id("shotgun");
	private static final Identifier GRENADE_LAUNCHER_ID = Guns.id("grenade_launcher");
	private static final Identifier SMG_ID = Guns.id("smg");
	private static final Identifier FLAMETHROWER_ID = Guns.id("flamethrower");
	private static final Identifier RAILGUN_ID = Guns.id("railgun");
	private static final Identifier GRENADE_ROUND_ID = Guns.id("grenade_round");
	private static final Identifier RIFLE_ROUND_ID = Guns.id("rifle_round");
	private static final Identifier SHOTGUN_SHELL_ID = Guns.id("shotgun_shell");
	private static final Identifier FUEL_CELL_ID = Guns.id("fuel_cell");
	private static final Identifier RAILGUN_CELL_ID = Guns.id("railgun_cell");
	private static final Identifier UPGRADE_TEMPLATE_ID = Guns.id("upgrade_template");
	private static final Identifier PRECISION_BARREL_ID = Guns.id("precision_barrel");
	private static final Identifier COOLING_SYSTEM_ID = Guns.id("cooling_system");
	private static final Identifier REINFORCED_RECEIVER_ID = Guns.id("reinforced_receiver");

	public static final Item SNIPER_RIFLE = Registry.register(
			Registries.ITEM,
			SNIPER_RIFLE_ID,
			new SniperRifleItem(weaponSettings(SNIPER_RIFLE_ID, 512, 10))
	);
	public static final Item SHOTGUN = Registry.register(
			Registries.ITEM,
			SHOTGUN_ID,
			new ShotgunItem(weaponSettings(SHOTGUN_ID, 384, 8))
	);
	public static final Item GRENADE_LAUNCHER = Registry.register(
			Registries.ITEM,
			GRENADE_LAUNCHER_ID,
			new GrenadeLauncherItem(weaponSettings(GRENADE_LAUNCHER_ID, 256, 6))
	);
	public static final Item SMG = registerAdvancedWeapon(SMG_ID, "tooltip.guns.smg", 420, 6);
	public static final Item FLAMETHROWER = registerAdvancedWeapon(FLAMETHROWER_ID, "tooltip.guns.flamethrower", 640, 4);
	public static final Item RAILGUN = registerAdvancedWeapon(RAILGUN_ID, "tooltip.guns.railgun", 180, 15);
	public static final Item GRENADE_ROUND = Registry.register(
			Registries.ITEM,
			GRENADE_ROUND_ID,
			new Item(new Item.Settings()
					.registryKey(itemKey(GRENADE_ROUND_ID))
					.maxCount(16)
					.rarity(Rarity.UNCOMMON))
	);
	public static final Item RIFLE_ROUND = registerAmmo(RIFLE_ROUND_ID, 64);
	public static final Item SHOTGUN_SHELL = registerAmmo(SHOTGUN_SHELL_ID, 64);
	public static final Item FUEL_CELL = registerAmmo(FUEL_CELL_ID, 64);
	public static final Item RAILGUN_CELL = registerAmmo(RAILGUN_CELL_ID, 64);
	public static final Item UPGRADE_TEMPLATE = Registry.register(
			Registries.ITEM,
			UPGRADE_TEMPLATE_ID,
			new UpgradeTemplateItem(new Item.Settings()
					.registryKey(itemKey(UPGRADE_TEMPLATE_ID))
					.maxCount(16))
	);
	public static final Item PRECISION_BARREL = registerUpgradeModule(PRECISION_BARREL_ID, GunUpgrade.PRECISION_BARREL);
	public static final Item COOLING_SYSTEM = registerUpgradeModule(COOLING_SYSTEM_ID, GunUpgrade.COOLING_SYSTEM);
	public static final Item REINFORCED_RECEIVER = registerUpgradeModule(REINFORCED_RECEIVER_ID, GunUpgrade.REINFORCED_RECEIVER);

	private ModItems() {
	}

	public static void initialize() {
		// Loading this class performs the static item registrations.
	}

	private static Item registerAdvancedWeapon(Identifier id, String tooltipKey, int maxDamage, int enchantability) {
		return Registry.register(
				Registries.ITEM,
				id,
				new AdvancedWeaponItem(weaponSettings(id, maxDamage, enchantability), tooltipKey)
		);
	}

	private static Item.Settings weaponSettings(Identifier id, int maxDamage, int enchantability) {
		return new Item.Settings()
				.registryKey(itemKey(id))
				.maxCount(1)
				.maxDamage(maxDamage)
				.enchantable(enchantability)
				.repairable(Items.IRON_INGOT)
				.rarity(Rarity.UNCOMMON);
	}

	private static Item registerAmmo(Identifier id, int maxCount) {
		return Registry.register(
				Registries.ITEM,
				id,
				new Item(new Item.Settings()
						.registryKey(itemKey(id))
						.maxCount(maxCount))
		);
	}

	private static Item registerUpgradeModule(Identifier id, GunUpgrade upgrade) {
		return Registry.register(
				Registries.ITEM,
				id,
				new UpgradeModuleItem(new Item.Settings()
						.registryKey(itemKey(id))
						.maxCount(16), upgrade)
		);
	}

	private static RegistryKey<Item> itemKey(Identifier id) {
		return RegistryKey.of(RegistryKeys.ITEM, id);
	}
}
