package simulator.physical;

public class Cooler {

    double getCOP(double temperature) {
        double COP = 0.0068 * temperature * temperature + 0.0008 * temperature + 0.458;
        return COP;
    }
}
