package simulator.am;

import java.util.logging.Logger;

import com.google.inject.Inject;

import simulator.Environment;
import simulator.physical.BladeServer;
import simulator.system.ComputeSystem;
import simulator.system.GeneralSystem;

public class ComputeSystemAM extends SystemAM {

    private static final Logger LOGGER = Logger.getLogger(ComputeSystemAM.class.getName());

    private ComputeSystem computeSystem;

    @Inject
    public ComputeSystemAM(Environment environment) {
        super(environment);      
    }
   
    public void setManagedSystem(GeneralSystem managedSystem) {
        this.computeSystem = (ComputeSystem) managedSystem;
    }

    @Override
    public void monitor() {
        getPercentageOfComputingPwr();
    }

    private double getPercentageOfComputingPwr() {
        double percnt = 0;
        int[] levels = { 0, 0, 0 };
        int index = 0;
        for (int j = 0; j < computeSystem.getComputeNodeList().size(); j++) {
            if (!computeSystem.getComputeNodeList().get(j).isIdle()) {
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

    private void analysisGreen() {

        if (getSLAViolationGen() > 0) {
            /*
             * Increase freq. of just fully utilized CPU nodes Activate just
             * half of sleep nodes if(all nodes are busy and this system is not
             * blocked) send(SOS, theParent)
             */
            for (BladeServer bladeServer : computeSystem.getComputeNodeList()) {
                if (bladeServer.isRunningBusy()) {
                    bladeServer.increaseFrequency();
                }
            }
            // Activate just half of sleep nodes
            int hlfNumofSlept = computeSystem.numberOfIdleNode() / 2;
            int tedad = 0;
            for (BladeServer bladeServer : computeSystem.getComputeNodeList()) {
                if (bladeServer.isIdle()) {
                    LOGGER.info("CSys GR: " + "\tactive a Server!\t\t @" + environment().getCurrentLocalTime()
                            + "\tNumber of runinng:  " + computeSystem.numberOfRunningNode());
                    bladeServer.setStatusAsRunningNormal();
                    bladeServer.setMips(1.4);
                    environment().updateNumberOfMessagesFromSystemToNodes();
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
                if (bladeServer.isRunning()) {
                    bladeServer.decreaseFrequency();
                }
            }
            // If node is ready and is not used make it sleep
            for (BladeServer bladeServer : computeSystem.getComputeNodeList()) {
                if (bladeServer.getActiveBatchList().isEmpty() && bladeServer.getBlockedBatchList().isEmpty()
                        && bladeServer.isRunning()) {
                    environment().updateNumberOfMessagesFromSystemToNodes();
                    bladeServer.setStatusAsIdle();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void analysisSLA() {
        /*
         * Increase freq. of all busy nodes Activate all sleep nodes if(all
         * nodes are busy and this system is not blocked) send(SOS, theParent)
         */
        if (getSLAViolationGen() > 0) {

            for (int i = 0; i < computeSystem.getComputeNodeList().size(); i++) {
                if (computeSystem.getComputeNodeList().get(i).isRunningBusy()) {
                    computeSystem.getComputeNodeList().get(i).increaseFrequency();
                }
                if (computeSystem.getComputeNodeList().get(i).isIdle()) {
                    environment().updateNumberOfMessagesFromSystemToNodes();
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
    public void analysis() {
        if (environment().localTimeByEpoch()) {
            return;
        }
        environment().updateNumberOfMessagesFromDataCenterToSystem(); // one
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
