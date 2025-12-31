package com.bkubiak.applebot;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import java.util.Random;

// Abstract base class for the client mod
public abstract class AppleBotClientBase implements ClientModInitializer {
    protected static final String MOD_PREFIX = "§8[§2BK-§aApple§8]";

    protected abstract boolean isFood(ItemStack stack);

    protected abstract void openScreen();

    protected abstract void executeOnMainThread(Runnable runnable);

    protected abstract ClickEvent createClickEvent(ClickEvent.Action action, String value);

    protected abstract int getSelectedSlot(net.minecraft.entity.player.PlayerEntity player);

    protected abstract void setInventorySlot(net.minecraft.entity.player.PlayerEntity player, int slot);

    protected boolean isRunning = false;
    private boolean pendingOpenScreen = false;

    // Config
    protected int tickDelay = 2;
    protected int repairMode = 1;
    protected boolean autoEat = true;
    protected int hungerThreshold = 14;
    protected String repairCommand = "repair";
    protected String language = "pl"; // pl or en

    protected int tickCounter = 0;
    protected int fullCycles = 0;
    protected int dropCycles = 0; // Counter for apple drop
    protected int totalCycles = 0; // Global counter for display
    protected int repairCooldown = 0; // Cooldown for repair cmd

    protected int state = 0;
    protected int slotIndex = 0;
    protected int previousSlot = 0;
    protected boolean isEating = false;
    protected int eatingTicks = 0;

    // Dumping State
    protected boolean isDumping = false;
    protected int dumpState = 0;
    protected int dumpTimer = 0;

    // Storage Config
    protected boolean storageMode = false;
    protected int storageCyclesLimit = 1000;

    // Crafting State
    protected boolean isCraftingMode = false;
    protected int craftingStage = 0;
    protected int ironSlotForCrafting = -1;

    // Moving Tool State
    protected boolean isMovingToolMode = false;
    protected int movingToolStage = 0;
    protected int toolSourceSlot = -1;

    // Pickup Config
    protected int pickupCycles = 0; // 0 = disabled, pause after X cycles
    protected int pickupWaitSeconds = 0; // How many seconds to wait
    protected int pickupCounter = 0;
    protected boolean isPickupPause = false;
    protected int pickupWaitTicks = 0;

    // Restock State
    protected boolean restockMode = false;
    protected boolean isRestocking = false;
    protected int restockState = 0;
    protected int restockTimer = 0;

    protected boolean shouldShowWelcome = false;

    protected final Random random = new Random();

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

    public boolean isRestockMode() {
        return restockMode;
    }

