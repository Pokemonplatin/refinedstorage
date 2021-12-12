package com.refinedmods.refinedstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ColoredNetworkBlock extends NetworkNodeBlock {
    public ColoredNetworkBlock(Properties props) {
        super(props);
    }

    // Don't do block drops if we change the color.
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock().getClass().equals(newState.getBlock().getClass())) {
            checkIfDirectionHasChanged(state, world, pos, newState);
        } else {
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }
}

