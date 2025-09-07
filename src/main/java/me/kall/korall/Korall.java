package me.kall.korall;

import me.kall.korall.api.Trackable;
import me.kall.korall.data.BlockTracker;
import me.kall.korall.data.EntityTracker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Korall.MOD_ID)
public final class Korall {
    public static final String MOD_ID = "korall";
    public static final String MOD_NAME = "Korall";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public Korall(FMLJavaModLoadingContext context) {
        EntityTracker.register();
        BlockTracker.register();
        context.getModEventBus().addListener((FMLCommonSetupEvent event) -> ForgeRegistries.BLOCKS.forEach(Trackable::trackBlock));
    }
}
