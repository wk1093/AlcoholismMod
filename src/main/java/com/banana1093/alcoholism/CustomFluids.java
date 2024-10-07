package com.banana1093.alcoholism;


import com.banana1093.alcoholism.abstraction.CustomBucket;
import com.banana1093.alcoholism.abstraction.CustomFluid;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class CustomFluids {
    public static class FluidData {
        public final CustomFluid still;
        public final CustomFluid flowing;
        public CustomBucket bucket;
        public Block block;

        public FluidData(CustomFluid still, CustomFluid flowing, CustomBucket bucket, Block block) {
            this.still = still;
            this.flowing = flowing;
            this.bucket = bucket;
            this.block = block;
        }
    }

    private Map<String, FluidData> FLUIDS = new HashMap<>();

    public CustomFluid getFluid(String name) {
        if (!FLUIDS.containsKey(name)) {
            return null;
        }
        return FLUIDS.get(name).still;
    }

    // Generics in Java don't work like that, so we need to pass the class as a parameter
    private FluidData register(String name, int color, float alc) {
        System.out.println("Registering fluid: " + name);
        FluidData fd = new FluidData(new CustomFluid.Still(color, alc), new CustomFluid.Flowing(color, alc), null, null);
        fd.still.STILL = fd.still;
        fd.still.FLOWING = fd.flowing;
        fd.still.BUCKET = fd.bucket;
        fd.still.BLOCK = fd.block;
        fd.flowing.STILL = fd.still;
        fd.flowing.FLOWING = fd.flowing;
        fd.flowing.BUCKET = fd.bucket;
        fd.flowing.BLOCK = fd.block;
        fd.bucket = new CustomBucket(new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1), fd.still.getColor(), fd.still);
        fd.block = new FluidBlock(fd.still, FabricBlockSettings.copy(Blocks.WATER));
        fd.still.BUCKET = fd.bucket;
        fd.still.BLOCK = fd.block;
        fd.flowing.BUCKET = fd.bucket;
        fd.flowing.BLOCK = fd.block;
        FLUIDS.put(name, fd);
        Registry.register(Registries.FLUID, new Identifier(Alcoholism.MODID, name), fd.still);
        Registry.register(Registries.FLUID, new Identifier(Alcoholism.MODID, "flowing_" + name), fd.flowing);
        Registry.register(Registries.ITEM, new Identifier(Alcoholism.MODID, "bucket_"+name), fd.bucket);
        Registry.register(Registries.BLOCK, new Identifier(Alcoholism.MODID, name), fd.block);
        return fd;
    }

    // function to get list of all buckets
    // don't need a map either
    public CustomBucket[] getBuckets() {
        return FLUIDS.values().stream().map(f -> f.bucket).toArray(CustomBucket[]::new);
    }

    public FluidData[] getFluids() {
        return FLUIDS.values().toArray(FluidData[]::new);
    }


    public FluidData DILETH10;
    public FluidData WINE;
    public FluidData WHISKEY;

    public CustomFluids() {
        DILETH10 = this.register("dileth10", 0xf7e09e, 0.1f);
        WINE = this.register("wine", 0x9c1138, 0.2f);
        WHISKEY = this.register("whiskey", 0xb37534, 0.48f);

    }


}
