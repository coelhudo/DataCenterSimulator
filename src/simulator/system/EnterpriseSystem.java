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
        resetNumberOfSLAViolation();
        setNumberOfNode(systemPOD.getNumberOfNode());
        setRackIDs(systemPOD.getRackIDs());
        setScheduler(new FIFOScheduler());
        loadEnterpriseApplications(systemPOD);
    }
    
    private void loadEnterpriseApplications(SystemPOD systemPOD) {
        for (EnterpriseApplicationPOD pod : ((EnterpriseSystemPOD) systemPOD).getApplicationPODs()) {
            EnterpriseApp enterpriseApplication = EnterpriseApp.Create(pod, this, environment);
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
            if (bladeServer.isNotApplicationAssigned()) {
                return true;
            }
        }
        return false;
    }

    public int numberofAvailableNodetoAlocate() {
        int n = 0;
        for (BladeServer bladeServer : getComputeNodeList()) {
            if (bladeServer.isNotApplicationAssigned()) {
                n++;
            }
        }
        return n;
    }

    boolean runAcycle() throws IOException {
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
                applicationList.get(i).destroyApplication();
                applicationList.remove(i);
                finishedBundle++;
            }
        }
        if (finishedBundle > 0) {
            getResourceAllocation().resourceAloc(this);
        }
        if (applicationList.isEmpty()) {
            markAsDone();
            return true;
        } else {
            return false;
        }
    }

    public static EnterpriseSystem Create(SystemPOD systemPOD, Environment environment, DataCenter dataCenter,
            SLAViolationLogger slaViolationLogger) {
        EnterpriseSystem enterpriseSystem = new EnterpriseSystem(systemPOD, environment, dataCenter);
        enterpriseSystem.getResourceAllocation().initialResourceAlocator(enterpriseSystem);
        //FIXME: why violation is logged in the AM class instead the system class
        enterpriseSystem.setAM(new EnterpriseSystemAM(enterpriseSystem, environment, slaViolationLogger));
        return enterpriseSystem;
    }
}
