package com.zander.campfire_overhaul.util;

public interface ICampfireExtra {
    int getLifeTime();
    void addLifeTime(int add);
    void setLifeTime(int set);

    /** Countdown-only mutation with the approved 20-server-tick dirty cadence. */
    void decrementLifeTime();

    void campfireOverhaul_extinguish();
    void campfireOverhaul_break();
}