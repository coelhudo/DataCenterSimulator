package simulator.physical;

import java.util.ArrayList;
import java.util.List;

public class ChassisPOD {

    private int id;
    private int rackID;
    private String chassisType;
    private String bladeType;
    private List<BladeServerPOD> serverPOD = new ArrayList<BladeServerPOD>();

    public ChassisPOD() {
    }

    public ChassisPOD(ChassisPOD chassisPOD) {
        id = chassisPOD.id;
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

    public int getID() {
        return id;
    }

    public void setID(int id) {
        this.id = id;
        for(BladeServerPOD bladeServerPOD : serverPOD) {
            bladeServerPOD.setChassisID(this.id);
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
}
