package com.zander.campfire_overhaul.event;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.util.CampfireHelper;
import com.zander.campfire_overhaul.util.ICampfireExtra;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class AlternativeFireMethods {

    @SubscribeEvent
    public void campfireInteractionEvents(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();

        Player player = event.getEntity();
        BlockPos blockPos = event.getPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (!(blockState.getBlock() instanceof CampfireBlock)) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (player.getMainHandItem().getItem() == Items.FLINT
                && player.getOffhandItem().getItem() == Items.FLINT) {

            if (!CampfireOverhaulConfig.DOUBLE_FLINT_IGNITION.get()) return;

            Random rand = new Random(world.random.nextLong());
            player.swing(InteractionHand.MAIN_HAND);

            if (rand.nextFloat() < CampfireOverhaulConfig.FLINT_IGNITE_CHANCE.get()
                    && !world.isClientSide) {
                world.setBlock(blockPos,
                        blockState.setValue(BlockStateProperties.LIT, Boolean.TRUE), 3);
            }

            world.playSound(null, blockPos, SoundEvents.STONE_STEP,
                    SoundSource.BLOCKS, 1.0F, 2F + rand.nextFloat() * 0.4F);

            if (world.isClientSide) {
                for (int i = 0; i < 5; i++) {
                    world.addParticle(ParticleTypes.SMOKE,
                            player.getX() + player.getLookAngle().x + rand.nextFloat() * 0.25,
                            player.getY() + 0.5f + rand.nextFloat() * 0.25,
                            player.getZ() + player.getLookAngle().z + rand.nextFloat() * 0.25,
                            0, 0.01, 0);
                }
                world.addParticle(ParticleTypes.FLAME,
                        player.getX() + player.getLookAngle().x + rand.nextFloat() * 0.25,
                        player.getY() + 0.5f + rand.nextFloat() * 0.25,
                        player.getZ() + player.getLookAngle().z + rand.nextFloat() * 0.25,
                        0, 0.01, 0);
            }

        } else {
            ResourceLocation heldItemId = ForgeRegistries.ITEMS.getKey(
                    player.getMainHandItem().getItem());
            ResourceLocation configItemId = ResourceLocation.tryParse(
                    CampfireOverhaulConfig.INFINTE_CAMPFIRE_ITEM.get());


            if (configItemId == null || !configItemId.equals(heldItemId)) return;
            if (!CampfireOverhaulConfig.MAKING_INFINITE_CAMPFIRES.get()) return;

            if (!world.isClientSide) {
                world.setBlock(blockPos,
                        blockState.setValue(BlockStateProperties.LIT, Boolean.TRUE), 3);
                if (world.getBlockEntity(blockPos) instanceof ICampfireExtra extra) {
                    extra.setLifeTime(-1337);
                }
            }
            world.playSound(null, blockPos,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public void checkCampfireLifeTime(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getLevel();

        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        BlockState blockState = world.getBlockState(pos);

        if (!(blockState.getBlock() instanceof CampfireBlock)) return;
        if (!player.getMainHandItem().isEmpty()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        if (!world.isClientSide) {
            if (world.getBlockEntity(pos) instanceof ICampfireExtra info) {
                if (info.getLifeTime() == -1337) {
                    player.sendSystemMessage(
                            Component.literal("This campfire will burn forever!"));
                } else if (info.getLifeTime() == 0) {
                    player.sendSystemMessage(
                            Component.literal("This campfire has no fuel!"));
                } else {
                    player.sendSystemMessage(Component.literal(
                            "This campfire has " + info.getLifeTime() / 20 + " seconds worth of fuel."));
                }
            }
        }
    }

    @SubscribeEvent
    public void campfireSet(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;

        BlockState placed = event.getPlacedBlock();
        BlockPos pos = event.getPos();

        if (!(level.getBlockEntity(pos) instanceof CampfireBlockEntity campfireEntity)) return;

        ICampfireExtra lifeTime = (ICampfireExtra) campfireEntity;

        if (CampfireHelper.isSoul(placed)) {
            lifeTime.setLifeTime(CampfireOverhaulConfig.SOUL_CAMPFIRE_INFINITE_LIFE_TIME.get()
                    ? -1337
                    : CampfireOverhaulConfig.SOUL_CAMPFIRE_DEFAULT_LIFE_TIME.get());
        } else {
            lifeTime.setLifeTime(CampfireOverhaulConfig.CAMPFIRE_INFINITE_LIFE_TIME.get()
                    ? -1337
                    : CampfireOverhaulConfig.CAMPFIRE_DEFAULT_LIFE_TIME.get());
        }
    }
}