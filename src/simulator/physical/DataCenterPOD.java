package simulator.physical;

import java.util.ArrayList;
import java.util.List;

public class DataCenterPOD {
    private List<Chassis> chassisSet = new ArrayList<Chassis>();
    private int redTemperature;
    private double[][] D;

    public List<Chassis> getChassis() {
        return chassisSet;
    }

    public int getRedTemperature() {
        return redTemperature;
    }

    public void setRedTemperature(int redTemperature) {
        this.redTemperature = redTemperature;
    }

    public double[][] getD() {
        return D;
    }

    public void appendChassis(Chassis chassis) {
        chassisSet.add(chassis);
    }

    public void setD(int row, int column, double value) {
        if (D == null) { //FIXME: there is an order dependency, that is how I'm going to fix this by now
            final int m = chassisSet.size();
            D = new double[m][m];
        }

        D[row][column] = value;
    }

    public void clearChassis() {
        chassisSet.clear();
    }

    public int getNumberOfChassis() {
        return chassisSet.size();
    }
}
