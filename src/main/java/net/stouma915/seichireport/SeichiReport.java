package net.stouma915.seichireport;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(
        modid = SeichiReport.MOD_ID,
        name = SeichiReport.MOD_NAME,
        version = SeichiReport.VERSION,
        useMetadata = true
)
public class SeichiReport {

    public static final String MOD_ID = "seichireport";
    public static final String MOD_NAME = "SeichiReport";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public static void init(FMLServerStartingEvent event) {
        event.registerServerCommand(new ReportCommand());
    }

}
