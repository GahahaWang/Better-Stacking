package CCPCT.better_stacking.mixin;

import CCPCT.better_stacking.util.EntityClusterManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Drops the entities that a cluster label already accounts for, before vanilla spends any work on
 * frustum checks or render state extraction for them.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void betterStacking$cullStackedEntities(
            E entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir
    ) {
        if (EntityClusterManager.shouldSkipRender(entity)) {
            cir.setReturnValue(false);
        }
    }
}
