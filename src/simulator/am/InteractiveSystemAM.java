package simulator.am;

import simulator.InteractiveSystem;
import simulator.Simulator;

public class InteractiveSystemAM extends GeneralAM {

    InteractiveSystem IS;
    double[] percentCompPwr;
    public int[] allocationVector;
    double wlkIntens = 0;
    int[] accuSLA;
    double[] queueLengthUsr;
    int lastTime = 0;

    public InteractiveSystemAM(InteractiveSystem is) {
        this.IS = is;
        setRecForCoop(new int[IS.UserList.size()]);
    }

    @Override
    public void analysis(Object violation) {
        //averageWeight();
        iterativeAlg();

    }

    @Override
    public void planning() {
        /////Server Provisioning for each application Bundle///////////
        if (Simulator.getInstance().localTime % 1200 == 0) {
//                numberOfActiveServ=0;
//                kalmanIndex=Main.localTime/1200;
//                serverProvisioning();
//                kalmanIndex++;
//                int i=ES.applicationList.get(0).occupiedPercentage();
            //System.out.println("occupied\t"+i);
//                if(i>50)
//                  ES.numberOfActiveServ=ES.applicationList.get(0).numberofRunningNode()+1;
//                else
//                  ES.numberOfActiveServ=ES.applicationList.get(0).numberofRunningNode()-1;
        }

    }

    @Override
    public void execution() {
        IS.rc.resourceProvision(IS, allocationVector);
    }

    void workloadIntensity() {
        double avg = 0.0;
        for (int i = 0; i < IS.UserList.size(); i++) {
            avg = avg + (double) IS.UserList.get(i).NumberofBasicNode / IS.UserList.get(i).MaxNumberOfRequest;
        }
        wlkIntens = (double) avg / IS.UserList.size();
    }

    @Override
    public void monitor() {
        percentCompPwr = new double[IS.UserList.size()];
        allocationVector = new int[IS.UserList.size()];
        accuSLA = new int[IS.UserList.size()];
        queueLengthUsr = new double[IS.UserList.size()];
        workloadIntensity();
        for (int i = 0; i < IS.UserList.size(); i++) {
            // assume epoch system 2 time epoch application 
            percentCompPwr[i] = IS.UserList.get(i).AM.percnt / ((Simulator.getInstance().localTime - lastTime) * 3 * IS.UserList.get(i).ComputeNodeList.size());//(Main.epochSys*/*3*ES.applicationList.get(i).ComputeNodeList.size());
            IS.UserList.get(i).AM.percnt = 0;
            accuSLA[i] = IS.UserList.get(i).AM.accumulativeSLA / (Simulator.getInstance().localTime - lastTime);//Main.epochSys;
            IS.UserList.get(i).AM.accumulativeSLA = 0;
            //for fair allocate/release node needs to know how many jobs are already in each application queue
            queueLengthUsr[i] = IS.UserList.get(i).numberOfWaitingJobs();
        }
        calcSysUtility();
        lastTime = Simulator.getInstance().localTime;
        SLAViolationGen = IS.SLAviolation;
    }

    public void calcSysUtility() {
        int localUtil = 0, globalUtil;
        for (int i = 0; i < IS.UserList.size(); i++) {
            localUtil += IS.UserList.get(i).AM.util;
        }
        localUtil = localUtil / IS.UserList.size();

//        if(ES.applicationList.isEmpty())
//        { super.utility=-1;
//            return;
//        }
//        localUtil=localUtil/ES.applicationList.size();
//        int idlePercent=100*ES.numberofIdleNode/ES.numberofNode;
//        int qos=ES.SLAviolation;
//        globalUtil=idlePercent+localUtil;
//        super.utility=sigmoid(globalUtil-100);        
    }

