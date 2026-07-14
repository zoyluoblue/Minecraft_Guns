package com.zoyluo.guns.upgrade;

import com.zoyluo.guns.item.GunItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public final class GunUpgradeService {
	public static final int SCHEMA_VERSION = 1;
	public static final int MAX_MODULES = 3;
	private static final String ROOT_KEY = "GunsUpgrades";
	private static final String SCHEMA_KEY = "schema";
	private static final String MODULES_KEY = "modules";
	private static final int MAX_STORED_MODULE_IDS = 16;

	private GunUpgradeService() {
	}

	public static boolean isUpgradeableWeapon(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof GunItem;
	}

	public static List<String> getInstalledIds(ItemStack stack) {
		NbtCompound root = readRoot(stack);
		if (root == null || !root.contains(MODULES_KEY, NbtElement.LIST_TYPE)) {
			return List.of();
		}

		NbtList modules = root.getList(MODULES_KEY, NbtElement.STRING_TYPE);
		List<String> ids = new ArrayList<>(Math.min(modules.size(), MAX_STORED_MODULE_IDS));
		for (int index = 0; index < modules.size() && index < MAX_STORED_MODULE_IDS; index++) {
			String id = modules.getString(index);
			if (!id.isBlank()) {
				ids.add(id);
			}
		}
		return List.copyOf(ids);
	}

	public static List<GunUpgrade> getInstalled(ItemStack stack) {
		List<GunUpgrade> upgrades = new ArrayList<>();
		for (String id : getInstalledIds(stack)) {
			GunUpgrade.fromIdentifier(net.minecraft.util.Identifier.tryParse(id)).ifPresent(upgrade -> {
				if (!upgrades.contains(upgrade)) {
					upgrades.add(upgrade);
				}
			});
		}
		return List.copyOf(upgrades);
	}

	public static boolean has(ItemStack stack, GunUpgrade upgrade) {
		return getInstalledIds(stack).contains(upgrade.identifier().toString());
	}

	public static boolean canInstall(ItemStack stack, GunUpgrade upgrade) {
		if (!isUpgradeableWeapon(stack) || upgrade == null || readSchema(stack) > SCHEMA_VERSION) {
			return false;
		}
		List<String> ids = getInstalledIds(stack);
		return ids.size() < MAX_MODULES && !ids.contains(upgrade.identifier().toString());
	}

	public static void install(ItemStack stack, GunUpgrade upgrade) {
		if (!canInstall(stack, upgrade)) {
			return;
		}

		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, nbt -> {
			NbtCompound root = readRoot(nbt);
			if (root == null) {
				root = new NbtCompound();
			}
			NbtList modules = copyModuleList(root);
			modules.add(NbtString.of(upgrade.identifier().toString()));
			root.putInt(SCHEMA_KEY, SCHEMA_VERSION);
			root.put(MODULES_KEY, modules);
			nbt.put(ROOT_KEY, root);
		});
	}

	public static void appendTooltip(ItemStack stack, List<Text> tooltip) {
		List<GunUpgrade> upgrades = getInstalled(stack);
		if (upgrades.isEmpty()) {
			return;
		}

		tooltip.add(Text.translatable("tooltip.guns.upgrades").formatted(Formatting.GOLD));
		for (GunUpgrade upgrade : upgrades) {
			tooltip.add(Text.translatable(upgrade.translationKey()).formatted(Formatting.AQUA));
		}
	}

	public static double range(ItemStack stack, double base) {
		return has(stack, GunUpgrade.PRECISION_BARREL) ? base * 1.20D : base;
	}

	public static double spread(ItemStack stack, double base) {
		return has(stack, GunUpgrade.PRECISION_BARREL) ? base * 0.75D : base;
	}

	public static double cone(ItemStack stack, double base) {
		return has(stack, GunUpgrade.PRECISION_BARREL) ? base * 0.75D : base;
	}

	public static double projectileSpeed(ItemStack stack, double base) {
		return has(stack, GunUpgrade.PRECISION_BARREL) ? base * 1.10D : base;
	}

	public static float damage(ItemStack stack, float base) {
		return has(stack, GunUpgrade.REINFORCED_RECEIVER) ? base * 1.15F : base;
	}

	public static int cooldown(ItemStack stack, int base) {
		if (!has(stack, GunUpgrade.COOLING_SYSTEM)) {
			return base;
		}
		return Math.max(1, (int) Math.ceil(base * 0.80D));
	}

	private static NbtCompound readRoot(ItemStack stack) {
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		return component == null ? null : readRoot(component.copyNbt());
	}

	private static NbtCompound readRoot(NbtCompound nbt) {
		if (!nbt.contains(ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
			return null;
		}
		NbtCompound root = nbt.getCompound(ROOT_KEY);
		int schema = root.contains(SCHEMA_KEY, NbtElement.INT_TYPE) ? root.getInt(SCHEMA_KEY) : 0;
		return schema == SCHEMA_VERSION ? root : null;
	}

	private static int readSchema(ItemStack stack) {
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		if (component == null) {
			return SCHEMA_VERSION;
		}
		NbtCompound nbt = component.copyNbt();
		if (!nbt.contains(ROOT_KEY, NbtElement.COMPOUND_TYPE)) {
			return SCHEMA_VERSION;
		}
		NbtCompound root = nbt.getCompound(ROOT_KEY);
		return root.contains(SCHEMA_KEY, NbtElement.INT_TYPE) ? root.getInt(SCHEMA_KEY) : 0;
	}

	private static NbtList copyModuleList(NbtCompound root) {
		if (!root.contains(MODULES_KEY, NbtElement.LIST_TYPE)) {
			return new NbtList();
		}
		NbtList existing = root.getList(MODULES_KEY, NbtElement.STRING_TYPE);
		return existing.copy();
	}
}
