package simulator.physical;

import java.util.ArrayList;
import java.util.List;

public class ChassisPOD {

    private int chassisID;
    private int rackID;
    private DataCenterEntityID id;
    private String chassisType;
    private String bladeType;
    private List<BladeServerPOD> serverPOD = new ArrayList<BladeServerPOD>();

    public ChassisPOD() {
    }

    public ChassisPOD(ChassisPOD chassisPOD) {
        chassisID = chassisPOD.chassisID;
        rackID = chassisPOD.rackID;
        chassisType = chassisPOD.chassisType;
        bladeType = chassisPOD.bladeType;
        for(BladeServerPOD bladeServerPOD : chassisPOD.serverPOD) {
            serverPOD.add(new BladeServerPOD(bladeServerPOD));
        }
    }

    public void setChassisType(String chassisType) {
        this.chassisType = chassisType;
    }

    public String getChassisType() {
        return chassisType;
    }

    public void appendServerPOD(BladeServerPOD bladeServer) {
        serverPOD.add(bladeServer);
    }

    public List<BladeServerPOD> getServerPODs() {
        return serverPOD;
    }

    public int getChassisID() {
        return chassisID;
    }

    public void setChassisID(int chassisID) {
        this.chassisID = chassisID;
        for(BladeServerPOD bladeServerPOD : serverPOD) {
            bladeServerPOD.setChassisID(this.chassisID);
        }
    }

    public int getRackID() {
        return rackID;
    }

    public void setRackID(int rackID) {
        this.rackID = rackID;
        for(BladeServerPOD bladeServerPOD : serverPOD) {
            bladeServerPOD.setRackID(this.rackID);
        }
    }

    public void setBladeType(String bladeType) {
        this.bladeType = bladeType;
    }

    public String getBladeType() {
        return bladeType;
    }

    public void setID(DataCenterEntityID id) {
        this.id = id;
    }
    
    public DataCenterEntityID getID() {
        return id;
    }
}
