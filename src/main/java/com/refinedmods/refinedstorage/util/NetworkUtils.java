package com.refinedmods.refinedstorage.util;

import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.network.node.INetworkNode;
import com.refinedmods.refinedstorage.api.network.node.INetworkNodeProxy;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.capability.NetworkNodeProxyCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public final class NetworkUtils {
    private NetworkUtils() {
    }

    @Nullable
    public static INetworkNode getNodeFromTile(@Nullable BlockEntity tile) {
        if (tile != null) {
            INetworkNodeProxy<?> proxy = tile.getCapability(NetworkNodeProxyCapability.NETWORK_NODE_PROXY_CAPABILITY).orElse(null);
            if (proxy != null) {
                return proxy.getNode();
            }
        }

        return null;
    }

    @Nullable
    public static INetwork getNetworkFromNode(@Nullable INetworkNode node) {
        if (node != null) {
            return node.getNetwork();
        }

        return null;
    }

    public static InteractionResult attemptModify(Level world, BlockPos pos, Player player, Runnable action) {
        return attempt(world, pos, player, action, Permission.MODIFY);
    }

    public static InteractionResult attempt(Level world, BlockPos pos, Player player, Runnable action, Permission... permissionsRequired) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        INetwork network = getNetworkFromNode(getNodeFromTile(world.getBlockEntity(pos)));

        if (network != null) {
            for (Permission permission : permissionsRequired) {
                if (!network.getSecurityManager().hasPermission(permission, player)) {
                    WorldUtils.sendNoPermissionMessage(player);

                    return InteractionResult.SUCCESS;
                }
            }
        }

        action.run();

        return InteractionResult.SUCCESS;
    }

    public static void extractBucketFromPlayerInventoryOrNetwork(Player player, INetwork network, Consumer<ItemStack> onBucketFound) {
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack slot = player.getInventory().getItem(i);

            if (API.instance().getComparer().isEqualNoQuantity(StackUtils.EMPTY_BUCKET, slot)) {
                player.getInventory().removeItem(i, 1);

                onBucketFound.accept(StackUtils.EMPTY_BUCKET.copy());

                return;
            }
        }

        ItemStack fromNetwork = network.extractItem(StackUtils.EMPTY_BUCKET, 1, Action.PERFORM);
        if (!fromNetwork.isEmpty()) {
            onBucketFound.accept(fromNetwork);
        }
    }
}
