package simulator.physical;

import java.util.ArrayList;
import java.util.List;

import simulator.Environment;

public class Chassis {

    private List<BladeServer> servers = new ArrayList<BladeServer>();
    private int chassisID, rackID;
    private String chassisType;

    public Chassis(ChassisPOD chassisPOD, Environment environment) {
        chassisType = chassisPOD.getChassisType();
        chassisID = chassisPOD.getID();
        rackID = chassisPOD.getRackID();
        for (BladeServerPOD bladeServerPOD : chassisPOD.getServerPODs()) {
            BladeServer bladeServer = new BladeServer(bladeServerPOD, environment);
            servers.add(bladeServer);
        }
    }

    public List<BladeServer> getServers() {
        return servers;
    }

    public int getRackID() {
        return rackID;
    }

    public int getChassisID() {
        return chassisID;
    }

    /**
     * This model does not take into account the power consumption of the
     * chassis, just the blades.
     * @return total power consumed
     */
    public double power() {
        double pw = 0;
        for (BladeServer bladeServer : servers) {
            pw = pw + bladeServer.getPower();
        }
        return pw;
    }

    protected String getChassisType() {
        return chassisType;
    }
}
