package com.zoyluo.guns.gametest;

import net.fabricmc.api.ModInitializer;

/** Test-only entrypoint for the isolated Guns GameTest source set. */
public final class GunsGameTestMod implements ModInitializer {
	@Override
	public void onInitialize() {
		// The production Guns mod owns all registrations; this entrypoint only loads test classes.
	}
}