    public void setRestockMode(boolean v) {
        restockMode = v;
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
            case "restock" -> language.equals("pl") ? "Uzupelnianie" : "Restock";
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
            case "msg_restock" -> language.equals("pl") ? "Uzupelniam zapasy..." : "Restocking supplies...";
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

    protected void performCrafting(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null)
            return;

        if (isCraftingMode)
            return;

        // Ensure we are in PlayerScreenHandler
        if (!(client.player.currentScreenHandler instanceof net.minecraft.screen.PlayerScreenHandler)) {
            // Close any open chest/screen to allow crafting in 2x2
            executeOnMainThread(() -> client.player.closeHandledScreen());
            return;
        }

        // 1. Check if we already have shears ANYWHERE
        // First check the target slot itself (Slot 1 / Screen Slot 36)
        if (client.player.playerScreenHandler.getSlot(36).getStack().getItem() == Items.SHEARS) {
            return;
        }

        int existingToolSlot = -1;
        // Check slots 0-44 (full screen handler)
        for (int i = 0; i < 45; i++) {
            if (i == 36)
                continue; // Already checked
            if (client.player.playerScreenHandler.getSlot(i).getStack().getItem() == Items.SHEARS) {
                existingToolSlot = i;
                break;
            }
        }

        // Check cursor too
        if (client.player.currentScreenHandler.getCursorStack().getItem() == Items.SHEARS) {
            existingToolSlot = 999; // Sentinel for cursor
        }

        if (existingToolSlot != -1) {
            // Found existing shears! Move them to slot 1 instead of crafting.
            this.toolSourceSlot = existingToolSlot;
            this.isMovingToolMode = true;
            this.movingToolStage = 0;
            return;
        }

        // 2. Iron Search
        int slot = -1;
        // Check cursor
        ItemStack cursor = client.player.currentScreenHandler.getCursorStack();
        if (cursor.getItem() == Items.IRON_INGOT && cursor.getCount() >= 2) {
            slot = 999; // Sentinel for cursor
        } else {
            // Check inventory
            for (int i = 0; i < 36; i++) {
                ItemStack s = client.player.getInventory().getStack(i);
                if (s.getItem() == Items.IRON_INGOT && s.getCount() >= 2) {
                    slot = i;
                    break;
                }
            }
        }

        if (slot == -1) {
            client.player.sendMessage(Text.of(MOD_PREFIX + " §c" + t("msg_no_iron")), true);
            return;
        }

        // Target slot for leftovers - MUST NOT BE 36 (Hotbar 1)
        this.ironSlotForCrafting = -1;
        for (int i = 9; i < 36; i++) { // Check main inventory first
            if (client.player.getInventory().getStack(i).isEmpty()) {
                this.ironSlotForCrafting = i;
                break;
            }
        }
        if (this.ironSlotForCrafting == -1) {
            // No empty inventory slot, find any slot that isn't 0 (hotbar 1)
            for (int i = 1; i < 9; i++) {
                if (client.player.getInventory().getStack(i).isEmpty()) {
                    this.ironSlotForCrafting = 36 + i;
                    break;
                }
            }
        }
        if (this.ironSlotForCrafting == -1)
            this.ironSlotForCrafting = 18; // Fallback to middle of inventory

        this.isCraftingMode = true;
        this.craftingStage = (slot == 999) ? 1 : 0;
        if (slot != 999) {
            this.ironSlotForCrafting = (slot < 9) ? (36 + slot) : slot;
            // If the iron stack is in slot 36, we'll pick it up in Stage 0
            // but we must ensure we put leftovers ELSEWHERE in Stage 3.
            if (this.ironSlotForCrafting == 36) {
                // Find a different slot for leftovers
                for (int i = 9; i < 36; i++) {
                    if (client.player.getInventory().getStack(i).isEmpty()) {
                        // we'll use this later
                        break;
                    }
                }
            }
        }
    }

    protected void doCraftingStep(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            isCraftingMode = false;
            return;
        }

