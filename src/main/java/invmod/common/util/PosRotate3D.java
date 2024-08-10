package invmod.common.util;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record PosRotate3D (Vec3d position, Vector3fc rotation) {

    public PosRotate3D() {
        this(new Vec3d(0, 0, 0), new Vector3f());
    }

    public PosRotate3D lerp(float delta, PosRotate3D b) {
        return new PosRotate3D(lerp(delta, position, b.position()), lerp(delta, rotation, b.rotation(), new Vector3f()));
    }

    public PosRotate3D multiplyPosition(Vec3d positionMul) {
        return new PosRotate3D(position.multiply(positionMul), rotation);
    }

    public static Vec3d lerp(float delta, Vec3d a, Vec3d b) {
        return new Vec3d(
                MathHelper.lerp(delta, a.x, b.x),
                MathHelper.lerp(delta, a.y, b.y),
                MathHelper.lerp(delta, a.z, b.z)
        );
    }

    public static Vector3fc lerp(float delta, Vector3fc a, Vector3fc b, Vector3f into) {
        return b.sub(a, into).mul(delta).add(a);
    }
}