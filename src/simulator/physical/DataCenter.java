package simulator.physical;

import java.util.ArrayList;
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
    private Map<DataCenterEntityID, Rack> racks = new HashMap<DataCenterEntityID, Rack>();
    private int redTemperature;
    private double[][] D;
    private DataCenterAM am;
    private ActivitiesLogger activitiesLogger;
    private List<Chassis> allChassis = new ArrayList<Chassis>();

    private Environment environment;

    public DataCenter(DataCenterPOD dataCenterPOD, DataCenterAM dataCenterAM, ActivitiesLogger activitiesLogger,
            Environment environment) {
        this.activitiesLogger = activitiesLogger;
        am = dataCenterAM;
        this.environment = environment;
        for (RackPOD rackPOD : dataCenterPOD.getRackPODs()) {
            racks.put(rackPOD.getID(), new Rack(rackPOD, environment));
        }
        redTemperature = dataCenterPOD.getRedTemperature();
        D = dataCenterPOD.getD();
    }

    /**
     * Calculate Power using Equation 6 from doi:10.1016/j.comnet.2009.06.008
     */
    public void calculatePower() {
        allChassis.clear();
        for (Rack rack : racks.values()) {
            allChassis.addAll(rack.getChassis());
        }
        
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
        
        int m = allChassis.size();
        double computingPower = 0;
        double[] temperature = new double[m];
        for (Chassis curretChassis : allChassis) {
            final double chassisComputingPower = curretChassis.power();
            activitiesLogger.write(curretChassis.getID() + " "+ chassisComputingPower + "\n");
            computingPower = computingPower + chassisComputingPower;
        }

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                temperature[i] = temperature[i] + D[i][j] * allChassis.get(j).power();
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
            for(Rack rack : racks.values()) {
                racksStats.add((RackStats)rack.getStats());
            }
            
            return racksStats;
        }
    }

    public DataCenterStats getStats() { 
        return new DataCenterStats();
    }
}
