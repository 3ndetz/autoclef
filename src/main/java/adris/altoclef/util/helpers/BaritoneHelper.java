package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.Vec3d;

public class BaritoneHelper {

    /**
     * Use whenever accessing Minecraft data (from ClientWorld or ClientPlayerEntity) in baritone
     */
    public static final Object MINECRAFT_LOCK = new Object();

    public static double calculateGenericHeuristic(Vec3d start, Vec3d target) {
        return calculateGenericHeuristic(start.x, start.y, start.z, target.x, target.y, target.z);
    }

    /**
     * Returns baritone's approximation heuristic from start to target
     */
    public static double calculateGenericHeuristic(double xStart, double yStart, double zStart, double xTarget, double yTarget, double zTarget) {
        double xDiff = xTarget - xStart;
        int yDiff = (int) yTarget - (int) yStart;
        double zDiff = zTarget - zStart;
        return GoalBlock.calculate(xDiff, yDiff < 0 ? yDiff + 1 : yDiff, zDiff);
    }

    public static double ticksToSeconds(double ticks) {
        return ticks / 20.0;
    }

    public static String getHeuristicString(AltoClef mod) {
        double heuristic = mod.getCurrentBaritoneHeuristic();
        if (heuristic<Double.POSITIVE_INFINITY) {
            return String.format("following path (%.0fs remains)", ticksToSeconds(heuristic));
        } else {
            return "UNREACHABLE PATH! CHANGE TASK NOW!";
        }
    }

}
