package com.zander.campfire_overhaul.util;

public interface ICampfireExtra {
    /** Infinite lifetime sentinel: the campfire never burns out. */
    int INFINITE_LIFE_TIME = -1337;

    /** Uninitialized lifetime sentinel: resolved on the first active cook tick. */
    int UNINITIALIZED_LIFE_TIME = -9999;

    int getLifeTime();
    void addLifeTime(int add);
    void setLifeTime(int set);

    /** Countdown-only mutation with the approved 20-server-tick dirty cadence. */
    void decrementLifeTime();

    void campfireOverhaul_extinguish();
    void campfireOverhaul_break();
}