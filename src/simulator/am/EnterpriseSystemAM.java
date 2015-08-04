package simulator.am;

import java.util.logging.Logger;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.Simulator;
import simulator.system.EnterpriseSystem;

public class EnterpriseSystemAM extends GeneralAM {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseSystemAM.class.getName());
    
    private EnterpriseSystem enterpriseSystem;
    static int kalmanIndex = 0;
    double[] percentCompPwr;
    double[] queueLengthApps;
    private int[] allocationVector;
    int lastTime = 0;
    int[] accuSLA;
    double wlkIntens = 0;
    private Environment environment;
    private SLAViolationLogger slaViolationLogger;

    public EnterpriseSystemAM(EnterpriseSystem enterpriseSystem, Environment environment, SLAViolationLogger slaViolationLogger) {
        // super(dtcenter);
        this.enterpriseSystem = enterpriseSystem;
        this.environment = environment;
        this.slaViolationLogger = slaViolationLogger;
        setRecForCoop(new int[enterpriseSystem.getApplications().size()]);
    }

    @Override
    public void analysis(Object violation) {
        // averageWeight();
        // iterativeAlg();
        utilityBasedPlanning();

    }

    @Override
    public void planning() {
        ///// Server Provisioning for each application Bundle///////////
        if (environment.getCurrentLocalTime() % 1200 == 0) {
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

    void workloadIntensity() {
        double avg = 0.0;
        for (int i = 0; i < enterpriseSystem.getApplications().size(); i++) {
            avg = avg + (double) enterpriseSystem.getApplications().get(i).getNumberofBasicNode()
                    / enterpriseSystem.getApplications().get(i).getMaxNumberOfRequest();
        }
        wlkIntens = (double) avg / enterpriseSystem.getApplications().size();
    }

    @Override
    public void monitor() {
        percentCompPwr = new double[enterpriseSystem.getApplications().size()];
        allocationVector = new int[enterpriseSystem.getApplications().size()];
        accuSLA = new int[enterpriseSystem.getApplications().size()];
        queueLengthApps = new double[enterpriseSystem.getApplications().size()];
        enterpriseSystem.setSLAviolation(0);
        workloadIntensity();
        for (int i = 0; i < enterpriseSystem.getApplications().size(); i++) {
            enterpriseSystem.setSLAviolation(enterpriseSystem.getSLAviolation() + enterpriseSystem.getApplications().get(i).getSLAviolation());
            // assume epoch system 2 time epoch application
            percentCompPwr[i] = enterpriseSystem.getApplications().get(i).getAM().getPercnt()
                    / ((environment.getCurrentLocalTime() - lastTime) * 3
                            * enterpriseSystem.getApplications().get(i).getComputeNodeList().size());// (Main.epochSys*/*3*ES.getApplications().get(i).ComputeNodeList.size());
            enterpriseSystem.getApplications().get(i).getAM().setPercnt(0);
            accuSLA[i] = enterpriseSystem.getApplications().get(i).getAM().getAccumulativeSLA()
                    / (environment.getCurrentLocalTime() - lastTime);// Main.epochSys;
            enterpriseSystem.getApplications().get(i).getAM().setAccumulativeSLA(0);
            // for fair allocate/release node needs to know how many jobs are
            // already in each application queue
            queueLengthApps[i] = enterpriseSystem.getApplications().get(i).numberOfWaitingJobs();
        }
        setSLAViolationGen(enterpriseSystem.getSLAviolation());
        if (enterpriseSystem.getSLAviolation() > 0) {
            slaViolationLogger.logEnterpriseViolation(enterpriseSystem.getName(), enterpriseSystem.getSLAviolation());
            enterpriseSystem.setAccumolatedViolation(enterpriseSystem.getAccumolatedViolation() + 1);
        }
        calcSysUtility();
        lastTime = environment.getCurrentLocalTime();
    }

    public void calcSysUtility() {
        int localUtil = 0;
        // int globalUtil;
        for (int i = 0; i < enterpriseSystem.getApplications().size(); i++) {
            localUtil += enterpriseSystem.getApplications().get(i).getAM().getUtil();
        }
        localUtil = localUtil / enterpriseSystem.getApplications().size();

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

    void iterativeAlg() {
        for (int i = 0; i < enterpriseSystem.getApplications().size(); i++) {
            enterpriseSystem.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.Green; // Green
            // Strategy
            double wkIntensApp;
            wkIntensApp = (double) enterpriseSystem.getApplications().get(i).getNumberofBasicNode()
                    / enterpriseSystem.getApplications().get(i).getMaxNumberOfRequest();
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
                enterpriseSystem.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
                // strategy
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
                enterpriseSystem.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA; // SLA
                // strategy
            }
        }
        int requestedNd = 0;
        for (int i = 0; i < allocationVector.length; i++) {
            int valNode = enterpriseSystem.getApplications().get(i).getComputeNodeList().size() + allocationVector[i];
            if (enterpriseSystem.getApplications().get(i).getMinProc() > valNode || enterpriseSystem.getApplications().get(i).getMaxProc() < valNode) {
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

    void averageWeight() {
        double[] cofficient = new double[enterpriseSystem.getApplications().size()];
        int[] sugestForAlo = new int[enterpriseSystem.getApplications().size()];
        double sumCoff = 0;
        // in each app calculate the expected Coefficient which is
        // multiplication SLA violation and queue Length
        for (int i = 0; i < enterpriseSystem.getApplications().size(); i++) {
            cofficient[i] = queueLengthApps[i] * accuSLA[i] + accuSLA[i] + queueLengthApps[i];
            sumCoff = sumCoff + cofficient[i];
        }
        int totalNode = enterpriseSystem.getComputeNodeList().size();
        for (int i = 0; i < enterpriseSystem.getApplications().size(); i++) {
            sugestForAlo[i] = (int) (cofficient[i] * totalNode / sumCoff);
            if (sugestForAlo[i] < enterpriseSystem.getApplications().get(i).getMinProc()) {
                sugestForAlo[i] = enterpriseSystem.getApplications().get(i).getMinProc();
            }
            if (sugestForAlo[i] > enterpriseSystem.getApplications().get(i).getMaxProc()) {
                sugestForAlo[i] = enterpriseSystem.getApplications().get(i).getMaxProc();
            }
            allocationVector[i] = sugestForAlo[i] - enterpriseSystem.getApplications().get(i).getComputeNodeList().size();
        }
        for (int i = 0; i < enterpriseSystem.getApplications().size(); i++) {
            enterpriseSystem.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.Green; // Green
            // Strategy
            if (accuSLA[i] > 0) {
                // LOGGER.info("Switching Strategy in Application =" +i
                // +" to SLA ");
                enterpriseSystem.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
                // strategy
            }
        }
    }

    void serverProvisioning() {
        int[] numberOfPredictedReq = { 251, 246, 229, 229, 223, 225, 231, 241, 265, 265, 271, 276, 273, 273, 268, 258,
                255, 257, 242, 241, 233, 228, 231, 261, 274, 302, 343, 375, 404, 405, 469, 562, 1188, 1806, 2150, 2499,
                2624, 2793, 2236, 1905, 1706, 1558, 1495, 1448, 1414, 1391, 1430, 1731, 2027, 2170, 2187, 2224, 2363,
                1317 };
        if (kalmanIndex >= numberOfPredictedReq.length) {
            return;
        }
        enterpriseSystem.setNumberOfActiveServ((int) Math
                .floor(numberOfPredictedReq[kalmanIndex] * 5 * enterpriseSystem.getApplications().get(0).getNumberofBasicNode()
                        / enterpriseSystem.getApplications().get(0).getMaxNumberOfRequest()));
        if (enterpriseSystem.getNumberOfActiveServ() > enterpriseSystem.getNumberOfNode()) {
            LOGGER.info("In ES : is gonna alocate this number of servers: "
                    + (enterpriseSystem.getNumberOfActiveServ() - enterpriseSystem.getNumberOfNode()));
        }
    }

    double sigmoid(double i) {
        return (1 / (1 + Math.exp(-i)));
    }

    void utilityBasedPlanning() {
        for (int i = 0; i < enterpriseSystem.getApplications().size(); i++) {
            enterpriseSystem.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.Green; // Green
            // Strategy
            allocationVector[i] = 0;
            if (sigmoid(queueLengthApps[i]) > 0.5 && accuSLA[i] > 0) {
                enterpriseSystem.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
                // strategy
                allocationVector[i] = 1;
                // LOGGER.info("allocate system!!!!! ");
            }
            if (sigmoid(queueLengthApps[i]) < 0.5 && accuSLA[i] > 0) {
                enterpriseSystem.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
                // strategy
            }
            if (sigmoid(queueLengthApps[i]) <= 0.5 && accuSLA[i] == 0) {
                allocationVector[i] = -1;
                // LOGGER.info("Resleasing in system!!!!! ");
            }
        }
        int requestedNd = 0;
        for (int i = 0; i < allocationVector.length; i++) {
            int valNode = enterpriseSystem.getApplications().get(i).getComputeNodeList().size() + allocationVector[i];
            if (enterpriseSystem.getApplications().get(i).getMinProc() > valNode || enterpriseSystem.getApplications().get(i).getMaxProc() < valNode) {
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
