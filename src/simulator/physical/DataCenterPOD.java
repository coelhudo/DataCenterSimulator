package simulator.physical;

import java.util.ArrayList;
import java.util.List;

public class DataCenterPOD {
    private List<ChassisPOD> chassisSet = new ArrayList<ChassisPOD>();
    private int redTemperature;
    private double[][] D;

    public List<ChassisPOD> getChassisPOD() {
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

    public void appendChassis(ChassisPOD chassis) {
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
