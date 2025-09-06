package me.kall.korall;

import me.kall.korall.data.BlockTracker;
import me.kall.korall.data.EntityTracker;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Korall.MOD_ID)
public final class Korall {
    public static final String MOD_ID = "korall";
    public static final String MOD_NAME = "Korall";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public Korall() {
        EntityTracker.register();
        BlockTracker.register();
    }
}
