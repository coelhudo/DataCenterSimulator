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

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.am.DataCenterAM;
import simulator.jobs.BatchJob;
import simulator.jobs.JobProducer;
import simulator.physical.BladeServerPOD;
import simulator.physical.ChassisPOD;
import simulator.physical.DataCenter;
import simulator.physical.DataCenterPOD;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemPOD;
import simulator.system.Systems;
import simulator.utils.ActivitiesLogger;

public class ComputeSystemIT {

    public static final double[] FREQUENCY_LEVEL = { 1.4, 1.4, 1.4 };
    public static final double[] POWER_IDLE = { 100, 100, 128 };
    public static final double[] POWER_BUSY = { 300, 336, 448 };

    @Test
    public void testBladeWithOneServerAndOneBatchJob() {
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setBladeType("DummyType");
        bladeServerPOD.setChassisID(0);
        bladeServerPOD.setRackID(0);
        bladeServerPOD.setServerID(0);
        bladeServerPOD.setFrequencyLevel(FREQUENCY_LEVEL);
        bladeServerPOD.setPowerBusy(POWER_BUSY);
        bladeServerPOD.setPowerIdle(POWER_IDLE);
        bladeServerPOD.setIdleConsumption(5);

        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        chassisPOD.setBladeType("DummyType");
        chassisPOD.setChassisType("DummyChassisType");
        chassisPOD.setID(0);
        chassisPOD.setRackID(0);

        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.setD(0, 0, 100);

        Environment mockedEnvironment = mock(Environment.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);

        ComputeSystemPOD computerSystemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);
        when(mockedJobProducer.hasNext()).thenReturn(true, false);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(1);
        batchJob.setRemainParam(1, 41.07, 1, 1);

        when(mockedJobProducer.next()).thenReturn(batchJob);

        computerSystemPOD.setJobProducer(mockedJobProducer);

        computerSystemPOD.setName("DummyHPCSystem");
        computerSystemPOD.setNumberofNode(1);
        computerSystemPOD.appendRackID(0);

        DataCenterAM mockedDataCenterAM = mock(DataCenterAM.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedDataCenterAM, mockedActivitiesLogger,
                mockedEnvironment);

        SLAViolationLogger slaViolationLogger = mock(SLAViolationLogger.class);
        Systems systems = new Systems(mockedEnvironment);
        systems.addComputeSystem(
                ComputeSystem.create(computerSystemPOD, mockedEnvironment, dataCenter, slaViolationLogger));

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        assertFalse(systems.allJobsDone());
        systems.runACycle();
        assertTrue(systems.allJobsDone());
        assertEquals(0, systems.getComputeSystems().get(0).getAccumolatedViolation());

        dataCenter.calculatePower();

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        verify(mockedActivitiesLogger, times(2)).write(argument.capture());

        List<String> values = argument.getAllValues();
        assertEquals("5\t", values.get(0));
        assertEquals("5\t5\t1\n", values.get(1));

        assertEquals(5.002941076127991, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertEquals(1, dataCenter.getOverRed());

        verify(mockedDataCenterAM).setSlowDownFromCooler(true);
        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();
        verify(mockedEnvironment, times(5)).getCurrentLocalTime();
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment).updateNumberOfMessagesFromSystemToNodes();

