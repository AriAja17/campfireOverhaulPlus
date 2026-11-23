package com.zander.campfire_overhaul.gametest;

import com.zander.campfire_overhaul.gametest.CampfireGameTestSupport.BurnoutProfileValues;
import com.zander.campfire_overhaul.gametest.CampfireGameTestSupport.CampfireFixture;
import com.zander.campfire_overhaul.util.ICampfireExtra;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/**
 * GT-03 BURNOUT profile. Covers G5 scenarios E1 (destructive normal burnout)
 * and E2 (non-destructive soul burnout). Both scenarios exercise the actual
 * transformed {@code cookTick} path through natural server ticks; neither
 * deletes a block nor invokes a production burnout helper directly.
 */
@GameTestHolder("campfire_overhaul_gt03_burnout")
@PrefixGameTestTemplate(false)
public class CampfireBurnoutGameTests {

    private static final String TEMPLATE = "gt03_burnout";
    private static final String NAMESPACE = "campfire_overhaul_gt03_burnout";
    private static final String PROFILE_LABEL = "GT03_BURNOUT";

    /** The four approved profile values, verified by the guard at runtime. */
    private static final BurnoutProfileValues PROFILE =
            new BurnoutProfileValues(PROFILE_LABEL, false, false, true, false);

    /** Long cooking time prevents normal cooking completion/ejection interfering with burnout. */
    private static final int COOKING_TIME = 6000;

    /** Four distinct vanilla-cookable items. */
    private static final Item[] FOODS = {Items.BEEF, Items.PORKCHOP, Items.COD, Items.CHICKEN};

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void burnoutProfileGuard(GameTestHelper helper) {
        CampfireGameTestSupport.assertEnabledNamespace(helper, NAMESPACE);
        CampfireGameTestSupport.assertBurnoutProfile(helper, PROFILE);
        CampfireGameTestSupport.emitBurnoutProfileEvidence(PROFILE);
        helper.succeed();
    }

