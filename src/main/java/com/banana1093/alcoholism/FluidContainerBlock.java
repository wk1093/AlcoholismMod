package com.banana1093.alcoholism;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import net.minecraft.registry.Registries;

import java.util.Objects;

public class FluidContainerBlock extends BlockWithEntity {
    public FluidContainerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new FluidContainerEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient){
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof FluidContainerEntity entity) {
                if (!player.getStackInHand(hand).isEmpty()) {
                    ItemStack stack = player.getStackInHand(hand);
                    String translationKey = stack.getTranslationKey();
                    if (entity.amount == 0) {
                        entity.fluid = "";
                    }
                    // if translation key starts with "item.alcoholism.bucket_"
                    // then we put the rest of the translation key (after "item.alcoholism.bucket_" part) into the fluid field of the entity
                    // and add to amount field by 1000 (1 bucket)
                    // if there is already a fluid in the entity, we add to the amount field by 1000 (1 bucket)
                    // unless it is a different fluid, then we do nothing

                    if (translationKey.startsWith("item.alcoholism.bucket_")) {
                        String fluid = translationKey.substring("item.alcoholism.bucket_".length());
                        if (entity.fluid == null) {
                            entity.fluid = fluid;
                        } else if (entity.fluid.isEmpty()) {
                            entity.fluid = fluid;
                        } else if (!entity.fluid.equals(fluid)) {
                            return ActionResult.FAIL;
                        }
                        entity.amount += 1000;
                        if (entity.amount > FluidContainerEntity.MAX_AMOUNT) {
                            entity.amount = FluidContainerEntity.MAX_AMOUNT;
                            return ActionResult.FAIL;
                        }

                        // turn into a normal bucket if not creative
                        if (!player.isCreative())
                            player.setStackInHand(hand, Objects.requireNonNull(player.getStackInHand(hand).getItem().getRecipeRemainder()).getDefaultStack());
                        // play bucket empty sound
//                        world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_BUCKET_EMPTY, player.getSoundCategory(), 1.0F, 1.0F);
                        world.emitGameEvent(player, GameEvent.FLUID_PLACE, pos);

                        return ActionResult.PASS;
                    } else if (stack.isOf(Items.BUCKET)) {
                        // fill empty bucket if we hve enough fluid
                        if (entity.amount >= 1000) {
                            entity.amount -= 1000;
                            String key = "alcoholism:bucket_" + entity.fluid;
                            ItemStack bucket = new ItemStack(Registries.ITEM.get(new Identifier(key)));
                            // if creative, don't give the player anything
                            // if survival, replace their empty bucket with the filled one
                            if (!player.isCreative()) {
                                player.setStackInHand(hand, bucket);
                            }
                            // play bucket fill sound
//                            world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_BUCKET_FILL, player.getSoundCategory(), 1.0F, 1.0F);
                            world.emitGameEvent(player, GameEvent.FLUID_PICKUP, pos);
                        }
                    }
                    // if the item inherits/is a Bottle class
                    else if (stack.getItem() instanceof Bottle bottle) {
                        // if the entity has enough fluid to fill the bottle
                        int bottleAmount = 0;
                        String bottleFluid = "";
                        if (stack.hasNbt()) {
                            assert stack.getNbt() != null;
                            bottleAmount = stack.getNbt().getInt("amount");
                            bottleFluid = stack.getNbt().getString("fluid");
                        }
                        if (bottleAmount == 0) {
                            bottleFluid = entity.fluid;
                            if (entity.amount >= bottle.MAX_AMOUNT) {
                                bottleAmount = bottle.MAX_AMOUNT;
                                entity.amount -= bottle.MAX_AMOUNT;
                            } else {
                                bottleAmount = entity.amount;
                                entity.amount = 0;
                            }
                        } else if (bottleFluid.equals(entity.fluid)) {
                            if (entity.amount >= bottle.MAX_AMOUNT - bottleAmount) {
                                int remaining = bottle.MAX_AMOUNT - bottleAmount;
                                bottleAmount = bottle.MAX_AMOUNT;
                                entity.amount -= remaining;
                            }
                        } else {
                            return ActionResult.FAIL;
                        }
                        stack.getOrCreateNbt().putString("fluid", bottleFluid);
                        stack.getOrCreateNbt().putInt("amount", bottleAmount);
                    }


                    if (entity.amount == 0) {
                        entity.fluid = "";
                    }
                }
            }
        }

        return ActionResult.SUCCESS;
    }
}
