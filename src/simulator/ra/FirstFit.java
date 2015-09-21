package simulator.ra;

import java.util.ArrayList;
import java.util.List;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerCollectionOperations;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterEntityID;

/**
 *
 * @author fnorouz
 */
public class FirstFit extends ResourceAllocation {

    public FirstFit(Environment environment, DataCenter dataCenter) {
        super(environment, dataCenter);
    }

    public int nextServer(List<BladeServer> bs) {
        for (int j = 0; j < bs.size(); j++) {
            if (bs.get(j).isRunningNormal()) {
                return j;
            }
        }

        return -2;
    }

    public BladeServer nextServerSys(List<Integer> chassisList) {
        
        for (int l = 0; l < chassisList.size(); l++) {
            for (int k = 0; k < dataCenter.getChassisSet().get(chassisList.get(l)).getServers().size(); k++) {
                if (dataCenter.getChassisSet().get(chassisList.get(l)).getServers().get(k).isNotSystemAssigned()) {
                    return dataCenter.getChassisSet().get(chassisList.get(l)).getServers().get(k);
                }
            }
        }
        return null;
    }

    public List<BladeServer> allocateSystemLevelServer(List<BladeServer> availableBladeServers, int numberOfRequestedServers) {
        List<BladeServer> requestedBladeServers = null;
        
        if (BladeServerCollectionOperations.countRunningNormal(availableBladeServers) < numberOfRequestedServers) {
            return requestedBladeServers; 
        }
        
        requestedBladeServers = new ArrayList<BladeServer>();
        
        for (int j = 0; j < numberOfRequestedServers; j++) {
            for (BladeServer bladeServer : availableBladeServers) {
                if (bladeServer.isRunningNormal()) {
                    requestedBladeServers.add(bladeServer);
                    break;
                }
            }
        }

        return requestedBladeServers;
    }
}
