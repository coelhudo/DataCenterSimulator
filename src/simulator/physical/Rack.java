package simulator.physical;

import java.util.ArrayList;
import java.util.List;

import simulator.Environment;

public class Rack {

    private List<Chassis> chassis = new ArrayList<Chassis>();
    private int id;
    
    public Rack(RackPOD rackPOD, Environment environment) {
        id = rackPOD.getID();
        for (ChassisPOD chassisPOD : rackPOD.getChassisPODs()) {
            chassis.add(new Chassis(chassisPOD, environment));
        }
    }
    
    public int getRackID() {
        return id;
    }
    
    public List<Chassis> getChassis() {
        return chassis;
    }
}