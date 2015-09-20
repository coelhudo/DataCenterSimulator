package simulator.physical;

import java.util.ArrayList;
import java.util.List;

public class RackPOD {
    
    private int rackID;
    private List<ChassisPOD> chassisPODs = new ArrayList<ChassisPOD>();

    public void setID(int rackID) {
        this.rackID = rackID;
    }
    
    public int getRackID() {
        return rackID;
    }

    public void appendChassis(ChassisPOD chassisPOD) {
        this.chassisPODs.add(chassisPOD);
    }
    
    public List<ChassisPOD> getChassisPODs() {
        return chassisPODs;
    }

}
