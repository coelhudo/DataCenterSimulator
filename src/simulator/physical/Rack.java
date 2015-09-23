package simulator.physical;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import simulator.Environment;

public class Rack extends DataCenterEntity {

    private Map<DataCenterEntityID, Chassis> chassiss = new HashMap<DataCenterEntityID, Chassis>();
    
    public Rack(RackPOD rackPOD, Environment environment) {
        super(rackPOD.getID());
        for (ChassisPOD chassisPOD : rackPOD.getChassisPODs()) {
            Chassis currentChassis = new Chassis(chassisPOD, environment);
            chassiss.put(chassisPOD.getID(), currentChassis);
        }
    }
    
    public Collection<Chassis> getChassis() {
        return chassiss.values();
    }
    
    public Chassis getChassis(DataCenterEntityID id) {
        return chassiss.get(id);
    }
}