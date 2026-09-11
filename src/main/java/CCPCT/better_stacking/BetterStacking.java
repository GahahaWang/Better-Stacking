package CCPCT.better_stacking;

import CCPCT.better_stacking.modConfig.ModConfig;
import CCPCT.better_stacking.util.EntityClusterManager;
import CCPCT.better_stacking.util.RenderUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterStacking implements ClientModInitializer {

    public static final String MOD_ID = "better_stacking";
    public static final Logger LOGGER = LoggerFactory.getLogger("Better Stacking");

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        ClientTickEvents.START_CLIENT_TICK.register(EntityClusterManager::tick);
        LevelRenderEvents.BEFORE_GIZMOS.register(RenderUtil::renderLabel);

        LOGGER.info("Better Stacking initialised and loaded its config :3");
    }
}
