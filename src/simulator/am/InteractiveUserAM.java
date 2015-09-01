package simulator.am;

import java.util.logging.Logger;

import simulator.Environment;
import simulator.Simulator;
import simulator.jobs.EnterpriseJob;
import simulator.physical.BladeServer;
import simulator.system.InteractiveSystem;
import simulator.system.InteractiveUser;

public class InteractiveUserAM extends GeneralAM {

    private static final Logger LOGGER = Logger.getLogger(InteractiveUserAM.class.getName());

    InteractiveUser user;
    InteractiveSystem sys;
    double util = 0;
    static int violationInEpoch = 0;
    double percnt = 0;
    int accumulativeSLA = 0;
    // int cpAccumu=0;
    Simulator.StrategyEnum currentStrategy = Simulator.StrategyEnum.Green; // Green
    Environment environment;

    public InteractiveUserAM(InteractiveSystem sys, InteractiveUser user, Environment environment) {
        this.sys = sys;
        this.user = user;
        this.environment = environment;
    }

    @Override
    public void monitor() {
        // localUtilCal();
        getPercentageOfComputingPwr();
        /// Check if its neighborhood are not happy to see if it can help or
        /// not!
        for (int i = 0; i < sys.getUserList().size(); i++) {
            if (sys.getAM().getRecForCoopAt(i) == 1) {
                // TODO
            }
        }
    }

    public void localUtilCal() {
        double averageCPUUtilization = user.getAverageCPUUtilization();
        double[] power = user.getAveragePwrParam();
        double x = averageCPUUtilization * (power[0] - power[1]) / 100 + user.numberofIdleNode() * power[2]
                + user.numberofRunningNode() * power[1];
        // U= a x+ b y a=b=1
        util = x + user.getSLAviolation();
        // util=sigmoid(util);
        // LOGGER.info(util);
    }

    public double getPercentageOfComputingPwr() {
        int[] levels = { 0, 0, 0 };
        int index = 0;
        for (int j = 0; j < user.getComputeNodeList().size(); j++) {
            if (!user.getComputeNodeList().get(j).isIdle()) {
                index = user.getComputeNodeList().get(j).getCurrentFreqLevel();
                levels[index]++;
            }
        }
        percnt = percnt + levels[0] + 2 * levels[1] + 3 * levels[2];
        sys.getAM().setCompPowerAppsAt(user.getID(), sys.getAM().getCompPowerAppsAt(user.getID()) + levels[0]
                + 2 * levels[1] + 3 * levels[2]);
        return percnt;
    }

    @Override
    public void analysis(Object violation) {
        // if(StrategyWsitch==0)
        // analysis_GR(violation);
        // else
        analysis_SLA(violation);
    }
    // SLA Policy

    public void analysis_SLA(Object violation) {
        if (environment.localTimeByEpoch()) {
            violationInEpoch = (Integer) violation + violationInEpoch;
            return;
        }

        if (violationInEpoch > 0) {
            for (int j = 0; j < user.getComputeNodeList().size(); j++) {
                if (!user.getComputeNodeList().get(j).isIdle()) {
                    user.getComputeNodeList().get(j).increaseFrequency();
                }
            }
            int tedad = user.getComputeNodeList().size();
            // Policy 4: if SLA violation then unshrink active server
            for (int j = 0; j < user.getComputeNodeList().size() && tedad > 0; j++) {
                if (user.getComputeNodeList().get(j).isIdle()) {
                    // LOGGER.info("Application:SLA" +app.id +"\tActive
                    // one Server!\t\t "+"Number of runinng:
                    // "+app.numberofRunningNode());
                    user.getComputeNodeList().get(j).setStatusAsRunningNormal();
                    user.getComputeNodeList().get(j).setMips(1.4);
                    tedad--;
                }
            }
        }
        violationInEpoch = 0;
    }

