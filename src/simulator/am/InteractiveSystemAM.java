package simulator.am;

import com.google.inject.Inject;

import simulator.Environment;
import simulator.ManagedResource;
import simulator.Simulator;
import simulator.system.InteractiveSystem;

public class InteractiveSystemAM extends GeneralAM {

    private InteractiveSystem interatctiveSystem;
    private double[] percentCompPwr;
    private int[] allocationVector;
    private double wlkIntens = 0;
    private int[] accuSLA;
    private double[] queueLengthUsr;
    private int lastTime = 0;

    @Inject
    public InteractiveSystemAM(Environment environment) {
        super(environment);
    }

    @Override
    public void setManagedResource(ManagedResource managedResource) {
        this.interatctiveSystem = (InteractiveSystem) managedResource;
        setRecForCoop(new int[interatctiveSystem.getUserList().size()]);
    }

    @Override
    public void analysis() {
        // averageWeight();
        iterativeAlg();

    }

    @Override
    public void planning() {
        ///// Server Provisioning for each application Bundle///////////
        if (environment().getCurrentLocalTime() % 1200 == 0) {
            // numberOfActiveServ=0;
            // kalmanIndex=Main.localTime/1200;
            // serverProvisioning();
            // kalmanIndex++;
            // int i=ES.applicationList.get(0).occupiedPercentage();
            // LOGGER.info("occupied\t"+i);
            // if(i>50)
            // ES.numberOfActiveServ=ES.applicationList.get(0).numberofRunningNode()+1;
            // else
            // ES.numberOfActiveServ=ES.applicationList.get(0).numberofRunningNode()-1;
        }

    }

    @Override
    public void execution() {
        interatctiveSystem.getResourceAllocation().resourceProvision(interatctiveSystem, getAllocationVector());
    }

    private void workloadIntensity() {
        double avg = 0.0;
        for (int i = 0; i < interatctiveSystem.getUserList().size(); i++) {
            avg = avg + (double) interatctiveSystem.getUserList().get(i).getNumberofBasicNode()
                    / interatctiveSystem.getUserList().get(i).getMaxNumberOfRequest();
        }
        wlkIntens = (double) avg / interatctiveSystem.getUserList().size();
    }

    @Override
    public void monitor() {
        percentCompPwr = new double[interatctiveSystem.getUserList().size()];
        setAllocationVector(new int[interatctiveSystem.getUserList().size()]);
        accuSLA = new int[interatctiveSystem.getUserList().size()];
        queueLengthUsr = new double[interatctiveSystem.getUserList().size()];
        workloadIntensity();
        for (int i = 0; i < interatctiveSystem.getUserList().size(); i++) {
            // assume epoch system 2 time epoch application
            percentCompPwr[i] = interatctiveSystem.getUserList().get(i).getAM().percnt
                    / ((environment().getCurrentLocalTime() - lastTime) * 3
                            * interatctiveSystem.getUserList().get(i).getComputeNodeList().size());// (Main.epochSys*/*3*ES.applicationList.get(i).ComputeNodeList.size());
            interatctiveSystem.getUserList().get(i).getAM().percnt = 0;
            accuSLA[i] = interatctiveSystem.getUserList().get(i).getAM().accumulativeSLA
                    / (environment().getCurrentLocalTime() - lastTime);// Main.epochSys;
            interatctiveSystem.getUserList().get(i).getAM().accumulativeSLA = 0;
            // for fair allocate/release node needs to know how many jobs are
            // already in each application queue
            queueLengthUsr[i] = interatctiveSystem.getUserList().get(i).numberOfWaitingJobs();
        }
        calcSysUtility();
        lastTime = environment().getCurrentLocalTime();
        setSLAViolationGen(interatctiveSystem.getSLAviolation());
    }

    private void calcSysUtility() {
        int localUtil = 0;
        // int globalUtil;
        for (int i = 0; i < interatctiveSystem.getUserList().size(); i++) {
            localUtil += interatctiveSystem.getUserList().get(i).getAM().util;
        }
        localUtil = localUtil / interatctiveSystem.getUserList().size();

        // if(ES.applicationList.isEmpty())
        // { super.utility=-1;
        // return;
        // }
        // localUtil=localUtil/ES.applicationList.size();
        // int idlePercent=100*ES.numberofIdleNode/ES.numberofNode;
        // int qos=ES.SLAviolation;
        // globalUtil=idlePercent+localUtil;
        // super.utility=sigmoid(globalUtil-100);
    }

