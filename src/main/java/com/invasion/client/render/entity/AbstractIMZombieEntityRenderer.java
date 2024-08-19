package com.invasion.client.render.entity;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.stream.Stream;

import com.invasion.InvasionMod;
import com.invasion.client.render.entity.model.LargeBipedEntityModel;
import com.invasion.entity.AbstractIMZombieEntity;

/**
 * Replicates the rendering code from ZombieBaseEntityRenderer and ZombieEntityRenderer
 * and adds model swapping for the big and normal modes.
 *
 * @see net.minecraft.client.render.entity.ZombieEntityRenderer
 * @see net.minecraft.client.render.entity.ZombieBaseEntityRenderer
 */
public class AbstractIMZombieEntityRenderer extends BipedEntityRenderer<AbstractIMZombieEntity, BipedEntityModel<AbstractIMZombieEntity>> {
    static final List<Identifier> TEXTURES = Stream.of(
            "textures/entity/zombie/old_zombie_t1.png",
            "textures/entity/zombie/zombie_t1.png",
            "textures/entity/zombie/zombie_t2.png",
            "textures/entity/zombie_pigman/zombie_pigman_t3.png",
            "textures/entity/zombie/zombie_t2a.png",
            "textures/entity/zombie/zombie_tar.png",
            "textures/entity/zombie/zombie_t3.png"
    ).map(InvasionMod::id).toList();

    protected final BipedEntityModel<AbstractIMZombieEntity> normalModel;
    protected final LargeBipedEntityModel<AbstractIMZombieEntity> bigModel;

    public AbstractIMZombieEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ZombieEntityModel(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5F);
        normalModel = model;
        bigModel = new BigZombieEntityModel(LargeBipedEntityModel.getTexturedModelData(Dilation.NONE, 0).createModel());

        addFeature(new ArmorFeature(this,
                new ZombieEntityModel(ctx.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieEntityModel(ctx.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)),
                ctx.getModelManager(),
                false
        ));
        addFeature(new ArmorFeature(this,
                new BigZombieEntityModel(LargeBipedEntityModel.getTexturedModelData(new Dilation(0.5F), 0).createModel()),
                new BigZombieEntityModel(LargeBipedEntityModel.getTexturedModelData(new Dilation(1), 0).createModel()),
                ctx.getModelManager(),
                true
        ));
    }

    @Override
    public void render(AbstractIMZombieEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertices, int light) {
        this.model = isBrute(entity) ? bigModel : normalModel;
        super.render(entity, yaw, tickDelta, matrices, vertices, light);
    }

    protected boolean isBrute(AbstractIMZombieEntity entity) {
        return entity.getTextureId() == 3 || entity.getTextureId() == 6;
    }

    @Override
    protected void scale(AbstractIMZombieEntity entity, MatrixStack matrices, float amount) {
        float scale = entity.scaleAmount();
        matrices.scale(scale, (2 + scale) / 3F, scale);
    }

    protected List<Identifier> getTextures() {
        return TEXTURES;
    }

    @Override
    public Identifier getTexture(AbstractIMZombieEntity entity) {
        int id = entity.getTextureId();
        List<Identifier> textures = getTextures();
        return textures.get(id >= textures.size() ? 0 : id);
    }

    private final class ArmorFeature extends ArmorFeatureRenderer<AbstractIMZombieEntity, BipedEntityModel<AbstractIMZombieEntity>, BipedEntityModel<AbstractIMZombieEntity>> {
        private final boolean isBig;

        public ArmorFeature(
                FeatureRendererContext<AbstractIMZombieEntity, BipedEntityModel<AbstractIMZombieEntity>> context,
                BipedEntityModel<AbstractIMZombieEntity> innerModel,
                BipedEntityModel<AbstractIMZombieEntity> outerModel,
                BakedModelManager bakery,
                boolean isBig) {
            super(context, innerModel, outerModel, bakery);
            this.isBig = isBig;
        }

        @Override
        public void render(MatrixStack matrices, VertexConsumerProvider vertices, int light, AbstractIMZombieEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
            if (isBrute(entity) == isBig) {
                super.render(matrices, vertices, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
            }
        }
    }

    private static final class ZombieEntityModel extends BipedEntityModel<AbstractIMZombieEntity> {
        public ZombieEntityModel(ModelPart root) {
            super(root);
        }

        @Override
        public void setAngles(AbstractIMZombieEntity hostileEntity, float f, float g, float h, float i, float j) {
            super.setAngles(hostileEntity, f, g, h, i, j);
            CrossbowPosing.meleeAttack(leftArm, rightArm, isAttacking(hostileEntity), handSwingProgress, h);
        }

        public boolean isAttacking(AbstractIMZombieEntity entity) {
            return entity.isAttacking();
        }
    }

    private static final class BigZombieEntityModel extends LargeBipedEntityModel<AbstractIMZombieEntity> {
        public BigZombieEntityModel(ModelPart root) {
            super(root);
        }

        @Override
        public void setAngles(AbstractIMZombieEntity hostileEntity, float f, float g, float h, float i, float j) {
            super.setAngles(hostileEntity, f, g, h, i, j);
            CrossbowPosing.meleeAttack(leftArm, rightArm, isAttacking(hostileEntity), handSwingProgress, h);
        }

        public boolean isAttacking(AbstractIMZombieEntity entity) {
            return entity.isAttacking();
        }
    }
}