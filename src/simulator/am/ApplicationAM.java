package simulator.am;

import simulator.physical.BladeServer;
import simulator.EnterpriseApp;
import simulator.EnterpriseSystem;
import simulator.Simulator;
import simulator.jobs.EnterpriseJob;

public class ApplicationAM extends GeneralAM {

    EnterpriseSystem sys;
    EnterpriseApp app;//=new application(null, null);
    static int violationInEpoch = 0;
    public double util = 0;
    public double percnt = 0;
    int accumulativeSLA = 0;
    //int cpAccumu=0;
    Simulator.StrategyEnum StrategyWsitch = Simulator.StrategyEnum.Green;

    public ApplicationAM(EnterpriseSystem Sys, EnterpriseApp app) {
        //dc=dtcenter;
        this.sys = Sys;
        this.app = app;
    }

    @Override
    public void monitor() {
        SLAcal(); //calculate SLA violation of itself
        localUtilCal();
        getPercentageOfComputingPwr();
        ///Check if its neighborhood are not happy to see if it can help or not!
        for (int i = 0; i < sys.applicationList.size(); i++) {
            if (sys.am.recForCoop[i] == 1) {
                if (app.numberOfWaitingJobs() < sys.applicationList.get(i).numberOfWaitingJobs()) {//this app can generously give one resource to the needed app e.i. index i
                    // if(allocateAnodetoThisApp(i)==true)
                    //     sys.AM.recForCoop[i]=0;
                }
            }
        }
    }

    public void localUtilCal() {
        double CPU = app.getAverageCPUutil(); //average CPU utilization of nodes
        double[] pwr = app.getAveragePwrParam();
        double x = CPU * (pwr[0] - pwr[1]) / 100 + app.numberofIdleNode() * pwr[2] + app.numberofRunningNode() * pwr[1];
        //U= a x+ b y a=b=1
        util = x + app.SLAviolation;
        //util=sigmoid(util);
        //System.out.println(util);
    }

    public double getPercentageOfComputingPwr() {
        int[] levels = {0, 0, 0};
        int index = 0;
        for (int j = 0; j < app.ComputeNodeList.size(); j++) {
            if (app.ComputeNodeList.get(j).ready != -1) //it is idle
            {
                index = app.ComputeNodeList.get(j).getCurrentFreqLevel();
                levels[index]++;
            }
        }
        percnt = percnt + levels[0] + 2 * levels[1] + 3 * levels[2];
        sys.am.compPwrApps[app.id] = sys.am.compPwrApps[app.id] + levels[0] + 2 * levels[1] + 3 * levels[2];
        return percnt;
    }

    public void SLAcal() {
        app.SLAviolation = 0;
        int percentage = app.ComputeNodeList.get(0).SLAPercentage;
        int treshold = app.ComputeNodeList.get(0).timeTreshold;
        double tmp = 0;
        double totalJob = 0;
        for (int j = 0; j < app.responseList.size(); j++) {
            if (app.responseList.get(j).getResponseTime() > treshold) {
                tmp = app.responseList.get(j).getNumberOfJob() + tmp;
            }
            totalJob = totalJob + app.responseList.get(j).getNumberOfJob();
        }
        app.responseList.clear();
        if ((tmp * 100.0 / totalJob) > (100.0 - percentage)) //SLAviolation: percentage of jobs have violation
        {
            app.SLAviolation = (int) Math.ceil(tmp * 100.0 / totalJob) - 100 + percentage;
            //app.NumofViolation=app.NumofViolation+app.SLAviolation;
            app.NumofViolation++;
            // System.out.println("SLA violation Application\t"+app.SLAviolation  + Main.localTime);
        }
        accumulativeSLA = accumulativeSLA + app.SLAviolation;
        sys.am.SlaApps[app.id] = sys.am.SlaApps[app.id] + accumulativeSLA;
        //cpAccumu=cpAccumu+app.SLAviolation;
        // System.out.println("ACCCUMU     \t"+accumulativeSLA);
    }

    @Override
    public void analysis(Object violation) {
        if (StrategyWsitch == Simulator.StrategyEnum.Green) {
            analysis_GR(violation);
        } else {
            analysis_SLA(violation);
        }
    }
    //SLA Policy 

