package simulator.physical;

import java.util.ArrayList;
import java.util.List;

public class RackPOD {
    
    private int id;
    private List<ChassisPOD> chassisPODs = new ArrayList<ChassisPOD>();

    public void setID(int id) {
        this.id = id;
    }
    
    public int getID() {
        return id;
    }

    public void appendChassis(ChassisPOD chassisPOD) {
        this.chassisPODs.add(chassisPOD);
    }
    
    public List<ChassisPOD> getChassisPODs() {
        return chassisPODs;
    }

}
