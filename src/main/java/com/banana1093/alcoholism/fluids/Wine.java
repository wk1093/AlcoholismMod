package com.banana1093.alcoholism.fluids;

import com.banana1093.alcoholism.Alcoholism;
import com.banana1093.alcoholism.CustomFluid;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.world.WorldView;

public abstract class Wine extends CustomFluid {
    public static final int COLOR = 0x9c1138;
    public static final float ALCOHOL_CONTENT = 0.2f;

    @Override
    public int getColor() {
        return COLOR;
    }

    @Override
    public float getAlcoholContent() {
        return ALCOHOL_CONTENT;
    }

    @Override
    public Fluid getStill() {
        return Alcoholism.STILL_WINE;
    }

    @Override
    public Fluid getFlowing() {
        return Alcoholism.FLOWING_WINE;
    }

    @Override
    public Item getBucketItem() {
        return Alcoholism.BUCKET_WINE;
    }

    @Override
    protected BlockState toBlockState(FluidState fluidState) {
        return Alcoholism.WINE.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(fluidState));
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView worldView) {
        return 3;
    }

    @Override
    protected int getFlowSpeed(WorldView worldView) {
        return 3;
    }

    public static class Flowing extends Wine {
        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getLevel(FluidState fluidState) {
            return fluidState.get(LEVEL);
        }

        @Override
        public boolean isStill(FluidState fluidState) {
            return false;
        }
    }

    public static class Still extends Wine {
        @Override
        public int getLevel(FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isStill(FluidState fluidState) {
            return true;
        }
    }
}