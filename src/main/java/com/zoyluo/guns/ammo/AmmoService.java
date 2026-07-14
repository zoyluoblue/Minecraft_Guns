package com.zoyluo.guns.ammo;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative direct-inventory ammunition consumption for v1. */
public final class AmmoService {
	private static final Map<UUID, Integer> MISSING_FEEDBACK_COOLDOWNS = new HashMap<>();
	private static final int MISSING_FEEDBACK_TICKS = 10;

	private AmmoService() {
	}

	public static boolean tryConsume(ServerPlayerEntity player, AmmoType type) {
		if (player.isInCreativeMode()) {
			return true;
		}

		if (findAndConsume(player, type)) {
			return true;
		}

		if (MISSING_FEEDBACK_COOLDOWNS.getOrDefault(player.getUuid(), 0) <= 0) {
			player.sendMessage(Text.translatable("message.guns.ammo.empty", type.displayName()), true);
			MISSING_FEEDBACK_COOLDOWNS.put(player.getUuid(), MISSING_FEEDBACK_TICKS);
		}
		return false;
	}

	public static void tick() {
		MISSING_FEEDBACK_COOLDOWNS.replaceAll((uuid, ticks) -> ticks - 1);
		MISSING_FEEDBACK_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= 0);
	}

	public static void clear(ServerPlayerEntity player) {
		MISSING_FEEDBACK_COOLDOWNS.remove(player.getUuid());
	}

	public static void clearAll() {
		MISSING_FEEDBACK_COOLDOWNS.clear();
	}

	private static boolean findAndConsume(ServerPlayerEntity player, AmmoType type) {
		for (ItemStack stack : player.getInventory().main) {
			if (stack.isOf(type.item())) {
				stack.decrement(1);
				return true;
			}
		}
		for (ItemStack stack : player.getInventory().offHand) {
			if (stack.isOf(type.item())) {
				stack.decrement(1);
				return true;
			}
		}
		return false;
	}
}
