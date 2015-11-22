package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.SimulatorEnvironment;
import simulator.am.DataCenterAM;
import simulator.am.EnterpriseSystemAM;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.EnterpriseJobProducer;
import simulator.physical.BladeServerPOD;
import simulator.physical.ChassisPOD;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterEntityID;
import simulator.physical.DataCenterPOD;
import simulator.physical.RackPOD;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.FIFOScheduler;
import simulator.schedulers.Scheduler;
import simulator.system.ComputeSystemFactory;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.InteractiveSystemFactory;
import simulator.system.SystemPOD;
import simulator.system.Systems;
import simulator.system.SystemsPOD;
import simulator.utils.ActivitiesLogger;

public class EnterpriseSystemIT {

    public static final double[] FREQUENCY_LEVEL = { 1.4, 1.4, 1.4 };
    public static final double[] POWER_IDLE = { 100, 100, 128 };
    public static final double[] POWER_BUSY = { 300, 336, 448 };

    @Test
    public void testWithOneServerOneEnterpriseApplication() {
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setBladeType("DummyType");
        bladeServerPOD.setFrequencyLevel(FREQUENCY_LEVEL);
        bladeServerPOD.setPowerBusy(POWER_BUSY);
        bladeServerPOD.setPowerIdle(POWER_IDLE);
        bladeServerPOD.setIdleConsumption(5);
        bladeServerPOD.setID(DataCenterEntityID.createServerID(1, 1, 1));

        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        chassisPOD.setBladeType("DummyType");
        chassisPOD.setChassisType("DummyChassisType");
        chassisPOD.setID(DataCenterEntityID.createChassisID(1, 1));

        RackPOD rackPOD = new RackPOD();
        rackPOD.appendChassis(chassisPOD);
        rackPOD.setID(DataCenterEntityID.createRackID(1));

        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.appendRack(rackPOD);
        dataCenterPOD.setD(0, 0, 100);

        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);
        Environment environment = new SimulatorEnvironment();

        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, environment);

        Systems systems = new Systems(environment, new SystemsPOD(), mock(ComputeSystemFactory.class),
                mock(InteractiveSystemFactory.class));
        systems.setup();
        DataCenterAM dataCenterAM = new DataCenterAM(environment, systems);
        dataCenter.setAM(dataCenterAM);

        Scheduler fcfsScheduler = new FIFOScheduler();
        ResourceAllocation resourceAllocation = new MHR(environment, dataCenter);
        SLAViolationLogger mockedSlaViolationLogger = mock(SLAViolationLogger.class);
        EnterpriseSystemAM enterpriseSystemAM = new EnterpriseSystemAM(environment, mockedSlaViolationLogger);
        EnterpriseJobProducer mockedEnterpriseJobProducer = mock(EnterpriseJobProducer.class);
        EnterpriseJob enterpriseJob = new EnterpriseJob();
        enterpriseJob.setArrivalTimeOfJob(1);
        enterpriseJob.setNumberOfJob(1);
        when(mockedEnterpriseJobProducer.hasNext()).thenReturn(true);
        when(mockedEnterpriseJobProducer.next()).thenReturn(enterpriseJob);

        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        enterpriseApplicationPOD.setJobProducer(mockedEnterpriseJobProducer);
        enterpriseApplicationPOD.setID(1);
        enterpriseApplicationPOD.setMinProc(1);
        enterpriseApplicationPOD.setMaxProc(1);
        enterpriseApplicationPOD.setTimeTreshold(2);
        enterpriseApplicationPOD.setSLAPercentage(90);
        enterpriseApplicationPOD.setMaxNumberOfRequest(1000);
        enterpriseApplicationPOD.setNumberofBasicNode(1);
        enterpriseApplicationPOD.setMaxExpectedResTime(2);

        SystemPOD systemPOD = new EnterpriseSystemPOD();
        systemPOD.setName("DummyHPCSystem");
        systemPOD.setNumberofNode(1);
        systemPOD.appendRackID(rackPOD.getID());

        List<EnterpriseApp> applications = Arrays
                .asList(new EnterpriseApp(enterpriseApplicationPOD, fcfsScheduler, resourceAllocation, environment));
        EnterpriseSystem enterpriseSystem = new EnterpriseSystem(systemPOD, applications, fcfsScheduler,
                resourceAllocation, enterpriseSystemAM);
        enterpriseSystem.getResourceAllocation().initialResourceAlocator(enterpriseSystem);
        enterpriseSystem.setupAM();
        systems.addEnterpriseSystem(enterpriseSystem);

        dataCenter.calculatePower();
        assertEquals(100.00014706045461, dataCenter.getTotalPowerConsumption(), 1.0E-8);

        assertFalse(enterpriseSystem.runAcycle());

        dataCenter.calculatePower();
        assertEquals(202.0002912373414, dataCenter.getTotalPowerConsumption(), 1.0E-8);

        verify(mockedEnterpriseJobProducer).hasNext();
        verify(mockedEnterpriseJobProducer).next();

        verify(mockedActivitiesLogger, times(4)).write(anyString());

        assertEquals(0.0, enterpriseSystem.getPower(), 1.0E-8);

        verifyNoMoreInteractions(mockedSlaViolationLogger, mockedEnterpriseJobProducer, mockedActivitiesLogger);
    }

}
