package invmod.client.render;

import invmod.common.entity.EntityIMSkeleton;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;

public class ModelIMSkeleton extends SkeletonEntityModel<EntityIMSkeleton> {
    public ModelIMSkeleton(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void animateModel(EntityIMSkeleton mobEntity, float f, float g, float h) {
    }
}