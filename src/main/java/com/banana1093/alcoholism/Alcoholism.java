package com.banana1093.alcoholism;

import com.banana1093.alcoholism.cardinal.SyncedFloatComponent;
import com.banana1093.alcoholism.fluids.DilEth10;
import com.banana1093.alcoholism.fluids.Wine;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/*
TODOS
TODO - smooth BAC changes, doesn't instantly go up when drinking:
    There will be a rtBAC (real-time BAC) and a BAC (displayed BAC) in the player's component, the BAC will slowly change towards the rtBAC, the rtBAC will slowly change towards 0
    When the player drinks, the rtBAC will increase by a formula based on the amount of alcohol consumed
    When the player stops drinking, the rtBAC will decrease by a formula based on the time since the player stopped drinking
    The BAC will be updated every tick to be closer to the rtBAC
TODO - add effects to different BAC levels:
    The player will get effects based on their BAC level, these effects will be applied every tick
    For example, when the player has a good amount (to be determined) their movement will be harder to control (random-based movement manipulation)
    When the player has a high amount (to be determined) a special shader will be applied to the player's screen (making far things look farther, and close things look closer, as well as some blur)
    When the player has a very high amount (to be determined) they will get blindness and mining fatigue
    These will not be exact amounts where the effect just appears, they will all be gradual and based on the player's BAC level.
    For example the shader will start to take affect at one BAC, but wont get fully applied until a higher BAC
    This will make it smooth and almost unnoticeable when the effects start to take place
TODO - add a way to lower BAC:
    The player will be able to lower their BAC by drinking water, eating food, or waiting
    Drinking water will lower the rtBAC by a certain amount, eating food will lower the rtBAC by a certain amount, and waiting will lower the rtBAC by a certain amount
    The player will also be able to see their BAC level in the debug screen


 */


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

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("bac")
                .executes(context -> {
                    ServerCommandSource s = (ServerCommandSource) context.getSource();
                    s.sendFeedback(() -> Text.literal("No arguments provided"), false);
                    return 1;
                }).then(CommandManager.literal("set")
                        .then(CommandManager.argument("value", FloatArgumentType.floatArg())
                                .executes(context -> {
                                    ServerCommandSource s = (ServerCommandSource) context.getSource();
                                    ServerPlayerEntity player = s.getPlayer();
                                    float value = FloatArgumentType.getFloat(context, "value");
                                    BAC.get(player).setValue(value);
                                    s.sendFeedback(() -> Text.literal("Set BAC to " + value), false);
                                    return 1;
                                })
                        )
                ).then(CommandManager.literal("get")
                        .executes(context -> {
                            ServerCommandSource s = (ServerCommandSource) context.getSource();
                            ServerPlayerEntity player = s.getPlayer();
                            float value = BAC.get(player).getValue();
                            s.sendFeedback(() -> Text.literal("BAC is " + value), false);
                            return 1;
                        })
                )
        ));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(BAC, SyncedFloatComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }
}
