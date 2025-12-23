package com.bkubiak.applebot;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import java.util.Random;

public class AppleBotClient implements ClientModInitializer {

    private boolean isRunning = false;

    // Config
    private int tickDelay = 2;
    private int repairMode = 1;
    private boolean autoEat = true;
    private int hungerThreshold = 14;
    private String repairCommand = "repair";
    private String language = "pl"; // pl or en

    private int tickCounter = 0;
    private int fullCycles = 0;
    private int dropCycles = 0; // Counter for apple drop
    private int totalCycles = 0; // Global counter for display
    private int repairCooldown = 0; // Cooldown for repair cmd

    private int state = 0;
    private int slotIndex = 0;
    private int previousSlot = 0;
    private boolean isEating = false;
    private int eatingTicks = 0;

    // Dumping State
    private boolean isDumping = false;
    private int dumpState = 0;
    private int dumpTimer = 0;

    // Storage Config
    private boolean storageMode = false;
    private int storageCyclesLimit = 1000;

    // Pickup Config
    private int pickupCycles = 0; // 0 = disabled, pause after X cycles
    private int pickupWaitSeconds = 0; // How many seconds to wait
    private int pickupCounter = 0;
    private boolean isPickupPause = false;
    private int pickupWaitTicks = 0;

    private final Random random = new Random();

