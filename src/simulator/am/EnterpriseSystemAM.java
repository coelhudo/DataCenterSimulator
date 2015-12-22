package simulator.am;

import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;

import simulator.Environment;
import simulator.ManagedResource;
import simulator.SLAViolationLogger;
import simulator.Simulator;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseSystem;

public class EnterpriseSystemAM extends GeneralAM {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseSystemAM.class.getName());

    private EnterpriseSystem enterpriseSystem;
    private List<EnterpriseApp> applications;
    static int kalmanIndex = 0;
    private double[] percentCompPwr;
    private double[] queueLengthApps;
    private int[] allocationVector;
    private int lastTime = 0;
    private int[] accuSLA;
    private double wlkIntens = 0;
    private SLAViolationLogger slaViolationLogger;

    @Inject
    public EnterpriseSystemAM(Environment environment, SLAViolationLogger slaViolationLogger) {
        super(environment);
        this.slaViolationLogger = slaViolationLogger;
    }

    @Override
    public void setManagedResource(ManagedResource managedResource) {
        this.enterpriseSystem = (EnterpriseSystem) managedResource;
        this.applications = enterpriseSystem.getApplications();
        setRecForCoop(new int[applications.size()]);
    }

    @Override
    public void analysis() {
        // averageWeight();
        // iterativeAlg();
        utilityBasedPlanning();
    }

    @Override
    public void planning() {
        ///// Server Provisioning for each application Bundle///////////
        if (environment().getCurrentLocalTime() % 1200 == 0) {
            // numberOfActiveServ=0;
            // kalmanIndex=Main.localTime/1200;
            // serverProvisioning();
            // kalmanIndex++;
            // int i=ES.getApplications().get(0).occupiedPercentage();
            // LOGGER.info("occupied\t"+i);
            // if(i>50)
            // ES.numberOfActiveServ=ES.getApplications().get(0).numberofRunningNode()+1;
            // else
            // ES.numberOfActiveServ=ES.getApplications().get(0).numberofRunningNode()-1;
        }

    }

    @Override
    public void execution() {
        enterpriseSystem.getResourceAllocation().resourceProvision(enterpriseSystem, allocationVector);
    }

    private void workloadIntensity() {
        double avg = 0.0;
        for (EnterpriseApp enterpriseApp : applications) {
            avg = avg + (double) enterpriseApp.getNumberofBasicNode() / enterpriseApp.getMaxNumberOfRequest();
        }
        wlkIntens = (double) avg / applications.size();
    }

    @Override
    public void monitor() {
        percentCompPwr = new double[applications.size()];
        allocationVector = new int[applications.size()];
        accuSLA = new int[applications.size()];
        queueLengthApps = new double[applications.size()];
        enterpriseSystem.resetNumberOfSLAViolation();
        workloadIntensity();
        for (int i = 0; i < applications.size(); i++) {
            enterpriseSystem
                    .setNumberOfSLAViolation(enterpriseSystem.getNumberOFSLAViolation() + applications.get(i).getSLAviolation());
            // assume epoch system 2 time epoch application
            GeneralAM applicationAM = applications.get(i).getAM();
            percentCompPwr[i] = applicationAM.getPercnt() / ((environment().getCurrentLocalTime() - lastTime) * 3
                            * applications.get(i).getComputeNodeList().size());// (Main.epochSys*/*3*ES.getApplications().get(i).ComputeNodeList.size());
            applicationAM.setPercnt(0);
            accuSLA[i] = applicationAM.getAccumulativeSLA()
                    / (environment().getCurrentLocalTime() - lastTime);// Main.epochSys;
            applicationAM.setAccumulativeSLA(0);
            // for fair allocate/release node needs to know how many jobs are
            // already in each application queue
            queueLengthApps[i] = applications.get(i).numberOfWaitingJobs();
        }
        setSLAViolationGen(enterpriseSystem.getNumberOFSLAViolation());
        if (enterpriseSystem.getNumberOFSLAViolation() > 0) {
            slaViolationLogger.logEnterpriseViolation(enterpriseSystem.getName(), enterpriseSystem.getNumberOFSLAViolation());
            enterpriseSystem.increaseAccumulatedViolation();
        }
        calcSysUtility();
        lastTime = environment().getCurrentLocalTime();
    }

    private void calcSysUtility() {
        int localUtil = 0;
        // int globalUtil;
        for (int i = 0; i < applications.size(); i++) {
            localUtil += applications.get(i).getAM().getUtil();
        }
        localUtil = localUtil / applications.size();

        // if(ES.getApplications().isEmpty())
        // { super.utility=-1;
        // return;
        // }
        // localUtil=localUtil/ES.getApplications().size();
        // int idlePercent=100*ES.numberofIdleNode/ES.numberofNode;
        // int qos=ES.SLAviolation;
        // globalUtil=idlePercent+localUtil;
        // super.utility=sigmoidsig(globalUtil-100);
    }

    @SuppressWarnings("unused")
    private void iterativeAlg() {
        for (int i = 0; i < applications.size(); i++) {
            applications.get(i).getAM().setStrategy(Simulator.StrategyEnum.Green);
            double wkIntensApp;
            wkIntensApp = (double) applications.get(i).getNumberofBasicNode()
                    / applications.get(i).getMaxNumberOfRequest();
            // if cpmPwr > 50% & violation then allocate a server
            allocationVector[i] = 0;
            if (percentCompPwr[i] > 0.5 && accuSLA[i] > 0) {

                // considering wl intensity of apps for node allocation
                // if app has more than average give it more node
                int bishtar = 0;
                if (wkIntensApp > wlkIntens) {
                    bishtar = (int) Math.ceil(Math.abs((wkIntensApp - wlkIntens) / wlkIntens));
                } else {
                    bishtar = 0;
                }
                allocationVector[i] = 1 + bishtar;// +(int)Math.abs((Math.floor((wlkIntens-wkIntensApp)/wlkIntens)));
                // LOGGER.info("Switching Strategy in Application =" +i
                // +" to SLA ");
                applications.get(i).getAM().setStrategy(Simulator.StrategyEnum.SLA);
            }
            // if cpmPwr < 50% & violation is less then release a server
            if (percentCompPwr[i] <= 0.5 && accuSLA[i] == 0) {
                allocationVector[i] = -1;
                LOGGER.info("Releasing a Server");
            }
            // if cpmPwr < 50% & violation is ziyad then nothing no server
            // exchange
            if (percentCompPwr[i] < 0.5 && accuSLA[i] > 0) {
                allocationVector[i] = 1;
                // LOGGER.info("Switching Strategy in Application =" +i
                // +" to SLA ");
                applications.get(i).getAM().setStrategy(Simulator.StrategyEnum.SLA);
            }
        }
        int requestedNd = 0;
        for (int i = 0; i < allocationVector.length; i++) {
            int valNode = applications.get(i).getComputeNodeList().size() + allocationVector[i];
            if (applications.get(i).getMinProc() > valNode || applications.get(i).getMaxProc() < valNode) {
                // if(ES.getApplications().get(i).minProc>
                // ES.getApplications().get(i).ComputeNodeList.size()+allocationVector[i])
                // LOGGER.info("error requested less than min in AM
                // system ");
                // if(ES.getApplications().get(i).maxProc<
                // ES.getApplications().get(i).ComputeNodeList.size()+allocationVector[i])
                // LOGGER.info("error requested more than maxxxx in AM
                // system ");
                allocationVector[i] = 0;
            }
            requestedNd = requestedNd + allocationVector[i];
        }
        // if(requestedNd>ES.numberofIdleNode)
        // LOGGER.info("IN AM system can not provide server reqested=
        // "+requestedNd);
    }
    // determining aloc/release vector and active strategy

    @SuppressWarnings("unused")
    private void averageWeight() {
        double[] cofficient = new double[applications.size()];
        int[] sugestForAlo = new int[applications.size()];
        double sumCoff = 0;
        // in each app calculate the expected Coefficient which is
        // multiplication SLA violation and queue Length
        for (int i = 0; i < applications.size(); i++) {
            cofficient[i] = queueLengthApps[i] * accuSLA[i] + accuSLA[i] + queueLengthApps[i];
            sumCoff = sumCoff + cofficient[i];
        }
        int totalNode = enterpriseSystem.getComputeNodeList().size();
        for (int i = 0; i < applications.size(); i++) {
            sugestForAlo[i] = (int) (cofficient[i] * totalNode / sumCoff);
            if (sugestForAlo[i] < applications.get(i).getMinProc()) {
                sugestForAlo[i] = applications.get(i).getMinProc();
            }
            if (sugestForAlo[i] > applications.get(i).getMaxProc()) {
                sugestForAlo[i] = applications.get(i).getMaxProc();
            }
            allocationVector[i] = sugestForAlo[i] - applications.get(i).getComputeNodeList().size();
        }
        for (int i = 0; i < applications.size(); i++) {
            applications.get(i).getAM().setStrategy(Simulator.StrategyEnum.Green);
            if (accuSLA[i] > 0) {
                // LOGGER.info("Switching Strategy in Application =" +i
                // +" to SLA ");
                applications.get(i).getAM().setStrategy(Simulator.StrategyEnum.SLA);
            }
        }
    }

    @SuppressWarnings("unused")
    private void serverProvisioning() {
        int[] numberOfPredictedReq = { 251, 246, 229, 229, 223, 225, 231, 241, 265, 265, 271, 276, 273, 273, 268, 258,
                255, 257, 242, 241, 233, 228, 231, 261, 274, 302, 343, 375, 404, 405, 469, 562, 1188, 1806, 2150, 2499,
                2624, 2793, 2236, 1905, 1706, 1558, 1495, 1448, 1414, 1391, 1430, 1731, 2027, 2170, 2187, 2224, 2363,
                1317 };
        if (kalmanIndex >= numberOfPredictedReq.length) {
            return;
        }
        enterpriseSystem.setNumberOfActiveServ((int) Math.floor(numberOfPredictedReq[kalmanIndex] * 5
                * applications.get(0).getNumberofBasicNode() / applications.get(0).getMaxNumberOfRequest()));
        if (enterpriseSystem.getNumberOfActiveServ() > enterpriseSystem.getNumberOfNode()) {
            LOGGER.info("In ES : is gonna alocate this number of servers: "
                    + (enterpriseSystem.getNumberOfActiveServ() - enterpriseSystem.getNumberOfNode()));
        }
    }

    private double sigmoid(double i) {
        return (1 / (1 + Math.exp(-i)));
    }

    private void utilityBasedPlanning() {
        for (int i = 0; i < applications.size(); i++) {
            applications.get(i).getAM().setStrategy(Simulator.StrategyEnum.Green);
            allocationVector[i] = 0;
            if (sigmoid(queueLengthApps[i]) > 0.5 && accuSLA[i] > 0) {
                applications.get(i).getAM().setStrategy(Simulator.StrategyEnum.SLA);
                allocationVector[i] = 1;
                // LOGGER.info("allocate system!!!!! ");
            }
            if (sigmoid(queueLengthApps[i]) < 0.5 && accuSLA[i] > 0) {
                applications.get(i).getAM().setStrategy(Simulator.StrategyEnum.SLA);
            }
            if (sigmoid(queueLengthApps[i]) <= 0.5 && accuSLA[i] == 0) {
                allocationVector[i] = -1;
                // LOGGER.info("Resleasing in system!!!!! ");
            }
        }
        int requestedNd = 0;
        for (int i = 0; i < allocationVector.length; i++) {
            int valNode = applications.get(i).getComputeNodeList().size() + allocationVector[i];
            if (applications.get(i).getMinProc() > valNode || applications.get(i).getMaxProc() < valNode) {
                // if(ES.getApplications().get(i).minProc>
                // ES.getApplications().get(i).ComputeNodeList.size()+allocationVector[i])
                // LOGGER.info("error requested less than min in AM
                // system ");
                // if(ES.getApplications().get(i).maxProc<
                // ES.getApplications().get(i).ComputeNodeList.size()+allocationVector[i])
                // LOGGER.info("error requested more than maxxxx in AM
                // system ");
                allocationVector[i] = 0;
            }
            requestedNd = requestedNd + allocationVector[i];
        }
        // if(requestedNd>ES.numberofIdleNode)
        // LOGGER.info("IN AM system can not provide server reqested=
        // "+requestedNd);
    }
}
