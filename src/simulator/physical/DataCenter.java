package simulator.physical;

import java.util.ArrayList;
import java.util.List;

import simulator.Environment;
import simulator.am.DataCenterAM;
import simulator.utils.ActivitiesLogger;

public class DataCenter {

    private int overRed = 0;
    private double totalPowerConsumption = 0;
    private List<Chassis> chassisSet = new ArrayList<Chassis>();
    private List<Rack> racks = new ArrayList<Rack>();
    private int redTemperature;
    private double[][] D;
    private DataCenterAM am;
    private ActivitiesLogger activitiesLogger;

    private Environment environment;

    public DataCenter(DataCenterPOD dataCenterPOD, DataCenterAM dataCenterAM, ActivitiesLogger activitiesLogger,
            Environment environment) {
        this.activitiesLogger = activitiesLogger;
        am = dataCenterAM;
        this.environment = environment;
        for (RackPOD rackPOD : dataCenterPOD.getRackPODs()) {
            racks.add(new Rack(rackPOD, environment));
            for (ChassisPOD chassisPOD : rackPOD.getChassisPODs()) {
                Chassis chassis = new Chassis(chassisPOD, environment);
                chassisSet.add(chassis);
            }
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

    /**
     * Calculate Power using Equation 6 from doi:10.1016/j.comnet.2009.06.008
     */
    public void calculatePower() {
        int m = chassisSet.size();
        double computingPower = 0;
        double[] temperature = new double[m];
        for (Chassis chassis : chassisSet) {
            final double chassisComputingPower = chassis.power();
            activitiesLogger.write((int) chassis.power() + "\t");
            computingPower = computingPower + chassisComputingPower;
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temperature[i] = temperature[i] + D[i][j] * chassisSet.get(j).power();
            }
        }

        double maxTemp = temperature[0];
        for (int i = 0; i < m; i++) {
            if (maxTemp < temperature[i]) {
                maxTemp = temperature[i];
            }
        }

        maxTemp = redTemperature - maxTemp;

        if (maxTemp <= 0) {
            am.setSlowDownFromCooler(true);
            overRed++;
        } else {
            am.setSlowDownFromCooler(false);
        }

        final double cop = Cooler.getCOP(maxTemp);
        final double currentTotalEnergyConsumption = computingPower * (1 + 1.0 / cop);

        activitiesLogger.write(((int) currentTotalEnergyConsumption) + "\t" + (int) computingPower + "\t"
                + environment.getCurrentLocalTime() + "\n");
        totalPowerConsumption = totalPowerConsumption + currentTotalEnergyConsumption;
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

    @Deprecated
    public BladeServer getServer(int i) {
        return chassisSet.get(getChasisIndex(i)).getServers().get(getServerIndex(i));
    }

    @Deprecated
    public BladeServer getServer(int indexChassis, int indexServer) {
        return chassisSet.get(indexChassis).getServers().get(indexServer);
    }
    
    public BladeServer getServer(DataCenterEntityID id) {
        return chassisSet.get(getChasisIndex(id.getServerID())).getServers().get(getServerIndex(id.getServerID()));
    }

    public List<Chassis> getChassisSet() {
        return chassisSet;
    }

    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }

    public List<Rack> getRacks() {
        return racks;
    }
}
