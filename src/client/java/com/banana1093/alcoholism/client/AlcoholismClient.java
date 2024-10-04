package com.banana1093.alcoholism.client;

import com.banana1093.alcoholism.Alcoholism;
import com.banana1093.alcoholism.Bottle;
import com.banana1093.alcoholism.CustomBucket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Objects;
import java.util.Optional;

public class AlcoholismClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FluidRenderHandlerRegistry.INSTANCE.register(Alcoholism.STILL_DILETH10, Alcoholism.FLOWING_DILETH10, new SimpleFluidRenderHandler(
                new Identifier("minecraft:block/water_still"),
                new Identifier("minecraft:block/water_flow"),
                (Alcoholism.BUCKET_DILETH10).getColor(1)
        ));

        FluidRenderHandlerRegistry.INSTANCE.register(Alcoholism.STILL_WINE, Alcoholism.FLOWING_WINE, new SimpleFluidRenderHandler(
                new Identifier("minecraft:block/water_still"),
                new Identifier("minecraft:block/water_flow"),
                (Alcoholism.BUCKET_WINE).getColor(1)
        ));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), Alcoholism.STILL_DILETH10, Alcoholism.FLOWING_DILETH10);

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), Alcoholism.STILL_WINE, Alcoholism.FLOWING_WINE);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ((CustomBucket)stack.getItem()).getColor(tintIndex),
                Alcoholism.BUCKET_DILETH10, Alcoholism.BUCKET_WINE);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0xd1b771, Alcoholism.YEAST);

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex== 1 ? Bottle.getColorOfFluid(stack.getOrCreateNbt().getString("fluid")) : -1, Alcoholism.WINE_BOTTLE);
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex== 1 ? Bottle.getColorOfFluid(stack.getOrCreateNbt().getString("fluid")) : -1, Alcoholism.SHOT_GLASS);


        // display BAC player attribute
        HudRenderCallback.EVENT.register(((matrixStack, tickDelta) -> {
            // text element
            TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
            PlayerEntity player = MinecraftClient.getInstance().player;
//            renderer.draw("This is red", 0, 100, 0xff0000, false);
            assert player != null;
//            EntityAttributeInstance bac = player.getAttributeInstance(Alcoholism.BAC);
//            System.out.println(bac == null);
//            if (bac == null) return;
//            System.out.println(bac.getBaseValue()+" "+bac.getValue());
            double value = Alcoholism.BAC.get(player).getValue();
            matrixStack.drawText(renderer, "BAC: " + value, 0, 0, 0xffffff, false);
        }));
    }
}
