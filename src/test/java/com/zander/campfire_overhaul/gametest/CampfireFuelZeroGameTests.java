package com.zander.campfire_overhaul.gametest;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.gametest.CampfireGameTestSupport.CampfireFixture;
import com.zander.campfire_overhaul.gametest.CampfireGameTestSupport.ProfileValues;
import com.zander.campfire_overhaul.util.ICampfireExtra;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** GT-02 ZERO profile. Covers manual scenario C2 (zero capacity rejection). */
@GameTestHolder("campfire_overhaul_gt02_zero")
@PrefixGameTestTemplate(false)
public class CampfireFuelZeroGameTests {

    private static final String TEMPLATE = "gt02_zero";
    private static final String NAMESPACE = "campfire_overhaul_gt02_zero";
    private static final ProfileValues PROFILE = new ProfileValues("ZERO", false, 0, 1, 2000);

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void zeroProfileGuard(GameTestHelper helper) {
        CampfireGameTestSupport.assertEnabledNamespace(helper, NAMESPACE);
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireGameTestSupport.emitProfileEvidence("ZERO");
        helper.succeed();
    }

    /** C2: capacity 0 rejects fuel; the coal must remain alive and owned. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void zeroCapacityRejectsFuel(GameTestHelper helper) {
        CampfireGameTestSupport.assertProfile(helper, PROFILE);
        CampfireFixture fixture = CampfireGameTestSupport.placeCampfire(helper);
        fixture.campfire().setLifeTime(0);
        int lifetime = fixture.campfire().getLifeTime();
        helper.assertTrue(lifetime == 0
                        && lifetime != ICampfireExtra.INFINITE_LIFE_TIME
                        && lifetime != ICampfireExtra.UNINITIALIZED_LIFE_TIME,
                "Setup lifetime must be exactly 0 but found " + lifetime);

        ArmorStand thrower = CampfireGameTestSupport.createRegisteredThrower(helper);
        ItemEntity coal = CampfireGameTestSupport.spawnFuel(helper, fixture.pos(), thrower,
                new ItemStack(Items.COAL), -1, true);

        int originalCount = coal.getItem().getCount();
        CampfireGameTestSupport.expectPreserved(helper, fixture, coal, thrower, 0,
                "zero capacity rejects fuel", () ->
                        CampfireGameTestSupport.emitScenarioEvidence("C2", "zeroCapacityRejectsFuel", "ZERO",
                                "lifetime=0; maxLifeTime=0; itemCountPreserved=true; fuelAlive=true; fuelRegistered=true; ownerOk=true",
                                "lifetime=" + fixture.campfire().getLifeTime()
                                        + "; maxLifeTime=" + CampfireOverhaulConfig.CAMPFIRE_MAX_LIFE_TIME.get()
                                        + "; originalCount=" + originalCount
                                        + "; itemCount=" + coal.getItem().getCount()
                                        + "; fuelAlive=" + coal.isAlive()
                                        + "; fuelRegistered=" + (helper.getLevel().getEntity(coal.getUUID()) == coal)
                                        + "; ownerOk=" + (coal.getOwner() == thrower),
                                "profileGuard; setupLifetime=0; expectPreserved[position,owner,lifetime,alive,registered,stackCount]"));
    }
}
