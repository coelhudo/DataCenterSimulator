package simulator.physical;

import java.util.List;

public class Chassis {

    private List<BladeServer> servers;
    private int chassisID, rackId;
    private String chassisType;
    
    public Chassis(ChassisPOD chassisPOD, int idArg) {
        // if it is -1 means this chassis is just a template and not assigned
        // yet
        servers = chassisPOD.getServers();
        chassisType = chassisPOD.getChassisType();
        chassisID = idArg;
    }

    public List<BladeServer> getServers() {
        return servers;
    }

    public int getRackID() {
        return rackId;
    }

    public void setRackID(int rackId) {
        this.rackId = rackId;
    }

    public int getChassisID() {
        return chassisID;
    }

    public boolean isReady() {
        int RDY = 0;
        for (BladeServer bladeServer : servers) {
            RDY = RDY + bladeServer.getReady();
        }
        if (RDY == 0) {
            return false;
        } else {
            return true;
        }
    }

    double power() {
        double pw = 0;
        for (BladeServer bladeServer : servers) {
            pw = pw + bladeServer.getPower();
        }
        // pw=(cpus*a/100)+w*servers.size();
        // LOGGER.info("powercost= " + (int)pw+"\t"+cpus);
        return pw;
    }

    protected String getChassisType() {
        return chassisType;
    }
}
