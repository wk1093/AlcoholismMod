package com.banana1093.alcoholism.cardinal;

import com.banana1093.alcoholism.Alcoholism;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

public class SyncedFloatComponent implements FloatComponent, AutoSyncedComponent {
    private float value = 0;
    private final Entity provider;

    public SyncedFloatComponent(Entity provider) {
        this.provider = provider;
    }

    public void setValue(float value) {
        this.value = value;
        Alcoholism.BAC.sync(provider);
    }


    public float getValue() {
        return value;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        value = tag.getFloat("value");

    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putFloat("value", value);
    }
}
