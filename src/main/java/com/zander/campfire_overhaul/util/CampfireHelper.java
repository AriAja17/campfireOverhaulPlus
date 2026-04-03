package com.zander.campfire_overhaul.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public class CampfireHelper {
    public static boolean isSoul(BlockState state) {
        String blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
        return blockId.contains("soul") || blockId.contains("ender");
    }
}