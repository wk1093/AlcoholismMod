package com.banana1093.alcoholism.cardinal;

import com.banana1093.alcoholism.Alcoholism;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;

public class BacComponent implements ComponentV3, AutoSyncedComponent {
    // rtbac is the value determined by the amount of alcohol consumed (it instantly increases when the player drinks alcohol)
    // bac is the value that is displayed on the screen and used for effects (it eases towards rtbac over time)

    private float rtbac = 0;
    private float bac = 0;
    private final Entity provider;

    public BacComponent(Entity provider) {
        this.provider = provider;
    }

    public void setRtBac(float value) {
        this.rtbac = value;
        Alcoholism.BAC.sync(provider);
    }


    public float getRtBac() {
        return rtbac;
    }

    public float getBac() {
        return bac;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        rtbac = tag.getFloat("rtbac");
        bac = tag.getFloat("bac");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putFloat("rtbac", rtbac);
        tag.putFloat("bac", bac);
    }

    public void serverTick() {
        if (rtbac > 0) {
            rtbac -= 0.000001f;
        }
        // if the bac is far from the rtbac, move it closer depending on how far it is
        // when it is super close, just set it to the rtbac
        if (Math.abs(bac - rtbac) > 0.001) {
            bac += (rtbac - bac) / 100;
        } else {
            bac = rtbac;
        }
        Alcoholism.BAC.sync(provider);
    }
}
