package com.zander.campfire_overhaul.gametest;

import com.zander.campfire_overhaul.CampfireOverhaul;
import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.util.ICampfireExtra;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(CampfireOverhaul.MOD_ID)
@PrefixGameTestTemplate(false)
public class CampfireLifetimeGameTests {

    @GameTest(template = "gt_01_lifetime_initialization", timeoutTicks = 200)
    public static void normalCampfireUsesConfiguredInitialLifetime(GameTestHelper helper) {
        Player player;
        try {
            player = helper.makeMockServerPlayerInLevel();
        } catch (RuntimeException ignored) {
            // Forge 47.3.0's makeMockServerPlayerInLevel() throws a NullPointerException
            // in NetworkFilters.injectIfNecessary() because its dummy Connection has no
            // channel. It fails before registering the player. Fall back to the public
            // mock player; the placement-event path under test does not require a player
            // registered in the player list.
            player = helper.makeMockPlayer();
        }
        player.getAbilities().mayBuild = true;

        BlockPos support = new BlockPos(1, 1, 1);
        helper.setBlock(support, Blocks.STONE.defaultBlockState());
        helper.setBlock(support.above(), Blocks.STONE.defaultBlockState());

        ItemStack campfireStack = new ItemStack(Items.CAMPFIRE);
        player.setItemInHand(InteractionHand.MAIN_HAND, campfireStack);
        helper.placeAt(player, campfireStack, support, Direction.UP);

        BlockPos campfirePos = null;
        for (int x = 0; x <= 2 && campfirePos == null; x++) {
            for (int y = 0; y <= 4 && campfirePos == null; y++) {
                for (int z = 0; z <= 2 && campfirePos == null; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (helper.getBlockState(candidate).getBlock() instanceof CampfireBlock) {
                        campfirePos = candidate;
                    }
                }
            }
        }
        helper.assertTrue(campfirePos != null, "Campfire was not placed by placeAt");

        BlockEntity blockEntity = helper.getBlockEntity(campfirePos);
        helper.assertTrue(blockEntity instanceof ICampfireExtra,
                "Campfire BlockEntity does not implement ICampfireExtra at " + campfirePos);

        helper.assertFalse(CampfireOverhaulConfig.CAMPFIRE_INFINITE_LIFE_TIME.get(),
                "GT-01 requires a finite normal-campfire configuration profile");

        int expected = CampfireOverhaulConfig.CAMPFIRE_DEFAULT_LIFE_TIME.get();
        int actual = ((ICampfireExtra) blockEntity).getLifeTime();

        helper.assertTrue(actual != ICampfireExtra.INFINITE_LIFE_TIME
                        && actual != ICampfireExtra.UNINITIALIZED_LIFE_TIME,
                "Initial lifetime must be a resolved value, but was " + actual);
        helper.assertTrue(actual == expected,
                "Expected initial lifetime " + expected + " but found " + actual);

        helper.succeed();
    }
}
