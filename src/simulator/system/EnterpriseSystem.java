package simulator.system;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import simulator.am.EnterpriseSystemAM;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

public class EnterpriseSystem extends GeneralSystem {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseSystem.class.getName());

    private List<EnterpriseApp> applications;

    private EnterpriseSystem(SystemPOD systemPOD, List<EnterpriseApp> applications, Scheduler scheduler,
            ResourceAllocation resourceAllocation) {
        super(systemPOD, scheduler, resourceAllocation);
        setComputeNodeList(new ArrayList<BladeServer>());
        setComputeNodeIndex(new ArrayList<Integer>());
        resetNumberOfSLAViolation();
        setNumberOfNode(systemPOD.getNumberOfNode());
        setRackIDs(systemPOD.getRackIDs());
        this.applications = applications;
    }

    public List<EnterpriseApp> getApplications() {
        return applications;
    }

    public boolean checkForViolation() {
        for (EnterpriseApp enterpriseApplication : applications) {
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

    public boolean runAcycle() throws IOException {
        for (int i = 0; i < applications.size(); i++) {
            // TODO: if each bundle needs some help should ask and here
            // resourceallocation should run
            if (applications.get(i).runAcycle() == false) {
                // if returns false if bundle set jobs are done, we need to
                // re-resourcealocation
                setNumberOfIdleNode(applications.get(i).getComputeNodeList().size() + getNumberOfIdleNode());
                LOGGER.info("Number of violation in " + applications.get(i).getID() + "th application=  "
                        + applications.get(i).getNumofViolation());
                applications.get(i).destroyApplication();
                applications.remove(i);
            }
        }

        if (applications.isEmpty()) {
            markAsDone();
            return true;
        }

        return false;
    }

    public static EnterpriseSystem create(SystemPOD systemPOD, Scheduler scheduler,
            ResourceAllocation resourceAllocation, EnterpriseSystemAM enterpriseSystemAM,
            List<EnterpriseApp> applications) {

        EnterpriseSystem enterpriseSystem = new EnterpriseSystem(systemPOD, applications, scheduler,
                resourceAllocation);
        enterpriseSystem.getResourceAllocation().initialResourceAlocator(enterpriseSystem);
        enterpriseSystem.setAM(enterpriseSystemAM);
        return enterpriseSystem;
    }
}
