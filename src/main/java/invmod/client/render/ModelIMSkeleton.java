package invmod.client.render;

import invmod.common.entity.EntityIMSkeleton;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;

/**
 * Extension of SkeletonEntityModel that does not play animations... or some reason
 */
@Deprecated
public class ModelIMSkeleton extends SkeletonEntityModel<EntityIMSkeleton> {
    public ModelIMSkeleton(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void animateModel(EntityIMSkeleton mobEntity, float f, float g, float h) {
    }
}