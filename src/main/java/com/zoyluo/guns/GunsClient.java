package com.zoyluo.guns;

import com.zoyluo.guns.item.SniperRifleItem;
import com.zoyluo.guns.network.AdvancedWeaponFirePayload;
import com.zoyluo.guns.network.GrenadeLauncherFirePayload;
import com.zoyluo.guns.network.ShotgunFirePayload;
import com.zoyluo.guns.network.SniperFirePayload;
import com.zoyluo.guns.network.SniperZoomPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;

public class GunsClient implements ClientModInitializer {
	private static final int[] ZOOM_LEVELS = {1, 2, 4, 8, 16};
	private static int zoomLevel;
	private static boolean useWasDown;
	private static boolean attackWasDown;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(GunsClient::tick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> clearZoom());
		HudRenderCallback.EVENT.register(GunsClient::renderScope);
		EntityRendererRegistry.register(Guns.GRENADE_PROJECTILE, context -> new FlyingItemEntityRenderer<>(context, 1.0F, true));
	}

	public static void cycleZoom() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || !client.player.getMainHandStack().isOf(Guns.SNIPER_RIFLE)) {
			clearZoom();
			return;
		}

		zoomLevel = nextZoomLevel();
		if (ClientPlayNetworking.canSend(SniperZoomPayload.ID)) {
			ClientPlayNetworking.send(new SniperZoomPayload(zoomLevel));
		}
	}

	private static int nextZoomLevel() {
		for (int i = 0; i < ZOOM_LEVELS.length; i++) {
			if (zoomLevel == ZOOM_LEVELS[i]) {
				return i + 1 < ZOOM_LEVELS.length ? ZOOM_LEVELS[i + 1] : 0;
			}
		}
		return ZOOM_LEVELS[0];
	}

	public static float getFovMultiplier() {
		if (zoomLevel <= 0) {
			return 1.0F;
		}
		return Math.max(0.05F, 1.0F / zoomLevel);
	}

	public static boolean isScoped() {
		MinecraftClient client = MinecraftClient.getInstance();
		return zoomLevel > 0 && client.player != null && client.player.getMainHandStack().isOf(Guns.SNIPER_RIFLE);
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
		if (client.player == null || !client.player.getMainHandStack().isOf(Guns.SNIPER_RIFLE) || zoomLevel <= 0) {
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
			clearZoom();
			useWasDown = false;
			attackWasDown = false;
			return;
		}
		if (!isHoldingAnyGun(client)) {
			clearZoom();
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

	private static void renderScope(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (zoomLevel <= 0 || client.player == null || !client.player.getMainHandStack().isOf(Guns.SNIPER_RIFLE) || client.options.hudHidden) {
			return;
		}

		int width = context.getScaledWindowWidth();
		int height = context.getScaledWindowHeight();
		int centerX = width / 2;
		int centerY = height / 2;
		int radius = Math.min(width, height) / 2 - 10;
		int black = 0xE8000000;
		context.fill(0, 0, width, centerY - radius, black);
		context.fill(0, centerY + radius, width, height, black);
		context.fill(0, centerY - radius, centerX - radius, centerY + radius, black);
		context.fill(centerX + radius, centerY - radius, width, centerY + radius, black);

		int line = 0x66111111;
		context.fill(centerX, centerY - radius, centerX + 1, centerY + radius, line);
		context.fill(centerX - radius, centerY, centerX + radius, centerY + 1, line);
		context.fill(centerX - 4, centerY, centerX + 4, centerY + 1, 0x99FFFFFF);
		context.fill(centerX, centerY - 4, centerX + 1, centerY + 4, 0x99FFFFFF);
		drawTicks(context, centerX, centerY, radius);
		context.drawTextWithShadow(client.textRenderer, Text.translatable("hud.guns.sniper_zoom", zoomLevel), centerX + 14, centerY + 14, 0xDDE6E6E6);
	}

	private static void drawTicks(DrawContext context, int centerX, int centerY, int radius) {
		int color = 0x66E6E6E6;
		for (int offset = 40; offset < radius; offset += 40) {
			context.fill(centerX + offset, centerY - 1, centerX + offset + 1, centerY + 2, color);
			context.fill(centerX - offset, centerY - 1, centerX - offset + 1, centerY + 2, color);
			context.fill(centerX - 1, centerY + offset, centerX + 2, centerY + offset + 1, color);
			context.fill(centerX - 1, centerY - offset, centerX + 2, centerY - offset + 1, color);
		}
	}

	private static void clearZoom() {
		if (zoomLevel != 0 && ClientPlayNetworking.canSend(SniperZoomPayload.ID)) {
			ClientPlayNetworking.send(new SniperZoomPayload(0));
		}
		zoomLevel = 0;
	}
}
