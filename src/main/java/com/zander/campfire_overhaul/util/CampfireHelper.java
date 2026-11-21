package com.zander.campfire_overhaul.util;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class CampfireHelper {
    public static boolean isSoul(BlockState state) {
        String blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
        return blockId.contains("soul") || blockId.contains("ender");
    }

    public static int getInitialLifeTime(BlockState state) {
        if (isSoul(state)) {
            return CampfireOverhaulConfig.SOUL_CAMPFIRE_INFINITE_LIFE_TIME.get()
                    ? ICampfireExtra.INFINITE_LIFE_TIME
                    : CampfireOverhaulConfig.SOUL_CAMPFIRE_DEFAULT_LIFE_TIME.get();
        } else {
            return CampfireOverhaulConfig.CAMPFIRE_INFINITE_LIFE_TIME.get()
                    ? ICampfireExtra.INFINITE_LIFE_TIME
                    : CampfireOverhaulConfig.CAMPFIRE_DEFAULT_LIFE_TIME.get();
        }
    }
}