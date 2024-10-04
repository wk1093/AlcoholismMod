package com.banana1093.alcoholism.data;

import com.banana1093.alcoholism.Alcoholism;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.util.Identifier;

import java.util.Optional;

class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput generator) {
        super(generator);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // ...
    }

    public static Model item(String parent) {
        return new Model(Optional.of(new Identifier(Alcoholism.MODID, "item/" + parent)), Optional.empty());
    }

    public static Model mc_item(String parent) {
        return new Model(Optional.of(new Identifier("minecraft", "item/" + parent)), Optional.empty());
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(Alcoholism.BUCKET_DILETH10, item("template_bucket"));
        itemModelGenerator.register(Alcoholism.BUCKET_WINE, item("template_bucket"));
        itemModelGenerator.register(Alcoholism.YEAST, mc_item("gunpowder"));
    }
}
