package com.banana1093.alcoholism.data;

import com.banana1093.alcoholism.Alcoholism;
import com.banana1093.alcoholism.CustomFluids;
import com.banana1093.alcoholism.abstraction.CustomBucket;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Blocks;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.util.Identifier;

import java.util.Optional;

class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput generator) {
        super(generator);
    }

    public static Model item(String parent) {
        return new Model(Optional.of(new Identifier(Alcoholism.MODID, "item/" + parent)), Optional.empty());
    }


    public static Model mc_item(String parent) {
        return new Model(Optional.of(new Identifier("minecraft", "item/" + parent)), Optional.empty());
    }


    public static Model mc_block(String parent) {
        return new Model(Optional.of(new Identifier("minecraft", "block/" + parent)), Optional.empty());
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        for (CustomBucket bucket : Alcoholism.FLUIDS.getBuckets()) {
            itemModelGenerator.register(bucket, item("template_bucket"));
        }

        itemModelGenerator.register(Alcoholism.YEAST, mc_item("gunpowder"));
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // all the fluid blocks should have this:
        /*
        {
  "variants": {
    "": {
      "model": "minecraft:block/water"
    }
  }
}
         */

        for (CustomFluids.FluidData fluid : Alcoholism.FLUIDS.getFluids()) {
            blockStateModelGenerator.registerStateWithModelReference(fluid.block, Blocks.WATER);
        }
    }
}
