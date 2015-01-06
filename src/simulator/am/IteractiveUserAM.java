package simulator.am;

import simulator.physical.BladeServer;
import simulator.InteractiveSystem;
import simulator.InteractiveUser;
import simulator.Simulator;
import simulator.jobs.EnterpriseJob;

public class IteractiveUserAM extends GeneralAM {

    InteractiveUser User;
    InteractiveSystem sys;
    double util = 0;
    static int violationInEpoch = 0;
    double percnt = 0;
    int accumulativeSLA = 0;
    //int cpAccumu=0;
    Simulator.StrategyEnum StrategyWsitch = Simulator.StrategyEnum.Green; //Green

    public IteractiveUserAM(InteractiveSystem sys, InteractiveUser usr) {
        this.sys = sys;
        this.User = usr;
    }

    @Override
    public void monitor() {
        //localUtilCal();
        getPercentageOfComputingPwr();
        ///Check if its neighborhood are not happy to see if it can help or not!
        for (int i = 0; i < sys.UserList.size(); i++) {
            if (sys.am.recForCoop[i] == 1) {
                //TODO
            }
        }
    }

    public void localUtilCal() {
        double CPU = User.getAverageCPUutil(); //average CPU utilization of nodes
        double[] pwr = User.getAveragePwrParam();
        double x = CPU * (pwr[0] - pwr[1]) / 100 + User.numberofIdleNode() * pwr[2] + User.numberofRunningNode() * pwr[1];
        //U= a x+ b y a=b=1
        util = x + User.SLAviolation;
        //util=sigmoid(util);
        //System.out.println(util);
    }

    public double getPercentageOfComputingPwr() {
        int[] levels = {0, 0, 0};
        int index = 0;
        for (int j = 0; j < User.ComputeNodeList.size(); j++) {
            if (User.ComputeNodeList.get(j).ready != -1) //it is idle
            {
                index = User.ComputeNodeList.get(j).getCurrentFreqLevel();
                levels[index]++;
            }
        }
        percnt = percnt + levels[0] + 2 * levels[1] + 3 * levels[2];
        sys.am.compPwrApps[User.id] = sys.am.compPwrApps[User.id] + levels[0] + 2 * levels[1] + 3 * levels[2];
        return percnt;
    }

    @Override
    public void analysis(Object violation) {
        //if(StrategyWsitch==0)
        //analysis_GR(violation);
        //else
        analysis_SLA(violation);
    }
    //SLA Policy 

    public void analysis_SLA(Object violation) {
        if (Simulator.getInstance().localTime % Simulator.getInstance().epochApp != 0)// || Main.localTime<0)
        {
            violationInEpoch = (Integer) violation + violationInEpoch;
            return;
        }

        if (violationInEpoch > 0) {
            for (int j = 0; j < User.ComputeNodeList.size(); j++) {
                if (User.ComputeNodeList.get(j).ready != -1) //except idle nodes
                {
                    User.ComputeNodeList.get(j).increaseFrequency();
                }
            }
            int tedad = User.ComputeNodeList.size();
            //Policy 4: if SLA violation then unshrink active server
            for (int j = 0; j < User.ComputeNodeList.size() && tedad > 0; j++) {
                if (User.ComputeNodeList.get(j).ready == -1) {
                    //System.out.println("Application:SLA" +app.id +"\tActive one Server!\t\t "+"Number of runinng:  "+app.numberofRunningNode());
                    User.ComputeNodeList.get(j).ready = 1;
                    User.ComputeNodeList.get(j).Mips = 1.4;
                    tedad--;
                }
            }
        }
        violationInEpoch = 0;
    }

