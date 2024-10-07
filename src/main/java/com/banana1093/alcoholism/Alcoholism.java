package com.banana1093.alcoholism;

import com.banana1093.alcoholism.abstraction.Bottle;
import com.banana1093.alcoholism.abstraction.CustomBucket;
import com.banana1093.alcoholism.abstraction.CustomFluid;
import com.banana1093.alcoholism.cardinal.BacComponent;
import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/*
TODOS
TODO - add a way to lower BAC:
    The player will be able to lower their BAC by drinking water, eating food, or waiting
    Drinking water will lower the rtBAC by a certain amount, eating food will lower the rtBAC by a certain amount, and waiting will lower the rtBAC by a certain amount
    The player will also be able to see their BAC level in the debug screen
TODO - balance effect values
TODO - fix fluid container being reset on restart/relog
TODO - custom drink animations
TODO - fermentation process
TODO - distillation process

 */


public class Alcoholism implements ModInitializer, EntityComponentInitializer, ServerTickEvents.StartTick {

    public static final String MODID = "alcoholism";


    public static CustomFluids FLUIDS;

    public static final Item YEAST = new Item(new Item.Settings());

    // a wine bottle should contain about 25 oz of fluid, which is about 740 mL
    // a shot glass should contain about 1.5 oz of fluid, which is about 44 mL
    // a liquor bottle should contain about 25.3 oz of fluid, which is about 750 mL
    public static final Item WINE_BOTTLE = new Bottle(new Item.Settings().maxCount(1), 740);
    public static final Item SHOT_GLASS = new Bottle(new Item.Settings().maxCount(1), 44);
    public static final Item LIQUOR_BOTTLE = new Bottle(new Item.Settings().maxCount(1), 750);

