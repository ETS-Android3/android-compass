package pl.wilenskid.compass;

public class Direction {

    private final String fullName;
    private final String shortName;
    private final int[] angles;

    public Direction(String fullName, String shortName, int[] angles) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.angles = angles;
    }

    public boolean hasAngle(int targetAngle) {
        for (int angle : angles) {
            if (targetAngle == angle) {
                return true;
            }
        }

        return false;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public int[] getAngles() {
        return angles;
    }

}
