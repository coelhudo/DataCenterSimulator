package simulator.physical;

import java.util.ArrayList;
import java.util.List;

public class RackPOD {
    
    private int rackID;
    private DataCenterEntityID id;
    private List<ChassisPOD> chassisPODs = new ArrayList<ChassisPOD>();

    public void setRackID(int rackID) {
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

    public void setID(DataCenterEntityID id) {
        this.id = id;
    }
    
    public DataCenterEntityID getID() {
        return id;
    }

}
