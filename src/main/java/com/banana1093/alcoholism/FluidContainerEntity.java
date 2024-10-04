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

public class FluidContainerEntity extends BlockEntity {
    public String fluid="";
    public int amount=0;

    public static final int MAX_AMOUNT = 2000;


    public FluidContainerEntity(BlockPos pos, BlockState state) {
        super(Alcoholism.FLUID_CONTAINER_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putString("fluid", fluid);
        nbt.putInt("amount", amount);
        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        fluid = nbt.getString("fluid");
        amount = nbt.getInt("amount");
    }

//    @Nullable
//    @Override
//    public Packet<ClientPlayPacketListener> toUpdatePacket() {
//        return BlockEntityUpdateS2CPacket.create(this);
//    }
//
//    @Override
//    public NbtCompound toInitialChunkDataNbt() {
//        return createNbt();
//    }
}
