package com.refinedmods.refinedstorage.block;

import com.refinedmods.refinedstorage.apiimpl.network.node.storage.StorageNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.storage.ItemStorageType;
import com.refinedmods.refinedstorage.container.StorageContainer;
import com.refinedmods.refinedstorage.container.factory.PositionalTileContainerProvider;
import com.refinedmods.refinedstorage.tile.CrafterTile;
import com.refinedmods.refinedstorage.tile.StorageTile;
import com.refinedmods.refinedstorage.util.BlockUtils;
import com.refinedmods.refinedstorage.util.NetworkUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;

public class StorageBlock extends NetworkNodeBlock {
    private final ItemStorageType type;

    public StorageBlock(ItemStorageType type) {
        super(BlockUtils.DEFAULT_ROCK_PROPERTIES);

        this.type = type;
    }

    public ItemStorageType getType() {
        return type;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (!world.isClientSide) {
            StorageNetworkNode storage = ((StorageTile) world.getBlockEntity(pos)).getNode();

            if (stack.hasTag() && stack.getTag().hasUUID(StorageNetworkNode.NBT_ID)) {
                storage.setStorageId(stack.getTag().getUUID(StorageNetworkNode.NBT_ID));
            }

            storage.loadStorage(entity instanceof Player ? (Player) entity : null);
        }

        // Call this after loading the storage, so the network discovery can use the loaded storage.
        super.setPlacedBy(world, pos, state, entity, stack);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageTile(type, pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!world.isClientSide) {
            return NetworkUtils.attemptModify(world, pos, player, () -> NetworkHooks.openGui((ServerPlayer) player, new PositionalTileContainerProvider<StorageTile>(
                ((StorageTile) world.getBlockEntity(pos)).getNode().getTitle(),
                (tile, windowId, inventory, p) -> new StorageContainer(tile, player, windowId),
                pos
            ), pos));
        }

        return InteractionResult.SUCCESS;
    }
}
