package simulator.am;

import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import simulator.Environment;
import simulator.ManagedResource;
import simulator.Simulator;
import simulator.jobs.EnterpriseJob;
import simulator.physical.BladeServer;
import simulator.system.EnterpriseApp;

public class ApplicationAM extends GeneralAM {

    private static final Logger LOGGER = Logger.getLogger(ApplicationAM.class.getName());

    private EnterpriseApp app;
    static int violationInEpoch = 0;
    private GeneralAM am;
    private List<EnterpriseApp> applications;

    @Inject
    public ApplicationAM(@Assisted List<EnterpriseApp> applications, @Assisted AutonomicManager am, Environment environment) {
        super(environment);
        this.am = (GeneralAM) am;
        this.applications = applications;
    }

	@Override
	public void setManagedResource(ManagedResource mananagedResource) {
		 this.app = (EnterpriseApp) mananagedResource;
		
	}

    @Override
    public void monitor() {
        SLAcal(); // calculate SLA violation of itself
        localUtilCal();
        getPercentageOfComputingPwr();
        /// Check if its neighborhood are not happy to see if it can help or
        /// not!
        for (int i = 0; i < applications.size(); i++) {
            if (am.getRecForCoopAt(i) == 1) {
                if (app.numberOfWaitingJobs() < applications.get(i).numberOfWaitingJobs()) {// this
                    // app
                    // can
                    // generously
                    // give
                    // one
                    // resource
                    // to
                    // the
                    // needed
                    // app
                    // e.i.
                    // index
                    // i
                    // if(allocateAnodetoThisApp(i)==true)
                    // sys.AM.recForCoop[i]=0;
                }
            }
        }
    }

    private void localUtilCal() {
        double CPU = app.getAverageCPUutil(); // average CPU utilization of
        // nodes
        double[] pwr = app.getAveragePwrParam();
        double x = CPU * (pwr[0] - pwr[1]) / 100 + app.numberofIdleNode() * pwr[2] + app.numberofRunningNode() * pwr[1];
        // U= a x+ b y a=b=1
        setUtil(x + app.getSLAviolation());
        // util=sigmoid(util);
        // LOGGER.info(util);
    }

    private double getPercentageOfComputingPwr() {
        int[] levels = { 0, 0, 0 };
        int index = 0;
        for (int j = 0; j < app.getComputeNodeList().size(); j++) {
            if (!app.getComputeNodeList().get(j).isIdle()) {
                index = app.getComputeNodeList().get(j).getCurrentFreqLevel();
                levels[index]++;
            }
        }
        setPercnt(getPercnt() + levels[0] + 2 * levels[1] + 3 * levels[2]);
        am.setCompPowerAppsAt(app.getID(),
                am.getCompPowerAppsAt(app.getID()) + levels[0] + 2 * levels[1] + 3 * levels[2]);
        return getPercnt();
    }

    private void SLAcal() {
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
        if ((tmp * 100.0 / totalJob) > (100.0 - percentage)) {
            // SLAviolation:
            // percentage of
            // jobs have
            // violation
            app.setSLAviolation((int) Math.ceil(tmp * 100.0 / totalJob) - 100 + percentage);
            app.setNumofViolation(app.getNumofViolation() + 1);
            // LOGGER.info("SLA violation Application\t"+app.SLAviolation
            // + Main.localTime);
        }
        setAccumulativeSLA(getAccumulativeSLA() + app.getSLAviolation());
        am.SlaApps[app.getID()] = am.SlaApps[app.getID()] + getAccumulativeSLA();
        // LOGGER.info("ACCCUMU \t"+accumulativeSLA);
    }

    @Override
    public void analysis() {
        if (getStrategy() == Simulator.StrategyEnum.Green) {
            analysis_GR();
        } else {
            analysis_SLA();
        }
    }
    // SLA Policy

