package com.swap;

import com.swap.mixin.PlayerInventoryAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Swap implements ClientModInitializer {
    private Item lastItem = null;
    private boolean isSwapEnabled = true;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.swap.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyBinding.Category.create(Identifier.of("swap", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;

        while (toggleKey.wasPressed()) {
            isSwapEnabled = !isSwapEnabled;
            String state = isSwapEnabled ? "§aON" : "§cOFF";
            client.player.sendMessage(Text.literal("§7Smart Swap: " + state), true);
        }

        if (!isSwapEnabled) {
            ItemStack current = client.player.getInventory().getStack(((PlayerInventoryAccessor) client.player.getInventory()).getSelectedSlot());
            lastItem = current.isEmpty() ? null : current.getItem();
            return;
        }

        PlayerInventory inv = client.player.getInventory();
        int selected = ((PlayerInventoryAccessor) inv).getSelectedSlot();
        ItemStack current = inv.getStack(selected);

        boolean isNearlyBroken = current.isDamageable() && (current.getMaxDamage() - current.getDamage() <= 2);
        boolean needsSwap = current.isEmpty() || isNearlyBroken;

        if (lastItem != null && needsSwap) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = client.player.playerScreenHandler.getSlot(i).getStack();
                boolean isSafeReplacement = !stack.isDamageable() || (stack.getMaxDamage() - stack.getDamage() > 2);

                if (isSafeReplacement && isMatch(lastItem, stack)) {
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

    private boolean isMatch(Item previousItem, ItemStack candidate) {
        if (candidate.isOf(previousItem)) return true;

        ItemStack prevStack = previousItem.getDefaultStack();
        
        if (prevStack.isIn(ItemTags.PICKAXES) && candidate.isIn(ItemTags.PICKAXES)) return true;
        if (prevStack.isIn(ItemTags.AXES) && candidate.isIn(ItemTags.AXES)) return true;
        if (prevStack.isIn(ItemTags.SHOVELS) && candidate.isIn(ItemTags.SHOVELS)) return true;
        if (prevStack.isIn(ItemTags.SWORDS) && candidate.isIn(ItemTags.SWORDS)) return true;
        if (prevStack.isIn(ItemTags.HOES) && candidate.isIn(ItemTags.HOES)) return true;
        if (prevStack.isIn(ItemTags.DIRT) && candidate.isIn(ItemTags.DIRT)) return true;
        if (prevStack.isIn(ItemTags.PLANKS) && candidate.isIn(ItemTags.PLANKS)) return true;

        return false;
    }
}

