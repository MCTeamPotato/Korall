package me.kall.korall;

import me.kall.korall.data.BlockTracker;
import me.kall.korall.data.EntityTracker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(Korall.MOD_ID)
public final class Korall {
    public static final String MOD_ID = "korall";
    public static final String MOD_NAME = "Korall";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public Korall(@NotNull FMLJavaModLoadingContext context) {
        EntityTracker.register();
        BlockTracker.register();
    }
}
