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
    private double util = 0;
    private double percnt = 0;
    int accumulativeSLA = 0;
    //int cpAccumu=0;
    Simulator.StrategyEnum StrategyWsitch = Simulator.StrategyEnum.Green;
    Simulator.LocalTime localTime;
    
    public ApplicationAM(EnterpriseSystem Sys, EnterpriseApp app, Simulator.LocalTime localTime) {
        //dc=dtcenter;
        this.sys = Sys;
        this.app = app;
        this.localTime = localTime;
    }

    @Override
    public void monitor() {
        SLAcal(); //calculate SLA violation of itself
        localUtilCal();
        getPercentageOfComputingPwr();
        ///Check if its neighborhood are not happy to see if it can help or not!
        for (int i = 0; i < sys.applicationList.size(); i++) {
            if (sys.getAM().getRecForCoop()[i] == 1) {
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
        setUtil(x + app.getSLAviolation());
        //util=sigmoid(util);
        //System.out.println(util);
    }

    public double getPercentageOfComputingPwr() {
        int[] levels = {0, 0, 0};
        int index = 0;
        for (int j = 0; j < app.getComputeNodeList().size(); j++) {
            if (app.getComputeNodeList().get(j).getReady() != -1) //it is idle
            {
                index = app.getComputeNodeList().get(j).getCurrentFreqLevel();
                levels[index]++;
            }
        }
        setPercnt(getPercnt() + levels[0] + 2 * levels[1] + 3 * levels[2]);
        sys.getAM().getCompPwrApps()[app.getID()] = sys.getAM().getCompPwrApps()[app.getID()] + levels[0] + 2 * levels[1] + 3 * levels[2];
        return getPercnt();
    }

    public void SLAcal() {
        app.setSLAviolation(0);
        int percentage = app.getComputeNodeList().get(0).getSLAPercentage();
        int treshold = app.getComputeNodeList().get(0).getTimeTreshold();
        double tmp = 0;
        double totalJob = 0;
        for (int j = 0; j < app.getResponseList().size(); j++) {
            if (app.getResponseList().get(j).getResponseTime() > treshold) {
                tmp = app.getResponseList().get(j).getNumberOfJob() + tmp;
            }
            totalJob = totalJob + app.getResponseList().get(j).getNumberOfJob();
        }
        app.getResponseList().clear();
        if ((tmp * 100.0 / totalJob) > (100.0 - percentage)) //SLAviolation: percentage of jobs have violation
        {
            app.setSLAviolation((int) Math.ceil(tmp * 100.0 / totalJob) - 100 + percentage);
            //app.NumofViolation=app.NumofViolation+app.SLAviolation;
            app.setNumofViolation(app.getNumofViolation() + 1);
            // System.out.println("SLA violation Application\t"+app.SLAviolation  + Main.localTime);
        }
        accumulativeSLA = accumulativeSLA + app.getSLAviolation();
        sys.getAM().SlaApps[app.getID()] = sys.getAM().SlaApps[app.getID()] + accumulativeSLA;
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
        if (localTime.getCurrentLocalTime() % Simulator.getInstance().epochApp != 0)// || Main.localTime<0)
        {
            violationInEpoch = (Integer) violation + violationInEpoch;
            return;
        }

        if (violationInEpoch > 0) {
            for (int j = 0; j < app.getComputeNodeList().size(); j++) {
                if (app.getComputeNodeList().get(j).getReady() != -1) //except idle nodes
                {
                    app.getComputeNodeList().get(j).increaseFrequency();
                }
            }
            int tedad = app.getComputeNodeList().size();
            //Policy 4: if SLA violation then unshrink active server
            for (int j = 0; j < app.getComputeNodeList().size() && tedad > 0; j++) {
                if (app.getComputeNodeList().get(j).getReady() == -1) {
                    //System.out.println("Application:SLA" +app.id +"\tActive one Server!\t\t "+"Number of runinng:  "+app.numberofRunningNode());
                    app.getComputeNodeList().get(j).setReady(1);
                    app.getComputeNodeList().get(j).setMips(1.4);
                    tedad--;
                    Simulator.getInstance().numberOfMessagesFromDataCenterToSystem++;
                }
            }
        }
        violationInEpoch = 0;
    }

    // Green policy is applied here:
    public void analysis_GR(Object violation) {
        if (localTime.getCurrentLocalTime() % Simulator.getInstance().epochApp != 0)// || Main.localTime<0)
        {
            violationInEpoch = (Integer) violation + violationInEpoch;
            return;
        }
        //Policy 1: if no SLA violation then decrease frequency
        if (violationInEpoch == 0) {
            for (int j = 0; j < app.getComputeNodeList().size(); j++) {
                if (app.getComputeNodeList().get(j).getReady() != -1) {
                    app.getComputeNodeList().get(j).decreaseFrequency();
                }
            }
            //Policy 3: if no SLA violation then Shrink active server

            for (int j = 0; j < app.getComputeNodeList().size() & app.numberofRunningNode() > (app.getMinProc() + 1); j++) {
                if (app.getComputeNodeList().get(j).getReady() == 1 && app.getComputeNodeList().get(j).getCurrentCPU() == 0) {
                    //System.out.print("App:GR  " +app.id);
                    app.getComputeNodeList().get(j).makeItIdle(new EnterpriseJob());
                    //System.out.println("\tIdle\t\t\t\t\t@:"+Main.localTime+"\tNumber of running==  "+app.numberofRunningNode());
                    Simulator.getInstance().numberOfMessagesFromDataCenterToSystem++;
                }
            }
        }
        //Policy 2: If SLA is violated then increase frequency  of the nodes
        if (violationInEpoch > 0) {
            for (int j = 0; j < app.getComputeNodeList().size(); j++) {
                if (app.getComputeNodeList().get(j).getReady() == 0) {
                    app.getComputeNodeList().get(j).increaseFrequency();
                }
            }
            //Policy 4: if SLA violation then unshrink active server half of sleep nodes will wake up!
            int tedad = app.numberofIdleNode() / 2;
            for (int j = 0; j < app.getComputeNodeList().size() && tedad > 0; j++) {
                if (app.getComputeNodeList().get(j).getReady() == -1) {
                    System.out.println("App GR: " + app.getID() + "\tactive a Server!\t\t @" + Simulator.getInstance().getLocalTime() + "\tNumber of runinng:  " + app.numberofRunningNode());
                    app.getComputeNodeList().get(j).setReady(1);
                    app.getComputeNodeList().get(j).setMips(1.4);
                    tedad--;
                    Simulator.getInstance().numberOfMessagesFromDataCenterToSystem++;
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
        if (app.getComputeNodeList().size() == 1)///*app.minProc*/ || sys.applicationList.get(targetApp).ComputeNodeList.size()==sys.applicationList.get(targetApp).maxProc)
        {
            return false;
        }
//        AMEnterpriseSys temp2=(AMEnterpriseSys)sys.AM;  
//        if(temp2.allocationVector[targetApp]<0)
//        {
//            System.out.println("no need pitttttttttttttttttttttttttttttttttttttttttttttttttttttttty");
//            return;
//        }
        BladeServer temp = new BladeServer(0, localTime);
        temp = app.getComputeNodeList().get(index);
        temp.setSLAPercentage(sys.applicationList.get(targetApp).getSLAPercentage());
        temp.setTimeTreshold(sys.applicationList.get(targetApp).getTimeTreshold());
        temp.setMips(1.4);
        temp.setReady(1);
        sys.applicationList.get(targetApp).getComputeNodeList().add(temp);
        app.getComputeNodeList().remove(index);
        System.out.println("app:\t" + app.getID() + " ----------> :\t\t " + targetApp + "\t\t@:" + Simulator.getInstance().getLocalTime() + "\tRunning target node= " + sys.applicationList.get(targetApp).numberofRunningNode() + "\tRunning this node= " + app.numberofRunningNode() + "\tstrtgy= " + StrategyWsitch);
        StrategyWsitch = Simulator.StrategyEnum.SLA;
        return true;
    }

	public double getUtil() {
		return util;
	}

	public void setUtil(double util) {
		this.util = util;
	}

	public double getPercnt() {
		return percnt;
	}

	public void setPercnt(double percnt) {
		this.percnt = percnt;
	}
}