    public void analysis_SLA(Object violation) {
        if (Simulator.getInstance().localTime % Simulator.getInstance().epochApp != 0)// || Main.localTime<0)
        {
            violationInEpoch = (Integer) violation + violationInEpoch;
            return;
        }

        if (violationInEpoch > 0) {
            for (int j = 0; j < app.ComputeNodeList.size(); j++) {
                if (app.ComputeNodeList.get(j).ready != -1) //except idle nodes
                {
                    app.ComputeNodeList.get(j).increaseFrequency();
                }
            }
            int tedad = app.ComputeNodeList.size();
            //Policy 4: if SLA violation then unshrink active server
            for (int j = 0; j < app.ComputeNodeList.size() && tedad > 0; j++) {
                if (app.ComputeNodeList.get(j).ready == -1) {
                    //System.out.println("Application:SLA" +app.id +"\tActive one Server!\t\t "+"Number of runinng:  "+app.numberofRunningNode());
                    app.ComputeNodeList.get(j).ready = 1;
                    app.ComputeNodeList.get(j).Mips = 1.4;
                    tedad--;
                    Simulator.getInstance().mesg++;
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
            for (int j = 0; j < app.ComputeNodeList.size(); j++) {
                if (app.ComputeNodeList.get(j).ready != -1) {
                    app.ComputeNodeList.get(j).decreaseFrequency();
                }
            }
            //Policy 3: if no SLA violation then Shrink active server

            for (int j = 0; j < app.ComputeNodeList.size() & app.numberofRunningNode() > (app.minProc + 1); j++) {
                if (app.ComputeNodeList.get(j).ready == 1 && app.ComputeNodeList.get(j).currentCPU == 0) {
                    //System.out.print("App:GR  " +app.id);
                    app.ComputeNodeList.get(j).makeItIdle(new EnterpriseJob());
                    //System.out.println("\tIdle\t\t\t\t\t@:"+Main.localTime+"\tNumber of running==  "+app.numberofRunningNode());
                    Simulator.getInstance().mesg++;
                }
            }
        }
        //Policy 2: If SLA is violated then increase frequency  of the nodes
        if (violationInEpoch > 0) {
            for (int j = 0; j < app.ComputeNodeList.size(); j++) {
                if (app.ComputeNodeList.get(j).ready == 0) {
                    app.ComputeNodeList.get(j).increaseFrequency();
                }
            }
            //Policy 4: if SLA violation then unshrink active server half of sleep nodes will wake up!
            int tedad = app.numberofIdleNode() / 2;
            for (int j = 0; j < app.ComputeNodeList.size() && tedad > 0; j++) {
                if (app.ComputeNodeList.get(j).ready == -1) {
                    System.out.println("App GR: " + app.id + "\tactive a Server!\t\t @" + Simulator.getInstance().localTime + "\tNumber of runinng:  " + app.numberofRunningNode());
                    app.ComputeNodeList.get(j).ready = 1;
                    app.ComputeNodeList.get(j).Mips = 1.4;
                    tedad--;
                    Simulator.getInstance().mesg++;
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

    boolean allocateAnodetoThisApp(int targetApp) {
        int index = app.myFirstIdleNode();
        if (index == -2) {
            return false;
        }
        if (app.ComputeNodeList.size() == 1)///*app.minProc*/ || sys.applicationList.get(targetApp).ComputeNodeList.size()==sys.applicationList.get(targetApp).maxProc)
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
        temp = app.ComputeNodeList.get(index);
        temp.SLAPercentage = sys.applicationList.get(targetApp).SLAPercentage;
        temp.timeTreshold = sys.applicationList.get(targetApp).timeTreshold;
        temp.Mips = 1.4;
        temp.ready = 1;
        sys.applicationList.get(targetApp).ComputeNodeList.add(temp);
        app.ComputeNodeList.remove(index);
        System.out.println("app:\t" + app.id + " ----------> :\t\t " + targetApp + "\t\t@:" + Simulator.getInstance().localTime + "\tRunning target node= " + sys.applicationList.get(targetApp).numberofRunningNode() + "\tRunning this node= " + app.numberofRunningNode() + "\tstrtgy= " + StrategyWsitch);
        StrategyWsitch = Simulator.StrategyEnum.SLA;
        return true;
    }
}
