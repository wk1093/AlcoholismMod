package com.banana1093.alcoholism.client;

import com.banana1093.alcoholism.Alcoholism;
import com.banana1093.alcoholism.CustomFluids;
import com.banana1093.alcoholism.abstraction.Bottle;
import com.banana1093.alcoholism.abstraction.CustomBucket;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedShaderEffect;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;

public class AlcoholismClient implements ClientModInitializer, ClientTickEvents.StartTick {

    private static final ManagedShaderEffect BLUR_SHADER = ShaderEffectManager.getInstance()
            .manage(Identifier.of("alcoholism", "shaders/post/blur.json"));
    private static boolean blurEnabled = true;

    private static final ManagedShaderEffect WOBBLE_SHADER = ShaderEffectManager.getInstance()
            .manage(Identifier.of("alcoholism", "shaders/post/wobble.json"));
    private static boolean wobbleEnabled = true;

    @Override
    public void onInitializeClient() {
        for (CustomFluids.FluidData fluid : Alcoholism.FLUIDS.getFluids()) {
            FluidRenderHandlerRegistry.INSTANCE.register(fluid.still, fluid.flowing, new SimpleFluidRenderHandler(
                    new Identifier("minecraft:block/water_still"),
                    new Identifier("minecraft:block/water_flow"),
                    (fluid.bucket).getColor(1)
            ));
            BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(), fluid.still, fluid.flowing);
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> ((CustomBucket)stack.getItem()).getColor(tintIndex),
                    fluid.bucket);
        }

        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> 0xd1b771, Alcoholism.YEAST);

        for (Bottle bottle : Alcoholism.BOTTLES.getBottles()) {
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> (tintIndex == 1 && Alcoholism.getFluid(stack.getOrCreateNbt().getString("fluid")) != null) ? Bottle.getColorOfFluid(stack.getOrCreateNbt().getString("fluid")) : -1, bottle);
            // if the fluid is null, we want it to be transparent
            // we have 2 models, one for the empty bottle, and one for the filled bottle
            // if it is empty, we want to render the empty bottle model
            // if it is filled, we want to render the filled bottle model


        }

        // display BAC player attribute
        HudRenderCallback.EVENT.register(((matrixStack, tickDelta) -> {
            // text element
            TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
            PlayerEntity player = MinecraftClient.getInstance().player;
            assert player != null;
            double bac = Alcoholism.BAC.get(player).getBac();
            matrixStack.drawText(renderer, "BAC: " + bac, 0, 0, 0xffffff, false);
            double rtbac = Alcoholism.BAC.get(player).getRtBac();
            matrixStack.drawText(renderer, "rtBAC: " + rtbac, 0, 10, 0xffffff, false);
        }));

        ClientTickEvents.START_CLIENT_TICK.register(this);

        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if (blurEnabled) {
                BLUR_SHADER.render(tickDelta);
            }
        });

        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if (wobbleEnabled) {
                WOBBLE_SHADER.render(tickDelta);
            }
        });


    }

    @Override
    public void onStartTick(MinecraftClient minecraftClient) {
        // if the players BAC is above 0.13, apply a blur shader
        ClientPlayerEntity player = minecraftClient.player;
        if (player == null) return;
        double bac = Alcoholism.BAC.get(player).getBac();
        if (bac > 0.13) {
            blurEnabled = true;
            float radius = (float) (0.5 + 9.5 * (bac - 0.13) / 0.12);
            BLUR_SHADER.setUniformValue("Radius", radius);
            // set the blur direction depending on the player looking direction
            Vec2f direction = new Vec2f((float) Math.sin(player.getYaw() * Math.PI / 180), (float) Math.cos(player.getYaw() * Math.PI / 180));
            BLUR_SHADER.setUniformValue("BlurDir", direction.x, direction.y);

        } else {
            blurEnabled = false;
        }
        if (bac > 0.1) {
            wobbleEnabled = true;
            float wobble = (float)((bac - 0.1) / 0.15);
            WOBBLE_SHADER.setUniformValue("Wobble", wobble);

            WOBBLE_SHADER.setUniformValue("Time", System.currentTimeMillis() / 1000.0f);
        } else {
            wobbleEnabled = false;
        }
    }
}