    // Green policy is applied here:
    public void analysis_GR(Object violation) {
        if (Simulator.getInstance().localTime % Simulator.getInstance().epochApp != 0)// || Main.localTime<0)
        {
            violationInEpoch = (Integer) violation + violationInEpoch;
            return;
        }
        //Policy 1: if no SLA violation then decrease frequency
        if (violationInEpoch == 0) {
            for (int j = 0; j < User.ComputeNodeList.size(); j++) {
                if (User.ComputeNodeList.get(j).ready != -1) {
                    User.ComputeNodeList.get(j).decreaseFrequency();
                }
            }
            //Policy 3: if no SLA violation then Shrink active server

            for (int j = 0; j < User.ComputeNodeList.size() & User.numberofRunningNode() > (User.minProc + 1); j++) {
                if (User.ComputeNodeList.get(j).ready == 1 && User.ComputeNodeList.get(j).currentCPU == 0) {
                    //System.out.print("App:GR  " +app.id);
                    User.ComputeNodeList.get(j).makeItIdle(new EnterpriseJob());
                    //System.out.println("\tIdle\t\t\t\t\t@:"+Main.localTime+"\tNumber of running==  "+app.numberofRunningNode());
                }
            }
        }
        //Policy 2: If SLA is violated then increase frequency  of the nodes
        if (violationInEpoch > 0) {
            for (int j = 0; j < User.ComputeNodeList.size(); j++) {
                if (User.ComputeNodeList.get(j).ready == 0) {
                    User.ComputeNodeList.get(j).increaseFrequency();
                }
            }
            //Policy 4: if SLA violation then unshrink active server half of sleep nodes will wake up!
            int tedad = User.numberofIdleNode() / 2;
            for (int j = 0; j < User.ComputeNodeList.size() && tedad > 0; j++) {
                if (User.ComputeNodeList.get(j).ready == -1) {
                    System.out.println("USer GR: " + User.id + "\tactive a Server!\t\t @" + Simulator.getInstance().localTime
                            + "\tNumber of runinng:  " + User.numberofRunningNode());
                    User.ComputeNodeList.get(j).ready = 1;
                    User.ComputeNodeList.get(j).Mips = 1.4;
                    tedad--;
                }
            }
        }
        violationInEpoch = 0;
    }

    @Override
    //Side strategy 
    public void planning() {
//        if( Main.localTime%(Main.epochSideApp)!=0 )
//             return;
////        if(!sys.isThereFreeNodeforApp())
////            return;  
////       System.out.println("there is no node available in system! ");
//        
//        if(!app.isThereIdleNode() ||(sys.AM.compPwrApps[app.id]/(Main.epochSideApp*2*sys.applicationList.get(app.id).ComputeNodeList.size())>=0.5))// having idle probably dont need it!
//        {   
//            sys.AM.compPwrApps[app.id]=0;
//            sys.AM.SlaApps[app.id]=0;
//            return;
//        }
//         double max=app.AM.SlaApps[0];
//         int targetApp=0;
//         for(int i=0;i<sys.applicationList.size();i++)
//             if(sys.AM.SlaApps[i]>=max)
//             {  max=sys.AM.SlaApps[i];
//                targetApp=i;
//             }
//         
//        if((sys.AM.compPwrApps[targetApp]/(Main.epochSideApp*2*sys.applicationList.get(targetApp).ComputeNodeList.size()))>= 0.5 && max!=0 && targetApp!=app.id)
//        {  
//          allocateAnodetoThisApp(targetApp);
//        } 
//        sys.AM.compPwrApps[app.id]=0;
//        sys.AM.SlaApps[app.id]=0;
    }

    @Override
    public void execution() {
    }

    boolean allocateAnodetoThisUser(int targetUsr) {
        int index = User.myFirstIdleNode();
        if (index == -2) {
            return false;
        }
        if (User.ComputeNodeList.size() == 1)///*app.minProc*/ || sys.applicationList.get(targetApp).ComputeNodeList.size()==sys.applicationList.get(targetApp).maxProc)
        {
            return false;
        }
//        AMEnterpriseSys temp2=(AMEnterpriseSys)sys.AM;  
//        if(temp2.allocationVector[targetApp]<0)
//        {
//            System.out.println("no need pitttttttttttttttttttttttttttttttttttttttttttttttttttttttty");
//            return;
//        }
        BladeServer temp = new BladeServer(0);
        temp = User.ComputeNodeList.get(index);
        temp.maxExpectedRes = sys.UserList.get(targetUsr).maxExpectedResTime;
        temp.Mips = 1.4;
        temp.ready = 1;
        sys.UserList.get(targetUsr).ComputeNodeList.add(temp);
        User.ComputeNodeList.remove(index);
        System.out.println("User :\t" + User.id + " ----------> :\t\t " + targetUsr + "\t\t@:" + Simulator.getInstance().localTime + "\tRunning target node= " + sys.UserList.get(targetUsr).numberofRunningNode() + "\tRunning this node= " + User.numberofRunningNode() + "\tstrtgy= " + StrategyWsitch);
        StrategyWsitch = Simulator.StrategyEnum.SLA;
        return true;
    }
}
