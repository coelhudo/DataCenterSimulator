package simulator.physical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import simulator.Environment;
import simulator.am.DataCenterAM;
import simulator.physical.Rack.RackStats;
import simulator.utils.ActivitiesLogger;

public class DataCenter {

    private int overRed = 0;
    private double totalPowerConsumption = 0;
    private final Map<DataCenterEntityID, Rack> racks = new HashMap<DataCenterEntityID, Rack>();
    private final int redTemperature;
    private final double[][] D;
    private final DataCenterAM am;
    private final ActivitiesLogger activitiesLogger;
    private final List<Chassis> allChassis;
    private final DataCenterStats stats;
    private final double[] temperatures;
    private final double[] powers;

    private Environment environment;

    public DataCenter(DataCenterPOD dataCenterPOD, DataCenterAM dataCenterAM, ActivitiesLogger activitiesLogger,
            Environment environment) {
        this.activitiesLogger = activitiesLogger;
        am = dataCenterAM;
        this.environment = environment;
        allChassis = new ArrayList<Chassis>();
        for (RackPOD rackPOD : dataCenterPOD.getRackPODs()) {
            Rack rack = new Rack(rackPOD, environment);
            racks.put(rackPOD.getID(), rack);
            allChassis.addAll(rack.getChassis());
        }
        redTemperature = dataCenterPOD.getRedTemperature();
        D = dataCenterPOD.getD();
        this.stats = new DataCenterStats();

        sortAllChassis();
        temperatures = new double[allChassis.size()];
        powers = new double[allChassis.size()];
    }

    private void sortAllChassis() {
        /**
         * The heat matrix is order dependent. Still need to fix this.
         */
        class ChassisComparator implements Comparator<Chassis> {

            @Override
            public int compare(Chassis o1, Chassis o2) {
                return o1.getID().compareTo(o2.getID());
            }

        }

        Collections.sort(allChassis, new ChassisComparator());

    }

    /**
     * Calculate Power using Equation 6 from doi:10.1016/j.comnet.2009.06.008
     */
    public void calculatePower() {
        final int m = allChassis.size();
        double computingPower = 0;
        Arrays.fill(temperatures, 0);
        int allChassisIndex = 0;
        for (Chassis curretChassis : allChassis) {
            final double chassisComputingPower = curretChassis.power();
            powers[allChassisIndex] = chassisComputingPower;
            allChassisIndex++;
            activitiesLogger.write(curretChassis.getID() + " " + chassisComputingPower + "\n");
            computingPower = computingPower + chassisComputingPower;
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temperatures[i] = temperatures[i] + D[i][j] * powers[j];
            }
        }

        double maxTemp = temperatures[0];
        for (int i = 0; i < m; i++) {
            if (maxTemp < temperatures[i]) {
                maxTemp = temperatures[i];
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

        activitiesLogger.write("\n" + (int) currentTotalEnergyConsumption + "\t" + (int) computingPower + "\t"
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

    public List<Chassis> getChassisFromRacks(Set<DataCenterEntityID> rackIDs) {
        List<Chassis> allChassisFromRacks = new ArrayList<Chassis>();
        for (DataCenterEntityID rackID : rackIDs) {
            allChassisFromRacks.addAll(racks.get(rackID).getChassis());
        }
        return allChassisFromRacks;
    }

    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }

    public Collection<Rack> getRacks() {
        return Collections.unmodifiableCollection(racks.values());
    }

    public Rack getRack(DataCenterEntityID id) {
        return racks.get(id);
    }

    public class DataCenterStats {
        public List<RackStats> getRacksStats() {
            List<RackStats> racksStats = new ArrayList<RackStats>();
            for (Rack rack : racks.values()) {
                racksStats.add((RackStats) rack.getStats());
            }

            return racksStats;
        }
    }

    public DataCenterStats getStats() {
        return stats;
    }

    public boolean newStatsAvailable() {
        for (Rack rack : racks.values()) {
            if(rack.newStatsAvailable()) {
                return true;
            }
        }
        return false;
    }
}
