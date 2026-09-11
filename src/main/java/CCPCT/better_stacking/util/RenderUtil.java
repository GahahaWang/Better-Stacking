package CCPCT.better_stacking.util;

import CCPCT.better_stacking.BetterStacking;
import CCPCT.better_stacking.ICustomNameTagSubmitter;
import CCPCT.better_stacking.modConfig.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Draws the labels prepared by {@link EntityClusterManager}. Runs every frame, so it only does
 * positioning work here; text and colours are decided when the cluster is built.
 */
public final class RenderUtil {

    private static boolean warnedAboutSubmitter;

    private RenderUtil() {
    }

    public static void renderLabel(LevelRenderContext context) {
        List<EntityClusterManager.ClusterEntry> clusters = EntityClusterManager.getActiveClusters();
        if (clusters.isEmpty()) return;

        ModConfig config = ModConfig.get();
        if (!config.modEnabled) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        OrderedSubmitNodeCollector collector = context.submitNodeCollector().order(0);
        if (!(collector instanceof ICustomNameTagSubmitter submitter)) {
            if (!warnedAboutSubmitter) {
                warnedAboutSubmitter = true;
                BetterStacking.LOGGER.error("Cannot submit stack labels: {} does not carry the Better Stacking mixin, labels stay hidden.",
                        collector.getClass().getName());
            }
            return;
        }

        CameraRenderState camera = context.levelState().cameraRenderState;
        Vec3 cameraPos = camera.pos != null ? camera.pos : client.gameRenderer.mainCamera().position();

        final float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final float scale = Math.max(0.01F, config.labelSize) / 40.0F;
        final double heightOffset = config.labelOffset;

        PoseStack poseStack = context.poseStack();

        for (EntityClusterManager.ClusterEntry entry : clusters) {
            Entity leader = entry.leader();
            if (!leader.isAlive()) continue;

            // Interpolated position, otherwise the label lags a tick behind the entity it belongs to.
            Vec3 position = leader.getPosition(partialTick);
            double worldY = position.y + leader.getBbHeight() + heightOffset;

            submitter.betterStacking$submitStackLabel(
                    poseStack,
                    position.x - cameraPos.x,
                    worldY - cameraPos.y,
                    position.z - cameraPos.z,
                    entry.label(),
                    config.renderThroughBlocks,
                    camera,
                    config.labelColour,
                    config.labelBgColour,
                    scale
            );
        }
    }
}
