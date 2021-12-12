package com.refinedmods.refinedstorage.render.tesr;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Vector3f;
import com.refinedmods.refinedstorage.RSBlocks;
import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.block.StorageMonitorBlock;
import com.refinedmods.refinedstorage.tile.StorageMonitorTile;
import com.refinedmods.refinedstorage.tile.config.IType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.model.TransformationHelper;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

public class StorageMonitorTileRenderer implements BlockEntityRenderer<StorageMonitorTile> {
    @Override
    public void render(StorageMonitorTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource renderTypeBuffer, int i, int i1) {
        Direction direction = Direction.NORTH;

        BlockState state = tile.getLevel().getBlockState(tile.getBlockPos());
        if (state.getBlock() instanceof StorageMonitorBlock) {
            direction = state.getValue(RSBlocks.STORAGE_MONITOR.get().getDirection().getProperty());
        }

        final int light = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().offset(direction.getNormal()));
        final float rotation = (float) (Math.PI * (360 - direction.getOpposite().get2DDataValue() * 90) / 180d);

        final int type = tile.getStackType();

        final ItemStack itemStack = tile.getItemStack();
        final FluidStack fluidStack = tile.getFluidStack();

        if (type == IType.ITEMS && itemStack != null && !itemStack.isEmpty()) {
            renderItem(matrixStack, renderTypeBuffer, direction, rotation, light, itemStack);

            String amount = API.instance().getQuantityFormatter().formatWithUnits(tile.getAmount());

            renderText(matrixStack, renderTypeBuffer, direction, rotation, light, amount);
        } else if (type == IType.FLUIDS && fluidStack != null && !fluidStack.isEmpty()) {
            renderFluid(matrixStack, renderTypeBuffer, direction, rotation, light, fluidStack);

            String amount = API.instance().getQuantityFormatter().formatInBucketFormWithOnlyTrailingDigitsIfZero(tile.getAmount());

            renderText(matrixStack, renderTypeBuffer, direction, rotation, light, amount);
        }
    }

    private void renderText(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, Direction direction, float rotation, int light, String amount) {
        matrixStack.pushPose();

        float stringOffset = -(Minecraft.getInstance().font.width(amount) * 0.01F) / 2F;

        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.translate(
            ((float) direction.getStepX() * 0.501F) + (direction.getStepZ() * stringOffset),
            -0.275,
            ((float) direction.getStepZ() * 0.501F) - (direction.getStepX() * stringOffset)
        );

        matrixStack.mulPose(TransformationHelper.quatFromXYZ(new Vector3f(direction.getStepX() * 180, 0, direction.getStepZ() * 180), true));
        matrixStack.mulPose(TransformationHelper.quatFromXYZ(new Vector3f(0, rotation, 0), false));

        matrixStack.scale(0.01F, 0.01F, 0.01F);

        Minecraft.getInstance().font.drawInBatch(
            amount,
            0,
            0,
            -1,
            false,
            matrixStack.last().pose(),
            renderTypeBuffer,
            false,
            0,
            light
        );

        matrixStack.popPose();
    }

    @SuppressWarnings("deprecation")
    private void renderItem(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, Direction direction, float rotation, int light, ItemStack itemStack) {
        matrixStack.pushPose();

        // Put it in the middle, outwards, and facing the correct direction
        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.translate((float) direction.getStepX() * 0.501F, 0, (float) direction.getStepZ() * 0.501F);
        matrixStack.mulPose(TransformationHelper.quatFromXYZ(new Vector3f(0, rotation, 0), false));

        // Make it look "flat"
        matrixStack.scale(0.5F, -0.5F, -0.00005f);

        // Fix rotation after making it look flat
        matrixStack.mulPose(TransformationHelper.quatFromXYZ(new Vector3f(0, 0, 180), true));

        BakedModel itemModel = Minecraft.getInstance().getItemRenderer().getModel(itemStack, null, null, 0);
        boolean render3D = itemModel.isGui3d();

        if (render3D) {
            Lighting.setupFor3DItems();
        } else {
            Lighting.setupForFlatItems();
        }

        matrixStack.last().normal().load(Matrix3f.createScaleMatrix(1, -1, 1));
        Minecraft.getInstance().getItemRenderer().render(
            itemStack,
            ItemTransforms.TransformType.GUI,
            false,
            matrixStack,
            renderTypeBuffer,
            light,
            OverlayTexture.NO_OVERLAY,
            itemModel
        );

        matrixStack.popPose();
    }

    private void renderFluid(PoseStack matrixStack, MultiBufferSource renderTypeBuffer, Direction direction, float rotation, int light, FluidStack fluidStack) {
        matrixStack.pushPose();

        matrixStack.translate(0.5D, 0.5D, 0.5D);
        matrixStack.translate((float) direction.getStepX() * 0.51F, 0.5F, (float) direction.getStepZ() * 0.51F);
        matrixStack.mulPose(TransformationHelper.quatFromXYZ(new Vector3f(0, rotation, 0), false));

        matrixStack.scale(0.5F, 0.5F, 0.5F);

        final Fluid fluid = fluidStack.getFluid();
        final FluidAttributes attributes = fluid.getAttributes();
        final ResourceLocation fluidStill = attributes.getStillTexture(fluidStack);
        final TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
        final int fluidColor = attributes.getColor(fluidStack);

        final VertexConsumer buffer = renderTypeBuffer.getBuffer(RenderType.text(sprite.atlas().location()));

        final int colorRed = fluidColor >> 16 & 0xFF;
        final int colorGreen = fluidColor >> 8 & 0xFF;
        final int colorBlue = fluidColor & 0xFF;
        final int colorAlpha = fluidColor >> 24 & 0xFF;

        buffer.vertex(matrixStack.last().pose(), -0.5F, -0.5F, 0F)
            .color(colorRed, colorGreen, colorBlue, colorAlpha)
            .uv(sprite.getU0(), sprite.getV0())
            .uv2(light)
            .endVertex();
        buffer.vertex(matrixStack.last().pose(), 0.5F, -0.5F, 0F)
            .color(colorRed, colorGreen, colorBlue, colorAlpha)
            .uv(sprite.getU1(), sprite.getV0())
            .uv2(light)
            .endVertex();
        buffer.vertex(matrixStack.last().pose(), 0.5F, -1.5F, 0F)
            .color(colorRed, colorGreen, colorBlue, colorAlpha)
            .uv(sprite.getU1(), sprite.getV1())
            .uv2(light)
            .endVertex();
        buffer.vertex(matrixStack.last().pose(), -0.5F, -1.5F, 0F)
            .color(colorRed, colorGreen, colorBlue, colorAlpha)
            .uv(sprite.getU0(), sprite.getV1())
            .uv2(light)
            .endVertex();

        matrixStack.popPose();
    }
}
