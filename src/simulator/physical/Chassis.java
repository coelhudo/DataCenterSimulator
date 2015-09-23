package simulator.physical;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import simulator.Environment;

public class Chassis extends DataCenterEntity {

    private final Map<DataCenterEntityID, BladeServer> servers = new HashMap<DataCenterEntityID, BladeServer>();
    private String chassisType;
    
    public Chassis(ChassisPOD chassisPOD, Environment environment) {
        super(chassisPOD.getID());
        chassisType = chassisPOD.getChassisType();
        for (BladeServerPOD bladeServerPOD : chassisPOD.getServerPODs()) {
            BladeServer bladeServer = new BladeServer(bladeServerPOD, environment);
            servers.put(bladeServer.getID(), bladeServer);
        }
    }
    
    public BladeServer getServer(DataCenterEntityID id) {
        return servers.get(id);
    }

    public Collection<BladeServer> getServers() {
        return Collections.unmodifiableCollection(servers.values());
    }

    /**
     * This model does not take into account the power consumption of the
     * chassis, just the blades.
     * @return total power consumed
     */
    public double power() {
        double pw = 0;
        for (BladeServer bladeServer : servers.values()) {
            pw = pw + bladeServer.getPower();
        }
        return pw;
    }
    
    public BladeServer getNextNotAssignedBladeServer() {
        for (BladeServer bladeServer : servers.values()) {
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

    @Override
    public String getStats() {
        String stats = "chassis: " + getID().toString() + "-> ";
        for(BladeServer bladeServer : servers.values()) {
            stats += bladeServer.getStats() + "; ";
        }
        
        stats += "\n";
        
        return stats;
    }
}
