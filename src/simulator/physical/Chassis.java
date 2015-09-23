package simulator.physical;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import simulator.Environment;

public class Chassis extends DataCenterEntity {

    private final Map<DataCenterEntityID, BladeServer> availableServers = new HashMap<DataCenterEntityID, BladeServer>();
    private String chassisType;
    
    public Chassis(ChassisPOD chassisPOD, Environment environment) {
        super(chassisPOD.getID());
        chassisType = chassisPOD.getChassisType();
        for (BladeServerPOD bladeServerPOD : chassisPOD.getServerPODs()) {
            BladeServer bladeServer = new BladeServer(bladeServerPOD, environment);
            availableServers.put(bladeServer.getID(), bladeServer);
        }
    }
    
    public BladeServer getServer(DataCenterEntityID id) {
        return availableServers.get(id);
    }

    public Collection<BladeServer> getServers() {
        return availableServers.values();
    }

    /**
     * This model does not take into account the power consumption of the
     * chassis, just the blades.
     * @return total power consumed
     */
    public double power() {
        double pw = 0;
        for (BladeServer bladeServer : availableServers.values()) {
            pw = pw + bladeServer.getPower();
        }
        return pw;
    }
    
    public BladeServer getNextNotAssignedBladeServer() {
        for (BladeServer bladeServer : availableServers.values()) {
            if (bladeServer.isNotSystemAssigned()) {
                return bladeServer;
            }
        }
        return null;
    }

    public String getChassisType() {
        return chassisType;
    }
    
    @Override
    public String toString() {
        return getID().toString();
    }
}
