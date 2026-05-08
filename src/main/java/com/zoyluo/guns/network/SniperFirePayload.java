package com.zoyluo.guns.network;

import com.zoyluo.guns.Guns;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SniperFirePayload() implements CustomPayload {
	public static final CustomPayload.Id<SniperFirePayload> ID = new CustomPayload.Id<>(Guns.id("sniper_fire"));
	public static final PacketCodec<RegistryByteBuf, SniperFirePayload> CODEC = CustomPayload.codecOf(SniperFirePayload::write, SniperFirePayload::read);

	private static SniperFirePayload read(RegistryByteBuf buf) {
		return new SniperFirePayload();
	}

	private void write(RegistryByteBuf buf) {
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
