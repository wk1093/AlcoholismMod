package com.banana1093.alcoholism;

import com.banana1093.alcoholism.abstraction.Bottle;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Bottles {


    // keep a list of all the bottles
    private List<Bottle> bottles = new ArrayList<>();

    private Item register(String name, int maxAmount) {
        Bottle i = Registry.register(Registries.ITEM, new Identifier(Alcoholism.MODID, name), new Bottle(new Item.Settings().maxCount(1), maxAmount));
        bottles.add(i);
        return i;
    }

    public List<Bottle> getBottles() {
        return bottles;
    }

    public Item WINE_BOTTLE;
    public Item SHOT_GLASS;
    public Item LIQUOR_BOTTLE;

    public Bottles() {
        WINE_BOTTLE = register("wine_bottle", 740);
        SHOT_GLASS = register("shot_glass", 44);
        LIQUOR_BOTTLE = register("liquor_bottle", 750);
    }
}
