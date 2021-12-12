package com.refinedmods.refinedstorage.network.grid;

import com.refinedmods.refinedstorage.api.network.grid.IGrid;
import com.refinedmods.refinedstorage.apiimpl.network.node.GridNetworkNode;
import com.refinedmods.refinedstorage.container.GridContainer;
import com.refinedmods.refinedstorage.tile.grid.portable.PortableGrid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PortableGridSettingsUpdateMessage {
    private final int viewType;
    private final int sortingDirection;
    private final int sortingType;
    private final int searchBoxMode;
    private final int size;
    private final int tabSelected;
    private final int tabPage;

    public PortableGridSettingsUpdateMessage(int viewType, int sortingDirection, int sortingType, int searchBoxMode, int size, int tabSelected, int tabPage) {
        this.viewType = viewType;
        this.sortingDirection = sortingDirection;
        this.sortingType = sortingType;
        this.searchBoxMode = searchBoxMode;
        this.size = size;
        this.tabSelected = tabSelected;
        this.tabPage = tabPage;
    }

    public static PortableGridSettingsUpdateMessage decode(FriendlyByteBuf buf) {
        return new PortableGridSettingsUpdateMessage(
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt()
        );
    }

    public static void encode(PortableGridSettingsUpdateMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.viewType);
        buf.writeInt(message.sortingDirection);
        buf.writeInt(message.sortingType);
        buf.writeInt(message.searchBoxMode);
        buf.writeInt(message.size);
        buf.writeInt(message.tabSelected);
        buf.writeInt(message.tabPage);
    }

    public static void handle(PortableGridSettingsUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        Player player = ctx.get().getSender();

        if (player != null) {
            ctx.get().enqueueWork(() -> {
                if (player.containerMenu instanceof GridContainer) {
                    IGrid grid = ((GridContainer) player.containerMenu).getGrid();

                    if (grid instanceof PortableGrid) {
                        ItemStack stack = ((PortableGrid) grid).getStack();

                        if (!stack.hasTag()) {
                            stack.setTag(new CompoundTag());
                        }

                        if (IGrid.isValidViewType(message.viewType)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_VIEW_TYPE, message.viewType);
                        }

                        if (IGrid.isValidSortingDirection(message.sortingDirection)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_SORTING_DIRECTION, message.sortingDirection);
                        }

                        if (IGrid.isValidSortingType(message.sortingType)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_SORTING_TYPE, message.sortingType);
                        }

                        if (IGrid.isValidSearchBoxMode(message.searchBoxMode)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_SEARCH_BOX_MODE, message.searchBoxMode);
                        }

                        if (IGrid.isValidSize(message.size)) {
                            stack.getTag().putInt(GridNetworkNode.NBT_SIZE, message.size);
                        }

                        stack.getTag().putInt(GridNetworkNode.NBT_TAB_SELECTED, message.tabSelected);
                        stack.getTag().putInt(GridNetworkNode.NBT_TAB_PAGE, message.tabPage);
                    }
                }
            });
        }

        ctx.get().setPacketHandled(true);
    }
}
