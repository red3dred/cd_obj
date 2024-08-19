package com.invasion.client.render.entity.model;

import com.invasion.entity.IMSkeletonEntity;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;

/**
 * Extension of SkeletonEntityModel that does not play animations... or some reason
 */
@Deprecated
public class ModelIMSkeleton extends SkeletonEntityModel<IMSkeletonEntity> {
    public ModelIMSkeleton(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void animateModel(IMSkeletonEntity mobEntity, float f, float g, float h) {
    }
}