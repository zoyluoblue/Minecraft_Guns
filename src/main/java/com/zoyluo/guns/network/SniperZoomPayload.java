package com.zoyluo.guns.network;

import com.zoyluo.guns.Guns;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SniperZoomPayload(int zoomLevel) implements CustomPayload {
	public static final CustomPayload.Id<SniperZoomPayload> ID = new CustomPayload.Id<>(Guns.id("sniper_zoom"));
	public static final PacketCodec<RegistryByteBuf, SniperZoomPayload> CODEC = CustomPayload.codecOf(SniperZoomPayload::write, SniperZoomPayload::read);

	private static SniperZoomPayload read(RegistryByteBuf buf) {
		return new SniperZoomPayload(buf.readVarInt());
	}

	private void write(RegistryByteBuf buf) {
		buf.writeVarInt(zoomLevel);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}
}
