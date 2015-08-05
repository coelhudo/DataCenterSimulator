package simulator.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.am.EnterpriseSystemAM;
import simulator.physical.BladeServer;
import simulator.physical.DataCenter;
import simulator.ra.MHR;
import simulator.schedulers.FIFOScheduler;

public class EnterpriseSystem extends GeneralSystem {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseSystem.class.getName());

    private List<EnterpriseApp> applicationList;
    private Environment environment;
    
    private EnterpriseSystem(SystemPOD systemPOD, Environment environment, DataCenter dataCenter) {
        super(systemPOD);
        this.environment = environment;
        setComputeNodeList(new ArrayList<BladeServer>());
        setComputeNodeIndex(new ArrayList<Integer>());
        applicationList = new ArrayList<EnterpriseApp>();
        setResourceAllocation(new MHR(environment, dataCenter));
        setSLAviolation(0);
        setNumberOfNode(systemPOD.getNumberOfNode());
        setRackIDs(systemPOD.getRackIDs());
        loadEnterpriseApplications(systemPOD);
        setScheduler(new FIFOScheduler());
    }
    
    private void loadEnterpriseApplications(SystemPOD systemPOD) {
        for (EnterpriseApplicationPOD pod : ((EnterpriseSystemPOD) systemPOD).getApplicationPODs()) {
            EnterpriseApp enterpriseApplication = new EnterpriseApp(pod, this, environment);
            applicationList.add(enterpriseApplication);
        }
    }

    public List<EnterpriseApp> getApplications() {
        return applicationList;
    }

    public boolean checkForViolation() {
        for (EnterpriseApp enterpriseApplication : applicationList) {
            if (enterpriseApplication.getSLAviolation() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean isThereFreeNodeforApp() {
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -2) {
                return true;
            }
        }
        return false;
    }

    public int numberofAvailableNodetoAlocate() {
        int n = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.getReady() == -2) {
                n++;
            }
        }
        return n;
    }

    boolean runAcycle() throws IOException {
        // if(applicationList.size()>0 & checkForViolation())//&
        // Main.localTime%Main.epochSys==0)
        // {
        // AM.monitor();
        // AM.analysis(SLAviolation);
        // AM.planning();
        // AM.execution();
        // Main.mesg++;
        //
        // }
        int finishedBundle = 0;
        for (int i = 0; i < applicationList.size(); i++) {
            // TODO: if each bundle needs some help should ask and here
            // resourceallocation should run
            if (applicationList.get(i).runAcycle() == false) // return false if
            // bundle set
            // jobs are
            // done, we need
            // to
            // re-resourcealocation
            {
                setNumberofIdleNode(applicationList.get(i).getComputeNodeList().size() + getNumberofIdleNode());
                LOGGER.info("Number of violation in " + applicationList.get(i).getID() + "th application=  "
                        + applicationList.get(i).getNumofViolation());
                // LOGGER.info("application "+i +"is destroyed and there
                // are: "+(applicationList.size()-1)+" left");
                applicationList.get(i).destroyApplication();
                applicationList.remove(i);
                finishedBundle++;
            }
        }
        if (finishedBundle > 0) {
            getResourceAllocation().resourceAloc(this); // Nothing for now!
        }
        if (applicationList.isEmpty()) {
            markAsDone(); // all done!
            return true;
        } else {
            return false;
        }
    }

    public static EnterpriseSystem Create(SystemPOD systemPOD, Environment environment, DataCenter dataCenter,
            SLAViolationLogger slaViolationLogger) {
        EnterpriseSystem enterpriseSytem = new EnterpriseSystem(systemPOD, environment, dataCenter);
        enterpriseSytem.getResourceAllocation().initialResourceAlocator(enterpriseSytem);
        //FIXME: why violation is logged in the AM class instead the system class
        enterpriseSytem.setAM(new EnterpriseSystemAM(enterpriseSytem, environment, slaViolationLogger));

        return enterpriseSytem;
    }
}