    /**
     * E1: a normal campfire is destroyed at burnout and each of four distinct
     * cooking items is dropped exactly once by the real tick path.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void destructiveNormalBurnoutDropsFourFoodsOnce(GameTestHelper helper) {
        CampfireGameTestSupport.assertBurnoutProfile(helper, PROFILE);
        CampfireFixture fixture = CampfireGameTestSupport.placeCampfire(helper);

        // Mixin trace: ICampfireExtra is only implemented because the production
        // CampfireBlockEntityMixin is applied in this runtime.
        boolean mixinApplied = fixture.blockEntity() instanceof ICampfireExtra;
        helper.assertTrue(mixinApplied,
                "GT-03 E1 Mixin not applied: CampfireBlockEntity does not implement ICampfireExtra");
        helper.assertTrue(fixture.blockEntity() instanceof CampfireBlockEntity,
                "GT-03 E1 BlockEntity is not a CampfireBlockEntity");
        CampfireBlockEntity blockEntity = (CampfireBlockEntity) fixture.blockEntity();

        // Unregistered mock player: cannot pick up any dropped food.
        Player player = helper.makeMockPlayer();
        for (Item food : FOODS) {
            helper.assertTrue(blockEntity.placeFood(player, new ItemStack(food), COOKING_TIME),
                    "GT-03 E1 could not place food " + food);
        }

        fixture.campfire().setLifeTime(1);
        helper.setBlock(fixture.pos(), helper.getBlockState(fixture.pos()).setValue(CampfireBlock.LIT, true));

        boolean[] emitted = {false};
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(fixture.pos()).is(Blocks.AIR),
                    "GT-03 E1 campfire block was not removed by natural burnout");
            helper.assertTrue(helper.getBlockEntity(fixture.pos()) == null,
                    "GT-03 E1 campfire BlockEntity was not removed by natural burnout");

            StringBuilder drops = new StringBuilder();
            for (Item food : FOODS) {
                int count = CampfireGameTestSupport.countItemEntities(helper, fixture.pos(), food, 4.0D);
                helper.assertTrue(count == 1,
                        "GT-03 E1 expected exactly one dropped " + food + " but found " + count);
                if (drops.length() > 0) {
                    drops.append(';');
                }
                drops.append(food).append('=').append(count);
            }
            int totalDrops = helper.getEntities(EntityType.ITEM, fixture.pos(), 4.0D).size();
            helper.assertTrue(totalDrops == 4,
                    "GT-03 E1 expected exactly four dropped item entities but found " + totalDrops);

            if (!emitted[0]) {
                emitted[0] = true;
                CampfireGameTestSupport.emitScenarioEvidence(
                        "E1", "destructiveNormalBurnoutDropsFourFoodsOnce", PROFILE_LABEL,
                        "block=Air; blockEntity=absent; drops[BEEF,PORKCHOP,COD,CHICKEN]=1 each; totalItemEntities=4; mixinApplied=true; naturalTickBurnout=true",
                        "block=" + helper.getBlockState(fixture.pos()).getBlock()
                                + "; blockEntity=" + (helper.getBlockEntity(fixture.pos()) == null ? "absent" : "present")
                                + "; drops=" + drops
                                + "; totalItemEntities=" + totalDrops
                                + "; mixinApplied=" + mixinApplied
                                + "; observedLifeTime=" + fixture.campfire().getLifeTime(),
                        "profileGuard; mixinTrace[BE implements ICampfireExtra]; fourDistinctFoods(cookTime=6000); setLifeTime(1); setLIT=true; naturalServerTicks; assert[Air,noBE,exactDrops]");
            }
        });
    }

    /**
     * E2: a soul campfire extinguishes at burnout without block destruction and
     * retains its BlockEntity and all four food stacks; zero food is dropped.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void nonDestructiveSoulBurnoutRetainsFood(GameTestHelper helper) {
        CampfireGameTestSupport.assertBurnoutProfile(helper, PROFILE);
        CampfireFixture fixture = CampfireGameTestSupport.placeSoulCampfire(helper);

        boolean mixinApplied = fixture.blockEntity() instanceof ICampfireExtra;
        helper.assertTrue(mixinApplied,
                "GT-03 E2 Mixin not applied: CampfireBlockEntity does not implement ICampfireExtra");
        helper.assertTrue(fixture.blockEntity() instanceof CampfireBlockEntity,
                "GT-03 E2 BlockEntity is not a CampfireBlockEntity");
        CampfireBlockEntity blockEntity = (CampfireBlockEntity) fixture.blockEntity();

        Player player = helper.makeMockPlayer();
        for (Item food : FOODS) {
            helper.assertTrue(blockEntity.placeFood(player, new ItemStack(food), COOKING_TIME),
                    "GT-03 E2 could not place food " + food);
        }

        fixture.campfire().setLifeTime(1);
        helper.setBlock(fixture.pos(), helper.getBlockState(fixture.pos()).setValue(CampfireBlock.LIT, true));

        boolean[] emitted = {false};
        helper.succeedWhen(() -> {
            BlockState state = helper.getBlockState(fixture.pos());
            helper.assertTrue(state.getBlock() == Blocks.SOUL_CAMPFIRE,
                    "GT-03 E2 soul campfire block is not present after burnout");
            helper.assertFalse(state.getValue(CampfireBlock.LIT),
                    "GT-03 E2 soul campfire is still LIT after burnout");
            helper.assertTrue(helper.getBlockEntity(fixture.pos()) == fixture.blockEntity(),
                    "GT-03 E2 BlockEntity was not retained after burnout");

            StringBuilder slots = new StringBuilder();
            for (int i = 0; i < FOODS.length; i++) {
                ItemStack stack = blockEntity.getItems().get(i);
                helper.assertTrue(stack.is(FOODS[i]) && stack.getCount() == 1,
                        "GT-03 E2 slot " + i + " expected one " + FOODS[i] + " but found " + stack);
                if (slots.length() > 0) {
                    slots.append(';');
                }
                slots.append('s').append(i).append('=').append(stack.getItem()).append('x').append(stack.getCount());
            }
            int dropped = helper.getEntities(EntityType.ITEM, fixture.pos(), 4.0D).size();
            helper.assertTrue(dropped == 0,
                    "GT-03 E2 expected zero dropped food items but found " + dropped);

            if (!emitted[0]) {
                emitted[0] = true;
                CampfireGameTestSupport.emitScenarioEvidence(
                        "E2", "nonDestructiveSoulBurnoutRetainsFood", PROFILE_LABEL,
                        "block=SoulCampfire; lit=false; blockEntity=retained; slots[0..3]=one of each food; foodDropped=0; mixinApplied=true; naturalTickBurnout=true",
                        "block=" + state.getBlock()
                                + "; lit=" + state.getValue(CampfireBlock.LIT)
                                + "; blockEntity=" + (helper.getBlockEntity(fixture.pos()) == fixture.blockEntity() ? "retained" : "changed")
                                + "; slots=" + slots
                                + "; foodDropped=" + dropped
                                + "; mixinApplied=" + mixinApplied
                                + "; observedLifeTime=" + fixture.campfire().getLifeTime(),
                        "profileGuard; mixinTrace[BE implements ICampfireExtra]; fourDistinctFoods(cookTime=6000); setLifeTime(1); setLIT=true; naturalServerTicks; assert[block,!LIT,BE,foodSlots,zeroDrops]");
            }
        });
    }
}
