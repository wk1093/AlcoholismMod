package com.banana1093.alcoholism.abstraction;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;

public class CustomBucket extends BucketItem {
    private final int fluidColor;

    public CustomBucket(Item.Settings settings, int fluidColor, Fluid fluid) {
        super(fluid, settings);
        this.fluidColor = fluidColor;
    }

    public int getColor(int tintIndex) {
        return tintIndex == 1 ? fluidColor: -1;
    }
}