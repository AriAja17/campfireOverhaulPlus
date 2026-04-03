package com.zander.campfire_overhaul;

import com.mojang.logging.LogUtils;
import com.zander.campfire_overhaul.config.CampfireOverhaulConfig;
import com.zander.campfire_overhaul.event.AlternativeFireMethods;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod("campfire_overhaul")
public class CampfireOverhaul {
        public static final String MOD_ID = "campfire_overhaul";

        private static final Logger LOGGER = LogUtils.getLogger();

        public CampfireOverhaul() {
                ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CampfireOverhaulConfig.CONFIG);
                MinecraftForge.EVENT_BUS.register(this);
                MinecraftForge.EVENT_BUS.register(new AlternativeFireMethods());
        }
}