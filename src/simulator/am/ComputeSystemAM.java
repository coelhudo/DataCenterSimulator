package simulator.am;

import simulator.ComputeSystem;
import simulator.Simulator;

public class ComputeSystemAM extends GeneralAM {

    ComputeSystem CS;

    public ComputeSystemAM(ComputeSystem cs) {
        CS = cs;
    }

    @Override
    public void monitor() {
        getPercentageOfComputingPwr();
    }

    public double getPercentageOfComputingPwr() {
        double percnt = 0;
        int[] levels = {0, 0, 0};
        int index = 0;
        for (int j = 0; j < CS.getComputeNodeList().size(); j++) {
            if (CS.getComputeNodeList().get(j).getReady() != -1) //it is idle
            {
                index = CS.getComputeNodeList().get(j).getCurrentFreqLevel();
                levels[index]++;
            }
        }
        percnt = percnt + levels[0] + 2 * levels[1] + 3 * levels[2];
        return percnt;
    }

    @Override
    public void planning() {
    }

    void analysisGreen() {

        if (SLAViolationGen > 0) /*    Increase freq. of just fully utilized CPU nodes
        Activate just half of sleep nodes
        if(all nodes are busy and this system is not blocked)
        send(SOS, theParent)
         */ {
            for (int i = 0; i < CS.getComputeNodeList().size(); i++) {
                if (CS.getComputeNodeList().get(i).getReady() == 0) {
                    CS.getComputeNodeList().get(i).increaseFrequency();
                }
            }
            // Activate just half of sleep nodes
            int hlfNumofSlept = CS.numberofIdleNode() / 2;
            int tedad = 0;
            for (int i = 0; i < CS.getComputeNodeList().size(); i++) {
                if (CS.getComputeNodeList().get(i).getReady() == -1) {
                    System.out.println("CSys GR: " + "\tactive a Server!\t\t @" + Simulator.getInstance().getLocalTime() + "\tNumber of runinng:  " + CS.numberofRunningNode());
                    CS.getComputeNodeList().get(i).setReady(1);
                    CS.getComputeNodeList().get(i).setMips(1.4);
                    Simulator.getInstance().numberOfMessagesFromSytemToNodes++;
                    tedad++;
                }
                if (tedad == hlfNumofSlept) {
                    break;
                }
            }
//           //if(all nodes are busy and this system is not blocked)  send(SOS, theParent)
//           if(CS.numberofRunningNode()==CS.ComputeNodeList.size()&& !CS.blocked)
//               DataCenter.theDataCenter.AM.SoSCS[CS.priority]=1;
        }
        if (SLAViolationGen == 0) /*
         *   Decrease freq. of all nodes
        If node is ready and is not used make it sleep
         */ {
            //Decrease freq. of all nodes
            for (int i = 0; i < CS.getComputeNodeList().size(); i++) {
                if (CS.getComputeNodeList().get(i).getReady() > -1) {
                    CS.getComputeNodeList().get(i).decreaseFrequency();

                }
            }
            // If node is ready and is not used make it sleep
            for (int i = 0; i < CS.getComputeNodeList().size(); i++) {
                if (CS.getComputeNodeList().get(i).getActiveBatchList().isEmpty() && CS.getComputeNodeList().get(i).getBlockedBatchList().isEmpty() && CS.getComputeNodeList().get(i).getReady() > -1) {
                    Simulator.getInstance().numberOfMessagesFromSytemToNodes++;
                    CS.getComputeNodeList().get(i).setReady(-1);
                }
            }
        }
    }

    void analysisSLA() {
        /*
         * Increase freq. of all busy nodes
        Activate all sleep nodes
        if(all nodes are busy and this system is not blocked)
        send(SOS, theParent)
         */
        if (SLAViolationGen > 0) {

            for (int i = 0; i < CS.getComputeNodeList().size(); i++) {
                if (CS.getComputeNodeList().get(i).getReady() == 0) {
                    CS.getComputeNodeList().get(i).increaseFrequency();
                }
                if (CS.getComputeNodeList().get(i).getReady() == -1) {
                    Simulator.getInstance().numberOfMessagesFromSytemToNodes++;
                    CS.getComputeNodeList().get(i).setReady(1);
                }
            }
            //if(all nodes are busy and this system is not blocked)       send(SOS, theParent)
//           int numBusy=0;
//           for(int i=0;i<CS.ComputeNodeList.size();i++)
//                if(CS.ComputeNodeList.get(i).ready==0)
//                     numBusy++;
//           if(CS.ComputeNodeList.size()==numBusy && !CS.blocked)
//               DataCenter.theDataCenter.AM.SoSIS[CS.priority]=1;
//               
        }
    }

    @Override
    public void execution() {
    }

    @Override
    public void analysis(Object vilation) {
        if (Simulator.getInstance().getLocalTime() % Simulator.getInstance().epochApp != 0) {
            return;
        }
        Simulator.getInstance().numberOfMessagesFromDataCenterToSystem++; // one message for monitoring the variables from compute node in the compute system.
        SLAViolationGen = CS.getSLAviolation();///Main.epochApp;
//        if(strtg==Main.strategyEnum.Green)
        analysisGreen();
//        if(strtg==Main.strategyEnum.SLA)
//           analysisSLA();
    }
}
