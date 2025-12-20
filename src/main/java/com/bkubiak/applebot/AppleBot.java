package com.bkubiak.applebot;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppleBot implements ModInitializer {
	public static final String MOD_ID = "applebot";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("AppleBot initialized!");
	}
}
