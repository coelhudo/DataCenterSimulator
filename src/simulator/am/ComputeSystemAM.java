package simulator.am;

import java.util.logging.Logger;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.system.ComputeSystem;

public class ComputeSystemAM extends GeneralAM {

    private static final Logger LOGGER = Logger.getLogger(ComputeSystemAM.class.getName());

    private ComputeSystem computeSystem;
    private Environment environment;

    public ComputeSystemAM(ComputeSystem computeSytem, Environment environment) {
        this.computeSystem = computeSytem;
        this.environment = environment;
    }

    @Override
    public void monitor() {
        getPercentageOfComputingPwr();
    }

    public double getPercentageOfComputingPwr() {
        double percnt = 0;
        int[] levels = { 0, 0, 0 };
        int index = 0;
        for (int j = 0; j < computeSystem.getComputeNodeList().size(); j++) {
            if (computeSystem.getComputeNodeList().get(j).getReady() != -1) // it
                                                                            // is
                                                                            // idle
            {
                index = computeSystem.getComputeNodeList().get(j).getCurrentFreqLevel();
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

        if (getSLAViolationGen() > 0) {
            /*
             * Increase freq. of just fully utilized CPU nodes Activate just
             * half of sleep nodes if(all nodes are busy and this system is not
             * blocked) send(SOS, theParent)
             */
            for (BladeServer bladeServer : computeSystem.getComputeNodeList()) {
                if (bladeServer.getReady() == 0) {
                    bladeServer.increaseFrequency();
                }
            }
            // Activate just half of sleep nodes
            int hlfNumofSlept = computeSystem.numberOfIdleNode() / 2;
            int tedad = 0;
            for (BladeServer bladeServer : computeSystem.getComputeNodeList()) {
                if (bladeServer.getReady() == -1) {
                    LOGGER.info("CSys GR: " + "\tactive a Server!\t\t @" + environment.getCurrentLocalTime()
                            + "\tNumber of runinng:  " + computeSystem.numberOfRunningNode());
                    bladeServer.setStatusAsRunningNormal();
                    bladeServer.setMips(1.4);
                    environment.updateNumberOfMessagesFromSystemToNodes();
                    tedad++;
                }
                if (tedad == hlfNumofSlept) {
                    break;
                }
            }
            // //if(all nodes are busy and this system is not blocked) send(SOS,
            // theParent)
            // if(CS.numberofRunningNode()==CS.ComputeNodeList.size()&&
            // !CS.blocked)
            // DataCenter.theDataCenter.AM.SoSCS[CS.priority]=1;
        }
        if (getSLAViolationGen() == 0) {
            /*
             * Decrease freq. of all nodes If node is ready and is not used make
             * it sleep
             */
            // Decrease freq. of all nodes
            for (BladeServer bladeServer : computeSystem.getComputeNodeList()) {
                if (bladeServer.getReady() > -1) {
                    bladeServer.decreaseFrequency();
                }
            }
            // If node is ready and is not used make it sleep
            for (BladeServer bladeServer : computeSystem.getComputeNodeList()) {
                if (bladeServer.getActiveBatchList().isEmpty() && bladeServer.getBlockedBatchList().isEmpty()
                        && bladeServer.getReady() > -1) {
                    environment.updateNumberOfMessagesFromSystemToNodes();
                    bladeServer.setStatusAsIdle();
                }
            }
        }
    }

    void analysisSLA() {
        /*
         * Increase freq. of all busy nodes Activate all sleep nodes if(all
         * nodes are busy and this system is not blocked) send(SOS, theParent)
         */
        if (getSLAViolationGen() > 0) {

            for (int i = 0; i < computeSystem.getComputeNodeList().size(); i++) {
                if (computeSystem.getComputeNodeList().get(i).getReady() == 0) {
                    computeSystem.getComputeNodeList().get(i).increaseFrequency();
                }
                if (computeSystem.getComputeNodeList().get(i).getReady() == -1) {
                    environment.updateNumberOfMessagesFromSystemToNodes();
                    computeSystem.getComputeNodeList().get(i).setStatusAsRunningNormal();
                }
            }
            // if(all nodes are busy and this system is not blocked) send(SOS,
            // theParent)
            // int numBusy=0;
            // for(int i=0;i<CS.ComputeNodeList.size();i++)
            // if(CS.ComputeNodeList.get(i).ready==0)
            // numBusy++;
            // if(CS.ComputeNodeList.size()==numBusy && !CS.blocked)
            // DataCenter.theDataCenter.AM.SoSIS[CS.priority]=1;
            //
        }
    }

    @Override
    public void execution() {
    }

    @Override
    public void analysis(Object vilation) {
        if (environment.localTimeByEpoch()) {
            return;
        }
        environment.updateNumberOfMessagesFromDataCenterToSystem(); // one
        // message
        // for
        // monitoring
        // the
        // variables
        // from
        // compute
        // node in
        // the
        // compute
        // system.
        setSLAViolationGen(computeSystem.getSLAviolation());/// Main.epochApp;
        // if(strtg==Main.strategyEnum.Green)
        analysisGreen();
        // if(strtg==Main.strategyEnum.SLA)
        // analysisSLA();
    }
}
