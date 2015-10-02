package simulator.physical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simulator.Environment;
import simulator.physical.Chassis.ChassisStats;

public class Rack extends DataCenterEntity {

    private Map<DataCenterEntityID, Chassis> chassis = new HashMap<DataCenterEntityID, Chassis>();
    
    public Rack(RackPOD rackPOD, Environment environment) {
        super(rackPOD.getID());
        for (ChassisPOD chassisPOD : rackPOD.getChassisPODs()) {
            Chassis currentChassis = new Chassis(chassisPOD, environment);
            chassis.put(chassisPOD.getID(), currentChassis);
        }
    }
    
    public Collection<Chassis> getChassis() {
        return Collections.unmodifiableCollection(chassis.values());
    }
    
    public Chassis getChassis(DataCenterEntityID id) {
        return chassis.get(id);
    }

    public class RackStats extends DataCenterEntityStats {
        private List<ChassisStats> chassisStats = new ArrayList<ChassisStats>();
        
        public RackStats() {
            for(Chassis currentChassis : chassis.values()) {
                chassisStats.add((ChassisStats)currentChassis.getStats());
            }
        }
        
        public List<ChassisStats> getChassisStats() {
            return chassisStats;
        }
   }
    
    @Override
    public DataCenterEntityStats getStats() {  
        return new RackStats();
    }
}