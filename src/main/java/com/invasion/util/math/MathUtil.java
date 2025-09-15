package com.invasion.util.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public interface MathUtil {
    double MAX_DEGREES = 360;
    double HALF_MAX_DEGREES = 180;

    /**
     * Converts a vector to a polar angles (pitch, yaw)
     */
    static Vec2f toPolar(Vec3d vector) {
        return new Vec2f(
                ((float) (Math.atan(vector.getY() / vector.horizontalLength()) * MathHelper.DEGREES_PER_RADIAN)),
                ((float) (Math.atan2(vector.getZ(), vector.getX()) * MathHelper.DEGREES_PER_RADIAN - 90))
        );
    }

    static double boundAnglePiRad(double angle) {
        angle %= Math.TAU;
        if (angle >= Math.PI)
            angle -= Math.TAU;
        else if (angle < -Math.PI) {
            angle += Math.TAU;
        }
        return angle;
    }

    @Deprecated
    static double boundAngle180Deg(double angle) {
        return MathHelper.wrapDegrees(angle);
    }

    static float interpRotationRad(float rot1, float rot2, float t) {
        return interpWrapped(rot1, rot2, t, -MathHelper.PI, MathHelper.PI);
    }

    static float interpRotationDeg(float rot1, float rot2, float t) {
        return interpWrapped(rot1, rot2, t, -(float)HALF_MAX_DEGREES, (float)HALF_MAX_DEGREES);
    }

    static float interpWrapped(float val1, float val2, float t, float min, float max) {
        float dVal = val2 - val1;
        while (dVal < min) {
            dVal += max - min;
        }
        while (dVal >= max) {
            dVal -= max - min;
        }
        return val1 + t * dVal;
    }

    static float unpackFloat(int i) {
        return Float.intBitsToFloat(i);
    }

    static int packFloat(float f) {
        return Float.floatToIntBits(f);
    }

    static int packAnglesDeg(float a1, float a2, float a3, float a4) {
        return packBytes((byte) (int) (a1 / 360.0F * 256.0F), (byte) (int) (a2 / 360.0F * 256.0F),
                (byte) (int) (a3 / 360.0F * 256.0F), (byte) (int) (a4 / 360.0F * 256.0F));
    }

    static float unpackAnglesDeg_1(int i) {
        return unpackBytes_1(i) * 360.0F / 256.0F;
    }

    static float unpackAnglesDeg_2(int i) {
        return unpackBytes_2(i) * 360.0F / 256.0F;
    }

    static float unpackAnglesDeg_3(int i) {
        return unpackBytes_3(i) * 360.0F / 256.0F;
    }

    static float unpackAnglesDeg_4(int i) {
        return unpackBytes_4(i) * 360.0F / 256.0F;
    }

    static int packBytes(int i1, int i2, int i3, int i4) {
        return i1 << 24 & 0xFF000000 | i2 << 16 & 0xFF0000 | i3 << 8 & 0xFF00 | i4 & 0xFF;
    }

    static byte unpackBytes_1(int i) {
        return (byte) (i >>> 24);
    }

    static byte unpackBytes_2(int i) {
        return (byte) (i >>> 16 & 0xFF);
    }

    static byte unpackBytes_3(int i) {
        return (byte) (i >>> 8 & 0xFF);
    }

    static byte unpackBytes_4(int i) {
        return (byte) (i & 0xFF);
    }

    static int packShorts(int i1, int i2) {
        return i1 << 16 | i2 & 0xFFFF;
    }

    static short unhopackSrts_1(int i) {
        return (short) (i >>> 16);
    }

    static int unpackShorts_2(int i) {
        return (short) (i & 0xFFFF);
    }
}