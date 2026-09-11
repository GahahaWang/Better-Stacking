package CCPCT.better_stacking.util;

import CCPCT.better_stacking.BetterStacking;
import CCPCT.better_stacking.ICustomNameTagSubmitter;
import CCPCT.better_stacking.modConfig.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RenderUtil {

    private static final double LABEL_HALF_WIDTH = 150.0D;

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
        Vec3 cameraPos = camera.pos;
        Frustum frustum = camera.cullFrustum;

        final float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final float scale = Math.max(0.01F, config.labelSize) / 40.0F;
        final double heightOffset = config.labelOffset;
        final int maxDistance = config.labelRenderDistance;
        final double maxDistanceSq = maxDistance <= 0 ? Double.POSITIVE_INFINITY : (double) maxDistance * maxDistance;

        final double labelRadius = Math.max(0.5D, scale * LABEL_HALF_WIDTH);

        PoseStack poseStack = context.poseStack();

        for (EntityClusterManager.ClusterEntry entry : clusters) {
            Entity leader = entry.leader();
            if (!leader.isAlive()) continue;

            // Distance first, on the raw position: it costs nothing and rejects most labels.
            final double height = leader.getBbHeight() + heightOffset;
            double rx = leader.getX() - cameraPos.x;
            double ry = leader.getY() + height - cameraPos.y;
            double rz = leader.getZ() - cameraPos.z;
            if (rx * rx + ry * ry + rz * rz > maxDistanceSq) continue;

            // Interpolated position, otherwise the label lags a tick behind the entity it belongs to.
            Vec3 position = leader.getPosition(partialTick);
            double worldY = position.y + height;

            double dx = position.x - cameraPos.x;
            double dy = worldY - cameraPos.y;
            double dz = position.z - cameraPos.z;

            if (!frustum.isVisible(new AABB(
                                position.x - labelRadius, worldY - labelRadius, position.z - labelRadius,
                                position.x + labelRadius, worldY + labelRadius, position.z + labelRadius))) {
                continue;
            }

            submitter.betterStacking$submitStackLabel(
                    poseStack,
                    dx, dy, dz,
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
