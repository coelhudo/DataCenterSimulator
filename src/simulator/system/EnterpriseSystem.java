package simulator.system;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import simulator.am.GeneralAM;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;

public class EnterpriseSystem extends GeneralSystem {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseSystem.class.getName());

    private List<EnterpriseApp> applications;

    @Inject
    public EnterpriseSystem(@Assisted SystemPOD systemPOD,
                            @Assisted List<EnterpriseApp> applications,
                            @Named("EnterpriseSystem") Scheduler scheduler,
                            @Named("EnterpriseSystem") ResourceAllocation resourceAllocation,
                            @Named("EnterpriseSystem") GeneralAM systemAM) {
        super(systemPOD, scheduler, resourceAllocation, systemAM);
        setComputeNodeList(new ArrayList<BladeServer>());
        resetNumberOfSLAViolation();
        setNumberOfNode(systemPOD.getNumberOfNode());
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

    @Override
    public boolean runAcycle() {
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

    @Override
    public void finish() {

    }
}
