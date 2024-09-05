package com.invasion.client.particle;

import org.joml.Quaternionf;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

public class DazeParticle extends SpriteBillboardParticle {

    public static ParticleFactory<SimpleParticleType> factory(SpriteProvider spriteProvider) {
        return (type, world, x, y, z, dX, dY, dZ) -> new DazeParticle(world, x, y, z, spriteProvider);
    }

    DazeParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
        maxAge = 10;
        gravityStrength = 0;
        setSpriteForAge(spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public BillboardParticle.Rotator getRotator() {
        return BillboardParticle.Rotator.Y_AND_W_ONLY;
    }

    @Override
    public void buildGeometry(VertexConsumer buffer, Camera camera, float tickDelta) {
        super.buildGeometry(buffer, camera, tickDelta);
    }

    @Override
    protected void method_60374(VertexConsumer buffer, Quaternionf rotation, float x, float y, float z, float tickDelta) {
        for (int i = 0; i < 6; i++) {
            float time = (age + (i * 10) + tickDelta) / 10F;
            float dX = MathHelper.sin(time) * 0.5F;
            float dZ = MathHelper.cos(time) * 0.5F;
            float dY = MathHelper.sin(time * 2) * 0.1F;

            super.method_60374(buffer, rotation, x + dX, y + dY, z + dZ, tickDelta);
        }
    }

}
