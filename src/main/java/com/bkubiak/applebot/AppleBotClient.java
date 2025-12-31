package com.bkubiak.applebot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;

public class AppleBotClient extends AppleBotClientBase {

    @Override
    public void onInitializeClient() {
        super.onInitializeClient();
    }

    @Override
    protected boolean isFood(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return stack.get(DataComponentTypes.FOOD) != null;
    }

    @Override
    protected void openScreen() {
        MinecraftClient.getInstance().setScreen(new XqbkScreen(this));
    }

    @Override
    protected void executeOnMainThread(Runnable runnable) {
        MinecraftClient.getInstance().execute(runnable);
    }

    @Override
    protected ClickEvent createClickEvent(ClickEvent.Action action, String value) {
        return new ClickEvent(action, value);
    }

    @Override
    protected int getSelectedSlot(net.minecraft.entity.player.PlayerEntity player) {
        return ((com.bkubiak.applebot.mixin.PlayerInventoryAccessor) player.getInventory()).getSelectedSlot();
    }

    @Override
    protected void setInventorySlot(net.minecraft.entity.player.PlayerEntity player, int slot) {
        ((com.bkubiak.applebot.mixin.PlayerInventoryAccessor) player.getInventory()).setSelectedSlot(slot);
    }
}
