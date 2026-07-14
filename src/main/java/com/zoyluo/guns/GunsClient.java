package com.zoyluo.guns;

import com.zoyluo.guns.client.GunParticlesClient;
import com.zoyluo.guns.client.SniperScopeController;
import com.zoyluo.guns.network.AdvancedWeaponFirePayload;
import com.zoyluo.guns.network.GrenadeLauncherFirePayload;
import com.zoyluo.guns.network.ShotgunFirePayload;
import com.zoyluo.guns.network.SniperFirePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

public class GunsClient implements ClientModInitializer {
	private static boolean useWasDown;
	private static boolean attackWasDown;

	@Override
	public void onInitializeClient() {
		GunParticlesClient.register();
		ClientTickEvents.END_CLIENT_TICK.register(GunsClient::tick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> SniperScopeController.clear());
		HudRenderCallback.EVENT.register(SniperScopeController::render);
		EntityRendererRegistry.register(Guns.GRENADE_PROJECTILE, context -> new FlyingItemEntityRenderer<>(context, 1.0F, true));
	}

	public static void cycleZoom() {
		SniperScopeController.cycleZoom();
	}

	public static float getFovMultiplier() {
		return SniperScopeController.getFovMultiplier();
	}

	public static boolean isScoped() {
		return SniperScopeController.isScoped();
	}

	public static boolean shouldUseSniperPose(BipedEntityRenderState state) {
		MinecraftClient client = MinecraftClient.getInstance();
		return state instanceof PlayerEntityRenderState playerState
				&& client.player != null
				&& playerState.id == client.player.getId()
				&& (isScoped()
				|| client.player.getMainHandStack().isOf(Guns.SHOTGUN)
				|| client.player.getMainHandStack().isOf(Guns.GRENADE_LAUNCHER)
				|| client.player.getMainHandStack().isOf(Guns.SMG)
				|| client.player.getMainHandStack().isOf(Guns.FLAMETHROWER)
				|| client.player.getMainHandStack().isOf(Guns.RAILGUN));
	}

	public static boolean handleScopedUse(MinecraftClient client) {
		if (client.player == null || !client.player.getMainHandStack().isOf(Guns.SNIPER_RIFLE)) {
			return false;
		}

		cycleZoom();
		client.options.useKey.setPressed(false);
		return true;
	}

	public static boolean handleScopedAttack(MinecraftClient client) {
		if (client.player == null || !client.player.getMainHandStack().isOf(Guns.SNIPER_RIFLE) || !SniperScopeController.isScoped()) {
			return false;
		}

		if (ClientPlayNetworking.canSend(SniperFirePayload.ID)) {
			ClientPlayNetworking.send(new SniperFirePayload());
		}
		client.options.attackKey.setPressed(false);
		if (client.interactionManager != null) {
			client.interactionManager.cancelBlockBreaking();
		}
		return true;
	}

	public static boolean handleGunAttack(MinecraftClient client) {
		if (handleScopedAttack(client)) {
			return true;
		}
		AdvancedWeaponFirePayload.Weapon advancedWeapon = getAdvancedWeapon(client);
		if (advancedWeapon != null) {
			if (ClientPlayNetworking.canSend(AdvancedWeaponFirePayload.ID)) {
				ClientPlayNetworking.send(new AdvancedWeaponFirePayload(advancedWeapon));
			}
			if (!isAutomaticWeapon(advancedWeapon)) {
				client.options.attackKey.setPressed(false);
			}
			if (client.interactionManager != null) {
				client.interactionManager.cancelBlockBreaking();
			}
			return true;
		}
		if (client.player == null || !client.player.getMainHandStack().isOf(Guns.SHOTGUN)) {
			if (client.player == null || !client.player.getMainHandStack().isOf(Guns.GRENADE_LAUNCHER)) {
				return false;
			}
			if (ClientPlayNetworking.canSend(GrenadeLauncherFirePayload.ID)) {
				ClientPlayNetworking.send(new GrenadeLauncherFirePayload());
			}
			client.options.attackKey.setPressed(false);
			if (client.interactionManager != null) {
				client.interactionManager.cancelBlockBreaking();
			}
			return true;
		}

		if (ClientPlayNetworking.canSend(ShotgunFirePayload.ID)) {
			ClientPlayNetworking.send(new ShotgunFirePayload());
		}
		client.options.attackKey.setPressed(false);
		if (client.interactionManager != null) {
			client.interactionManager.cancelBlockBreaking();
		}
		return true;
	}

	private static AdvancedWeaponFirePayload.Weapon getAdvancedWeapon(MinecraftClient client) {
		if (client.player == null) {
			return null;
		}
		if (client.player.getMainHandStack().isOf(Guns.SMG)) {
			return AdvancedWeaponFirePayload.Weapon.SMG;
		}
		if (client.player.getMainHandStack().isOf(Guns.FLAMETHROWER)) {
			return AdvancedWeaponFirePayload.Weapon.FLAMETHROWER;
		}
		if (client.player.getMainHandStack().isOf(Guns.RAILGUN)) {
			return AdvancedWeaponFirePayload.Weapon.RAILGUN;
		}
		return null;
	}

	private static void tick(MinecraftClient client) {
		if (client.player == null || client.currentScreen != null) {
			SniperScopeController.clear();
			useWasDown = false;
			attackWasDown = false;
			return;
		}
		if (!isHoldingAnyGun(client)) {
			SniperScopeController.clear();
			useWasDown = client.options.useKey.isPressed();
			attackWasDown = client.options.attackKey.isPressed();
			return;
		}

		boolean useDown = client.options.useKey.isPressed();
		boolean attackDown = client.options.attackKey.isPressed();
		if (useDown && !useWasDown) {
			handleScopedUse(client);
		}
		if (attackDown && (isAutomaticWeapon(client) || !attackWasDown)) {
			handleGunAttack(client);
		}

		useWasDown = useDown;
		attackWasDown = attackDown;
	}

	private static boolean isAutomaticWeapon(MinecraftClient client) {
		return isAutomaticWeapon(getAdvancedWeapon(client));
	}

	private static boolean isAutomaticWeapon(AdvancedWeaponFirePayload.Weapon weapon) {
		return weapon == AdvancedWeaponFirePayload.Weapon.SMG
				|| weapon == AdvancedWeaponFirePayload.Weapon.FLAMETHROWER;
	}

	private static boolean isHoldingAnyGun(MinecraftClient client) {
		return client.player != null
				&& (client.player.getMainHandStack().isOf(Guns.SNIPER_RIFLE)
				|| client.player.getMainHandStack().isOf(Guns.SHOTGUN)
				|| client.player.getMainHandStack().isOf(Guns.GRENADE_LAUNCHER)
				|| getAdvancedWeapon(client) != null);
	}
}
