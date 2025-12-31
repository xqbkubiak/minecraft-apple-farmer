package com.bkubiak.applebot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_BASE_DIR = FabricLoader.getInstance().getConfigDir().resolve("BK-Mods")
            .resolve("BK-Apple");
    private static final File CONFIG_FILE = CONFIG_BASE_DIR.resolve("config.json").toFile();

    public static class Config {
        public int tickDelay = 2;
        public int repairMode = 1;
        public boolean autoEat = true;
        public int hungerThreshold = 14;
        public String repairCommand = "repair";
        public String language = "pl";
        public boolean storageMode = false;
        public int storageCyclesLimit = 1000;
        public int pickupCycles = 0; // 0 = disabled, otherwise pause after X cycles
        public int pickupWaitSeconds = 0; // How many seconds to wait (and pickup)
        public boolean restockMode = false;
    }

    public static Config load() {
        if (!CONFIG_FILE.exists()) {
            return new Config();
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Config config = GSON.fromJson(reader, Config.class);
            return config != null ? config : new Config();
        } catch (Exception e) {
            System.err.println("[AppleBot] Failed to load config: " + e.getMessage());
            return new Config();
        }
    }

    public static void save(Config config) {
        try {
            CONFIG_BASE_DIR.toFile().mkdirs();
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (Exception e) {
            System.err.println("[AppleBot] Failed to save config: " + e.getMessage());
        }
    }

    public static Config createFromBot(AppleBotClientBase bot) {
        Config config = new Config();
        config.tickDelay = bot.getTickDelay();
        config.repairMode = bot.getRepairMode();
        config.autoEat = bot.isAutoEat();
        config.repairCommand = bot.getRepairCommand();
        config.language = bot.getLanguage();
        config.storageMode = bot.isStorageMode();
        config.storageCyclesLimit = bot.getStorageCycles();
        config.pickupCycles = bot.getPickupCycles();
        config.pickupWaitSeconds = bot.getPickupWaitSeconds();
        config.restockMode = bot.isRestockMode();
        return config;
    }
}