        if (!(client.player.currentScreenHandler instanceof net.minecraft.screen.PlayerScreenHandler)) {
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;
        ItemStack cursor = client.player.currentScreenHandler.getCursorStack();

        try {
            switch (craftingStage) {
                case 0: // Pick up Iron Stack
                    if (cursor.getItem() == Items.IRON_INGOT) {
                        craftingStage = 1;
                        return;
                    }
                    client.interactionManager.clickSlot(syncId, ironSlotForCrafting, 0, SlotActionType.PICKUP,
                            client.player);
                    break;

                case 1: // Place 1 in Grid 1 (top-left)
                    if (cursor.isEmpty() || cursor.getItem() != Items.IRON_INGOT) {
                        craftingStage = 0;
                        return;
                    }
                    client.interactionManager.clickSlot(syncId, 1, 1, SlotActionType.PICKUP, client.player);
                    break;

                case 2: // Place 1 in Grid 4 (bottom-right)
                    if (cursor.isEmpty() || cursor.getItem() != Items.IRON_INGOT) {
                        craftingStage = 4;
                        return;
                    }
                    client.interactionManager.clickSlot(syncId, 4, 1, SlotActionType.PICKUP, client.player);
                    break;

                case 3: // Put back leftover Iron Stack
                    if (cursor.isEmpty()) {
                        craftingStage = 4;
                        return;
                    }
                    // CRITICAL: If ironSlotForCrafting is 36, find some other slot
                    int target = (ironSlotForCrafting == 36) ? findSafeReturnSlot(client) : ironSlotForCrafting;
                    client.interactionManager.clickSlot(syncId, target, 0, SlotActionType.PICKUP, client.player);
                    break;

                case 4: // Wait for Result and Pick up Shears
                    if (!cursor.isEmpty()) {
                        // Put leftovers away if still holding them
                        int safe = findSafeReturnSlot(client);
                        client.interactionManager.clickSlot(syncId, safe, 0, SlotActionType.PICKUP, client.player);
                        return;
                    }
                    ItemStack result = client.player.currentScreenHandler.getSlot(0).getStack();
                    if (result.getItem() == Items.SHEARS) {
                        client.interactionManager.clickSlot(syncId, 0, 0, SlotActionType.PICKUP, client.player);
                    } else {
                        // Retry grid if it failed? No, just abort and let it retry next cycle
                        isCraftingMode = false;
                        return;
                    }
                    break;

                case 5: // Place Shears in Hotbar Slot 1 (36)
                    if (cursor.getItem() != Items.SHEARS) {
                        // Abort if lost
                        isCraftingMode = false;
                        return;
                    }
                    // Place new shears in 36. Swaps with whatever was there.
                    client.interactionManager.clickSlot(syncId, 36, 0, SlotActionType.PICKUP, client.player);
                    break;

                case 6: // Final Cleanup
                    if (!cursor.isEmpty()) {
                        // If holding old broken tool or swapped items, put them in a safe place
                        int safeSlot = findSafeReturnSlot(client);
                        client.interactionManager.clickSlot(syncId, safeSlot, 0, SlotActionType.PICKUP, client.player);
                    }
                    client.player.sendMessage(Text.of(MOD_PREFIX + " §a" + t("msg_shears")), true);
                    isCraftingMode = false;
                    return;
            }
            craftingStage++;
        } catch (Exception e) {
            e.printStackTrace();
            isCraftingMode = false;
        }
    }

    protected void doMovingToolStep(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) {
            isMovingToolMode = false;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;
        ItemStack cursor = client.player.currentScreenHandler.getCursorStack();

        try {
            switch (movingToolStage) {
                case 0: // Pick up Tool
                    if (cursor.getItem() == Items.SHEARS) {
                        movingToolStage = 1;
                        return;
                    }
                    if (toolSourceSlot == 999) { // Already on cursor
                        movingToolStage = 1;
                        return;
                    }
                    client.interactionManager.clickSlot(syncId, toolSourceSlot, 0, SlotActionType.PICKUP,
                            client.player);
                    break;

                case 1: // Place in Slot 1 (36)
                    if (cursor.getItem() != Items.SHEARS) {
                        isMovingToolMode = false;
                        return;
                    }
                    client.interactionManager.clickSlot(syncId, 36, 0, SlotActionType.PICKUP, client.player);
                    break;

                case 2: // Put away whatever was swapped
                    if (cursor.isEmpty()) {
                        isMovingToolMode = false;
                        return;
                    }
                    int safe = findSafeReturnSlot(client);
                    client.interactionManager.clickSlot(syncId, safe, 0, SlotActionType.PICKUP, client.player);
                    isMovingToolMode = false;
                    return;
            }
            movingToolStage++;
        } catch (Exception e) {
            e.printStackTrace();
            isMovingToolMode = false;
        }
    }

