package com.banana1093.alcoholism;

import com.banana1093.alcoholism.fluids.DilEth10;
import com.banana1093.alcoholism.fluids.Wine;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

// Able to contain a custom fluid, and amount of that fluid
public class Bottle extends Item {
    public final int MAX_AMOUNT;

    public Bottle(Settings settings, int maxAmount) {
        super(settings);
        this.MAX_AMOUNT = maxAmount;
    }

    public ItemStack getEmptyStack() {
        return new ItemStack(this);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        // check if the stack has the required tags/NBT data, if not add them with default values
        if (!stack.hasNbt()) {
            stack.getOrCreateNbt().putString("fluid", "empty");
            assert stack.getNbt() != null;
            stack.getNbt().putInt("amount", 0);
        }

        if (stack.getNbt().getString("fluid").equals("empty")) {
            tooltip.add(Text.of("§7Empty"));
            if (stack.getNbt().getInt("amount") > 0) {
                stack.getNbt().putInt("amount", 0);
            }
        } else {
//            tooltip.add(Text.of(stack.getNbt().getInt("amount") + "/" + MAX_AMOUNT + "mB of " + stack.getNbt().getString("fluid")));
            // color it and make it fancy with a progress bar
            String name = stack.getNbt().getString("fluid"); // get translation of block.alcoholism.NAME
            String translatedName = Text.translatable("block.alcoholism."+name).getString();
            if (stack.getNbt().getInt("amount") > this.MAX_AMOUNT) {
                stack.getNbt().putInt("amount", this.MAX_AMOUNT);
            }
            tooltip.add(Text.of("§7" + stack.getNbt().getInt("amount") + "/" + MAX_AMOUNT + "mB of " + translatedName));
        }
    }

    public static int getColorOfFluid(String fluid) {
        // translatable fluid name to color
        switch (fluid) {
            case "dileth10":
                return DilEth10.COLOR;
            case "wine":
                return Wine.COLOR;
            default:
                return 0xFFFFFF;
        }
    }

    public static float getAlcoholContent(String fluid) {
        // translatable fluid name to alcohol content
        switch (fluid) {
            case "dileth10":
                return DilEth10.ALCOHOL_CONTENT;
            case "wine":
                return Wine.ALCOHOL_CONTENT;
            default:
                return 0;
        }
    }

    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity)user : null;
        if (playerEntity instanceof ServerPlayerEntity spe) {
            Criteria.CONSUME_ITEM.trigger(spe, stack);
            assert stack.getNbt() != null;

            // playerBAC += ((ozConsumed * drinkABV * 4.05546) / (146));
            if (stack.getNbt().getInt("amount") > this.MAX_AMOUNT) {
                stack.getNbt().putInt("amount", this.MAX_AMOUNT);
            }
            float ozConsumed = stack.getNbt().getInt("amount") / 29.5735f;
            float drinkABV = getAlcoholContent(stack.getNbt().getString("fluid"));
            float bac = Alcoholism.BAC.get(spe).getValue();
            bac += ((ozConsumed * drinkABV * 4.05546f) / (146));
            Alcoholism.BAC.get(spe).setValue(bac);
        }

        if (playerEntity != null) {
            playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
            if (!playerEntity.getAbilities().creativeMode) {
                stack.decrement(1);
            }
        }

        if (playerEntity == null || !playerEntity.getAbilities().creativeMode) {
            if (stack.isEmpty()) {
                return this.getEmptyStack();
            }

            if (playerEntity != null) {
                playerEntity.getInventory().insertStack(this.getEmptyStack());
            }
        }

        user.emitGameEvent(GameEvent.DRINK);
        return stack;
    }

    public int getMaxUseTime(ItemStack stack) {
        return 32;
    }

    public UseAction getUseAction(ItemStack stack) {
        if (!stack.hasNbt()) {
            stack.getOrCreateNbt().putString("fluid", "empty");
            assert stack.getNbt() != null;
            stack.getNbt().putInt("amount", 0);
        }
        if (stack.getNbt().getString("fluid").equals("empty") || stack.getNbt().getInt("amount") == 0) {
            return UseAction.NONE;
        }
        return UseAction.DRINK;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return ItemUsage.consumeHeldItem(world, user, hand);
    }
}