        verifyNoMoreInteractions(mockedDataCenterAM, mockedActivitiesLogger, mockedJobProducer, mockedEnvironment);
    }

    @Test
    public void testBladeWithTwoServersAndOneBatchJob_OneServerPerChassis() {
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setBladeType("DummyType");
        bladeServerPOD.setChassisID(0);
        bladeServerPOD.setRackID(0);
        bladeServerPOD.setServerID(0);
        bladeServerPOD.setFrequencyLevel(FREQUENCY_LEVEL);
        bladeServerPOD.setPowerBusy(POWER_BUSY);
        bladeServerPOD.setPowerIdle(POWER_IDLE);
        bladeServerPOD.setIdleConsumption(5);

        ChassisPOD firstChassisPOD = new ChassisPOD();
        firstChassisPOD.appendServerPOD(bladeServerPOD);
        firstChassisPOD.setBladeType("DummyType");
        firstChassisPOD.setChassisType("DummyChassisType");
        firstChassisPOD.setID(0);
        firstChassisPOD.setRackID(0);

        ChassisPOD secondChassisPOS = new ChassisPOD(firstChassisPOD);
        secondChassisPOS.setID(1);
        secondChassisPOS.getServerPODs().get(0).setServerID(1);

        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(firstChassisPOD);
        dataCenterPOD.appendChassis(secondChassisPOS);
        dataCenterPOD.setD(0, 0, 100);
        dataCenterPOD.setD(0, 1, 100);
        dataCenterPOD.setD(1, 0, 100);
        dataCenterPOD.setD(1, 1, 100);

        Environment mockedEnvironment = mock(Environment.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);

        ComputeSystemPOD computerSystemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);
        when(mockedJobProducer.hasNext()).thenReturn(true, false);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(1);
        batchJob.setRemainParam(1, 41.07, 1, 1);

        when(mockedJobProducer.next()).thenReturn(batchJob);

        computerSystemPOD.setJobProducer(mockedJobProducer);
        computerSystemPOD.setName("DummyHPCSystem");
        computerSystemPOD.setNumberofNode(2);
        computerSystemPOD.appendRackID(0);

        DataCenterAM mockedDataCenterAM = mock(DataCenterAM.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedDataCenterAM, mockedActivitiesLogger,
                mockedEnvironment);

        SLAViolationLogger slaViolationLogger = mock(SLAViolationLogger.class);
        Systems systems = new Systems(mockedEnvironment);
        systems.addComputeSystem(
                ComputeSystem.create(computerSystemPOD, mockedEnvironment, dataCenter, slaViolationLogger));

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
        assertEquals("5\t", values.get(0));
        assertEquals("5\t", values.get(1));
        assertEquals("10\t10\t1\n", values.get(2));

        assertEquals(10.00147066220095, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertEquals(1, dataCenter.getOverRed());

        verify(mockedDataCenterAM).setSlowDownFromCooler(true);
        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();
        verify(mockedEnvironment, times(5)).getCurrentLocalTime();
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment, times(2)).updateNumberOfMessagesFromSystemToNodes();

        verifyNoMoreInteractions(mockedDataCenterAM, mockedActivitiesLogger, mockedJobProducer, mockedEnvironment);
    }

    @Test
    public void testBladeWithOneServersAndOneBatchJobs_InsuficientServerToProcess() {
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setBladeType("DummyType");
        bladeServerPOD.setChassisID(0);
        bladeServerPOD.setRackID(0);
        bladeServerPOD.setServerID(0);
        bladeServerPOD.setFrequencyLevel(FREQUENCY_LEVEL);
        bladeServerPOD.setPowerBusy(POWER_BUSY);
        bladeServerPOD.setPowerIdle(POWER_IDLE);
        bladeServerPOD.setIdleConsumption(5);

        ChassisPOD chassisPOD = new ChassisPOD();
        chassisPOD.appendServerPOD(bladeServerPOD);
        chassisPOD.setBladeType("DummyType");
        chassisPOD.setChassisType("DummyChassisType");
        chassisPOD.setID(0);
        chassisPOD.setRackID(0);

        DataCenterPOD dataCenterPOD = new DataCenterPOD();
        dataCenterPOD.appendChassis(chassisPOD);
        dataCenterPOD.setD(0, 0, 100);

        Environment mockedEnvironment = mock(Environment.class);
        ActivitiesLogger mockedActivitiesLogger = mock(ActivitiesLogger.class);

        ComputeSystemPOD computerSystemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);
        when(mockedJobProducer.hasNext()).thenReturn(true, false);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(1);
        batchJob.setRemainParam(1, 41.07, 2, 1);

        when(mockedJobProducer.next()).thenReturn(batchJob);
        computerSystemPOD.setJobProducer(mockedJobProducer);

        computerSystemPOD.setName("DummyHPCSystem");
        computerSystemPOD.setNumberofNode(1);
        computerSystemPOD.appendRackID(0);

        DataCenterAM mockedDataCenterAM = mock(DataCenterAM.class);
        DataCenter dataCenter = new DataCenter(dataCenterPOD, mockedDataCenterAM, mockedActivitiesLogger,
                mockedEnvironment);

        SLAViolationLogger slaViolationLogger = mock(SLAViolationLogger.class);
        Systems systems = new Systems(mockedEnvironment);
        systems.addComputeSystem(
                ComputeSystem.create(computerSystemPOD, mockedEnvironment, dataCenter, slaViolationLogger));

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
        assertEquals("100\t", values.get(0));
        assertEquals("100\t100\t1\n", values.get(1));

        assertEquals(100.000147066220095, dataCenter.getTotalPowerConsumption(), 1.0E-8);
        assertEquals(1, dataCenter.getOverRed());

        verify(mockedDataCenterAM).setSlowDownFromCooler(true);
        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();

        verifyNoMoreInteractions(mockedDataCenterAM, mockedActivitiesLogger, mockedJobProducer, mockedEnvironment);
    }
}
