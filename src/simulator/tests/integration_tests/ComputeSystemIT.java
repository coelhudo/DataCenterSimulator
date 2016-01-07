package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.am.ComputeSystemAM;
import simulator.am.DataCenterAM;
import simulator.am.GeneralAM;
import simulator.jobs.BatchJob;
import simulator.jobs.JobProducer;
import simulator.physical.BladeServerPOD;
import simulator.physical.ChassisPOD;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterEntityID;
import simulator.physical.DataCenterPOD;
import simulator.physical.RackPOD;
import simulator.ra.MHR;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.LeastRemainFirstScheduler;
import simulator.schedulers.Scheduler;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemFactory;
import simulator.system.ComputeSystemPOD;
import simulator.system.EnterpriseSystemFactory;
import simulator.system.InteractiveSystemFactory;
import simulator.system.Systems;
import simulator.system.SystemsPOD;
import simulator.utils.ActivitiesLogger;

public class ComputeSystemIT {

    public static final double[] FREQUENCY_LEVEL = { 1.4, 1.4, 1.4 };
    public static final double[] POWER_IDLE = { 100, 100, 128 };
    public static final double[] POWER_BUSY = { 300, 336, 448 };

    public BladeServerPOD bladeServerPOD;
    public ChassisPOD chassisPOD;
    public RackPOD rackPOD;
    public Environment mockedEnvironment;
    public ActivitiesLogger mockedActivitiesLogger;

    public ComputeSystemPOD computeSystemPOD;
    public SystemsPOD systemsPOD;
    public JobProducer mockedJobProducer;

    public DataCenterAM mockedDataCenterAM;
    public SLAViolationLogger slaViolationLogger;
    
    public ComputeSystemFactory computeSystemFactory;
    public InteractiveSystemFactory interactiveSystemFactory;
    public EnterpriseSystemFactory enterpriseSystemFactory;
    
    
    @Before
    public void setUp() {
        Logger log = LogManager.getLogManager().getLogger("");
        for (Handler h : log.getHandlers()) {
            h.setLevel(Level.FINE);
        }

        bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setBladeType("DummyType");
        bladeServerPOD.setFrequencyLevel(FREQUENCY_LEVEL);
        bladeServerPOD.setPowerBusy(POWER_BUSY);
        bladeServerPOD.setPowerIdle(POWER_IDLE);
        bladeServerPOD.setStandByConsumption(5);
        bladeServerPOD.setID(DataCenterEntityID.createServerID(1, 1, 1));

        chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        chassisPOD.setBladeType("DummyType");
        chassisPOD.setChassisType("DummyChassisType");
        chassisPOD.setID(DataCenterEntityID.createChassisID(1, 1));

        rackPOD = new RackPOD();
        rackPOD.appendChassis(chassisPOD);
        rackPOD.setID(DataCenterEntityID.createRackID(1));

        mockedEnvironment = mock(Environment.class);
        mockedActivitiesLogger = mock(ActivitiesLogger.class);

        mockedJobProducer = mock(JobProducer.class);

        computeSystemPOD = new ComputeSystemPOD();
        computeSystemPOD.setName("DummyHPCSystem");
        computeSystemPOD.appendRackID(rackPOD.getID());
        computeSystemPOD.setJobProducer(mockedJobProducer);
        
        systemsPOD = new SystemsPOD();
        systemsPOD.appendComputeSystemPOD(computeSystemPOD);

        mockedDataCenterAM = mock(DataCenterAM.class);

        slaViolationLogger = mock(SLAViolationLogger.class);
        
        computeSystemFactory = mock(ComputeSystemFactory.class);
        interactiveSystemFactory = mock(InteractiveSystemFactory.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockedActivitiesLogger, mockedEnvironment);
    }

    @Test
    public void testBladeWithOneServerAndOneBatchJob() {
        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.appendRack(rackPOD);
        dataCenterPOD.setD(0, 0, 100);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(1);
        batchJob.setRemainParam(1, 41.07, 1, 1);

        when(mockedJobProducer.next()).thenReturn(batchJob);

        computeSystemPOD.setNumberofNode(1);

        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment);
        dataCenter.setAM(mockedDataCenterAM);

        ResourceAllocation resourceAllocation = new MHR(mockedEnvironment, dataCenter);
        Scheduler scheduler = new LeastRemainFirstScheduler();
        GeneralAM generalAM = new ComputeSystemAM(mockedEnvironment);

        when(computeSystemFactory.create(computeSystemPOD)).thenReturn(new ComputeSystem(computeSystemPOD,
                mockedEnvironment, scheduler, resourceAllocation, generalAM, slaViolationLogger));

        Systems systems = new Systems(mockedEnvironment, systemsPOD, computeSystemFactory, interactiveSystemFactory, enterpriseSystemFactory);
        systems.setup();
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        assertFalse(systems.allJobsDone());
        systems.runACycle();
        assertTrue(systems.allJobsDone());
        assertEquals(0, systems.getComputeSystems().get(0).getAccumolatedViolation());

        dataCenter.calculatePower();

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(mockedActivitiesLogger, times(2)).write(argument.capture());

        List<String> values = argument.getAllValues();
        assertEquals("1_1_0 5.0\n", values.get(0));
        assertEquals("\n5\t5\t1\n", values.get(1));

