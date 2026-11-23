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
 * GT-02 CAP1000 profile. Covers manual scenarios C1, C5 (plus the fuel half of
 * A3), C6 and C7. No ignition, save/reload or persistence is automated here.
 */
@GameTestHolder("campfire_overhaul_gt02_cap1000")
@PrefixGameTestTemplate(false)
public class CampfireFuelCap1000GameTests {

    private static final String TEMPLATE = "gt02_cap1000";
    private static final String NAMESPACE = "campfire_overhaul_gt02_cap1000";
    private static final ProfileValues PROFILE = new ProfileValues("CAP1000", false, 1000, 1, 2000);

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void cap1000ProfileGuard(GameTestHelper helper) {
        CampfireGameTestSupport.assertEnabledNamespace(helper, NAMESPACE);
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireGameTestSupport.emitProfileEvidence("CAP1000");
        helper.succeed();
    }

    /** C1: 100 + min(coal 1600 x multiplier 1, capacity 900) = 1000. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void normalCoalClampedToSmallMaximum(GameTestHelper helper) {
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireFixture fixture = CampfireGameTestSupport.placeCampfire(helper);
        helper.assertTrue(fixture.campfire().getLifeTime() == 2000,
                "Expected placed lifetime 2000 before setup but found " + fixture.campfire().getLifeTime());
        fixture.campfire().setLifeTime(100);
        helper.assertTrue(fixture.campfire().getLifeTime() == 100,
                "Expected setup lifetime 100 but found " + fixture.campfire().getLifeTime());

        ArmorStand thrower = CampfireGameTestSupport.createRegisteredThrower(helper);
        ItemEntity coal = CampfireGameTestSupport.spawnFuel(helper, fixture.pos(), thrower,
                new ItemStack(Items.COAL), 200, false);

        helper.succeedWhen(() -> {
            CampfireGameTestSupport.assertIngested(
                    helper, fixture, coal, thrower, 1000, 0, 1000,
                    "100 + min(coal 1600 x multiplier 1, capacity 900) = 1000");
            CampfireGameTestSupport.emitScenarioEvidence("C1", "normalCoalClampedToSmallMaximum", "CAP1000",
                    "lifetime=1000; expectedMax=1000; fuelConsumed=true; fuelDeregistered=true",
                    "lifetime=" + fixture.campfire().getLifeTime()
                            + "; expectedMax=" + CampfireOverhaulConfig.CAMPFIRE_MAX_LIFE_TIME.get()
                            + "; fuelConsumed=" + !coal.isAlive()
                            + "; fuelDeregistered=" + (helper.getLevel().getEntity(coal.getUUID()) == null),
                    "profileGuard; setupLifetime=100; assertIngested[owner,range,exact,alive,deregistered]");
        });
    }

    /** C5 and the fuel-ingestion portion of A3: 100 + min(20000, 900) = 1000. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void lavaBucketWithAvailableCapacity(GameTestHelper helper) {
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireFixture fixture = CampfireGameTestSupport.placeCampfire(helper);
        fixture.campfire().setLifeTime(100);
        helper.assertTrue(fixture.campfire().getLifeTime() == 100,
                "Expected setup lifetime 100 but found " + fixture.campfire().getLifeTime());

        helper.assertItemEntityCountIs(Items.BUCKET, fixture.pos(), 3.0D, 0);
        ArmorStand thrower = CampfireGameTestSupport.createRegisteredThrower(helper);
        ItemEntity lavaBucket = CampfireGameTestSupport.spawnFuel(helper, fixture.pos(), thrower,
                new ItemStack(Items.LAVA_BUCKET), 200, false);

        helper.succeedWhen(() -> {
            CampfireGameTestSupport.assertIngested(
                    helper, fixture, lavaBucket, thrower, 1000, 0, 1000,
                    "100 + min(lava bucket 20000 x multiplier 1, capacity 900) = 1000");
            helper.assertItemEntityCountIs(Items.BUCKET, fixture.pos(), 3.0D, 1);
            helper.assertItemEntityNotPresent(Items.LAVA_BUCKET, fixture.pos(), 3.0D);
            CampfireGameTestSupport.emitScenarioEvidence("C5", "lavaBucketWithAvailableCapacity", "CAP1000",
                    "lifetime=1000; emptyBuckets=1; lavaBuckets=0; fuelConsumed=true",
                    "lifetime=" + fixture.campfire().getLifeTime()
                            + "; emptyBuckets=" + CampfireGameTestSupport.countItemEntities(helper, fixture.pos(), Items.BUCKET, 3.0D)
                            + "; lavaBuckets=" + CampfireGameTestSupport.countItemEntities(helper, fixture.pos(), Items.LAVA_BUCKET, 3.0D)
                            + "; fuelConsumed=" + !lavaBucket.isAlive(),
                    "profileGuard; setupLifetime=100; assertIngested; assertItemEntityCountIs(BUCKET,1); assertItemEntityNotPresent(LAVA_BUCKET)");
        });
    }

    /** C6: a campfire already at max rejects fuel without consuming the item. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void fullCapacityRejectsFuel(GameTestHelper helper) {
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireFixture fixture = CampfireGameTestSupport.placeCampfire(helper);
        fixture.campfire().setLifeTime(1000);
        helper.assertTrue(fixture.campfire().getLifeTime() == 1000,
                "Expected full-capacity setup lifetime 1000 but found " + fixture.campfire().getLifeTime());

        ArmorStand thrower = CampfireGameTestSupport.createRegisteredThrower(helper);
        ItemEntity coal = CampfireGameTestSupport.spawnFuel(helper, fixture.pos(), thrower,
                new ItemStack(Items.COAL), -1, true);

        CampfireGameTestSupport.expectPreserved(helper, fixture, coal, thrower, 1000,
                "full capacity rejects fuel", () ->
                        CampfireGameTestSupport.emitScenarioEvidence("C6", "fullCapacityRejectsFuel", "CAP1000",
                                "lifetime=1000; itemCountPreserved=true; fuelAlive=true; fuelRegistered=true; ownerOk=true",
                                "lifetime=" + fixture.campfire().getLifeTime()
                                        + "; itemCount=" + coal.getItem().getCount()
                                        + "; fuelAlive=" + coal.isAlive()
                                        + "; fuelRegistered=" + (helper.getLevel().getEntity(coal.getUUID()) == coal)
                                        + "; ownerOk=" + (coal.getOwner() == thrower),
                                "profileGuard; setupLifetime=1000; expectPreserved[position,owner,lifetime,alive,registered,stackCount]"));
    }

    /** C7: STICK is 100 ticks; 16 sticks clamped to the 900-tick capacity. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void stackedFuelCountApplied(GameTestHelper helper) {
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireFixture fixture = CampfireGameTestSupport.placeCampfire(helper);
        fixture.campfire().setLifeTime(100);
        helper.assertTrue(fixture.campfire().getLifeTime() == 100,
                "Expected setup lifetime 100 but found " + fixture.campfire().getLifeTime());

        ArmorStand thrower = CampfireGameTestSupport.createRegisteredThrower(helper);
        ItemEntity sticks = CampfireGameTestSupport.spawnFuel(helper, fixture.pos(), thrower,
                new ItemStack(Items.STICK, 16), 200, false);
        helper.assertTrue(sticks.getItem().getCount() == 16,
                "Expected a 16-stick fuel stack but found " + sticks.getItem().getCount());

        int inputCount = sticks.getItem().getCount();
        helper.succeedWhen(() -> {
            CampfireGameTestSupport.assertIngested(
                    helper, fixture, sticks, thrower, 1000, 0, 1000,
                    "100 + clamp(STICK 100 x 16, capacity 900) = 1000");
            CampfireGameTestSupport.emitScenarioEvidence("C7", "stackedFuelCountApplied", "CAP1000",
                    "inputCount=16; lifetime=1000; fuelConsumed=true",
                    "inputCount=" + inputCount
                            + "; lifetime=" + fixture.campfire().getLifeTime()
                            + "; fuelConsumed=" + !sticks.isAlive(),
                    "profileGuard; setupLifetime=100; stackCountPrecheck; assertIngested[owner,range,exact,alive,deregistered]");
        });
    }
}