    public static final Block FLUID_CONTAINER = new FluidContainerBlock(FabricBlockSettings.copy(Blocks.CAULDRON));
    public static final BlockEntityType<FluidContainerEntity> FLUID_CONTAINER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MODID, "fluid_container"), BlockEntityType.Builder.create(FluidContainerEntity::new, FLUID_CONTAINER).build(null));
    public static final Item FLUID_CONTAINER_ITEM = new BlockItem(FLUID_CONTAINER, new Item.Settings());

    public static final ComponentKey<BacComponent> BAC =
            ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "bac"), BacComponent.class);

    public static ItemGroup ITEM_GROUP;

    public static CustomFluid getFluid(String id) {
        return FLUIDS.getFluid(id);
    }


    @Override
    public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(this);

        FLUIDS = new CustomFluids();

        Registry.register(Registries.ITEM, new Identifier(MODID, "yeast"), YEAST);

        Registry.register(Registries.ITEM, new Identifier(MODID, "wine_bottle"), WINE_BOTTLE);
        Registry.register(Registries.ITEM, new Identifier(MODID, "shot_glass"), SHOT_GLASS);
        Registry.register(Registries.ITEM, new Identifier(MODID, "liquor_bottle"), LIQUOR_BOTTLE);

        Registry.register(Registries.BLOCK, new Identifier(MODID, "fluid_container"), FLUID_CONTAINER);
        Registry.register(Registries.ITEM, new Identifier(MODID, "fluid_container"), FLUID_CONTAINER_ITEM);

        ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, new Identifier("alcoholism", "group"), FabricItemGroup.builder()
                .icon(() -> new ItemStack(Items.BUCKET))
                .displayName(Text.translatable("itemGroup.alcoholism.group"))
                .entries((context, entries) -> {
                    entries.add(YEAST);
                    entries.add(WINE_BOTTLE);
                    entries.add(SHOT_GLASS);
                    entries.add(FLUID_CONTAINER_ITEM);
                    for (CustomBucket bucket : FLUIDS.getBuckets()) {
                        entries.add(bucket);
                    }
                })
                .build());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("rtbac")
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
                                    assert player != null;
                                    BAC.get(player).setRtBac(value);
                                    s.sendFeedback(() -> Text.literal("Set BAC to " + value), false);
                                    return 1;
                                })
                        )
                ).then(CommandManager.literal("get")
                        .executes(context -> {
                            ServerCommandSource s = (ServerCommandSource) context.getSource();
                            ServerPlayerEntity player = s.getPlayer();
                            assert player != null;
                            float value = BAC.get(player).getRtBac();
                            s.sendFeedback(() -> Text.literal("BAC is " + value), false);
                            return 1;
                        })
                )
        ));
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(BAC, BacComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }

    private void effectMan(EntityAttributeInstance effect, float modifierValue, String name) {
        boolean hasModifier = false;
        for (EntityAttributeModifier modifier : effect.getModifiers()) {
            if (modifier.getName().equals(name)) {
                if (modifier.getValue() != modifierValue) {
                    effect.removeModifier(modifier);
                } else {
                    if (hasModifier) {
                        effect.removeModifier(modifier);
                    }
                    hasModifier = true;
                }
            }
        }
        if (!hasModifier) {
            effect.addTemporaryModifier(new EntityAttributeModifier(name, modifierValue, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }


    @Override
    public void onStartTick(MinecraftServer minecraftServer) {
        for (ServerPlayerEntity player : minecraftServer.getPlayerManager().getPlayerList()) {
            BAC.get(player).serverTick();
            float bac = BAC.get(player).getBac();
            // effect are here
            /*
0.00%: Sober
0.01%–0.06%: Typically tipsy or "buzzed"
0.05%: May feel uninhibited and have impaired judgment
0.08%: Legally intoxicated
0.10%–0.12%: May have slurred speech and obvious physical impairment
0.13%–0.15%: May have blurred vision, loss of balance and coordination, and anxiety or restlessness
0.16%–0.19%: May be described as "sloppy drunk" and may experience nausea
0.20%–0.29%: May feel confused, disoriented, and dazed, and may have difficulty walking
0.30%–0.39%: May experience alcohol poisoning, which can be life-threatening
0.40% and over: May result in coma or death from respiratory arrest
*/
            if (bac >= 0.35) {
                DamageSource ds = new DamageSource(player.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.GENERIC));
                // bac 0.35 -> 0.5
                // bac 0.4 -> 1
                float damage = 0.5f + (bac - 0.35f) * 1.5f / 0.15f;
                player.damage(ds, damage);
            }

            EntityAttributeInstance move_speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            EntityAttributeInstance attack_speed = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
            EntityAttributeInstance attack_damage = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            EntityAttributeInstance armor = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR);
            EntityAttributeInstance armor_toughness = player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
            EntityAttributeInstance knockback_resistance = player.getAttributeInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);

            assert move_speed != null;
            if (bac >= 0.08) {
                float modifierValue = -0.05f + (bac - 0.08f) * -0.45f / 0.17f;
                effectMan(move_speed, modifierValue, "alcoholism:drunk_slowness");
            } else {
                for (EntityAttributeModifier modifier : move_speed.getModifiers()) {
                    if (modifier.getName().equals("alcoholism:drunk_slowness")) {
                        move_speed.removeModifier(modifier);
                    }
                }
            }

            assert attack_speed != null;
            if (bac >= 0.08) {
                float modifierValue = -0.05f + (bac - 0.08f) * -0.45f / 0.17f;
                effectMan(attack_speed, modifierValue, "alcoholism:drunk_slowatt");
            } else {
                for (EntityAttributeModifier modifier : attack_speed.getModifiers()) {
                    if (modifier.getName().equals("alcoholism:drunk_slowatt")) {
                        attack_speed.removeModifier(modifier);
                    }
                }
            }

            assert attack_damage != null;
            if (bac >= 0.08) {
                float modifierValue = -0.05f + (bac - 0.08f) * -0.45f / 0.17f;
                effectMan(attack_damage, modifierValue, "alcoholism:drunk_weakatt");
            } else {
                for (EntityAttributeModifier modifier : attack_damage.getModifiers()) {
                    if (modifier.getName().equals("alcoholism:drunk_weakatt")) {
                        attack_damage.removeModifier(modifier);
                    }
                }
            }

            assert armor != null;
            if (bac >= 0.08) {
                float modifierValue = -0.05f + (bac - 0.08f) * -0.45f / 0.17f;
                effectMan(armor, modifierValue, "alcoholism:drunk_weakarmor");
            } else {
                for (EntityAttributeModifier modifier : armor.getModifiers()) {
                    if (modifier.getName().equals("alcoholism:drunk_weakarmor")) {
                        armor.removeModifier(modifier);
                    }
                }
            }

            assert armor_toughness != null;
            if (bac >= 0.08) {
                float modifierValue = -0.05f + (bac - 0.08f) * -0.45f / 0.17f;
                effectMan(armor_toughness, modifierValue, "alcoholism:drunk_weakarmor_toughness");
            } else {
                for (EntityAttributeModifier modifier : armor_toughness.getModifiers()) {
                    if (modifier.getName().equals("alcoholism:drunk_weakarmor_toughness")) {
                        armor_toughness.removeModifier(modifier);
                    }
                }
            }

            assert knockback_resistance != null;
            if (bac >= 0.08) {
                float modifierValue = -0.05f + (bac - 0.08f) * -0.45f / 0.17f;
                effectMan(knockback_resistance, modifierValue, "alcoholism:drunk_weakknockback");
            } else {
                for (EntityAttributeModifier modifier : knockback_resistance.getModifiers()) {
                    if (modifier.getName().equals("alcoholism:drunk_weakknockback")) {
                        knockback_resistance.removeModifier(modifier);
                    }
                }
            }
        }
    }
}
