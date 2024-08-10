package invmod.client.render;

import invmod.common.entity.EntityIMEgg;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;

public class ModelEgg extends SinglePartEntityModel<EntityIMEgg> {
    private final ModelPart root;

    public ModelEgg(ModelPart root) {
        this.root = root;
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData data = new ModelData();
        ModelPartData root = data.getRoot();
        root.addChild("top", ModelPartBuilder.create().mirrored()
                .uv(0, 0).cuboid(1F, 0F, 1F, 7, 1, 7)
                .uv(0, 8).cuboid(2F, -11F, 2F, 5, 1, 5)
                .uv(28, 23).cuboid(1F, -10F, 2F, 1, 3, 6)
                .uv(0, 24).cuboid(1F, -10F, 1F, 6, 3, 1)
                .uv(28, 23).cuboid(7F, -10F, 1F, 1, 3, 6)
                .uv(0, 24).cuboid(2F, -10F, 7F, 6, 3, 1)
                .uv(10, 22).cuboid(0F, -7F, 1F, 1, 2, 8)
                .uv(0, 21).cuboid(0F, -7F, 0F, 8, 2, 1)
                .uv(10, 22).cuboid(8F, -7F, 0F, 1, 2, 8)
                .uv(0, 21).cuboid(1F, -7F, 8F, 8, 2, 1)
                .uv(20, 10).cuboid(-1F, -5F, 0F, 1, 4, 9)
                .uv(0, 16).cuboid(0F, -5F, -1F, 9, 4, 1)
                .uv(20, 10).cuboid(9F, -5F, 0F, 1, 4, 9)
                .uv(0, 16).cuboid(0F, -5F, 9F, 9, 4, 1)
                .uv(28, 0).cuboid(0F, -1F, 1F, 1, 1, 8)
                .uv(0, 14).cuboid(0F, -1F, 0F, 8, 1, 1)
                .uv(28, 0).cuboid(8F, -1F, 0F, 1, 1, 8), ModelTransform.NONE);
        root.addChild("bottom", ModelPartBuilder.create().mirrored().uv(0, 14).cuboid(0F, 0F, 0F, 8, 1, 1), ModelTransform.pivot(1F, -1F, 8F));
        return TexturedModelData.of(data, 64, 32);
    }

    @Override
    public ModelPart getPart() {
        return root;
    }

    @Override
    public void setAngles(EntityIMEgg entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    }
}