package com.zoyluo.guns.network;

import com.zoyluo.guns.Guns;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record GrenadeLauncherFirePayload() implements CustomPayload {
	public static final CustomPayload.Id<GrenadeLauncherFirePayload> ID = new CustomPayload.Id<>(Guns.id("grenade_launcher_fire"));
	public static final PacketCodec<RegistryByteBuf, GrenadeLauncherFirePayload> CODEC = CustomPayload.codecOf(GrenadeLauncherFirePayload::write, GrenadeLauncherFirePayload::read);

	private static GrenadeLauncherFirePayload read(RegistryByteBuf buf) {
		return new GrenadeLauncherFirePayload();
	}

	private void write(RegistryByteBuf buf) {
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
