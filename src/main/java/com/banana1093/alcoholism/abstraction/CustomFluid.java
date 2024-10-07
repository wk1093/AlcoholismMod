package com.banana1093.alcoholism.abstraction;

import com.banana1093.alcoholism.Alcoholism;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.util.math.Direction;

import java.util.Optional;

public abstract class CustomFluid extends FlowableFluid {
    private final int COLOR;
    private final float ALCOHOL_CONTENT;
    public Fluid STILL;
    public Fluid FLOWING;
    public Item BUCKET;
    public Block BLOCK;


    public int getColor() {
        return COLOR;
    }

    public float getAlcoholContent() {
        return ALCOHOL_CONTENT;
    }

    @Override
    public Fluid getStill() {
        return STILL;
    }

    @Override
    public Fluid getFlowing() {
        return FLOWING;
    }

    @Override
    public Item getBucketItem() {
        return BUCKET;
    }

    @Override
    protected BlockState toBlockState(FluidState fluidState) {
        return BLOCK.getDefaultState().with(Properties.LEVEL_15, getBlockStateLevel(fluidState));
    }

    public CustomFluid(int color, float alcoholContent, Fluid still, Fluid flowing, Item bucket, Block block) {
        super();
        this.COLOR = color;
        this.ALCOHOL_CONTENT = alcoholContent;
        this.STILL = still;
        this.FLOWING = flowing;
        this.BUCKET = bucket;
        this.BLOCK = block;
        if (this.STILL == null) this.STILL = this;
        if (this.FLOWING == null) this.FLOWING = this;
    }

    public CustomFluid(int color, float alc) {
        this(color, alc, null, null, null, null);
    }

    /**
     * @return whether the given fluid an instance of this fluid
     */
    @Override
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }

    /**
     * @return whether the fluid is infinite (which means can be infinitely created like water). In vanilla, it depends on the game rule.
     */
    @Override
    protected boolean isInfinite(World world) {
        return false;
    }

    @Override
    public Optional<SoundEvent> getBucketFillSound() {
        return Optional.of(SoundEvents.ITEM_BUCKET_FILL);
    }

    /**
     * Perform actions when the fluid flows into a replaceable block. Water drops
     * the block's loot table. Lava plays the "block.lava.extinguish" sound.
     */
    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        final BlockEntity blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

    /**
     * Lava returns true if it's FluidState is above a certain height and the
     * Fluid is Water.
     *
     * @return whether the given Fluid can flow into this FluidState
     */
    protected boolean canBeReplacedWith(FluidState fluidState, BlockView blockView, BlockPos blockPos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    protected int getLevelDecreasePerBlock(WorldView worldView) {
        return 3;
    }

    @Override
    protected int getFlowSpeed(WorldView worldView) {
        return 3;
    }

    /**
     * Water returns 5. Lava returns 30 in the Overworld and 10 in the Nether.
     */
    @Override
    public int getTickRate(WorldView worldView) {
        return 5;
    }

    /**
     * Water and Lava both return 100.0F.
     */
    @Override
    protected float getBlastResistance() {
        return 100.0F;
    }


    @Override
    public int getLevel(FluidState fluidState) {
        return fluidState.get(LEVEL);
    }

    @Override
    public boolean isStill(FluidState fluidState) {
        return false;
    }

    public static class Flowing extends CustomFluid {
        public Flowing(int color, float alcoholContent, Fluid still, Fluid flowing, Item bucket, Block block) {
            super(color, alcoholContent, still, null, bucket, block);
        }

        public Flowing(int color, float alc) {
            super(color, alc, null, null, null, null);
        }

        @Override
        protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
            super.appendProperties(builder);
            builder.add(LEVEL);
        }
    }

    public static class Still extends CustomFluid {
        public Still(int color, float alcoholContent, Fluid still, Fluid flowing, Item bucket, Block block) {
            super(color, alcoholContent, null, flowing, bucket, block);
        }

        public Still(int color, float alc) {
            super(color, alc, null, null, null, null);
        }

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