    // Green policy is applied here:
    public void analysis_GR(Object violation) {
        if (environment.localTimeByEpoch()) {
            violationInEpoch = (Integer) violation + violationInEpoch;
            return;
        }
        // Policy 1: if no SLA violation then decrease frequency
        if (violationInEpoch == 0) {
            for (int j = 0; j < user.getComputeNodeList().size(); j++) {
                if (!user.getComputeNodeList().get(j).isIdle()) {
                    user.getComputeNodeList().get(j).decreaseFrequency();
                }
            }
            // Policy 3: if no SLA violation then Shrink active server

            for (int j = 0; j < user.getComputeNodeList().size()
                    & user.numberofRunningNode() > (user.getMinProc() + 1); j++) {
                if (user.getComputeNodeList().get(j).isRunningNormal()
                        && user.getComputeNodeList().get(j).getCurrentCPU() == 0) {
                    // System.out.print("App:GR " +app.id);
                    user.getComputeNodeList().get(j).makeItIdle(new EnterpriseJob());
                    // LOGGER.info("\tIdle\t\t\t\t\t@:"+Main.localTime+"\tNumber
                    // of running== "+app.numberofRunningNode());
                }
            }
        }
        // Policy 2: If SLA is violated then increase frequency of the nodes
        if (violationInEpoch > 0) {
            for (int j = 0; j < user.getComputeNodeList().size(); j++) {
                if (user.getComputeNodeList().get(j).isRunningBusy()) {
                    user.getComputeNodeList().get(j).increaseFrequency();
                }
            }
            // Policy 4: if SLA violation then unshrink active server half of
            // sleep nodes will wake up!
            int tedad = user.numberofIdleNode() / 2;
            for (int j = 0; j < user.getComputeNodeList().size() && tedad > 0; j++) {
                if (user.getComputeNodeList().get(j).isIdle()) {
                    LOGGER.info(
                            "USer GR: " + user.getID() + "\tactive a Server!\t\t @" + environment.getCurrentLocalTime()
                                    + "\tNumber of runinng:  " + user.numberofRunningNode());
                    user.getComputeNodeList().get(j).setStatusAsRunningNormal();
                    user.getComputeNodeList().get(j).setMips(1.4);
                    tedad--;
                }
            }
        }
        violationInEpoch = 0;
    }

    @Override
    // Side strategy
    public void planning() {
        // if( Main.localTime%(Main.epochSideApp)!=0 )
        // return;
        //// if(!sys.isThereFreeNodeforApp())
        //// return;
        //// LOGGER.info("there is no node available in system! ");
        //
        // if(!app.isThereIdleNode()
        // ||(sys.AM.compPwrApps[app.id]/(Main.epochSideApp*2*sys.applicationList.get(app.id).ComputeNodeList.size())>=0.5))//
        // having idle probably dont need it!
        // {
        // sys.AM.compPwrApps[app.id]=0;
        // sys.AM.SlaApps[app.id]=0;
        // return;
        // }
        // double max=app.AM.SlaApps[0];
        // int targetApp=0;
        // for(int i=0;i<sys.applicationList.size();i++)
        // if(sys.AM.SlaApps[i]>=max)
        // { max=sys.AM.SlaApps[i];
        // targetApp=i;
        // }
        //
        // if((sys.AM.compPwrApps[targetApp]/(Main.epochSideApp*2*sys.applicationList.get(targetApp).ComputeNodeList.size()))>=
        // 0.5 && max!=0 && targetApp!=app.id)
        // {
        // allocateAnodetoThisApp(targetApp);
        // }
        // sys.AM.compPwrApps[app.id]=0;
        // sys.AM.SlaApps[app.id]=0;
    }

    @Override
    public void execution() {
    }

    boolean allocateAnodetoThisUser(int targetUsr) {
        int index = user.myFirstIdleNode();
        if (index == -2) {
            return false;
        }
        if (user.getComputeNodeList().size() == 1) /// *app.minProc*/ ||
        /// sys.applicationList.get(targetApp).ComputeNodeList.size()==sys.applicationList.get(targetApp).maxProc)
        {
            return false;
        }
        // AMEnterpriseSys temp2=(AMEnterpriseSys)sys.AM;
        // if(temp2.allocationVector[targetApp]<0)
        // {
        // LOGGER.info("no need
        // pitttttttttttttttttttttttttttttttttttttttttttttttttttttttty");
        // return;
        // }
        BladeServer bladeServer = user.getComputeNodeList().get(index);
        bladeServer.setMaxExpectedRes(sys.getUserList().get(targetUsr).getMaxExpectedResTime());
        bladeServer.setMips(1.4);
        bladeServer.setStatusAsRunningNormal();
        sys.getUserList().get(targetUsr).getComputeNodeList().add(bladeServer);
        user.getComputeNodeList().remove(index);
        LOGGER.info("User :\t" + user.getID() + " ----------> :\t\t " + targetUsr + "\t\t@:"
                + environment.getCurrentLocalTime() + "\tRunning target node= "
                + sys.getUserList().get(targetUsr).numberofRunningNode() + "\tRunning this node= "
                + user.numberofRunningNode() + "\tstrtgy= " + currentStrategy);
        currentStrategy = Simulator.StrategyEnum.SLA;
        return true;
    }
}
