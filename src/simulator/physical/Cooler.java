package simulator.physical;

public final class Cooler {
    
    private Cooler() {
        
    }

    public static double getCOP(double temperature) {
        return 0.0068 * temperature * temperature + 0.0008 * temperature + 0.458;
    }
}
