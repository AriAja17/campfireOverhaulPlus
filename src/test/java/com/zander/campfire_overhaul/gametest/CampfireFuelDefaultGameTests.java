package com.zander.campfire_overhaul.gametest;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.gametest.CampfireGameTestSupport.CampfireFixture;
import com.zander.campfire_overhaul.gametest.CampfireGameTestSupport.ProfileValues;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * GT-02 DEFAULT profile tests. This namespace is selected only by the
 * runGameTestServerGt02Default run configuration so no GT-01 test can be
 * registered in this process.
 */
@GameTestHolder("campfire_overhaul_gt02_default")
@PrefixGameTestTemplate(false)
public class CampfireFuelDefaultGameTests {

    private static final String TEMPLATE = "gt02_default";
    private static final String NAMESPACE = "campfire_overhaul_gt02_default";
    private static final ProfileValues PROFILE =
            new ProfileValues("DEFAULT", false, Integer.MAX_VALUE, 1, 2000);

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void defaultProfileGuard(GameTestHelper helper) {
        CampfireGameTestSupport.assertEnabledNamespace(helper, NAMESPACE);
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        helper.succeed();
    }

    /**
     * GT-02-01: a coal ItemEntity with a registered non-player thrower passes
     * through the vanilla entity collision pipeline; the production Mixin adds
     * 1600 ticks (coal burn time 1600 x multiplier 1) to a campfire set to 100.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void normalCoalIngestion(GameTestHelper helper) {
        CampfireGameTestSupport.assertProfile(helper, PROFILE);

        CampfireFixture fixture = CampfireGameTestSupport.placeCampfire(helper);

        int configuredDefault = CampfireOverhaulConfig.CAMPFIRE_DEFAULT_LIFE_TIME.get();
        helper.assertTrue(configuredDefault == 2000,
                "GT-02 DEFAULT expects campfireDefaultLifeTime=2000 but got " + configuredDefault);
        helper.assertTrue(fixture.campfire().getLifeTime() == configuredDefault,
                "Expected initial lifetime " + configuredDefault + " but found " + fixture.campfire().getLifeTime());

        fixture.campfire().setLifeTime(100);
        helper.assertTrue(fixture.campfire().getLifeTime() == 100,
                "Expected setup lifetime 100 but found " + fixture.campfire().getLifeTime());

        ArmorStand thrower = CampfireGameTestSupport.createRegisteredThrower(helper);
        ItemEntity coal = CampfireGameTestSupport.spawnFuel(helper, fixture.pos(), thrower,
                new ItemStack(Items.COAL), 200, false);

        helper.succeedWhen(() -> CampfireGameTestSupport.assertIngested(
                helper, fixture, coal, thrower, 1700, 0, Integer.MAX_VALUE,
                "100 + coal 1600 x multiplier 1"));
    }
}
