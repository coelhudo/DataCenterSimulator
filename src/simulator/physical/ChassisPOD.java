package simulator.physical;

import java.util.ArrayList;
import java.util.List;

public class ChassisPOD {

    private String chassisType;
    private List<BladeServer> servers = new ArrayList<BladeServer>();
    
    public void setChassisType(String chassisType) {
        this.chassisType = chassisType;
    }
    
    public String getChassisType() {
        return chassisType;
    }
    
    public void appendServer(BladeServer bladeServer) {
        servers.add(bladeServer);
    }
    
    public List<BladeServer> getServers() {
        return servers;
    }
}
