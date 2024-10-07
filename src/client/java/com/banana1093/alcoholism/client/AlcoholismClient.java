package com.banana1093.alcoholism.client;

import com.banana1093.alcoholism.Alcoholism;
import com.banana1093.alcoholism.Bottle;
import com.banana1093.alcoholism.CustomBucket;
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
import net.minecraft.util.Identifier;

public class AlcoholismClient implements ClientModInitializer, ClientTickEvents.StartTick {

    private static final ManagedShaderEffect BLUR_SHADER = ShaderEffectManager.getInstance()
            .manage(Identifier.of("alcoholism", "shaders/post/blur.json"));
    private static boolean blurEnabled = true;

    private static final ManagedShaderEffect WOBBLE_SHADER = ShaderEffectManager.getInstance()
            .manage(Identifier.of("alcoholism", "shaders/post/wobble.json"));
    private static boolean wobbleEnabled = true;

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
        } else {
            blurEnabled = false;
        }
        if (bac > 0.1) {
            wobbleEnabled = true;
            float wobble = (float) (2 * (bac - 0.1) / 0.15);
            WOBBLE_SHADER.setUniformValue("Wobble", wobble);
            WOBBLE_SHADER.setUniformValue("Time", (float) (System.currentTimeMillis() / 1000.0));
        } else {
            wobbleEnabled = false;
        }
    }
}
