package simulator.physical;

import java.util.ArrayList;
import java.util.List;

import simulator.Environment;
import simulator.am.DataCenterAM;
import simulator.utils.ActivitiesLogger;

public class DataCenter {

    private int overRed = 0;
    private double totalPowerConsumption = 0;
    private Cooler cooler1 = new Cooler();
    private List<Chassis> chassisSet = new ArrayList<Chassis>();
    private int redTemperature;
    private double[][] D;
    private DataCenterAM am;
    private ActivitiesLogger activitiesLogger;

    private Environment environment;

    public DataCenter(DataCenterPOD dataCenterPOD, DataCenterAM dataCenterAM, ActivitiesLogger activitiesLogger, Environment environment) {
        this.activitiesLogger = activitiesLogger;
        am = dataCenterAM;
        this.environment = environment;
        for(ChassisPOD chassisPOD : dataCenterPOD.getChassisPOD()) {
            Chassis chassis = new Chassis(chassisPOD, environment);
            chassisSet.add(chassis);
        }
        redTemperature = dataCenterPOD.getRedTemperature();
        D = dataCenterPOD.getD();
    }

    private int getServerIndex(int i) {
        return i % chassisSet.get(0).getServers().size();
    }

    private int getChasisIndex(int i) {
        return i / chassisSet.get(0).getServers().size();
    }

    public void calculatePower() {
        int m = chassisSet.size();
        double computingPower = 0;
        double[] temprature = new double[m];
        for (int i = 0; i < m; i++) {
            double temp = chassisSet.get(i).power();
            activitiesLogger.write((int) temp + "\t");
            computingPower = computingPower + temp;
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temprature[i] = temprature[i] + D[i][j] * chassisSet.get(j).power();
            }
        }

        double maxTemp = temprature[0];
        for (int i = 0; i < m; i++) {
            if (maxTemp < temprature[i]) {
                maxTemp = temprature[i];
            }
        }

        maxTemp = redTemperature - maxTemp;

        if (maxTemp <= 0) {
            am.setSlowDownFromCooler(true);
            overRed++;

        } else {
            am.setSlowDownFromCooler(false);
        }

        final double cop = cooler1.getCOP(maxTemp);

        activitiesLogger.write(((int) (computingPower * (1 + 1.0 / cop))) + "\t" + (int) computingPower + "\t"
                + environment.getCurrentLocalTime() + "\n");
        totalPowerConsumption = totalPowerConsumption + computingPower * (1 + 1.0 / cop);
    }

    public void shutDownDC() {
        activitiesLogger.close();
    }

    public DataCenterAM getAM() {
        return am;
    }

    public int getOverRed() {
        return overRed;
    }

    public BladeServer getServer(int i) {
        return chassisSet.get(getChasisIndex(i)).getServers().get(getServerIndex(i));
    }

    public BladeServer getServer(int indexChassis, int indexServer) {
        return chassisSet.get(indexChassis).getServers().get(indexServer);
    }

    public List<Chassis> getChassisSet() {
        return chassisSet;
    }

    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }
}
