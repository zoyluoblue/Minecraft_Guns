package com.zoyluo.guns.client;

import com.zoyluo.guns.Guns;
import com.zoyluo.guns.network.SniperZoomPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public final class SniperScopeController {
	private static final int[] ZOOM_LEVELS = {1, 2, 4, 8, 16};
	private static int zoomLevel;

	private SniperScopeController() {
	}

	public static void cycleZoom() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || !client.player.getMainHandStack().isOf(Guns.SNIPER_RIFLE)) {
			clear();
			return;
		}

		zoomLevel = nextZoomLevel();
		if (ClientPlayNetworking.canSend(SniperZoomPayload.ID)) {
			ClientPlayNetworking.send(new SniperZoomPayload(zoomLevel));
		}
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

	public static void clear() {
		if (zoomLevel != 0 && ClientPlayNetworking.canSend(SniperZoomPayload.ID)) {
			ClientPlayNetworking.send(new SniperZoomPayload(0));
		}
		zoomLevel = 0;
	}

	public static void render(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!isScoped() || client.options.hudHidden) {
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

	private static int nextZoomLevel() {
		for (int i = 0; i < ZOOM_LEVELS.length; i++) {
			if (zoomLevel == ZOOM_LEVELS[i]) {
				return i + 1 < ZOOM_LEVELS.length ? ZOOM_LEVELS[i + 1] : 0;
			}
		}
		return ZOOM_LEVELS[0];
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
}
