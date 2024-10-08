package com.banana1093.alcoholism.abstraction;

import com.banana1093.alcoholism.Alcoholism;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
        if (!stack.hasNbt() || stack.getOrCreateNbt().getString("fluid").isEmpty()) {
            stack.getOrCreateNbt().putString("fluid", "empty");
            assert stack.getNbt() != null;
            stack.getNbt().putInt("amount", 0);
        }

        if (stack.getNbt().getString("fluid").equals("empty")) {
            tooltip.add(Text.of("§7Empty / "+ MAX_AMOUNT + "mB"));
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
        CustomFluid customFluid = Alcoholism.getFluid(fluid);
        if (customFluid == null) {
            return 0;
        }
        return customFluid.getColor();
    }

    public static float getAlcoholContent(String fluid) {
        // translatable fluid name to alcohol content
        CustomFluid customFluid = Alcoholism.getFluid(fluid);
        if (customFluid == null) {
            return 0;
        }
        return customFluid.getAlcoholContent();
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
            float bac = Alcoholism.BAC.get(spe).getRtBac();
            bac += ((ozConsumed * drinkABV * 4.05546f) / (146));
            Alcoholism.BAC.get(spe).setRtBac(bac);
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
        return MAX_AMOUNT / 15 + 15;
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

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (!stack.hasNbt()) {
            stack.getOrCreateNbt().putString("fluid", "empty");
            stack.getOrCreateNbt().putInt("amount", 0);
        }
        NbtCompound nbt = stack.getNbt();
        assert nbt != null;
        if (!nbt.contains("amount")) {
            nbt.putInt("amount", 0);
        }
        if (nbt.getInt("amount") > this.MAX_AMOUNT) {
            nbt.putInt("amount", this.MAX_AMOUNT);
        }
        if (nbt.getInt("amount") < 0) {
            nbt.putInt("amount", 0);
        }
        if (!nbt.contains("fluid")) {
            nbt.putString("fluid", "empty");
        }
        if (nbt.getString("fluid").equals("empty") && nbt.getInt("amount") > 0) {
            nbt.putInt("amount", 0);
        }
        if (nbt.getInt("amount") == 0) {
            nbt.putString("fluid", "empty");
        }
        // if we have an invalid fluid, set it to empty 0
        if (!nbt.getString("fluid").equals("empty")) {
            if (Alcoholism.getFluid(nbt.getString("fluid")) == null) {
                nbt.putString("fluid", "empty");
                nbt.putInt("amount", 0);
            }
        }

        // if it is empty set custom_model_data to 1
        // else 0
        if (nbt.getString("fluid").equals("empty")) {
            nbt.putInt("CustomModelData", 1);
        } else {
            nbt.putInt("CustomModelData", 0);
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        if (!stack.hasNbt()) {
            stack.getOrCreateNbt().putString("fluid", "empty");
            assert stack.getNbt() != null;
            stack.getNbt().putInt("amount", 0);
        }
        assert stack.getNbt() != null;
        return stack.getNbt().getInt("amount") * 13 / this.MAX_AMOUNT;
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        if (!stack.hasNbt()) {
            stack.getOrCreateNbt().putString("fluid", "empty");
            assert stack.getNbt() != null;
            stack.getNbt().putInt("amount", 0);
        }
        assert stack.getNbt() != null;
        return getColorOfFluid(stack.getNbt().getString("fluid"));
    }


}
