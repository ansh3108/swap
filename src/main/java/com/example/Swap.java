package com.example;

import com.example.mixin.PlayerInventoryAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class Swap implements ClientModInitializer {
    private Item lastItem = null;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        PlayerInventory inv = client.player.getInventory();
        int selected = ((PlayerInventoryAccessor) inv).getSelectedSlot();
        ItemStack current = inv.getStack(selected);

        boolean isNearlyBroken = current.isDamageable() && (current.getMaxDamage() - current.getDamage() <= 2);
        boolean needsSwap = current.isEmpty() || isNearlyBroken;

        if (lastItem != null && needsSwap) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = client.player.playerScreenHandler.getSlot(i).getStack();
                
                boolean isSafeReplacement = !stack.isDamageable() || (stack.getMaxDamage() - stack.getDamage() > 2);

                if (stack.isOf(lastItem) && isSafeReplacement) {
                    client.interactionManager.clickSlot(
                        client.player.playerScreenHandler.syncId,
                        i,
                        selected,
                        SlotActionType.SWAP,
                        client.player
                    );
                    break;
                }
            }
        }

        lastItem = current.isEmpty() ? null : current.getItem();
    }
}