package CCPCT.better_stacking;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;

public interface ICustomNameTagSubmitter {

    /** Labels are self lit, exactly like vanilla name tags. */
    int FULL_BRIGHT = 0xF000F0;

    void betterStacking$submitStackLabel(
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
    );
}
