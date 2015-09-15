package simulator.tests.integration_tests;

import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.am.EnterpriseSystemAM;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.SystemPOD;

public class EnterpriseSystemIT {
    
    private static final Logger LOGGER = Logger.getLogger(EnterpriseSystemIT.class.getName());

    @Test
    public void testWithOneServerOneEnterpriseApplication() {
        Scheduler mockedScheduler = mock(Scheduler.class);
        ResourceAllocation mockedResourceAllocation = mock(ResourceAllocation.class);
        Environment environment = new Environment();
        SLAViolationLogger mockedSlaViolationLogger = mock(SLAViolationLogger.class);
        EnterpriseSystemAM enterpriseSystemAM = new EnterpriseSystemAM(environment, mockedSlaViolationLogger);
        SystemPOD systemPOD = new EnterpriseSystemPOD();
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        List<EnterpriseApp> applications = Arrays.asList(
                new EnterpriseApp(enterpriseApplicationPOD, mockedScheduler, mockedResourceAllocation, environment));
        EnterpriseSystem enterpriseSystem = EnterpriseSystem.create(systemPOD, mockedScheduler,
                mockedResourceAllocation, enterpriseSystemAM, applications);
        
        /*try {
            enterpriseSystem.runAcycle();
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }*/
    }

}
