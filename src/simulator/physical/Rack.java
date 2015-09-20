package simulator.physical;

import java.util.ArrayList;
import java.util.List;

import simulator.Environment;

public class Rack extends DataCenterEntity {

    private List<Chassis> chassis = new ArrayList<Chassis>();
    private int rackID;
    
    public Rack(RackPOD rackPOD, Environment environment) {
        super(rackPOD.getID());
        rackID = rackPOD.getRackID();
        for (ChassisPOD chassisPOD : rackPOD.getChassisPODs()) {
            chassis.add(new Chassis(chassisPOD, environment));
        }
    }
    
    public int getRackID() {
        return rackID;
    }
    
    public List<Chassis> getChassis() {
        return chassis;
    }
}