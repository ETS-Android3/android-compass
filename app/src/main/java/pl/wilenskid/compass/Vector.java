package pl.wilenskid.compass;

public class Vector {

    private final float[] value;

    public Vector(float[] value) {
        this.value = value;
    }

    public Vector() {
        this.value = new float[3];
    }

    public Vector multiply(Vector vector) {
        this.value[0] *= vector.getX();
        this.value[1] *= vector.getY();
        this.value[2] *= vector.getZ();
        return this;
    }

    public Vector multiply(float scale) {
        this.value[0] *= scale;
        this.value[1] *= scale;
        this.value[2] *= scale;
        return this;
    }

    public Vector add(Vector vector) {
        this.value[0] += vector.getX();
        this.value[1] += vector.getY();
        this.value[2] += vector.getZ();
        return this;
    }

    public float getX() {
        return value[0];
    }

    public float getY() {
        return value[1];
    }

    public float getZ() {
        return value[2];
    }

    public float[] asArray() {
        return new float[]{
                value[0],
                value[1],
                value[2]
        };
    }
}
