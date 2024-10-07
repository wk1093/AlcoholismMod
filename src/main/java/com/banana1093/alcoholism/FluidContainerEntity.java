package com.banana1093.alcoholism;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class FluidContainerEntity extends BlockEntity {
    public String fluid="";
    private int amount=0;

    public static final int MAX_AMOUNT = 2000;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public FluidContainerEntity(BlockPos pos, BlockState state) {
        super(Alcoholism.FLUID_CONTAINER_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putString("fluid", fluid);
//        nbt.putInt("amount", amount);
        nbt.putInt("amount", amount);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        fluid = nbt.getString("fluid");
        amount = nbt.getInt("amount");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }


    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this, new Function<BlockEntity, NbtCompound>() {
            @Override
            public NbtCompound apply(BlockEntity blockEntity) {
                NbtCompound nbt = new NbtCompound();
                ((FluidContainerEntity)blockEntity).writeNbt(nbt);
                return nbt;
            }
        });
    }
}