    private int findSafeReturnSlot(MinecraftClient client) {
        // Find empty player inventory slots (9-35) or non-hotbar-1 hotbar (37-44)
        for (int i = 9; i < 36; i++) {
            if (client.player.getInventory().getStack(i).isEmpty())
                return i;
        }
        for (int i = 1; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isEmpty())
                return 36 + i;
        }
        return 9; // Fallback to a mid-inventory slot even if not empty
    }

    protected void saveConfig() {
        // We'll need to cast 'this' to AppleBotClient for the ConfigManager, or update
        // ConfigManager to accept Base
        // For simplicity, let's assume we update ConfigManager to take Base or specific
        // fields
        ConfigManager.save(ConfigManager.createFromBot(this));
    }

    protected void loadConfig() {
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
        AppleBot.LOGGER.error("[AppleBot] Client Init Start");
        loadConfig();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("bka")
                    .executes(context -> {
                        sendHelp(context.getSource());
                        return 1;
                    })
                    .then(ClientCommandManager.literal("gui")
                            .executes(context -> {
                                try {
                                    pendingOpenScreen = true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("start")
                            .executes(context -> {
                                isRunning = true;
                                saveConfig();
                                context.getSource().sendFeedback(Text.of(MOD_PREFIX + " §a" + t("msg_started")));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("stop")
                            .executes(context -> {
                                isRunning = false;
                                saveConfig();
                                context.getSource().sendFeedback(Text.of(MOD_PREFIX + " §c" + t("msg_stopped")));
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
                                                        Text.of(MOD_PREFIX + " §7Delay: §a" + ticks + "t"));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("repairmode")
                                    .then(ClientCommandManager.literal("off")
                                            .executes(context -> {
                                                repairMode = 0;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of(MOD_PREFIX + " §7Repair Mode: §cOFF"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("command")
                                            .executes(context -> {
                                                repairMode = 1;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(
                                                                Text.of(MOD_PREFIX + " §7Repair Mode: §aCOMMAND"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("craft")
                                            .executes(context -> {
                                                repairMode = 2;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of(MOD_PREFIX + " §7Repair Mode: §aCRAFT"));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("repaircmd")
                                    .then(ClientCommandManager.argument("command", StringArgumentType.word())
                                            .executes(context -> {
                                                String cmd = StringArgumentType.getString(context, "command");
                                                repairCommand = cmd;
                                                saveConfig();
                                                context.getSource().sendFeedback(
                                                        Text.of(MOD_PREFIX + " §7" + t("command") + ": §a/" + cmd));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("autoeat")
                                    .then(ClientCommandManager.literal("on")
                                            .executes(context -> {
                                                autoEat = true;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of(MOD_PREFIX + " §aAuto-Eat: ON"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("off")
                                            .executes(context -> {
                                                autoEat = false;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of(MOD_PREFIX + " §cAuto-Eat: OFF"));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("restock")
                                    .then(ClientCommandManager.literal("on")
                                            .executes(context -> {
                                                setRestockMode(true);
                                                context.getSource().sendFeedback(Text.of(MOD_PREFIX + " §aRestock ON"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("off")
                                            .executes(context -> {
                                                setRestockMode(false);
                                                context.getSource()
                                                        .sendFeedback(Text.of(MOD_PREFIX + " §cRestock OFF"));
                                                return 1;
                                            })))
                            .then(ClientCommandManager.literal("storage")
                                    .then(ClientCommandManager.literal("off")
                                            .executes(context -> {
                                                storageMode = false;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of(MOD_PREFIX + " §cStorage: OFF"));
                                                return 1;
                                            }))
                                    .then(ClientCommandManager.literal("on")
                                            .executes(context -> {
                                                storageMode = true;
                                                saveConfig();
                                                context.getSource()
                                                        .sendFeedback(Text.of(MOD_PREFIX + " §aStorage: ON (Default: "
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
                                                                Text.of(MOD_PREFIX + " §aStorage: ON (Cycles: "
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
                                                        .sendFeedback(Text.of(MOD_PREFIX + " §cPickup: §cOFF"));
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
                                                                    Text.of(MOD_PREFIX + " §cPickup: §cOFF"));
                                                        } else {
                                                            context.getSource().sendFeedback(
                                                                    Text.of(MOD_PREFIX + " §aPickup: Every §e" + cycles
                                                                            + " §acycles, wait §e" + seconds + "s"));
                                                        }
                                                        return 1;
                                                    })))))
                    .then(ClientCommandManager.literal("pl")
                            .executes(context -> {
                                language = "pl";
                                saveConfig();
                                context.getSource().sendFeedback(Text.of(MOD_PREFIX + " §aJezyk: Polski"));
                                return 1;
                            }))
                    .then(ClientCommandManager.literal("en")
                            .executes(context -> {
                                language = "en";
                                saveConfig();
                                context.getSource().sendFeedback(Text.of(MOD_PREFIX + " §aLanguage: English"));
                                return 1;
                            }))); // End dispatcher.register
        }); // End ClientCommandRegistrationCallback.EVENT.register

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            shouldShowWelcome = true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (pendingOpenScreen) {
                pendingOpenScreen = false;
                openScreen();
            }

            // WELCOME MESSAGE
            if (shouldShowWelcome && client.player != null) {
                shouldShowWelcome = false;
                if (language.equals("pl")) {
                    client.player.sendMessage(Text.of("§8§m                                                  "), false);
                    client.player.sendMessage(
                            Text.of(MOD_PREFIX + " §fBKA Apple Bot został uruchomiony §apomyślnie§f!"),
                            false);
                    client.player.sendMessage(Text.literal("§8  » §7Discord: ")
                            .append(Text.literal("§adc.bkubiak.dev")
                                    .setStyle(Style.EMPTY.withClickEvent(
                                            createClickEvent(ClickEvent.Action.OPEN_URL, "https://dc.bkubiak.dev")))),
                            false);
                    client.player.sendMessage(Text.of("§8  » §7Komendy: §a/bka"), false);
                    client.player.sendMessage(Text.of("§8§m                                                  "), false);
                } else {
                    client.player.sendMessage(Text.of("§8§m                                                  "), false);
                    client.player.sendMessage(Text.of(MOD_PREFIX + " §fBKA Apple Bot started §asuccessfully§f!"),
                            false);
                    client.player.sendMessage(Text.literal("§8  » §7Discord: ")
                            .append(Text.literal("§adc.bkubiak.dev")
                                    .setStyle(Style.EMPTY.withClickEvent(
                                            createClickEvent(ClickEvent.Action.OPEN_URL, "https://dc.bkubiak.dev")))),
                            false);
                    client.player.sendMessage(Text.of("§8  » §7Commands: §a/bka"), false);
                    client.player.sendMessage(Text.of("§8§m                                                  "), false);
                }
            }

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
                        setInventorySlot(client.player, previousSlot);
                        isEating = false;
                        eatingTicks = 0;
                        client.player.sendMessage(Text.of(MOD_PREFIX + " §a" + t("msg_ate")), true);
                    }
                    return; // Don't do other actions while eating
                }

                // AUTO-RESTOCK PRIORITY
                if (isRestocking) {
                    performRestock(client);
                    return; // Don't mine while restocking
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
                                        Text.of(MOD_PREFIX + " §a"
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
        source.sendFeedback(Text.of(MOD_PREFIX + " §7BKA Bot §av2.6"));
        source.sendFeedback(Text.of(""));
        source.sendFeedback(Text.of("§8 - §a/bka gui §8- §7" + t("help_gui")));
        source.sendFeedback(Text.of("§8 - §a/bka start §8- §7" + t("help_start")));
        source.sendFeedback(Text.of("§8 - §a/bka stop §8- §7" + t("help_stop")));
        source.sendFeedback(Text.of("§8 - §a/bka config delay <1-20> §8- §7" + t("help_speed")));
        source.sendFeedback(Text.of("§8 - §a/bka config repaircmd <cmd> §8- §7" + t("command")));
        source.sendFeedback(Text.of("§8 - §a/bka config repairmode <off/command/craft>"));
        source.sendFeedback(Text.of("§8 - §a/bka config autoeat <on/off>"));
        source.sendFeedback(Text.of("§8 - §a/bka config storage <on/off> [cycles]"));
        if (language.equals("pl")) {
            source.sendFeedback(Text.of("§8 - §a/bka config pickupcooldown <cykle> <sekundy>"));
        } else {
            source.sendFeedback(Text.of("§8 - §a/bka config pickupcooldown <cycles> <seconds>"));
        }
        source.sendFeedback(Text.of("§8 - §a/bka pl §8| §a/bka en §8- §7" + t("msg_lang")));
        source.sendFeedback(Text.of(""));
        // Clickable links on one line: GitHub | Discord
        source.sendFeedback(
                Text.literal("\uD83D\uDCBB GitHub")
                        .setStyle(Style.EMPTY
                                .withColor(Formatting.WHITE)
                                .withUnderline(true)
                                .withClickEvent(
                                        createClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/xqbkubiak")))
                        .append(Text.literal("  §8|  ").setStyle(Style.EMPTY))
                        .append(Text.literal("\uD83D\uDCAC Discord")
                                .setStyle(Style.EMPTY
                                        .withColor(Formatting.AQUA)
                                        .withUnderline(true)
                                        .withClickEvent(createClickEvent(ClickEvent.Action.OPEN_URL,
                                                "https://dc.bkubiak.dev")))));
        source.sendFeedback(Text.of("§8§m                                                  "));
    }

    private void performAction(MinecraftClient client) {
        if (client.interactionManager == null || client.player == null || client.world == null)
            return;

        if (isMovingToolMode) {
            doMovingToolStep(client);
            return;
        }

        if (isCraftingMode) {
            doCraftingStep(client);
            return;
        }

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
                setInventorySlot(client.player, slotIndex);
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
                setInventorySlot(client.player, 0);
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
                client.player.sendMessage(Text.of(MOD_PREFIX + " §7" + t("cycles") + ": §a" + totalCycles), true);

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

                // Auto-Restock (Leaves/Iron)
                if (restockMode && fullCycles >= 500) {
                    // Check if we actually need anything
                    int leafStacks = countLeafStacks(client);
                    int ironCount = countIron(client);

                    if (leafStacks < 9 || ironCount < 64) {
                        isRestocking = true;
                        restockState = 0;
                        restockTimer = 0;
                        client.player.sendMessage(Text.of(MOD_PREFIX + " §e" + t("msg_restock") + " §7(Leaves: "
                                + leafStacks + "/9, Iron: " + ironCount + "/64)"), true);
                    } else {
                        client.player.sendMessage(Text
                                .of(MOD_PREFIX + " §7Restock: Supplies OK (L:" + leafStacks + ", I:" + ironCount + ")"),
                                true);
                    }
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
                        client.player.sendMessage(Text
                                .of(MOD_PREFIX + " §e" + (language.equals("pl") ? "Pauza " + pickupWaitSeconds + "s..."
                                        : "Pause " + pickupWaitSeconds + "s...")),
                                true);
                    }
                }
                break;
        }
    }

    private void performRestock(MinecraftClient client) {
        restockTimer++;
        if (restockTimer < 5)
            return; // Wait a bit between steps

        // Safety check
        if (restockState > 1 && client.currentScreen == null) {
            restockState = 3; // Force finish if screen closed
        }

        switch (restockState) {
            case 0: // Rotate Right (90 degrees) to face chest
                client.player.setYaw(client.player.getYaw() + 90);
                restockTimer = 0;
                restockState = 1;
                break;

            case 1: // Open Chest (Right Click)
                net.minecraft.util.hit.HitResult hit = client.crosshairTarget;
                if (hit != null && hit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                    net.minecraft.util.hit.BlockHitResult blockHit = (net.minecraft.util.hit.BlockHitResult) hit;
                    client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
                restockTimer = 0;
                restockState = 2; // Wait for screen to open
                break;

            case 2: // Withdraw items
                if (client.currentScreen instanceof net.minecraft.client.gui.screen.ingame.GenericContainerScreen) {
                    net.minecraft.client.gui.screen.ingame.GenericContainerScreen chestScreen = (net.minecraft.client.gui.screen.ingame.GenericContainerScreen) client.currentScreen;

                    int leafStacks = countLeafStacks(client);
                    int ironCount = countIron(client);

                    boolean needLeaves = leafStacks < 9;
                    boolean needIron = ironCount < 64;

                    if (!needLeaves && !needIron) {
                        client.player.closeHandledScreen();
                        restockTimer = 0;
                        restockState = 3;
                        return;
                    }

                    boolean moved = false;
                    // GenericContainerScreen slots: first X are chest, then player inventory (36)
                    int containerSize = chestScreen.getScreenHandler().slots.size() - 36;

                    for (int i = 0; i < containerSize; i++) {
                        ItemStack stack = chestScreen.getScreenHandler().slots.get(i).getStack();
                        if (stack.isEmpty())
                            continue;

                        if (needLeaves && isLeaves(stack)) {
                            // Quick Move (Shift + Click) to take stack
                            client.interactionManager.clickSlot(chestScreen.getScreenHandler().syncId, i, 0,
                                    SlotActionType.QUICK_MOVE, client.player);
                            moved = true;
                            break; // Take one stack at a time
                        }

                        if (needIron && stack.getItem() == Items.IRON_INGOT) {
                            client.interactionManager.clickSlot(chestScreen.getScreenHandler().syncId, i, 0,
                                    SlotActionType.QUICK_MOVE, client.player);
                            moved = true;
                            break;
                        }
                    }

                    if (!moved) {
                        // Nothing more to take or chest empty
                        client.player.closeHandledScreen();
                        restockTimer = 0;
                        restockState = 3;
                    } else {
                        // Reset timer for next withdrawal step
                        restockTimer = 2;
                    }
                } else {
                    // Waiting for screen to open...
                    if (restockTimer > 20) { // Timeout
                        restockState = 3;
                    }
                }
                break;

            case 3: // Rotate Back (Left 90 degrees)
                client.player.setYaw(client.player.getYaw() - 90);
                isRestocking = false; // Finished
                restockTimer = 0;
                break;
        }
    }

    // Dumping Sequence Logic
    private int countLeafStacks(MinecraftClient client) {
        if (client.player == null)
            return 0;
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (isLeaves(stack)) {
                count++;
            }
        }
        return count;
    }

    private int countIron(MinecraftClient client) {
        if (client.player == null)
            return 0;
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.getItem() == Items.IRON_INGOT) {
                count += stack.getCount();
            }
        }
        return count;
    }

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
            previousSlot = getSelectedSlot(client.player);
            setInventorySlot(client.player, foodSlot);

            // Start eating
            isEating = true;
            eatingTicks = 0;
            client.options.useKey.setPressed(true);

            client.player.sendMessage(Text.of(MOD_PREFIX + " §e" + t("msg_eating")), true);
        }
    }

    // Abstract helper
    // protected abstract boolean isFood(ItemStack stack);

    private boolean isLeaves(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        // Check if item ID contains "leaves" - works for all leaf types
        String itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString();
        return itemId.contains("leaves");
    }

    private void refillHotbar(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null)
            return;

        int minSlot = autoEat ? 2 : 1; // Index 2 (Slot 3) or Index 1 (Slot 2)
        int syncId = client.player.playerScreenHandler.syncId;

        // Iterate through rotation slots (2-9 or 3-9)
        for (int i = minSlot; i <= 8; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                // Find leaves in inventory (9-35)
                for (int j = 9; j < 36; j++) {
                    ItemStack invStack = client.player.getInventory().getStack(j);
                    if (isLeaves(invStack)) {
                        try {
                            // Pick up from inventory
                            client.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, client.player);
                            // Put in hotbar (Slot IDs 36-44 correspond to hotbar indices 0-8)
                            client.interactionManager.clickSlot(syncId, 36 + i, 0, SlotActionType.PICKUP,
                                    client.player);

                            // Safety: if the hotbar slot wasn't actually empty (e.g. ghost item),
                            // putting it back will clear the cursor.
                            if (!client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                                client.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, client.player);
                            }

                            client.player.sendMessage(Text.of(
                                    MOD_PREFIX + " §a"
                                            + (language.equals("pl") ? "Uzupelnilem slot " : "Refilled slot ")
                                            + (i + 1)),
                                    true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return; // Refill only one slot per call to be safer
                    }
                }
            }
        }
    }
}
