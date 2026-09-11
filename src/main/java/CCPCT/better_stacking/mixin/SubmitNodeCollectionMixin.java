package CCPCT.better_stacking.mixin;

import CCPCT.better_stacking.ICustomNameTagSubmitter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.NameTagFeatureRenderer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SubmitNodeCollection.class)
public class SubmitNodeCollectionMixin implements ICustomNameTagSubmitter {

    @Final
    @Shadow
    public SimpleFeatureRenderPhase nameTags;

    @Final
    @Shadow
    public TranslucentFeatureRenderPhase seeThroughNameTags;

    @Override
    public void betterStacking$submitStackLabel(
            PoseStack poseStack,
            double x,
            double y,
            double z,
            Component label,
            boolean seeThrough,
            CameraRenderState camera,
            int textColor,
            int backgroundColor,
            float scale
    ) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(scale, -scale, scale);
        Matrix4f pose = new Matrix4f(poseStack.last().pose());
        poseStack.popPose();

        float left = -Minecraft.getInstance().font.width(label) / 2.0F;

        if (seeThrough) {
            // Same split as vanilla: an opaque depth tested pass for the readable text in front of
            // geometry, and the translucent pass carrying the background and the see through ghost.
            // Submitting the configured colour twice would double its alpha where both are visible.
            this.nameTags.submit(new NameTagFeatureRenderer.Submit(
                    pose, left, 0.0F, label, FULL_BRIGHT, ARGB.opaque(textColor), 0, Font.DisplayMode.NORMAL
            ));
            this.seeThroughNameTags.submit(new NameTagFeatureRenderer.Submit(
                    pose, left, 0.0F, label, FULL_BRIGHT, textColor, backgroundColor, Font.DisplayMode.SEE_THROUGH
            ));
        } else {
            this.nameTags.submit(new NameTagFeatureRenderer.Submit(
                    pose, left, 0.0F, label, FULL_BRIGHT, textColor, backgroundColor, Font.DisplayMode.NORMAL
            ));
        }
    }
}
