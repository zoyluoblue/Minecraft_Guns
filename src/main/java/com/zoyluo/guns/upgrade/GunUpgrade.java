package com.zoyluo.guns.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum GunUpgrade {
	PRECISION_BARREL("precision_barrel"),
	COOLING_SYSTEM("cooling_system"),
	REINFORCED_RECEIVER("reinforced_receiver");

	private static final Map<Identifier, GunUpgrade> BY_ID = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(GunUpgrade::identifier, Function.identity()));
	public static final Codec<GunUpgrade> CODEC = Identifier.CODEC.comapFlatMap(
		identifier -> fromIdentifier(identifier)
				.map(DataResult::success)
				.orElseGet(() -> DataResult.error(() -> "Unknown Guns upgrade: " + identifier)),
		GunUpgrade::identifier
	);
	public static final PacketCodec<RegistryByteBuf, GunUpgrade> PACKET_CODEC = Identifier.PACKET_CODEC
			.xmap(identifier -> fromIdentifier(identifier).orElseThrow(() -> new IllegalArgumentException("Unknown Guns upgrade: " + identifier)), GunUpgrade::identifier)
			.cast();

	private final Identifier identifier;

	GunUpgrade(String id) {
		this.identifier = Identifier.of("guns", id);
	}

	public Identifier identifier() {
		return identifier;
	}

	public String translationKey() {
		return "item.guns.upgrade." + identifier.getPath();
	}

	public static Optional<GunUpgrade> fromIdentifier(Identifier identifier) {
		return Optional.ofNullable(BY_ID.get(identifier));
	}
}
