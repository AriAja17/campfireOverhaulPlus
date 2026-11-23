package com.zander.campfire_overhaul.gametest;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.util.ICampfireExtra;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Test-only helpers shared by the GT-02 GameTest profile classes.
 *
 * <p>Only public Minecraft / Forge APIs are used. Nothing here calls
 * {@code CampfireBlock.entityInside}, a Mixin method, a production-private
 * method, or reflection. {@link ICampfireExtra#setLifeTime(int)} is used only to
 * prepare an independent fixture state; fuel ingestion is always exercised by
 * ticking a real, owned {@link ItemEntity}.
 */
public final class CampfireGameTestSupport {

    private static final Logger EVIDENCE_LOGGER = LoggerFactory.getLogger("campfire_overhaul.gametest");

    /** Per-JVM runtime identifier: each isolated GameTest profile process gets its own value. */
    private static final String RUN_ID = "run-" + UUID.randomUUID();

    private CampfireGameTestSupport() {
    }

    /**
     * Test-only structured evidence marker. Called only after all relevant
     * assertions in a scenario have already succeeded.
     */
    public static void emitScenarioEvidence(String scenario, String testMethod, String profile,
                                            String expected, String actual, String assertions) {
        EVIDENCE_LOGGER.info("[P3-T2-EVIDENCE] scenario={} result=PASS test={} profile={} runId={} expected={} actual={} assertions={}",
                scenario, testMethod, profile, RUN_ID, expected, actual, assertions);
    }

    /** Profile-guard marker. This is not a scenario PASS marker. */
    public static void emitProfileEvidence(String profile) {
        EVIDENCE_LOGGER.info("[P3-T2-PROFILE] kind=GUARD result=PASS profile={} runId={}", profile, RUN_ID);
    }

    /**
     * GT-03 burnout profile values. The four values below are the only keys the
     * GT-03 profile guard checks; all four must match at runtime.
     */
    public record BurnoutProfileValues(String label, boolean normalInfinite, boolean soulInfinite,
                                       boolean normalDestroyedOnBurnout, boolean soulDestroyedOnBurnout) {
    }

    /** Fail fast with key, expected and actual when the GT-03 burnout profile is wrong. */
    public static void assertBurnoutProfile(GameTestHelper helper, BurnoutProfileValues expected) {
        String label = expected.label();
        helper.assertTrue(CampfireOverhaulConfig.CAMPFIRE_INFINITE_LIFE_TIME.get() == expected.normalInfinite(),
                "GT-03 " + label + " profile mismatch for campfireInfiniteLifeTime: expected "
                        + expected.normalInfinite() + " but got "
                        + CampfireOverhaulConfig.CAMPFIRE_INFINITE_LIFE_TIME.get());
        helper.assertTrue(CampfireOverhaulConfig.SOUL_CAMPFIRE_INFINITE_LIFE_TIME.get() == expected.soulInfinite(),
                "GT-03 " + label + " profile mismatch for soulCampfireInfiniteLifeTime: expected "
                        + expected.soulInfinite() + " but got "
                        + CampfireOverhaulConfig.SOUL_CAMPFIRE_INFINITE_LIFE_TIME.get());
        helper.assertTrue(CampfireOverhaulConfig.CAMPFIRE_DESTROYED_ON_BURNOUT.get() == expected.normalDestroyedOnBurnout(),
                "GT-03 " + label + " profile mismatch for campfireDestroyedOnBurnout: expected "
                        + expected.normalDestroyedOnBurnout() + " but got "
                        + CampfireOverhaulConfig.CAMPFIRE_DESTROYED_ON_BURNOUT.get());
        helper.assertTrue(CampfireOverhaulConfig.SOUL_CAMPFIRE_DESTROYED_ON_BURNOUT.get() == expected.soulDestroyedOnBurnout(),
                "GT-03 " + label + " profile mismatch for soulCampfireDestroyedOnBurnout: expected "
                        + expected.soulDestroyedOnBurnout() + " but got "
                        + CampfireOverhaulConfig.SOUL_CAMPFIRE_DESTROYED_ON_BURNOUT.get());
    }

    /** Distinguishable GT-03 profile guard marker (not a scenario PASS marker). */
    public static void emitBurnoutProfileEvidence(BurnoutProfileValues expected) {
        EVIDENCE_LOGGER.info(
                "[P3-T2-PROFILE] kind=GUARD result=PASS profile={} runId={} campfireInfiniteLifeTime={}; soulCampfireInfiniteLifeTime={}; campfireDestroyedOnBurnout={}; soulCampfireDestroyedOnBurnout={}",
                expected.label(), RUN_ID, expected.normalInfinite(), expected.soulInfinite(),
                expected.normalDestroyedOnBurnout(), expected.soulDestroyedOnBurnout());
    }

    /** Place one soul campfire through the verified mock-player placement path. */
    public static CampfireFixture placeSoulCampfire(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
        player.getAbilities().mayBuild = true;

        BlockPos support = new BlockPos(1, 1, 1);
        helper.setBlock(support, Blocks.STONE.defaultBlockState());
        helper.setBlock(support.above(), Blocks.STONE.defaultBlockState());

        ItemStack stack = new ItemStack(Items.SOUL_CAMPFIRE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.placeAt(player, stack, support, Direction.UP);

        BlockPos campfirePos = null;
        for (int x = 0; x <= 2 && campfirePos == null; x++) {
            for (int y = 0; y <= 4 && campfirePos == null; y++) {
                for (int z = 0; z <= 2 && campfirePos == null; z++) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (helper.getBlockState(candidate).getBlock() == Blocks.SOUL_CAMPFIRE) {
                        campfirePos = candidate;
                    }
                }
            }
        }
        helper.assertTrue(campfirePos != null, "Soul campfire was not placed by placeAt");

        BlockEntity blockEntity = helper.getBlockEntity(campfirePos);
        helper.assertTrue(blockEntity instanceof CampfireBlockEntity,
                "Soul campfire BlockEntity is not a CampfireBlockEntity at " + campfirePos);
        helper.assertTrue(blockEntity instanceof ICampfireExtra,
                "GT-03 Mixin trace: CampfireBlockEntity does not implement ICampfireExtra at " + campfirePos);
        return new CampfireFixture(campfirePos, blockEntity, (ICampfireExtra) blockEntity);
    }

    /** Count ItemEntities of a given item inside the test cell radius. */
    public static int countItemEntities(GameTestHelper helper, BlockPos pos, Item item, double radius) {
        return helper.getEntities(EntityType.ITEM, pos, radius).stream()
                .filter(e -> e.getItem().is(item))
                .mapToInt(e -> e.getItem().getCount())
                .sum();
    }

    /** A freshly placed normal campfire and its extra-state view. */
    public record CampfireFixture(BlockPos pos, BlockEntity blockEntity, ICampfireExtra campfire) {
    }

    /** Expected profile values; constructed by each profile-specific test class. */
    public record ProfileValues(String label, boolean infinite, int maxLifeTime,
                                int fuelMultiplier, int defaultLifeTime) {
    }

    /** Fail fast with key, expected and actual when the active profile is wrong. */
    public static void assertProfile(GameTestHelper helper, ProfileValues expected) {
        String label = expected.label();
        boolean infinite = CampfireOverhaulConfig.CAMPFIRE_INFINITE_LIFE_TIME.get();
        helper.assertTrue(infinite == expected.infinite(),
                "GT-02 " + label + " profile mismatch for campfireInfiniteLifeTime: expected "
                        + expected.infinite() + " but got " + infinite);

        int maxLifeTime = CampfireOverhaulConfig.CAMPFIRE_MAX_LIFE_TIME.get();
        helper.assertTrue(maxLifeTime == expected.maxLifeTime(),
                "GT-02 " + label + " profile mismatch for campfireMaxLifeTime: expected "
                        + expected.maxLifeTime() + " but got " + maxLifeTime);

        int fuelMultiplier = CampfireOverhaulConfig.CAMPFIRE_FUEL_MULTIPLIER.get();
        helper.assertTrue(fuelMultiplier == expected.fuelMultiplier(),
                "GT-02 " + label + " profile mismatch for campfireFuelMultiplier: expected "
                        + expected.fuelMultiplier() + " but got " + fuelMultiplier);

        int defaultLifeTime = CampfireOverhaulConfig.CAMPFIRE_DEFAULT_LIFE_TIME.get();
        helper.assertTrue(defaultLifeTime == expected.defaultLifeTime(),
                "GT-02 " + label + " profile mismatch for campfireDefaultLifeTime: expected "
                        + expected.defaultLifeTime() + " but got " + defaultLifeTime);
    }

    /** Assert the exact GameTest namespace selected for this process. */
    public static void assertEnabledNamespace(GameTestHelper helper, String expected) {
        String actual = System.getProperty("forge.enabledGameTestNamespaces");
        helper.assertTrue(expected.equals(actual),
                "GT-02 enabledGameTestNamespaces mismatch: expected " + expected + " but got " + actual);
    }

    /** Place one normal campfire through the verified mock-player placement path. */
    public static CampfireFixture placeCampfire(GameTestHelper helper) {
        Player player = helper.makeMockPlayer();
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

        helper.assertFalse(helper.getBlockState(campfirePos).getValue(CampfireBlock.LIT),
                "GT-02 fixture requires an unlit campfire so countdown cannot change the exact lifetime");

        return new CampfireFixture(campfirePos, blockEntity, (ICampfireExtra) blockEntity);
    }

    /** Create and register the non-player thrower used by all fuel ItemEntities. */
    public static ArmorStand createRegisteredThrower(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ArmorStand thrower = EntityType.ARMOR_STAND.create(level);
        helper.assertTrue(thrower != null, "Could not create ArmorStand thrower");
        Vec3 throwerPos = helper.absoluteVec(new Vec3(3.5D, 2.5D, 3.5D));
        thrower.setPos(throwerPos.x, throwerPos.y, throwerPos.z);
        thrower.setNoGravity(true);
        helper.assertTrue(level.addFreshEntity(thrower), "ArmorStand was not registered in the level");
        helper.assertTrue(level.getEntity(thrower.getUUID()) == thrower,
                "Registered ArmorStand could not be resolved by UUID");
        return thrower;
    }

    /**
     * Spawn one owned fuel ItemEntity just above the campfire and give it a
     * downward motion. Starting inside the solid campfire shape causes the
     * collision resolver to eject the item before the ingestion tick.
     *
     * @param pickupDelay negative means never pick up; unlimitedDespawn keeps the item alive for the whole observation
     */
    public static ItemEntity spawnFuel(GameTestHelper helper, BlockPos campfirePos, ArmorStand thrower,
                                       ItemStack fuelStack, int pickupDelay, boolean unlimitedDespawn) {
        ServerLevel level = helper.getLevel();
        Item item = fuelStack.getItem();
        ItemEntity fuel = helper.spawnItem(item, campfirePos);
        fuel.setItem(fuelStack);
        fuel.setThrower(thrower.getUUID());

        Vec3 fuelPos = helper.absoluteVec(new Vec3(
                campfirePos.getX() + 0.5D, campfirePos.getY() + 1.1D, campfirePos.getZ() + 0.5D));
        fuel.setPos(fuelPos.x, fuelPos.y, fuelPos.z);
        fuel.setDeltaMovement(new Vec3(0.0D, -0.25D, 0.0D));

        if (pickupDelay < 0) {
            fuel.setNeverPickUp();
        } else {
            fuel.setPickUpDelay(pickupDelay);
        }
        if (unlimitedDespawn) {
            fuel.setUnlimitedLifetime();
        }

        helper.assertTrue(level.getEntity(fuel.getUUID()) == fuel, "Fuel ItemEntity was not registered");
        helper.assertTrue(fuel.getOwner() == thrower,
                "Fuel thrower did not resolve to the registered ArmorStand before the collision tick");
        return fuel;
    }

    /** Diagnostic detail appended to every delayed assertion message. */
    public static String diagnostics(GameTestHelper helper, CampfireFixture fixture, ItemEntity fuel) {
        ServerLevel level = helper.getLevel();
        Vec3 campfireBase = helper.absoluteVec(new Vec3(
                fixture.pos().getX() + 0.5D, fixture.pos().getY() + 0.5D, fixture.pos().getZ() + 0.5D));
        BlockPos expectedCampfireBlock = BlockPos.containing(campfireBase);
        return " [diagnostics: campfirePos=" + fixture.pos()
                + " expectedCampfireBlock=" + expectedCampfireBlock
                + " campfireBlockState=" + helper.getBlockState(fixture.pos()).getBlock()
                + " fuelAlive=" + fuel.isAlive()
                + " fuelBlock=" + fuel.blockPosition()
                + " fuelPos=(" + fuel.getX() + "," + fuel.getY() + "," + fuel.getZ() + ")"
                + " fuelCount=" + fuel.getItem().getCount()
                + " fuelOwner=" + fuel.getOwner()
                + " fuelRegistered=" + (level.getEntity(fuel.getUUID()) != null)
                + " stackedBlock=" + level.getBlockState(fuel.blockPosition()).getBlock() + "]";
    }

    /** Body of a successful fuel-ingestion assertion (polled by succeedWhen). */
    public static void assertIngested(GameTestHelper helper, CampfireFixture fixture, ItemEntity fuel,
                                      ArmorStand thrower, int expectedLifetime,
                                      int lowerBound, int upperBound, String expectation) {
        ServerLevel level = helper.getLevel();
        String diag = diagnostics(helper, fixture, fuel);
        helper.assertTrue(fuel.getOwner() == thrower,
                "Fuel thrower did not resolve to the registered ArmorStand during the collision tick" + diag);
        int lifetime = fixture.campfire().getLifeTime();
        helper.assertTrue(lifetime >= lowerBound && lifetime <= upperBound,
                "Final lifetime out of [" + lowerBound + "," + upperBound + "]: " + lifetime + diag);
        helper.assertTrue(lifetime == expectedLifetime,
                "Expected final lifetime " + expectedLifetime + " (" + expectation + ") but found "
                        + lifetime + diag);
        helper.assertTrue(!fuel.isAlive(), "Fuel ItemEntity is still alive after ingestion" + diag);
        helper.assertTrue(level.getEntity(fuel.getUUID()) == null,
                "Fuel ItemEntity is still registered in the level after ingestion" + diag);
    }

    /**
     * Poll until the fuel has genuinely reached the campfire, then assert that
     * rejection left both the lifetime and the item untouched.
     */
    public static void expectPreserved(GameTestHelper helper, CampfireFixture fixture, ItemEntity fuel,
                                       ArmorStand thrower, int expectedLifetime, String expectation) {
        expectPreserved(helper, fixture, fuel, thrower, expectedLifetime, expectation, null);
    }

    /**
     * Same rejection verification as above, with an optional test-only callback
     * invoked only after every assertion in the polled body has succeeded.
     */
    public static void expectPreserved(GameTestHelper helper, CampfireFixture fixture, ItemEntity fuel,
                                       ArmorStand thrower, int expectedLifetime, String expectation,
                                       Runnable onSuccess) {
        int originalCount = fuel.getItem().getCount();
        helper.assertTrue(originalCount > 0,
                "Rejection fixture requires a non-empty fuel stack but found count " + originalCount
                        + diagnostics(helper, fixture, fuel));

        helper.succeedWhen(() -> {
            Vec3 campfireBase = helper.absoluteVec(new Vec3(
                    fixture.pos().getX() + 0.5D, fixture.pos().getY() + 0.5D, fixture.pos().getZ() + 0.5D));
            String diag = diagnostics(helper, fixture, fuel);
            // Geometry verified from the 1.20.1 source: CampfireBlock.SHAPE is
            // Block.box(0,0,0,16,7,16) - full XZ footprint, Y in [blockY,
            // blockY + 7/16]. A 0.25-high ItemEntity settles with its bottom at
            // about blockY + 0.44, still inside the campfire block. An archived
            // failure log showed an ejected item three blocks lower, so a single
            // upper-bound Y check is not enough: require the correct block and
            // a bounded position inside that block.
            BlockPos campfireBlock = BlockPos.containing(campfireBase);
            helper.assertTrue(fuel.blockPosition().equals(campfireBlock),
                    "Fuel is not in the intended campfire block" + diag);
            helper.assertTrue(fuel.getX() >= campfireBase.x - 0.5D && fuel.getX() <= campfireBase.x + 0.5D,
                    "Fuel X is outside the campfire footprint" + diag);
            helper.assertTrue(fuel.getZ() >= campfireBase.z - 0.5D && fuel.getZ() <= campfireBase.z + 0.5D,
                    "Fuel Z is outside the campfire footprint" + diag);
            helper.assertTrue(fuel.getY() >= campfireBase.y - 0.5D && fuel.getY() <= campfireBase.y + 0.25D,
                    "Fuel Y is outside the campfire block" + diag);
            helper.assertTrue(fuel.getOwner() == thrower,
                    "Fuel thrower did not resolve to the registered ArmorStand during the collision tick" + diag);
            int lifetime = fixture.campfire().getLifeTime();
            helper.assertTrue(lifetime == expectedLifetime,
                    "Expected unchanged lifetime " + expectedLifetime + " (" + expectation
                            + ") but found " + lifetime + diag);
            helper.assertTrue(fuel.isAlive(), "Fuel was consumed despite rejection" + diag);
            helper.assertTrue(helper.getLevel().getEntity(fuel.getUUID()) == fuel,
                    "Fuel ItemEntity was deregistered despite rejection" + diag);
            int currentCount = fuel.getItem().getCount();
            helper.assertTrue(currentCount == originalCount,
                    "Fuel stack count changed despite rejection: expected " + originalCount
                            + " but got " + currentCount + diag);
            if (onSuccess != null) {
                onSuccess.run();
            }
        });
    }
}