    private void analysis_SLA() {
        if (environment().localTimeByEpoch()) {
            return;
        }

        if (violationInEpoch > 0) {
            for (BladeServer bladeServer : app.getComputeNodeList()) {
                if (!bladeServer.isIdle()) {
                    bladeServer.increaseFrequency();
                }
            }
            int tedad = app.getComputeNodeList().size();
            // Policy 4: if SLA violation then unshrink active server
            for (int j = 0; j < app.getComputeNodeList().size() && tedad > 0; j++) {
                if (app.getComputeNodeList().get(j).isIdle()) {
                    // LOGGER.info("Application:SLA" +app.id +"\tActive
                    // one Server!\t\t "+"Number of runinng:
                    // "+app.numberofRunningNode());
                    app.getComputeNodeList().get(j).setStatusAsRunningNormal();
                    app.getComputeNodeList().get(j).setMips(1.4);
                    tedad--;
                    environment().updateNumberOfMessagesFromDataCenterToSystem();
                }
            }
        }
        violationInEpoch = 0;
    }

    // Green policy is applied here:
    private void analysis_GR() {
        if (environment().localTimeByEpoch()) {
            return;
        }
        // Policy 1: if no SLA violation then decrease frequency
        if (violationInEpoch == 0) {
            for (BladeServer bladeServer : app.getComputeNodeList()) {
                if (!bladeServer.isIdle()) {
                    bladeServer.decreaseFrequency();
                }
            }
            // Policy 3: if no SLA violation then Shrink active server

            for (int j = 0; j < app.getComputeNodeList().size()
                    & app.numberofRunningNode() > (app.getMinProc() + 1); j++) {
                if (app.getComputeNodeList().get(j).isIdle() && app.getComputeNodeList().get(j).getCurrentCPU() == 0) {
                    app.getComputeNodeList().get(j).makeItIdle(new EnterpriseJob());
                    // LOGGER.info("\tIdle\t\t\t\t\t@:"+Main.localTime+"\tNumber
                    // of running== "+app.numberofRunningNode());
                    environment().updateNumberOfMessagesFromDataCenterToSystem();
                }
            }
        }
        // Policy 2: If SLA is violated then increase frequency of the nodes
        if (violationInEpoch > 0) {
            for (BladeServer bladeServer : app.getComputeNodeList()) {
                if (bladeServer.isRunningBusy()) {
                    bladeServer.increaseFrequency();
                }
            }
            // Policy 4: if SLA violation then unshrink active server half of
            // sleep nodes will wake up!
            int tedad = app.numberofIdleNode() / 2;
            for (int j = 0; j < app.getComputeNodeList().size() && tedad > 0; j++) {
                if (app.getComputeNodeList().get(j).isIdle()) {
                    LOGGER.info(
                            "App GR: " + app.getID() + "\tactive a Server!\t\t @" + environment().getCurrentLocalTime()
                                    + "\tNumber of runinng:  " + app.numberofRunningNode());
                    app.getComputeNodeList().get(j).setStatusAsRunningNormal();
                    app.getComputeNodeList().get(j).setMips(1.4);
                    tedad--;
                    environment().updateNumberOfMessagesFromDataCenterToSystem();
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

    @SuppressWarnings("unused")
    private boolean allocateAnodetoThisApp(int targetApp) {
        int index = app.myFirstIdleNode();
        if (index == -2) {
            return false;
        }
        if (app.getComputeNodeList().size() == 1) {
            /// *app.minProc*/ ||
            /// sys.applicationList.get(targetApp).ComputeNodeList.size()==sys.applicationList.get(targetApp).maxProc)
            return false;
        }
        // AMEnterpriseSys temp2=(AMEnterpriseSys)sys.AM;
        // if(temp2.allocationVector[targetApp]<0)
        // {
        // LOGGER.info("no need
        // pitttttttttttttttttttttttttttttttttttttttttttttttttttttttty");
        // return;
        // }
        BladeServer bladeServer = app.getComputeNodeList().get(index);
        bladeServer.setSLAPercentage(applications.get(targetApp).getSLAPercentage());
        bladeServer.setTimeTreshold(applications.get(targetApp).getTimeTreshold());
        bladeServer.setMips(1.4);
        bladeServer.setStatusAsRunningNormal();
        applications.get(targetApp).getComputeNodeList().add(bladeServer);
        app.getComputeNodeList().remove(index);
        LOGGER.info("app:\t" + app.getID() + " ----------> :\t\t " + targetApp + "\t\t@:"
                + environment().getCurrentLocalTime() + "\tRunning target node= "
                + applications.get(targetApp).numberofRunningNode() + "\tRunning this node= "
                + app.numberofRunningNode() + "\tstrtgy= " + getStrategy());
        setStrategy(Simulator.StrategyEnum.SLA);
        return true;
    }
}
