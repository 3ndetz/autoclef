package adris.altoclef.util.agent;

public class AgentInputBridge {
    public static boolean isAgentInputActive = false; // Включи это в true, когда бот работает
    
    private static double accumulatedDx = 0.0;
    private static double accumulatedDy = 0.0;

    public static void addDelta(double dx, double dy) {
        accumulatedDx += dx;
        accumulatedDy += dy;
    }

    // Забираем и обнуляем (атомарно для одного кадра)
    public static double[] consumeDeltas() {
        double dx = accumulatedDx;
        double dy = accumulatedDy;
        accumulatedDx = 0;
        accumulatedDy = 0;
        return new double[]{dx, dy};
    }
}