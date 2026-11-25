package com.gtlsupb.gtlsupb.common.data;

import org.joml.Vector3f;

public class RotationParams {

    private final Vector3f axis;
    private final float speed;
    private final float offset;

    public RotationParams(Vector3f axis, float speed, float offset) {
        this.axis = axis;
        this.speed = speed;
        this.offset = offset;
    }

    public Vector3f getAxis() {
        return axis;
    }

    public float getSpeed() {
        return speed;
    }

    public float getOffset() {
        return offset;
    }

    public float getAngle(float tick) {
        return (offset + tick * speed) % 360f;
    }
}
