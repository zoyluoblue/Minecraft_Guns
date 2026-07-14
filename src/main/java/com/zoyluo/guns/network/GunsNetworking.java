package com.zoyluo.guns.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class GunsNetworking {
	private GunsNetworking() {
	}

	public static void registerPayloadTypes() {
		PayloadTypeRegistry.playC2S().register(SniperZoomPayload.ID, SniperZoomPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SniperFirePayload.ID, SniperFirePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ShotgunFirePayload.ID, ShotgunFirePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(GrenadeLauncherFirePayload.ID, GrenadeLauncherFirePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(AdvancedWeaponFirePayload.ID, AdvancedWeaponFirePayload.CODEC);
	}
}
