package com.zander.campfire_overhaul.mixin;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.util.CampfireHelper;
import com.zander.campfire_overhaul.util.ICampfireExtra;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity implements ICampfireExtra {
    @Final
    @Shadow
    private NonNullList<ItemStack> items;

    @Unique
    private int campfireOverhaul$lifeTime = -1337;

    @Unique
    @Override
    public int getLifeTime() { return campfireOverhaul$lifeTime; }

    @Unique
    @Override
    public void addLifeTime(int add) { campfireOverhaul$lifeTime += add; }

    @Unique
    @Override
    public void setLifeTime(int set) { campfireOverhaul$lifeTime = set; }

    public CampfireBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    @Override
    public void campfireOverhaul_extinguish() {
        if (this.level != null && !this.level.isClientSide) {
            this.level.playSound(null, this.worldPosition,
                    SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.level.setBlock(this.worldPosition,
                    this.getBlockState().setValue(CampfireBlock.LIT, false), 3);
        }
    }

    @Unique
    @Override
    public void campfireOverhaul_break() {
        if (this.level != null && !this.level.isClientSide) {
            for (ItemStack stack : items) {
                if (!stack.isEmpty()) {
                    Block.popResource(this.level, this.worldPosition, stack);
                }
            }
            items.clear();
            this.level.playSound(null, this.worldPosition,
                    SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.level.setBlock(this.worldPosition, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Inject(
            at = @At("RETURN"),
            method = "<init>(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
    )
    private void campfireOverhaul$init(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (CampfireOverhaulConfig.WORLDGEN_CAMPFIRES_INFINITE.get())
            campfireOverhaul$lifeTime = -1337;
        else
            campfireOverhaul$lifeTime = -9999;
    }

    @Inject(
            at = @At("HEAD"),
            method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V"
    )
    private static void campfireOverhaul$cookTick(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
        ICampfireExtra extra = (ICampfireExtra) campfire;

        if (extra.getLifeTime() == -9999) {
            if (CampfireHelper.isSoul(state)) {
                extra.setLifeTime(CampfireOverhaulConfig.SOUL_CAMPFIRE_INFINITE_LIFE_TIME.get()
                        ? -1337
                        : CampfireOverhaulConfig.SOUL_CAMPFIRE_DEFAULT_LIFE_TIME.get() * 10000);
            } else {
                extra.setLifeTime(CampfireOverhaulConfig.CAMPFIRE_INFINITE_LIFE_TIME.get()
                        ? -1337
                        : CampfireOverhaulConfig.CAMPFIRE_DEFAULT_LIFE_TIME.get() * 10000);
            }
        }

        if (extra.getLifeTime() != -1337) {
            if (level.getBlockState(pos).getValue(CampfireBlock.LIT)) {
                if (extra.getLifeTime() > 0) {
                    extra.addLifeTime(-1);
                } else {
                    extra.setLifeTime(0);
                    if (CampfireHelper.isSoul(state)) {
                        if (CampfireOverhaulConfig.SOUL_CAMPFIRE_DESTROYED_ON_BURNOUT.get())
                            extra.campfireOverhaul_break();
                        else
                            extra.campfireOverhaul_extinguish();
                    } else {
                        if (CampfireOverhaulConfig.CAMPFIRE_DESTROYED_ON_BURNOUT.get())
                            extra.campfireOverhaul_break();
                        else
                            extra.campfireOverhaul_extinguish();
                    }
                }
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "load(Lnet/minecraft/nbt/CompoundTag;)V")
    private void campfireOverhaul$load(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("LifeTime", 3)) {
            setLifeTime(nbt.getInt("LifeTime"));
        }
    }

    @Inject(at = @At("HEAD"), method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;)V")
    private void campfireOverhaul$saveAdditional(CompoundTag nbt, CallbackInfo ci) {
        nbt.putInt("LifeTime", campfireOverhaul$lifeTime);
    }
}