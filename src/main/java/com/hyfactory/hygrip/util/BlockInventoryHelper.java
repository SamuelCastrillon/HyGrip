package com.hyfactory.hygrip.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nullable;

/**
 * Helpers to take one item from a block container or put one item into it.
 * Uses BlockModule, ItemContainerState and ItemContainer API.
 */
public final class BlockInventoryHelper {

    private BlockInventoryHelper() {}

    /**
     * Takes one item from the block at (x, y, z) if it has an item container.
     * Iterates slots and removes 1 from the first non-empty slot.
     *
     * @return the item stack taken (quantity 1), or null if block has no container or no items
     */
    @Nullable
    public static ItemStack takeOneItemFromBlock(World world, int x, int y, int z) {
        ItemContainerState state = getItemContainerState(world, x, y, z);
        if (state == null) {
            return null;
        }
        ItemContainer container = state.getItemContainer();
        short capacity = container.getCapacity();
        for (short slot = 0; slot < capacity; slot++) {
            ItemStack stack = container.getItemStack(slot);
            if (ItemStack.isEmpty(stack)) {
                continue;
            }
            ItemStackSlotTransaction tx = container.removeItemStackFromSlot(slot, 1);
            if (tx.succeeded()) {
                ItemStack removed = tx.getOutput();
                if (removed != null && !ItemStack.isEmpty(removed)) {
                    return removed;
                }
                return stack.withQuantity(1);
            }
        }
        return null;
    }

    /**
     * Puts the given item stack into the block container at (x, y, z).
     *
     * @return remainder (what did not fit), or null if fully deposited or block has no container
     */
    @Nullable
    public static ItemStack putItemAtBlock(World world, int x, int y, int z, ItemStack itemStack) {
        if (ItemStack.isEmpty(itemStack)) {
            return null;
        }
        ItemContainerState state = getItemContainerState(world, x, y, z);
        if (state == null) {
            return itemStack;
        }
        ItemContainer container = state.getItemContainer();
        ItemStackTransaction tx = container.addItemStack(itemStack);
        return tx.getRemainder();
    }

    @Nullable
    private static ItemContainerState getItemContainerState(World world, int x, int y, int z) {
        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(world, x, y, z);
        if (blockRef == null || !blockRef.isValid()) {
            return null;
        }
        Store<ChunkStore> store = world.getChunkStore().getStore();
        var componentType = BlockStateModule.get().getComponentType(ItemContainerState.class);
        if (componentType == null) {
            return null;
        }
        return store.getComponent(blockRef, componentType);
    }
}
