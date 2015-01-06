/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.ra;

import simulator.EnterpriseSystem;
import simulator.InteractiveSystem;
import simulator.ra.ResourceAllocation;
import java.util.ArrayList;
import simulator.physical.BladeServer;

/**
 *
 * @author fnorouz
 */
public class FirstFit extends ResourceAllocation {

    @Override
    public int nextServer(ArrayList<BladeServer> bs) {
        for (int j = 0; j < bs.size(); j++) {
            if (bs.get(j).ready == 1) {
                return j;
            }
        }


        return -2;
    }

    @Override
    public int[] nextServerSys(ArrayList<Integer> chassisList) {
        int[] retValue = new int[2];
        retValue[0] = -2;
        retValue[1] = -2;

        ////////////////////////
        for (int l = 0; l < chassisList.size(); l++) {
            for (int k = 0; k < dc.chassisSet.get(chassisList.get(l)).servers.size(); k++) {
                if (dc.chassisSet.get(chassisList.get(l)).servers.get(k).ready == -3) {
                    retValue[0] = chassisList.get(l);  // chassis id
                    retValue[1] = k;  //Server ID
                    return retValue;
                }
            }
        }
        return retValue;
    }

    @Override
    public int[] allocateSystemLevelServer(ArrayList<BladeServer> ComputeNodeList, int list[]) {
        int j = 0, i = 0;
        int totalReadyNodes = 0;
        for (i = 0; i < list.length; i++) {
            list[i] = -2;
        }
        for (int k = 0; k < ComputeNodeList.size(); k++) {
            if (ComputeNodeList.get(k).ready == 1) {
                totalReadyNodes++;
            }
        }
        if (totalReadyNodes < list.length) {
            return list; //there is not enought ready node to accept this job
        }        //in CS which compute node is ready just save its index
        i = 0;
        for (j = 0; j < list.length; j++) {
            for (; i < ComputeNodeList.size(); i++) {
                if (ComputeNodeList.get(i).ready == 1)//& ComputeNodeList.get(i).activeBatchList.size()==0)
                {
                    list[j] = i++;
                    break;
                }
            }
        }

        return list;
    }

    @Override
    public void resourceAloc(InteractiveSystem WS) {
        // ToDo
    }

    @Override
    public void resourceAloc(EnterpriseSystem ES) {
        // ToDo
    }
}