    private void iterativeAlg() {
        for (int i = 0; i < interatctiveSystem.getUserList().size(); i++) {
            interatctiveSystem.getUserList().get(i).getAM().currentStrategy = Simulator.StrategyEnum.Green; // Green
            // Strategy
            double wkIntensApp;
            wkIntensApp = (double) interatctiveSystem.getUserList().get(i).getNumberofBasicNode()
                    / interatctiveSystem.getUserList().get(i).getMaxNumberOfRequest();
            // if cpmPwr > 50% & violation then allocate a server
            getAllocationVector()[i] = 0;
            if (percentCompPwr[i] > 0.5 && accuSLA[i] > 0) {

                // considering wl intensity of apps for node allocation
                // if app has more than average give it more node
                int bishtar = 0;
                if (wkIntensApp > wlkIntens) {
                    bishtar = (int) Math.ceil(Math.abs((wkIntensApp - wlkIntens) / wlkIntens));
                } else {
                    bishtar = 0;
                }
                getAllocationVector()[i] = 1 + bishtar;// +(int)Math.abs((Math.floor((wlkIntens-wkIntensApp)/wlkIntens)));
                // LOGGER.info("Switching Strategy in Application =" +i
                // +" to SLA ");
                interatctiveSystem.getUserList().get(i).getAM().currentStrategy = Simulator.StrategyEnum.SLA;// SLA
                // strategy
            }
            // if cpmPwr < 50% & violation is less then release a server
            if (percentCompPwr[i] <= 0.5 && accuSLA[i] == 0) {
                getAllocationVector()[i] = -1;
                // LOGGER.info("Releasing a Server");
            }
            // if cpmPwr < 50% & violation is ziyad then nothing no server
            // exchange
            if (percentCompPwr[i] < 0.5 && accuSLA[i] > 0) {
                getAllocationVector()[i] = 1;
                // LOGGER.info("Switching Strategy in Application =" +i
                // +" to SLA ");
                interatctiveSystem.getUserList().get(i).getAM().currentStrategy = Simulator.StrategyEnum.SLA; // SLA
                // strategy
            }
        }
        int requestedNd = 0;
        for (int i = 0; i < getAllocationVector().length; i++) {
            int valNode = interatctiveSystem.getUserList().get(i).getComputeNodeList().size()
                    + getAllocationVector()[i];
            if (interatctiveSystem.getUserList().get(i).getMinProc() > valNode
                    || interatctiveSystem.getUserList().get(i).getMaxProc() < valNode) {
                // if(ES.applicationList.get(i).minProc>
                // ES.applicationList.get(i).ComputeNodeList.size()+allocationVector[i])
                // LOGGER.info("error requested less than min in AM
                // system ");
                // if(ES.applicationList.get(i).maxProc<
                // ES.applicationList.get(i).ComputeNodeList.size()+allocationVector[i])
                // LOGGER.info("error requested more than maxxxx in AM
                // system ");
                getAllocationVector()[i] = 0;
            }
            requestedNd = requestedNd + getAllocationVector()[i];
        }
        // if(requestedNd>ES.numberofIdleNode)
        // LOGGER.info("IN AM system can not provide server reqested=
        // "+requestedNd);
    }
    // determining aloc/release vector and active strategy

    @SuppressWarnings("unused")
    private void averageWeight() {
        double[] cofficient = new double[interatctiveSystem.getUserList().size()];
        int[] sugestForAlo = new int[interatctiveSystem.getUserList().size()];
        double sumCoff = 0;
        // in each app calculate the expected Coefficient which is
        // multiplication SLA violation and queue Length
        for (int i = 0; i < interatctiveSystem.getUserList().size(); i++) {
            cofficient[i] = queueLengthUsr[i] * accuSLA[i] + accuSLA[i] + queueLengthUsr[i];
            sumCoff = sumCoff + cofficient[i];
        }
        int totalNode = interatctiveSystem.getComputeNodeList().size();
        for (int i = 0; i < interatctiveSystem.getUserList().size(); i++) {
            sugestForAlo[i] = (int) (cofficient[i] * totalNode / sumCoff);
            if (sugestForAlo[i] < interatctiveSystem.getUserList().get(i).getMinProc()) {
                sugestForAlo[i] = interatctiveSystem.getUserList().get(i).getMinProc();
            }
            if (sugestForAlo[i] > interatctiveSystem.getUserList().get(i).getMaxProc()) {
                sugestForAlo[i] = interatctiveSystem.getUserList().get(i).getMaxProc();
            }
            getAllocationVector()[i] = sugestForAlo[i]
                    - interatctiveSystem.getUserList().get(i).getComputeNodeList().size();
        }
        for (int i = 0; i < interatctiveSystem.getUserList().size(); i++) {
            interatctiveSystem.getUserList().get(i).getAM().currentStrategy = Simulator.StrategyEnum.Green; // Green
            // Strategy
            if (accuSLA[i] > 0) {
                // LOGGER.info("Switching Strategy in Application =" +i
                // +" to SLA ");
                interatctiveSystem.getUserList().get(i).getAM().currentStrategy = Simulator.StrategyEnum.SLA;// SLA
                // strategy
            }
        }
    }
    // void serverProvisioning() {
    // int[] numberOfPredictedReq = {251, 246, 229, 229, 223, 225, 231, 241,
    // 265, 265, 271, 276, 273, 273, 268, 258, 255, 257, 242, 241, 233, 228,
    // 231, 261, 274, 302, 343, 375, 404, 405, 469, 562, 1188, 1806, 2150, 2499,
    // 2624, 2793, 2236, 1905, 1706, 1558, 1495, 1448, 1414, 1391, 1430, 1731,
    // 2027, 2170, 2187, 2224, 2363, 1317};
    // if (kalmanIndex >= numberOfPredictedReq.length) {
    // return;
    // }
    // ES.numberOfActiveServ = (int)
    // Math.floor(numberOfPredictedReq[kalmanIndex]*5*ES.applicationList.get(0).NumberofBasicNode/
    // ES.applicationList.get(0).MaxNumberOfRequest);
    // if (ES.numberOfActiveServ > ES.numberofNode) {
    // LOGGER.info("In ES : is gonna alocate this number of servers:
    // "+(ES.numberOfActiveServ-ES.numberofNode));
    // }
    // }

    private int[] getAllocationVector() {
        return allocationVector;
    }

    private void setAllocationVector(int[] allocationVector) {
        this.allocationVector = allocationVector;
    }
}