        assertEquals(5.002941076127991, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertEquals(1, dataCenter.getOverRed());

        verify(mockedDataCenterAM).setSlowDownFromCooler(true);
        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();
        verify(mockedEnvironment, times(5)).getCurrentLocalTime();
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment).updateNumberOfMessagesFromSystemToNodes();

        verifyNoMoreInteractions(mockedDataCenterAM, mockedJobProducer);
    }

    @Test
    public void testBladeWithTwoServersAndOneBatchJob_OneServerPerChassis() {
        BladeServerPOD otherBladeServePOD = new BladeServerPOD(bladeServerPOD);
        otherBladeServePOD.setID(DataCenterEntityID.createServerID(1, 2, 1));
        ChassisPOD otherChassisPOD = new ChassisPOD();
        otherChassisPOD.setBladeType(chassisPOD.getBladeType());
        otherChassisPOD.appendServerPOD(otherBladeServePOD);
        otherChassisPOD.setID(DataCenterEntityID.createChassisID(1, 2));

        rackPOD.appendChassis(otherChassisPOD);

        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.appendChassis(otherChassisPOD);
        dataCenterPOD.appendRack(rackPOD);
        dataCenterPOD.setD(0, 0, 100);
        dataCenterPOD.setD(0, 1, 100);
        dataCenterPOD.setD(1, 0, 100);
        dataCenterPOD.setD(1, 1, 100);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(1);
        batchJob.setRemainParam(1, 41.07, 1, 1);

        when(mockedJobProducer.next()).thenReturn(batchJob);

        computeSystemPOD.setNumberofNode(2);

        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment);
        dataCenter.setAM(mockedDataCenterAM);

        ResourceAllocation resourceAllocation = new MHR(mockedEnvironment, dataCenter);
        Scheduler scheduler = new LeastRemainFirstScheduler();
        GeneralAM generalAM = new ComputeSystemAM(mockedEnvironment);

        when(computeSystemFactory.create(computeSystemPOD)).thenReturn(new ComputeSystem(computeSystemPOD,
                mockedEnvironment, scheduler, resourceAllocation, generalAM, slaViolationLogger));

        Systems systems = new Systems(mockedEnvironment, systemsPOD, computeSystemFactory, interactiveSystemFactory, enterpriseSystemFactory);
        systems.setup();

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        assertFalse(systems.allJobsDone());
        systems.runACycle();
        assertTrue(systems.allJobsDone());
        assertEquals(0, systems.getComputeSystems().get(0).getAccumolatedViolation());

        dataCenter.calculatePower();

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(mockedActivitiesLogger, times(3)).write(argument.capture());

        List<String> values = argument.getAllValues();
        assertEquals(3, values.size());
        assertEquals("1_1_0 5.0\n", values.get(0));
        assertEquals("1_2_0 5.0\n", values.get(1));
        assertEquals("\n10\t10\t1\n", values.get(2));

        assertEquals(10.00147066220095, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertEquals(1, dataCenter.getOverRed());

        verify(mockedDataCenterAM).setSlowDownFromCooler(true);
        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();
        verify(mockedEnvironment, times(5)).getCurrentLocalTime();
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment, times(2)).updateNumberOfMessagesFromSystemToNodes();

        verifyNoMoreInteractions(mockedDataCenterAM, mockedJobProducer);
    }

    @Test
    public void testBladeWithOneServersAndOneBatchJobs_InsuficientServerToProcess() {
        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.appendRack(rackPOD);
        dataCenterPOD.setD(0, 0, 100);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(1);
        batchJob.setRemainParam(1, 41.07, 2, 1);

        when(mockedJobProducer.next()).thenReturn(batchJob);

        computeSystemPOD.setNumberofNode(1);

        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedActivitiesLogger, mockedEnvironment);
        dataCenter.setAM(mockedDataCenterAM);

        ResourceAllocation resourceAllocation = new MHR(mockedEnvironment, dataCenter);
        Scheduler scheduler = new LeastRemainFirstScheduler();
        GeneralAM generalAM = new ComputeSystemAM(mockedEnvironment);

        when(computeSystemFactory.create(computeSystemPOD)).thenReturn(new ComputeSystem(computeSystemPOD,
                mockedEnvironment, scheduler, resourceAllocation, generalAM, slaViolationLogger));

        Systems systems = new Systems(mockedEnvironment, systemsPOD, computeSystemFactory, interactiveSystemFactory, enterpriseSystemFactory);
        systems.setup();

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        assertFalse(systems.allJobsDone());
        systems.runACycle();
        assertFalse(systems.allJobsDone());
        assertEquals(1, systems.getComputeSystems().get(0).getAccumolatedViolation());

        dataCenter.calculatePower();

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(mockedActivitiesLogger, times(2)).write(argument.capture());

        List<String> values = argument.getAllValues();
        assertEquals(2, values.size());
        assertEquals("1_1_0 100.0\n", values.get(0));
        assertEquals("\n100\t100\t1\n", values.get(1));

        assertEquals(100.000147066220095, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertEquals(1, dataCenter.getOverRed());

        verify(mockedDataCenterAM).setSlowDownFromCooler(true);
        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();

        verifyNoMoreInteractions(mockedDataCenterAM, mockedJobProducer);
    }
}
