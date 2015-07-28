package simulator.am;

import simulator.EnterpriseSystem;
import simulator.Simulator;

public class EnterpriseSystemAM extends GeneralAM {

    EnterpriseSystem ES;
    static int kalmanIndex = 0;
    double[] percentCompPwr;
    double[] queueLengthApps;
    private int[] allocationVector;
    int lastTime = 0;
    int[] accuSLA;
    double wlkIntens = 0;
    private Simulator.Environment environment;

    public EnterpriseSystemAM(EnterpriseSystem ES, Simulator.Environment environment) {
        // super(dtcenter);
        this.ES = ES;
        this.environment = environment;
        setRecForCoop(new int[ES.getApplications().size()]);
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
            // System.out.println("occupied\t"+i);
            // if(i>50)
            // ES.numberOfActiveServ=ES.getApplications().get(0).numberofRunningNode()+1;
            // else
            // ES.numberOfActiveServ=ES.getApplications().get(0).numberofRunningNode()-1;
        }

    }

    @Override
    public void execution() {
        ES.getResourceAllocation().resourceProvision(ES, allocationVector);
    }

    void workloadIntensity() {
        double avg = 0.0;
        for (int i = 0; i < ES.getApplications().size(); i++) {
            avg = avg + (double) ES.getApplications().get(i).getNumberofBasicNode()
                    / ES.getApplications().get(i).getMaxNumberOfRequest();
        }
        wlkIntens = (double) avg / ES.getApplications().size();
    }

    @Override
    public void monitor() {
        percentCompPwr = new double[ES.getApplications().size()];
        allocationVector = new int[ES.getApplications().size()];
        accuSLA = new int[ES.getApplications().size()];
        queueLengthApps = new double[ES.getApplications().size()];
        ES.setSLAviolation(0);
        workloadIntensity();
        for (int i = 0; i < ES.getApplications().size(); i++) {
            ES.setSLAviolation(ES.getSLAviolation() + ES.getApplications().get(i).getSLAviolation());
            // assume epoch system 2 time epoch application
            percentCompPwr[i] = ES.getApplications().get(i).getAM().getPercnt()
                    / ((environment.getCurrentLocalTime() - lastTime) * 3
                            * ES.getApplications().get(i).getComputeNodeList().size());// (Main.epochSys*/*3*ES.getApplications().get(i).ComputeNodeList.size());
            ES.getApplications().get(i).getAM().setPercnt(0);
            accuSLA[i] = ES.getApplications().get(i).getAM().accumulativeSLA
                    / (environment.getCurrentLocalTime() - lastTime);// Main.epochSys;
            ES.getApplications().get(i).getAM().accumulativeSLA = 0;
            // for fair allocate/release node needs to know how many jobs are
            // already in each application queue
            queueLengthApps[i] = ES.getApplications().get(i).numberOfWaitingJobs();
        }
        SLAViolationGen = ES.getSLAviolation();
        if (ES.getSLAviolation() > 0) {
            environment.logEnterpriseViolation(ES.getName(), ES.getSLAviolation());
            ES.setAccumolatedViolation(ES.getAccumolatedViolation() + 1);
        }
        calcSysUtility();
        lastTime = environment.getCurrentLocalTime();
    }

    public void calcSysUtility() {
        int localUtil = 0;
        // int globalUtil;
        for (int i = 0; i < ES.getApplications().size(); i++) {
            localUtil += ES.getApplications().get(i).getAM().getUtil();
        }
        localUtil = localUtil / ES.getApplications().size();

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
        for (int i = 0; i < ES.getApplications().size(); i++) {
            ES.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.Green; // Green
            // Strategy
            double wkIntensApp;
            wkIntensApp = (double) ES.getApplications().get(i).getNumberofBasicNode()
                    / ES.getApplications().get(i).getMaxNumberOfRequest();
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
                // System.out.println("Switching Strategy in Application =" +i
                // +" to SLA ");
                ES.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
                // strategy
            }
            // if cpmPwr < 50% & violation is less then release a server
            if (percentCompPwr[i] <= 0.5 && accuSLA[i] == 0) {
                allocationVector[i] = -1;
                System.out.println("Releasing a Server");
            }
            // if cpmPwr < 50% & violation is ziyad then nothing no server
            // exchange
            if (percentCompPwr[i] < 0.5 && accuSLA[i] > 0) {
                allocationVector[i] = 1;
                // System.out.println("Switching Strategy in Application =" +i
                // +" to SLA ");
                ES.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA; // SLA
                // strategy
            }
        }
        int requestedNd = 0;
        for (int i = 0; i < allocationVector.length; i++) {
            int valNode = ES.getApplications().get(i).getComputeNodeList().size() + allocationVector[i];
            if (ES.getApplications().get(i).getMinProc() > valNode || ES.getApplications().get(i).getMaxProc() < valNode) {
                // if(ES.getApplications().get(i).minProc>
                // ES.getApplications().get(i).ComputeNodeList.size()+allocationVector[i])
                // System.out.println("error requested less than min in AM
                // system ");
                // if(ES.getApplications().get(i).maxProc<
                // ES.getApplications().get(i).ComputeNodeList.size()+allocationVector[i])
                // System.out.println("error requested more than maxxxx in AM
                // system ");
                allocationVector[i] = 0;
            }
            requestedNd = requestedNd + allocationVector[i];
        }
        // if(requestedNd>ES.numberofIdleNode)
        // System.out.println("IN AM system can not provide server reqested=
        // "+requestedNd);
    }
    // determining aloc/release vector and active strategy

    void averageWeight() {
        double[] cofficient = new double[ES.getApplications().size()];
        int[] sugestForAlo = new int[ES.getApplications().size()];
        double sumCoff = 0;
        // in each app calculate the expected Coefficient which is
        // multiplication SLA violation and queue Length
        for (int i = 0; i < ES.getApplications().size(); i++) {
            cofficient[i] = queueLengthApps[i] * accuSLA[i] + accuSLA[i] + queueLengthApps[i];
            sumCoff = sumCoff + cofficient[i];
        }
        int totalNode = ES.getComputeNodeList().size();
        for (int i = 0; i < ES.getApplications().size(); i++) {
            sugestForAlo[i] = (int) (cofficient[i] * totalNode / sumCoff);
            if (sugestForAlo[i] < ES.getApplications().get(i).getMinProc()) {
                sugestForAlo[i] = ES.getApplications().get(i).getMinProc();
            }
            if (sugestForAlo[i] > ES.getApplications().get(i).getMaxProc()) {
                sugestForAlo[i] = ES.getApplications().get(i).getMaxProc();
            }
            allocationVector[i] = sugestForAlo[i] - ES.getApplications().get(i).getComputeNodeList().size();
        }
        for (int i = 0; i < ES.getApplications().size(); i++) {
            ES.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.Green; // Green
            // Strategy
            if (accuSLA[i] > 0) {
                // System.out.println("Switching Strategy in Application =" +i
                // +" to SLA ");
                ES.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
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
        ES.setNumberOfActiveServ((int) Math
                .floor(numberOfPredictedReq[kalmanIndex] * 5 * ES.getApplications().get(0).getNumberofBasicNode()
                        / ES.getApplications().get(0).getMaxNumberOfRequest()));
        if (ES.getNumberOfActiveServ() > ES.getNumberofNode()) {
            System.out.println("In ES : is gonna alocate this number of servers: "
                    + (ES.getNumberOfActiveServ() - ES.getNumberofNode()));
        }
    }

    double sigmoid(double i) {
        return (1 / (1 + Math.exp(-i)));
    }

    void utilityBasedPlanning() {
        for (int i = 0; i < ES.getApplications().size(); i++) {
            ES.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.Green; // Green
            // Strategy
            allocationVector[i] = 0;
            if (sigmoid(queueLengthApps[i]) > 0.5 && accuSLA[i] > 0) {
                ES.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
                // strategy
                allocationVector[i] = 1;
                // System.out.println("allocate system!!!!! ");
            }
            if (sigmoid(queueLengthApps[i]) < 0.5 && accuSLA[i] > 0) {
                ES.getApplications().get(i).getAM().StrategyWsitch = Simulator.StrategyEnum.SLA;// SLA
                // strategy
            }
            if (sigmoid(queueLengthApps[i]) <= 0.5 && accuSLA[i] == 0) {
                allocationVector[i] = -1;
                // System.out.println("Resleasing in system!!!!! ");
            }
        }
        int requestedNd = 0;
        for (int i = 0; i < allocationVector.length; i++) {
            int valNode = ES.getApplications().get(i).getComputeNodeList().size() + allocationVector[i];
            if (ES.getApplications().get(i).getMinProc() > valNode || ES.getApplications().get(i).getMaxProc() < valNode) {
                // if(ES.getApplications().get(i).minProc>
                // ES.getApplications().get(i).ComputeNodeList.size()+allocationVector[i])
                // System.out.println("error requested less than min in AM
                // system ");
                // if(ES.getApplications().get(i).maxProc<
                // ES.getApplications().get(i).ComputeNodeList.size()+allocationVector[i])
                // System.out.println("error requested more than maxxxx in AM
                // system ");
                allocationVector[i] = 0;
            }
            requestedNd = requestedNd + allocationVector[i];
        }
        // if(requestedNd>ES.numberofIdleNode)
        // System.out.println("IN AM system can not provide server reqested=
        // "+requestedNd);
    }
}
