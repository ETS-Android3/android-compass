package pl.wilenskid.compass;

import android.content.Context;

public class DirectionFactory {

    private final Context context;
    private final Direction[] directions;

    public DirectionFactory(Context context) {
        this.context = context;
        this.directions = new Direction[] {
                new Direction("North", "N", new int[] {0, 360}),
                new Direction("North East", "NE", new int[] {45}),
                new Direction("East", "E", new int[] {90}),
                new Direction("South East", "SE", new int[] {135}),
                new Direction("South", "S", new int[] {180}),
                new Direction("South West", "SW", new int[] {225}),
                new Direction("West", "W", new int[] {270}),
                new Direction("North West", "NW", new int[] {315}),
        };
    }

    public Direction getDirection(int targetAngle) {
        int closestAngle = 0;

        for (Direction direction : directions) {
            int closestAngleLength = Math.abs(targetAngle - closestAngle);

            for (int angle : direction.getAngles()) {
                int currentAngleLength = Math.abs(targetAngle - angle);

                if (currentAngleLength < closestAngleLength) {
                    closestAngle = angle;
                }
            }
        }

        for (Direction direction : directions) {
            if (direction.hasAngle(closestAngle)) {
                return direction;
            }
        }

        return null;
    }

}
