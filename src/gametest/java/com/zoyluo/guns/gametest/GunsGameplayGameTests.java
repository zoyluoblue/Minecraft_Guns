package com.zoyluo.guns.gametest;

import com.zoyluo.guns.ammo.AmmoService;
import com.zoyluo.guns.ammo.AmmoType;
import com.zoyluo.guns.recipe.GunUpgradeRecipe;
import com.zoyluo.guns.registry.ModItems;
import com.zoyluo.guns.upgrade.GunUpgrade;
import com.zoyluo.guns.upgrade.GunUpgradeService;
import com.zoyluo.guns.visual.BallisticsVisuals;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;

/** Runtime tests for the Guns survival loop that cannot be proven by JSON validation alone. */
@SuppressWarnings("removal")
public final class GunsGameplayGameTests implements FabricGameTest {
	private static final String UPGRADE_ROOT = "GunsUpgrades";

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
	public void ammunitionConsumesFromSurvivalInventory(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		player.changeGameMode(GameMode.SURVIVAL);
		PlayerInventory inventory = player.getInventory();
		inventory.clear();
		inventory.insertStack(new ItemStack(ModItems.RIFLE_ROUND, 2));

		require(context, AmmoService.tryConsume(player, AmmoType.RIFLE_ROUND), "first rifle round was not consumed");
		require(context, count(inventory, ModItems.RIFLE_ROUND) == 1, "first rifle round count mismatch");
		require(context, AmmoService.tryConsume(player, AmmoType.RIFLE_ROUND), "second rifle round was not consumed");
		require(context, count(inventory, ModItems.RIFLE_ROUND) == 0, "second rifle round count mismatch");
		require(context, !AmmoService.tryConsume(player, AmmoType.RIFLE_ROUND), "missing rifle round did not reject firing");
		AmmoService.clear(player);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
	public void creativeInventoryDoesNotConsumeAmmunition(TestContext context) {
		ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
		PlayerInventory inventory = player.getInventory();
		inventory.clear();
		inventory.insertStack(new ItemStack(ModItems.GRENADE_ROUND, 1));

		require(context, AmmoService.tryConsume(player, AmmoType.GRENADE_ROUND), "creative ammunition check rejected firing");
		require(context, count(inventory, ModItems.GRENADE_ROUND) == 1, "creative ammunition was consumed");
		AmmoService.clear(player);
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
	public void upgradesPersistAndEnforceUniqueThreeModuleLimit(TestContext context) {
		ItemStack bareWeapon = new ItemStack(ModItems.SNIPER_RIFLE);
		require(context, GunUpgradeService.range(bareWeapon, 100.0D) == 100.0D, "bare weapon range changed unexpectedly");
		require(context, GunUpgradeService.cooldown(bareWeapon, 5) == 5, "bare weapon cooldown changed unexpectedly");
		require(context, GunUpgradeService.damage(bareWeapon, 10.0F) == 10.0F, "bare weapon damage changed unexpectedly");
		require(context, new ItemStack(ModItems.SNIPER_RIFLE).get(DataComponentTypes.REPAIRABLE) != null, "sniper rifle is not Anvil-repairable");
		require(context, new ItemStack(ModItems.SHOTGUN).get(DataComponentTypes.REPAIRABLE) != null, "shotgun is not Anvil-repairable");
		require(context, new ItemStack(ModItems.GRENADE_LAUNCHER).get(DataComponentTypes.REPAIRABLE) != null, "grenade launcher is not Anvil-repairable");
		require(context, new ItemStack(ModItems.SMG).get(DataComponentTypes.REPAIRABLE) != null, "SMG is not Anvil-repairable");
		require(context, new ItemStack(ModItems.FLAMETHROWER).get(DataComponentTypes.REPAIRABLE) != null, "flamethrower is not Anvil-repairable");
		require(context, new ItemStack(ModItems.RAILGUN).get(DataComponentTypes.REPAIRABLE) != null, "railgun is not Anvil-repairable");

		ItemStack weapon = new ItemStack(ModItems.SNIPER_RIFLE);
		GunUpgradeService.install(weapon, GunUpgrade.PRECISION_BARREL);
		GunUpgradeService.install(weapon, GunUpgrade.COOLING_SYSTEM);
		GunUpgradeService.install(weapon, GunUpgrade.REINFORCED_RECEIVER);

		require(context, GunUpgradeService.getInstalled(weapon).size() == 3, "three installed modules were not retained");
		require(context, GunUpgradeService.has(weapon, GunUpgrade.PRECISION_BARREL), "precision barrel was not retained");
		require(context, GunUpgradeService.range(weapon, 100.0D) == 120.0D, "precision barrel range effect mismatch");
		require(context, GunUpgradeService.cooldown(weapon, 10) == 8, "cooling system effect mismatch");
		require(context, Math.abs(GunUpgradeService.damage(weapon, 10.0F) - 11.5F) < 0.001F, "reinforced receiver effect mismatch");
		require(context, !GunUpgradeService.canInstall(weapon, GunUpgrade.PRECISION_BARREL), "duplicate module was accepted");
		require(context, !GunUpgradeService.canInstall(weapon, GunUpgrade.COOLING_SYSTEM), "fourth module was accepted");
		require(context, GunUpgradeService.getInstalled(weapon.copy()).size() == 3, "module data was not copied with ItemStack");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
	public void futureUpgradeSchemaIsReadOnly(TestContext context) {
		ItemStack weapon = new ItemStack(ModItems.SNIPER_RIFLE);
		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, weapon, nbt -> {
			NbtCompound root = new NbtCompound();
			root.putInt("schema", GunUpgradeService.SCHEMA_VERSION + 1);
			root.put("modules", new NbtList());
			nbt.put(UPGRADE_ROOT, root);
		});

		require(context, !GunUpgradeService.canInstall(weapon, GunUpgrade.PRECISION_BARREL), "future schema accepted a new module");
		require(context, GunUpgradeService.getInstalled(weapon).isEmpty(), "future schema applied current-version effects");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
	public void smithingRecipeCraftsAndPreservesExistingModule(TestContext context) {
		RegistryKey<net.minecraft.recipe.Recipe<?>> recipeKey = RegistryKey.of(
				RegistryKeys.RECIPE,
				Identifier.of("guns", "smithing_precision_barrel")
		);
		RecipeEntry<?> entry = context.getWorld().getRecipeManager().get(recipeKey).orElseThrow(
				() -> new IllegalStateException("missing Guns Smithing recipe: " + recipeKey));
		require(context, entry.value() instanceof GunUpgradeRecipe, "Guns Smithing recipe has the wrong implementation");
		SmithingRecipe recipe = (SmithingRecipe) entry.value();
		ItemStack base = new ItemStack(ModItems.SNIPER_RIFLE);
		GunUpgradeService.install(base, GunUpgrade.COOLING_SYSTEM);
		SmithingRecipeInput input = new SmithingRecipeInput(
				new ItemStack(ModItems.UPGRADE_TEMPLATE),
				base,
				new ItemStack(ModItems.PRECISION_BARREL)
		);

		require(context, recipe.matches(input, context.getWorld()), "Smithing recipe did not match a valid input");
		ItemStack result = recipe.craft(input, context.getWorld().getRegistryManager());
		require(context, GunUpgradeService.has(result, GunUpgrade.COOLING_SYSTEM), "existing module was lost during Smithing craft");
		require(context, GunUpgradeService.has(result, GunUpgrade.PRECISION_BARREL), "new module was not installed during Smithing craft");

		ItemStack duplicateBase = new ItemStack(ModItems.SNIPER_RIFLE);
		GunUpgradeService.install(duplicateBase, GunUpgrade.PRECISION_BARREL);
		SmithingRecipeInput duplicateInput = new SmithingRecipeInput(
				new ItemStack(ModItems.UPGRADE_TEMPLATE),
				duplicateBase,
				new ItemStack(ModItems.PRECISION_BARREL)
		);
		require(context, !recipe.matches(duplicateInput, context.getWorld()), "duplicate Smithing module was accepted");
		context.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
	public void ballisticsSamplingHasHardBudgets(TestContext context) {
		require(context, BallisticsVisuals.sampleCount(0.0D, 1.60D, 80) == 0, "zero-length tracer generated samples");
		require(context, BallisticsVisuals.sampleCount(Double.NaN, 1.60D, 80) == 0, "invalid tracer length generated samples");
		require(context, BallisticsVisuals.sampleCount(128.0D, 1.60D, BallisticsVisuals.SNIPER_MAX_SAMPLES) == BallisticsVisuals.SNIPER_MAX_SAMPLES, "sniper solid-round effect exceeded or missed its hard cap");
		require(context, Math.abs(BallisticsVisuals.SNIPER_VISUAL_SPEED - 0.110D) < 0.000001D, "sniper visual speed must be exactly twice the previous value");
		require(context, BallisticsVisuals.sampleCount(8.0D, 1.55D, BallisticsVisuals.SHOTGUN_MAX_SAMPLES_PER_PELLET) == BallisticsVisuals.SHOTGUN_MAX_SAMPLES_PER_PELLET, "shotgun range-effect cap mismatch");
		require(context, BallisticsVisuals.sampleCount(32.0D, 2.80D, BallisticsVisuals.SMG_MAX_SAMPLES) == BallisticsVisuals.SMG_MAX_SAMPLES, "SMG solid-round effect cap mismatch");

		require(context, BallisticsVisuals.shotgunWorstCaseCalls() == 91, "shotgun visual composition call count changed unexpectedly");
		require(context, BallisticsVisuals.shotgunWorstCaseCalls() <= BallisticsVisuals.SHOTGUN_MAX_CALLS, "shotgun visual composition exceeds its hard budget");
		require(context, BallisticsVisuals.railgunWorstCaseCalls() == 204, "railgun visual composition call count changed unexpectedly");
		require(context, BallisticsVisuals.railgunWorstCaseCalls() <= BallisticsVisuals.RAILGUN_MAX_CALLS, "railgun visual composition exceeds its hard budget");
		require(context, BallisticsVisuals.RAILGUN_FADE_TICKS == 60, "railgun beam must fade over exactly three seconds");
		require(context, BallisticsVisuals.RAILGUN_VISIBLE_ENTITY_HITS == 4, "railgun visible entity impact limit changed");
		require(context, BallisticsVisuals.SHOTGUN_PELLETS == 13, "shotgun visual pellet count changed");
		require(context, BallisticsVisuals.FLAMETHROWER_MAX_PARTICLES == 6, "flamethrower moving-particle budget changed");
		require(context, BallisticsVisuals.GRENADE_TRAIL_MAX_PARTICLES == 3, "grenade trail visual budget changed");
		context.complete();
	}

	private static int count(PlayerInventory inventory, Item item) {
		int total = 0;
		for (ItemStack stack : inventory.main) {
			if (stack.isOf(item)) {
				total += stack.getCount();
			}
		}
		for (ItemStack stack : inventory.offHand) {
			if (stack.isOf(item)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static void require(TestContext context, boolean condition, String message) {
		if (!condition) {
			context.throwGameTestException(message);
		}
	}
}