    void iterativeAlg() {
        for (int i = 0; i < IS.UserList.size(); i++) {
            IS.UserList.get(i).AM.StrategyWsitch = Simulator.StrategyEnum.Green; //Green Strategy
            double wkIntensApp;
            wkIntensApp = (double) IS.UserList.get(i).NumberofBasicNode / IS.UserList.get(i).MaxNumberOfRequest;
            //if cpmPwr > 50% & violation then allocate a server 
            allocationVector[i] = 0;
            if (percentCompPwr[i] > 0.5 && accuSLA[i] > 0) {

                //considering wl intensity of apps for node allocation
                //if app has more than average give it more node
                int bishtar = 0;
                if (wkIntensApp > wlkIntens) {
                    bishtar = (int) Math.ceil(Math.abs((wkIntensApp - wlkIntens) / wlkIntens));
                } else {
                    bishtar = 0;
                }
                allocationVector[i] = 1 + bishtar;//+(int)Math.abs((Math.floor((wlkIntens-wkIntensApp)/wlkIntens)));
                //System.out.println("Switching Strategy in Application   =" +i +" to SLA ");
                IS.UserList.get(i).AM.StrategyWsitch = Simulator.StrategyEnum.SLA;//SLA strategy
            }
            //if cpmPwr < 50% & violation is less then release a server 
            if (percentCompPwr[i] <= 0.5 && accuSLA[i] == 0) {
                allocationVector[i] = -1;
                //System.out.println("Releasing a Server");
            }
            //if cpmPwr < 50% & violation is ziyad then nothing no server exchange 
            if (percentCompPwr[i] < 0.5 && accuSLA[i] > 0) {
                allocationVector[i] = 1;
                //System.out.println("Switching Strategy in Application   =" +i +" to SLA ");
                IS.UserList.get(i).AM.StrategyWsitch = Simulator.StrategyEnum.SLA;   //SLA strategy
            }
        }
        int requestedNd = 0;
        for (int i = 0; i < allocationVector.length; i++) {
            int valNode = IS.UserList.get(i).ComputeNodeList.size() + allocationVector[i];
            if (IS.UserList.get(i).minProc > valNode
                    || IS.UserList.get(i).maxProc < valNode) {
//                if(ES.applicationList.get(i).minProc> ES.applicationList.get(i).ComputeNodeList.size()+allocationVector[i])
//                        System.out.println("error requested less than min in AM system ");
//                if(ES.applicationList.get(i).maxProc< ES.applicationList.get(i).ComputeNodeList.size()+allocationVector[i])
//                        System.out.println("error requested more than maxxxx in AM system ");
                allocationVector[i] = 0;
            }
            requestedNd = requestedNd + allocationVector[i];
        }
//        if(requestedNd>ES.numberofIdleNode) 
//            System.out.println("IN AM system can not provide server reqested= "+requestedNd);
    }
    //determining aloc/release vector and active strategy

    void averageWeight() {
        double[] cofficient = new double[IS.UserList.size()];
        int[] sugestForAlo = new int[IS.UserList.size()];
        double sumCoff = 0;
        //in each app calculate the expected Coefficient which is multiplication SLA violation and queue Length
        for (int i = 0; i < IS.UserList.size(); i++) {
            cofficient[i] = queueLengthUsr[i] * accuSLA[i] + accuSLA[i] + queueLengthUsr[i];
            sumCoff = sumCoff + cofficient[i];
        }
        int totalNode = IS.ComputeNodeList.size();
        for (int i = 0; i < IS.UserList.size(); i++) {
            sugestForAlo[i] = (int) (cofficient[i] * totalNode / sumCoff);
            if (sugestForAlo[i] < IS.UserList.get(i).minProc) {
                sugestForAlo[i] = IS.UserList.get(i).minProc;
            }
            if (sugestForAlo[i] > IS.UserList.get(i).maxProc) {
                sugestForAlo[i] = IS.UserList.get(i).maxProc;
            }
            allocationVector[i] = sugestForAlo[i] - IS.UserList.get(i).ComputeNodeList.size();
        }
        for (int i = 0; i < IS.UserList.size(); i++) {
            IS.UserList.get(i).AM.StrategyWsitch = Simulator.StrategyEnum.Green; //Green Strategy
            if (accuSLA[i] > 0) {
                //System.out.println("Switching Strategy in Application   =" +i +" to SLA ");
                IS.UserList.get(i).AM.StrategyWsitch = Simulator.StrategyEnum.SLA;//SLA strategy
            }
        }
    }
// void serverProvisioning() {
//        int[] numberOfPredictedReq = {251, 246, 229, 229, 223, 225, 231, 241, 265, 265, 271, 276, 273, 273, 268, 258, 255, 257, 242, 241, 233, 228, 231, 261, 274, 302, 343, 375, 404, 405, 469, 562, 1188, 1806, 2150, 2499, 2624, 2793, 2236, 1905, 1706, 1558, 1495, 1448, 1414, 1391, 1430, 1731, 2027, 2170, 2187, 2224, 2363, 1317};
//        if (kalmanIndex >= numberOfPredictedReq.length) {
//            return;
//        }
//        ES.numberOfActiveServ = (int) Math.floor(numberOfPredictedReq[kalmanIndex]*5*ES.applicationList.get(0).NumberofBasicNode/ ES.applicationList.get(0).MaxNumberOfRequest);
//        if (ES.numberOfActiveServ > ES.numberofNode) {
//            System.out.println("In ES : is gonna alocate this number of servers: "+(ES.numberOfActiveServ-ES.numberofNode));
//        }
//    }    
}
