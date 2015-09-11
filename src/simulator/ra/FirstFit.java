package simulator.ra;

import java.util.List;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.physical.DataCenter;

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

    public int[] nextServerSys(List<Integer> chassisList) {
        int[] retValue = new int[2];
        retValue[0] = -2;
        retValue[1] = -2;

        for (int l = 0; l < chassisList.size(); l++) {
            for (int k = 0; k < dataCenter.getChassisSet().get(chassisList.get(l)).getServers().size(); k++) {
                if (dataCenter.getChassisSet().get(chassisList.get(l)).getServers().get(k).isNotSystemAssigned()) {
                    retValue[0] = chassisList.get(l); // chassis id
                    retValue[1] = k; // Server ID
                    return retValue;
                }
            }
        }
        return retValue;
    }

    public int[] allocateSystemLevelServer(List<BladeServer> ComputeNodeList, int list[]) {
        int j = 0, i = 0;
        int totalReadyNodes = 0;
        for (i = 0; i < list.length; i++) {
            list[i] = -2;
        }
        for (int k = 0; k < ComputeNodeList.size(); k++) {
            if (ComputeNodeList.get(k).isRunningNormal()) {
                totalReadyNodes++;
            }
        }
        if (totalReadyNodes < list.length) {
            return list; // there is not enought ready node to accept this job
        } // in CS which compute node is ready just save its index
        i = 0;
        for (j = 0; j < list.length; j++) {
            for (; i < ComputeNodeList.size(); i++) {
                if (ComputeNodeList.get(i).isRunningNormal()) {
                    list[j] = i++;
                    break;
                }
            }
        }

        return list;
    }
}
