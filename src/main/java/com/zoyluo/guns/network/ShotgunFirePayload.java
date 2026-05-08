package com.zoyluo.guns.network;

import com.zoyluo.guns.Guns;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ShotgunFirePayload() implements CustomPayload {
	public static final CustomPayload.Id<ShotgunFirePayload> ID = new CustomPayload.Id<>(Guns.id("shotgun_fire"));
	public static final PacketCodec<RegistryByteBuf, ShotgunFirePayload> CODEC = CustomPayload.codecOf(ShotgunFirePayload::write, ShotgunFirePayload::read);

	private static ShotgunFirePayload read(RegistryByteBuf buf) {
		return new ShotgunFirePayload();
	}

	private void write(RegistryByteBuf buf) {
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