    // Getters and Setters for GUI
    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean v) {
        isRunning = v;
    }

    public int getTickDelay() {
        return tickDelay;
    }

    public void setTickDelay(int v) {
        tickDelay = v;
        saveConfig();
    }

    // Repair Mode: 0=OFF, 1=COMMAND, 2=CRAFT
    public int getRepairMode() {
        return repairMode;
    }

    public void setRepairMode(int v) {
        repairMode = v;
        saveConfig();
    }

    public void cycleRepairMode() {
        repairMode = (repairMode + 1) % 3;
        saveConfig();
    }

    public String getRepairModeName() {
        return switch (repairMode) {
            case 0 -> "OFF";
            case 1 -> "COMMAND";
            case 2 -> "CRAFT";
            default -> "OFF";
        };
    }

    // Legacy methods for compatibility
    public boolean isAutoRepair() {
        return repairMode == 1;
    }

    public void setAutoRepair(boolean v) {
        if (v)
            repairMode = 1;
        else if (repairMode == 1)
            repairMode = 0;
    }

    public boolean isFreeRepair() {
        return repairMode == 2;
    }

    public void setFreeRepair(boolean v) {
        if (v)
            repairMode = 2;
        else if (repairMode == 2)
            repairMode = 0;
    }

    public boolean isAutoEat() {
        return autoEat;
    }

    public void setAutoEat(boolean v) {
        autoEat = v;
        saveConfig();
    }

    public boolean isStorageMode() {
        return storageMode;
    }

    public void setStorageMode(boolean v) {
        storageMode = v;
        saveConfig();
    }

    public int getStorageCycles() {
        return storageCyclesLimit;
    }

    public void setStorageCycles(int v) {
        storageCyclesLimit = v;
        saveConfig();
    }

    public void cycleStorageCycles() {
        if (storageCyclesLimit == 20)
            storageCyclesLimit = 100;
        else if (storageCyclesLimit == 100)
            storageCyclesLimit = 500;
        else if (storageCyclesLimit == 500)
            storageCyclesLimit = 1000;
        else if (storageCyclesLimit == 1000)
            storageCyclesLimit = 2000;
        else
            storageCyclesLimit = 20;
        saveConfig();
    }

    public String getRepairCommand() {
        return repairCommand;
    }

    public void setRepairCommand(String cmd) {
        repairCommand = cmd;
        saveConfig();
    }

    public int getTotalCycles() {
        return totalCycles;
    }

    // Language system
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        language = lang;
        saveConfig();
    }

    public int getPickupCycles() {
        return pickupCycles;
    }

    public void setPickupCycles(int v) {
        pickupCycles = v;
        saveConfig();
    }

    public int getPickupWaitSeconds() {
        return pickupWaitSeconds;
    }

    public void setPickupWaitSeconds(int v) {
        pickupWaitSeconds = v;
        saveConfig();
    }

    // Translation helper
    public String t(String key) {
        return switch (key) {
            // GUI Labels
            case "status_bot" -> language.equals("pl") ? "Status Bota" : "Bot Status";
            case "delay" -> language.equals("pl") ? "Opoznienie" : "Delay";
            case "repair_mode" -> language.equals("pl") ? "Tryb Naprawy" : "Repair Mode";
            case "auto_eat" -> "Auto-Eat";
            case "slot" -> language.equals("pl") ? "Aktualny Slot" : "Current Slot";
            case "durability" -> language.equals("pl") ? "Wytrzymalosc" : "Durability";
            case "command" -> language.equals("pl") ? "Komenda" : "Command";
            case "cycles" -> language.equals("pl") ? "Cykle" : "Cycles";
            case "profile" -> language.equals("pl") ? "Profil" : "Profile";
            case "function" -> language.equals("pl") ? "Funkcja" : "Function";
            case "status" -> "Status";
            // Status values
            case "on" -> language.equals("pl") ? "WLACZONY" : "ON";
            case "off" -> language.equals("pl") ? "WYLACZONY" : "OFF";
            case "active" -> language.equals("pl") ? "AKTYWNY" : "ACTIVE";
            case "inactive" -> language.equals("pl") ? "NIEAKTYWNY" : "INACTIVE";
            case "ticks" -> language.equals("pl") ? "tickow" : "ticks";
            // Messages
            case "msg_eating" -> language.equals("pl") ? "Jem..." : "Eating...";
            case "msg_ate" -> language.equals("pl") ? "Zjadlem!" : "Ate!";
            case "msg_shears" -> language.equals("pl") ? "Nozyce -> Slot 1!" : "Shears -> Slot 1!";
            case "msg_no_iron" -> language.equals("pl") ? "Brak zelaza!" : "No iron!";
            case "msg_started" -> language.equals("pl") ? "Bot uruchomiony!" : "Bot started!";
            case "msg_stopped" -> language.equals("pl") ? "Bot zatrzymany!" : "Bot stopped!";
            case "msg_ad" -> language.equals("pl") ? "Najlepszy skrypt na yt: @rajzeh" : "Best script on yt: @rajzeh";
            case "msg_lang" -> language.equals("pl") ? "Jezyk: Polski" : "Language: English";
            // Help texts
            case "help_gui" -> language.equals("pl") ? "Panel Sterowania" : "Control Panel";
            case "help_start" -> language.equals("pl") ? "Uruchom bota" : "Start bot";
            case "help_stop" -> language.equals("pl") ? "Zatrzymaj bota" : "Stop bot";
            case "help_speed" -> language.equals("pl") ? "Szybkosc" : "Speed";
            default -> key;
        };
    }

    public void performCraftingPublic(MinecraftClient client) {
        performCrafting(client);
    }

    private void saveConfig() {
        ConfigManager.save(ConfigManager.createFromBot(this));
    }

    private void loadConfig() {
        ConfigManager.Config config = ConfigManager.load();
        this.tickDelay = config.tickDelay;
        this.repairMode = config.repairMode;
        this.autoEat = config.autoEat;
        this.hungerThreshold = config.hungerThreshold;
        this.repairCommand = config.repairCommand;
        this.language = config.language;
        this.storageMode = config.storageMode;
        this.storageCyclesLimit = config.storageCyclesLimit;
        this.pickupCycles = config.pickupCycles;
        this.pickupWaitSeconds = config.pickupWaitSeconds;
    }

    @Override
    public void onInitializeClient() {
        loadConfig();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("xqbk")
                    .executes(context -> {
                        sendHelp(context.getSource());
                        return 1;
                    })
                    .then(ClientCommandManager.literal("gui")
                            .executes(context -> {
                                MinecraftClient.getInstance().send(() -> {
                                    MinecraftClient.getInstance().setScreen(new XqbkScreen(this));
                                });
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("start")
                            .executes(context -> {
                                isRunning = true;
                                saveConfig();
                                context.getSource().sendFeedback(Text.of("§8[§2xQBK§8] §a" + t("msg_started")));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("stop")
                            .executes(context -> {
                                isRunning = false;
                                saveConfig();
                                context.getSource().sendFeedback(Text.of("§8[§2xQBK§8] §c" + t("msg_stopped")));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("config")
                            .then(ClientCommandManager.literal("delay")
                                    .then(ClientCommandManager.argument("ticks", IntegerArgumentType.integer(1, 20))
                                            .executes(context -> {
                                                int ticks = IntegerArgumentType.getInteger(context, "ticks");
                                                tickDelay = ticks;
                                                saveConfig();
                                                context.getSource().sendFeedback(
                                                        Text.of("§8[§2xQBK§8] §7Delay: §a" + ticks + "t"));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("repairmode")
                                    .then(ClientCommandManager.literal("off")
                                            .executes(context -> {
                                                repairMode = 0;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of("§8[§2xQBK§8] §7Repair Mode: §cOFF"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("command")
                                            .executes(context -> {
                                                repairMode = 1;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of("§8[§2xQBK§8] §7Repair Mode: §aCOMMAND"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("craft")
                                            .executes(context -> {
                                                repairMode = 2;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of("§8[§2xQBK§8] §7Repair Mode: §aCRAFT"));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("repaircmd")
                                    .then(ClientCommandManager.argument("command", StringArgumentType.word())
                                            .executes(context -> {
                                                String cmd = StringArgumentType.getString(context, "command");
                                                repairCommand = cmd;
                                                saveConfig();
                                                context.getSource().sendFeedback(
                                                        Text.of("§8[§2xQBK§8] §7" + t("command") + ": §a/" + cmd));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("autoeat")
                                    .then(ClientCommandManager.literal("on")
                                            .executes(context -> {
                                                autoEat = true;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of("§8[§2xQBK§8] §aAuto-Eat: ON"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("off")
                                            .executes(context -> {
                                                autoEat = false;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of("§8[§2xQBK§8] §cAuto-Eat: OFF"));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("storage")
                                    .then(ClientCommandManager.literal("off")
                                            .executes(context -> {
                                                storageMode = false;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of("§8[§2xQBK§8] §cStorage: OFF"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("on")
                                            .executes(context -> {
                                                storageMode = true;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of("§8[§2xQBK§8] §aStorage: ON (Default: "
                                                                + storageCyclesLimit + ")"));
                                                return 1;
                                            })
                                            .then(ClientCommandManager
                                                    .argument("cycles", IntegerArgumentType.integer(10))
                                                    .executes(context -> {
                                                        storageMode = true;
                                                        storageCyclesLimit = IntegerArgumentType.getInteger(context,
                                                                "cycles");
                                                        saveConfig();
                                                        context.getSource().sendFeedback(
                                                                Text.of("§8[§2xQBK§8] §aStorage: ON (Cycles: "
                                                                        + storageCyclesLimit + ")"));
                                                        return 1;
                                                    }))))
                            .then(ClientCommandManager.literal("pickupcooldown")
                                    .then(ClientCommandManager.literal("off")
                                            .executes(context -> {
                                                pickupCycles = 0;
                                                pickupWaitSeconds = 0;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of("§8[§2xQBK§8] §cPickup: §cOFF"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.argument("cycles", IntegerArgumentType.integer(0))
                                            .then(ClientCommandManager
                                                    .argument("seconds", IntegerArgumentType.integer(0))
                                                    .executes(context -> {
                                                        int cycles = IntegerArgumentType.getInteger(context, "cycles");
                                                        int seconds = IntegerArgumentType.getInteger(context,
                                                                "seconds");
                                                        pickupCycles = cycles;
                                                        pickupWaitSeconds = seconds;
                                                        saveConfig();
                                                        if (cycles == 0) {
                                                            context.getSource().sendFeedback(
                                                                    Text.of("§8[§2xQBK§8] §cPickup: §cOFF"));
                                                        } else {
                                                            context.getSource().sendFeedback(
                                                                    Text.of("§8[§2xQBK§8] §aPickup: Every §e" + cycles
                                                                            + " §acycles, wait §e" + seconds + "s"));
                                                        }
                                                        return 1;
                                                    })))))
                    .then(ClientCommandManager.literal("pl")
                            .executes(context -> {
                                language = "pl";
                                saveConfig();
                                context.getSource().sendFeedback(Text.of("§8[§2xQBK§8] §aJezyk: Polski"));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("en")
                            .executes(context -> {
                                language = "en";
                                saveConfig();
                                context.getSource().sendFeedback(Text.of("§8[§2xQBK§8] §aLanguage: English"));
                                return 1;
                            }))); // End dispatcher.register
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (isRunning && client.player != null) {
                if (repairCooldown > 0)
                    repairCooldown--;

                // AUTO-EAT CHECK - Priority over normal actions
                if (autoEat && !isEating) {
                    int hunger = client.player.getHungerManager().getFoodLevel();
                    if (hunger <= hungerThreshold) {
                        tryToEat(client);
                    }
                }

                // Handle eating in progress
                if (isEating) {
                    eatingTicks++;
                    // Keep holding right click while eating (32 ticks = eat time)
                    if (eatingTicks < 40) {
                        // Continue eating
                        client.options.useKey.setPressed(true);
                    } else {
                        // Done eating
                        client.options.useKey.setPressed(false);
                        client.player.getInventory().selectedSlot = previousSlot;
                        isEating = false;
                        eatingTicks = 0;
                        client.player.sendMessage(Text.of("§8[§2xQBK§8] §a" + t("msg_ate")), true);
                    }
                    return; // Don't do other actions while eating
                }

                // AUTO-DUMP PRIORITY
                if (isDumping) {
                    performDump(client);
                    return; // Don't mine while dumping
                }

                // PICKUP PAUSE - Wait and skip farming
                if (isPickupPause) {
                    pickupWaitTicks--;
                    if (pickupWaitTicks <= 0) {
                        isPickupPause = false;
                        client.player
                                .sendMessage(
                                        Text.of("§8[§2xQBK§8] §a"
                                                + (language.equals("pl") ? "Wznawiam farming!" : "Resuming farming!")),
                                        true);
                    }
                    return; // Don't farm during pause
                }

                tickCounter++;
                if (tickCounter >= tickDelay) {
                    tickCounter = 0;
                    performAction(client);
                }
            }
        });
    }

    private void sendHelp(net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source) {
        source.sendFeedback(Text.of("§8§m                                                  "));
        source.sendFeedback(Text.of("§8[§2xQBK§8] §7xQBK Bot §av2.6"));
        source.sendFeedback(Text.of(""));
        source.sendFeedback(Text.of("§8 - §a/xqbk gui §8- §7" + t("help_gui")));
        source.sendFeedback(Text.of("§8 - §a/xqbk start §8- §7" + t("help_start")));
        source.sendFeedback(Text.of("§8 - §a/xqbk stop §8- §7" + t("help_stop")));
        source.sendFeedback(Text.of("§8 - §a/xqbk config delay <1-20> §8- §7" + t("help_speed")));
        source.sendFeedback(Text.of("§8 - §a/xqbk config repaircmd <cmd> §8- §7" + t("command")));
        source.sendFeedback(Text.of("§8 - §a/xqbk config repairmode <off/command/craft>"));
        source.sendFeedback(Text.of("§8 - §a/xqbk config autoeat <on/off>"));
        source.sendFeedback(Text.of("§8 - §a/xqbk config storage <on/off> [cycles]"));
        if (language.equals("pl")) {
            source.sendFeedback(Text.of("§8 - §a/xqbk config pickupcooldown <cykle> <sekundy>"));
        } else {
            source.sendFeedback(Text.of("§8 - §a/xqbk config pickupcooldown <cycles> <seconds>"));
        }
        source.sendFeedback(Text.of("§8 - §a/xqbk pl §8| §a/xqbk en §8- §7" + t("msg_lang")));
        source.sendFeedback(Text.of(""));
        // Clickable links on one line: GitHub | Discord
        source.sendFeedback(
                Text.literal("\uD83D\uDCBB GitHub")
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.WHITE)
                                .withUnderline(true)
                                .withClickEvent(
                                        new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/xqbkubiak")))
                        .append(Text.literal("  §8|  ").setStyle(Style.EMPTY))
                        .append(Text.literal("\uD83D\uDCAC Discord")
                                .setStyle(Style.EMPTY
                                        .withColor(Formatting.WHITE)
                                        .withUnderline(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                                "https://discord.com/invite/getnotify")))));
        source.sendFeedback(Text.of("§8§m                                                  "));
    }

    private void performAction(MinecraftClient client) {
        if (client.interactionManager == null || client.player == null || client.world == null)
            return;

        // Durability Check - based on repairMode
        // Durability Check - based on repairMode
        if (state == 0 && repairMode > 0) {
            ItemStack stack = client.player.getMainHandStack();
            boolean needsAction = stack.isEmpty();

            // Command Mode: also repair if >50% damaged
            if (!needsAction && repairMode == 1) {
                if (stack.isDamageable()) {
                    if (stack.getDamage() > stack.getMaxDamage() / 2) {
                        needsAction = true;
                    }
                }
            }

            if (needsAction) {
                if (repairMode == 2) {
                    // Craft mode - only if empty
                    if (stack.isEmpty())
                        performCrafting(client);
                } else if (repairMode == 1) {
                    // Command mode - with cooldown
                    if (repairCooldown == 0) {
                        client.player.networkHandler.sendChatCommand(repairCommand);
                        repairCooldown = 100; // 5 seconds cooldown
                    }
                }
            }
        }

        net.minecraft.util.hit.HitResult hit = client.crosshairTarget;

        switch (state) {
            case 0: // Select Slot (Rotation)
                // If autoEat ON: slots 3-9 (index 2-8), skip slot 2 (food)
                // If autoEat OFF: slots 2-9 (index 1-8)
                int minSlot = autoEat ? 2 : 1;
                slotIndex++;
                if (slotIndex > 8 || slotIndex < minSlot)
                    slotIndex = minSlot;
                client.player.getInventory().selectedSlot = slotIndex;
                state = 1;
                break;

            case 1: // Right Click (Place/Interact)
                if (hit != null && hit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                    net.minecraft.util.hit.BlockHitResult blockHit = (net.minecraft.util.hit.BlockHitResult) hit;
                    client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
                    client.player.swingHand(Hand.MAIN_HAND);
                } else {
                    client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                }
                state = 2;
                break;

            case 2: // Select Slot 1 (Pickaxe/Tool)
                client.player.getInventory().selectedSlot = 0;
                state = 3;
                break;

            case 3: // Left Click (Break)
                if (hit != null && hit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                    net.minecraft.util.hit.BlockHitResult blockHit = (net.minecraft.util.hit.BlockHitResult) hit;
                    client.interactionManager.attackBlock(blockHit.getBlockPos(), blockHit.getSide());
                    client.player.swingHand(Hand.MAIN_HAND);
                }
                state = 0;

                // Count Cycles
                fullCycles++;
                dropCycles++;
                totalCycles++;

                // Show cycle count on Action Bar
                client.player.sendMessage(Text.of("§8[§2xQBK§8] §7" + t("cycles") + ": §a" + totalCycles), true);

                // Refill Hotbar with leaves every 10 cycles
                if (fullCycles % 10 == 0) {
                    refillHotbar(client);
                }

                // Auto-Dump Apples (Storage Mode)
                if (storageMode && dropCycles >= storageCyclesLimit) {
                    dropCycles = 0;
                    isDumping = true;
                    dumpState = 0;
                    dumpTimer = 0;
                    // Reset fullCycles for consistent ad message timing? No, keep ad independent.
                } else if (!storageMode) {
                    dropCycles = 0; // Reset counter if mode disabled
                }

                // Ad Message every 500 cycles
                if (fullCycles >= 500) {
                    fullCycles = 0;
                    sendAdMessage(client);
                }

                // Pickup Pause System (if enabled)
                if (pickupCycles > 0 && pickupWaitSeconds > 0) {
                    pickupCounter++;
                    if (pickupCounter >= pickupCycles) {
                        pickupCounter = 0;
                        isPickupPause = true;
                        pickupWaitTicks = pickupWaitSeconds * 20; // Convert seconds to ticks
                        performPickup(client);
                        client.player.sendMessage(Text
                                .of("§8[§2xQBK§8] §e" + (language.equals("pl") ? "Pauza " + pickupWaitSeconds + "s..."
                                        : "Pause " + pickupWaitSeconds + "s...")),
                                true);
                    }
                }
                break;
        }
    }

    // Dumping Sequence Logic
    private void performDump(MinecraftClient client) {
        dumpTimer++;
        if (dumpTimer < 5)
            return; // Wait a bit between steps

        // Safety check to ensure we don't get stuck if screen is closed unexpectedly
        if (dumpState > 1 && client.currentScreen == null) {
            dumpState = 3; // Force finish if screen closed
        }

        switch (dumpState) {
            case 0: // Rotate Right (90 degrees) to face chest
                client.player.setYaw(client.player.getYaw() + 90);
                dumpTimer = 0;
                dumpState = 1;
                break;

            case 1: // Open Chest (Right Click)
                net.minecraft.util.hit.HitResult hit = client.crosshairTarget;
                if (hit != null && hit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                    net.minecraft.util.hit.BlockHitResult blockHit = (net.minecraft.util.hit.BlockHitResult) hit;
                    client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
                dumpTimer = 0;
                dumpState = 2; // Wait for screen to open
                break;

            case 2: // Move Apples
                if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.GenericContainerScreen) {
                    net.minecraft.client.gui.screen.ingame.GenericContainerScreen chestScreen = (net.minecraft.client.gui.screen.ingame.GenericContainerScreen) client.currentScreen;

                    boolean moved = false;
                    // Lower inventory slots (usually 27-62 for single chest + player inv, or
                    // similar offset)
                    // We iterate player inventory slots from ScreenHandler
                    // Player inventory is typically the last 36 slots
                    int totalSlots = chestScreen.getScreenHandler().slots.size();
                    int playerStart = totalSlots - 36;

                    for (int i = playerStart; i < totalSlots; i++) {
                        ItemStack stack = chestScreen.getScreenHandler().slots.get(i).getStack();
                        if (stack.getItem() == Items.APPLE && !stack.isEmpty()) {
                            // Shift Click to move to chest
                            client.interactionManager.clickSlot(chestScreen.getScreenHandler().syncId, i, 0,
                                    SlotActionType.QUICK_MOVE, client.player);
                            moved = true;
                        }
                    }

                    if (!moved) {
                        // No more apples or finished moving
                        client.player.closeHandledScreen();
                        dumpTimer = 0;
                        dumpState = 3;
                    } else {
                        // Reset timer to allow moving next batch or slight delay
                        dumpTimer = 3;
                    }
                } else {
                    // Waiting for screen to open...
                    if (dumpTimer > 20) { // Timeout
                        dumpState = 3;
                    }
                }
                break;

            case 3: // Rotate Back (Left 90 degrees)
                client.player.setYaw(client.player.getYaw() - 90);
                isDumping = false; // Finished
                dumpTimer = 0;
                break;
        }
    }

    private void sendAdMessage(MinecraftClient client) {
        int exclamations = random.nextInt(3) + 1;
        String suffix = "!".repeat(exclamations);
        String msg = t("msg_ad") + " " + suffix;
        client.player.networkHandler.sendChatMessage(msg);
    }

    private void performPickup(MinecraftClient client) {
        if (client.player == null)
            return;

        // Simulate pressing pickup key (default F - swap item to offhand triggers
        // pickup)
        client.options.swapHandsKey.setPressed(true);
        // Release after 1 tick
        MinecraftClient.getInstance().send(() -> {
            client.options.swapHandsKey.setPressed(false);
        });

        client.player.sendMessage(
                Text.of("§8[§2xQBK§8] §e" + (language.equals("pl") ? "Podnoszę przedmioty!" : "Picking up items!")),
                true);
    }

    private void tryToEat(MinecraftClient client) {
        if (client.player == null)
            return;

        // Find food in hotbar (slots 0-8)
        int foodSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (!stack.isEmpty() && isFood(stack)) {
                foodSlot = i;
                break;
            }
        }

        if (foodSlot != -1) {
            // Save current slot and switch to food
            previousSlot = client.player.getInventory().selectedSlot;
            client.player.getInventory().selectedSlot = foodSlot;

            // Start eating
            isEating = true;
            eatingTicks = 0;
            client.options.useKey.setPressed(true);

            client.player.sendMessage(Text.of("§8[§2xQBK§8] §e" + t("msg_eating")), true);
        }
    }

    private boolean isFood(ItemStack stack) {
        // Check if item has food component (MC 1.21+ way)
        FoodComponent food = stack.get(DataComponentTypes.FOOD);
        return food != null;
    }

    private boolean isLeaves(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        // Check if item ID contains "leaves" - works for all leaf types
        String itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
        return itemId.contains("leaves");
    }

    private void refillHotbar(MinecraftClient client) {
        if (client.player == null)
            return;

        // Check slots 3-9 (index 2-8) for empty slots
        for (int hotbarIndex = 2; hotbarIndex <= 8; hotbarIndex++) {
            ItemStack hotbarStack = client.player.getInventory().getStack(hotbarIndex);

            // If slot is empty or has few items, try to refill
            if (hotbarStack.isEmpty()) {
                // Find leaves in main inventory (slots 9-35 in inventory = slots 9-35 in screen
                // handler)
                for (int invSlot = 9; invSlot < 36; invSlot++) {
                    ItemStack invStack = client.player.getInventory().getStack(invSlot);
                    if (isLeaves(invStack)) {
                        // Move leaves to hotbar slot
                        int syncId = client.player.playerScreenHandler.syncId;
                        // Screen handler slots: 9-35 = main inventory, 36-44 = hotbar
                        int screenInvSlot = invSlot; // Inventory slot in screen handler
                        int screenHotbarSlot = 36 + hotbarIndex; // Hotbar slot in screen handler

                        try {
                            // Pick up from inventory
                            client.interactionManager.clickSlot(syncId, screenInvSlot, 0, SlotActionType.PICKUP,
                                    client.player);
                            // Put in hotbar
                            client.interactionManager.clickSlot(syncId, screenHotbarSlot, 0, SlotActionType.PICKUP,
                                    client.player);

                            client.player.sendMessage(Text.of(
                                    "§8[§2xQBK§8] §a" + (language.equals("pl") ? "Uzupelnilem slot " : "Refilled slot ")
                                            + (hotbarIndex + 1)),
                                    true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break; // Found leaves for this slot, move to next hotbar slot
                    }
                }
            }
        }
    }

    private void performCrafting(MinecraftClient client) {
        if (client.player == null)
            return;

        // Find iron ingot in inventory (slots 9-44 in screen handler)
        int ironSlot = -1;
        for (int i = 9; i < 45; i++) {
            if (client.player.playerScreenHandler.getSlot(i).getStack().getItem() == Items.IRON_INGOT) {
                ironSlot = i;
                break;
            }
        }

        if (ironSlot != -1) {
            int syncId = client.player.playerScreenHandler.syncId;
            try {
                // Craft shears: put iron in slots 1 and 4 of crafting grid
                client.interactionManager.clickSlot(syncId, ironSlot, 0, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(syncId, 1, 1, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(syncId, 4, 1, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(syncId, ironSlot, 0, SlotActionType.PICKUP, client.player);

                // Take shears from result slot (0)
                client.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, client.player);

                // Put shears in hotbar slot 1 (screen handler slot 36)
                // If slot 36 has something, swap it
                client.interactionManager.clickSlot(syncId, 36, 0, SlotActionType.PICKUP, client.player);

                // If we picked up something from slot 36, put it back somewhere
                if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    // Put old item in first empty slot
                    client.interactionManager.clickSlot(syncId, ironSlot, 0, SlotActionType.PICKUP, client.player);
                }

                client.player.sendMessage(Text.of("§8[§2xQBK§8] §a" + t("msg_shears")), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            client.player.sendMessage(Text.of("§8[§2xQBK§8] §c" + t("msg_no_iron")), true);
        }
    }
}
