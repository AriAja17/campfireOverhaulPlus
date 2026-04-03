package com.zander.campfire_overhaul.mixin;

import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.util.CampfireHelper;
import com.zander.campfire_overhaul.util.ICampfireExtra;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends BaseEntityBlock {

    protected CampfireBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(
            at = @At("RETURN"),
            method = "getStateForPlacement(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;",
            cancellable = true
    )
    protected void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState returnValue = cir.getReturnValue();

        if (CampfireHelper.isSoul(defaultBlockState())) {
            boolean shouldBeLit = returnValue.getValue(CampfireBlock.LIT)
                    && !CampfireOverhaulConfig.SOUL_CAMPFIRE_CREATED_UNLIT.get();
            cir.setReturnValue(returnValue.setValue(CampfireBlock.LIT, shouldBeLit));
        } else {
            boolean shouldBeLit = returnValue.getValue(CampfireBlock.LIT)
                    && !CampfireOverhaulConfig.CAMPFIRE_CREATED_UNLIT.get();
            cir.setReturnValue(returnValue.setValue(CampfireBlock.LIT, shouldBeLit));
        }
    }

    @Inject(
            at = @At("HEAD"),
            method = "entityInside(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V"
    )
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entityIn, CallbackInfo ci) {
        if (!(entityIn instanceof ItemEntity itemEntity)) return;

        int rawBurnTime = ForgeHooks.getBurnTime(itemEntity.getItem(), RecipeType.SMELTING);
        if (rawBurnTime <= 0) return;

        if (world.isClientSide) {
            if (state.getValue(CampfireBlock.LIT)) {
                world.addParticle(ParticleTypes.SMOKE,
                        entityIn.getX(), entityIn.getY() + 0.25D, entityIn.getZ(),
                        0, 0.05D, 0);
            }
            return;
        }

        if (itemEntity.getOwner() == null) return;

        if (!(world.getBlockEntity(pos) instanceof ICampfireExtra extra)) return;
        if (extra.getLifeTime() == -1337) return;

        int maxLife = CampfireHelper.isSoul(state)
                ? CampfireOverhaulConfig.SOUL_CAMPFIRE_MAX_LIFE_TIME.get()
                : CampfireOverhaulConfig.CAMPFIRE_MAX_LIFE_TIME.get();

        if (extra.getLifeTime() >= 0 && extra.getLifeTime() < maxLife) {
            int burnTime = rawBurnTime
                    * CampfireOverhaulConfig.CAMPFIRE_FUEL_MULTIPLIER.get()
                    * itemEntity.getItem().getCount();
            extra.addLifeTime(burnTime);

            if (itemEntity.getItem().getItem() == Items.LAVA_BUCKET) {
                Block.popResource(world, pos, new ItemStack(Items.BUCKET));
            }
            entityIn.discard();
        }
    }
}