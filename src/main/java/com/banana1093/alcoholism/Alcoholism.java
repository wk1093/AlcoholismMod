package com.banana1093.alcoholism;

import com.banana1093.alcoholism.cardinal.SyncedFloatComponent;
import com.banana1093.alcoholism.fluids.DilEth10;
import com.banana1093.alcoholism.fluids.Wine;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Alcoholism implements ModInitializer, EntityComponentInitializer {

    public static final String MODID = "alcoholism";

    public static final FlowableFluid STILL_DILETH10 = new DilEth10.Still();
    public static final FlowableFluid FLOWING_DILETH10 = new DilEth10.Flowing();
    public static final CustomBucket BUCKET_DILETH10 = new CustomBucket(new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1), DilEth10.COLOR, STILL_DILETH10);
    public static final Block DILETH10 = new FluidBlock(STILL_DILETH10, FabricBlockSettings.copy(Blocks.WATER));

    public static final FlowableFluid STILL_WINE = new Wine.Still();
    public static final FlowableFluid FLOWING_WINE = new Wine.Flowing();
    public static final CustomBucket BUCKET_WINE = new CustomBucket(new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1), Wine.COLOR, STILL_WINE);
    public static final Block WINE = new FluidBlock(STILL_WINE, FabricBlockSettings.copy(Blocks.WATER));

    public static final Item YEAST = new Item(new Item.Settings());

    // a wine bottle should contain about 25 oz of fluid, which is about 740 mL
    // a shot glass should contain about 1.5 oz of fluid, which is about 44 mL
    public static final Item WINE_BOTTLE = new Bottle(new Item.Settings(), 740);
    public static final Item SHOT_GLASS = new Bottle(new Item.Settings(), 44);

    public static final Block FLUID_CONTAINER = new FluidContainerBlock(FabricBlockSettings.copy(Blocks.CAULDRON));
    public static final BlockEntityType<FluidContainerEntity> FLUID_CONTAINER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MODID, "fluid_container"), BlockEntityType.Builder.create(FluidContainerEntity::new, FLUID_CONTAINER).build(null));
    public static final Item FLUID_CONTAINER_ITEM = new BlockItem(FLUID_CONTAINER, new Item.Settings());

    public static final ComponentKey<SyncedFloatComponent> BAC =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "bac"), SyncedFloatComponent.class);

    public static final ItemGroup ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(Items.BUCKET))
            .displayName(Text.translatable("itemGroup.alcoholism.group"))
            .entries((context, entries) -> {
                entries.add(YEAST);
                entries.add(WINE_BOTTLE);
                entries.add(SHOT_GLASS);
                entries.add(FLUID_CONTAINER_ITEM);
                entries.add(BUCKET_DILETH10);
                entries.add(BUCKET_WINE);

            })
            .build();


    @Override
    public void onInitialize() {
        Registry.register(Registries.FLUID, new Identifier(MODID, "dileth10"), STILL_DILETH10);
        Registry.register(Registries.FLUID, new Identifier(MODID, "flowing_dileth10"), FLOWING_DILETH10);
        Registry.register(Registries.ITEM, new Identifier(MODID, "bucket_dileth10"), BUCKET_DILETH10);
        Registry.register(Registries.BLOCK, new Identifier(MODID, "dileth10"), DILETH10);

        Registry.register(Registries.FLUID, new Identifier(MODID, "wine"), STILL_WINE);
        Registry.register(Registries.FLUID, new Identifier(MODID, "flowing_wine"), FLOWING_WINE);
        Registry.register(Registries.ITEM, new Identifier(MODID, "bucket_wine"), BUCKET_WINE);
        Registry.register(Registries.BLOCK, new Identifier(MODID, "wine"), WINE);


        Registry.register(Registries.ITEM, new Identifier(MODID, "yeast"), YEAST);

        Registry.register(Registries.ITEM, new Identifier(MODID, "wine_bottle"), WINE_BOTTLE);
        Registry.register(Registries.ITEM, new Identifier(MODID, "shot_glass"), SHOT_GLASS);

        Registry.register(Registries.BLOCK, new Identifier(MODID, "fluid_container"), FLUID_CONTAINER);
        Registry.register(Registries.ITEM, new Identifier(MODID, "fluid_container"), FLUID_CONTAINER_ITEM);

        Registry.register(Registries.ITEM_GROUP, new Identifier("alcoholism", "group"), ITEM_GROUP);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(BAC, SyncedFloatComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }
}
