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

/** GT-02 OVERFLOW profile. Covers manual scenario C3 (saturation at the cap). */
@GameTestHolder("campfire_overhaul_gt02_overflow")
@PrefixGameTestTemplate(false)
public class CampfireFuelOverflowGameTests {

    private static final String TEMPLATE = "gt02_overflow";
    private static final String NAMESPACE = "campfire_overhaul_gt02_overflow";
    private static final ProfileValues PROFILE =
            new ProfileValues("OVERFLOW", false, 1000, 100000000, 2000);

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void overflowProfileGuard(GameTestHelper helper) {
        CampfireGameTestSupport.assertEnabledNamespace(helper, NAMESPACE);
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireGameTestSupport.emitProfileEvidence("OVERFLOW");
        helper.succeed();
    }

    /** C3: 100 + saturate(coal 1600 x 100000000, capacity 900) = 1000. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void largeMultiplierOverflowSaturated(GameTestHelper helper) {
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireFixture fixture = CampfireGameTestSupport.placeCampfire(helper);
        fixture.campfire().setLifeTime(100);
        helper.assertTrue(fixture.campfire().getLifeTime() == 100,
                "Expected setup lifetime 100 but found " + fixture.campfire().getLifeTime());

        ArmorStand thrower = CampfireGameTestSupport.createRegisteredThrower(helper);
        ItemEntity coal = CampfireGameTestSupport.spawnFuel(helper, fixture.pos(), thrower,
                new ItemStack(Items.COAL), 200, false);

        helper.succeedWhen(() -> {
            CampfireGameTestSupport.assertIngested(
                    helper, fixture, coal, thrower, 1000, 0, 1000,
                    "100 + saturate(coal 1600 x 100000000, capacity 900) = 1000");
            int observedLifetime = fixture.campfire().getLifeTime();
            int observedMax = CampfireOverhaulConfig.CAMPFIRE_MAX_LIFE_TIME.get();
            int observedMultiplier = CampfireOverhaulConfig.CAMPFIRE_FUEL_MULTIPLIER.get();
            CampfireGameTestSupport.emitScenarioEvidence("C3", "largeMultiplierOverflowSaturated", "OVERFLOW",
                    "lifetime=1000; maxLifeTime=1000; fuelMultiplier=100000000; lifetimeValid=true",
                    "lifetime=" + observedLifetime
                            + "; maxLifeTime=" + observedMax
                            + "; fuelMultiplier=" + observedMultiplier
                            + "; lifetimeValid=" + (observedLifetime >= 0 && observedLifetime <= observedMax),
                    "profileGuard; setupLifetime=100; assertIngested[owner,range,exact,alive,deregistered]");
        });
    }
}
