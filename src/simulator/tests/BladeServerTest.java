package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyInt;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.SimulatorEnvironment;
import simulator.jobs.BatchJob;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.InteractiveJob;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerPOD;
import simulator.physical.DataCenterEntityID;

public class BladeServerTest {

    public Environment environment;
    BladeServerPOD bladeServerPOD;
    final double frequency = 1.4;
    BladeServer bladeServer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        environment = new SimulatorEnvironment();
        bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setFrequencyLevel(new double[1]);
        final double frequency = 1.4;
        bladeServerPOD.setFrequencyLevelAt(0, frequency);
        bladeServerPOD.setPowerBusy(new double[1]);
        bladeServerPOD.setPowerBusyAt(0, 100.0);
        bladeServerPOD.setPowerIdle(new double[1]);
        bladeServerPOD.setPowerIdleAt(0, 50.0);
        bladeServerPOD.setID(DataCenterEntityID.createServerID(1, 1, 1));
        bladeServer = new BladeServer(bladeServerPOD, environment);
    }

    @Test
    public void testBladeServerCreation() {
        List<BatchJob> activeBatchJobs = bladeServer.activeBatchJobs();
        assertTrue(activeBatchJobs.isEmpty());
        List<BatchJob> blockedJobs = bladeServer.getBlockedBatchList();
        assertTrue(blockedJobs.isEmpty());
        assertEquals(0, bladeServer.getID().getChassisID());
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertEquals(0, bladeServer.getCurrentFreqLevel());
        List<EnterpriseJob> enterpriseJobs = bladeServer.getEnterpriseList();
        assertTrue(enterpriseJobs.isEmpty());
        assertEquals(1, bladeServer.getNumberOfFrequencyLevel());
        assertEquals(0.0, bladeServer.getStandByConsumption(), 1.0E-8);
        assertEquals(0, bladeServer.getMaxExpectedRes());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(1, bladeServer.getNumberOfPowerBusy());
        assertEquals(1, bladeServer.getNumberOfPowerIdle());
        assertEquals(3, bladeServer.getPwrParam().length);
        assertEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertEquals(0, bladeServer.getID().getRackID());
        assertTrue(bladeServer.isNotSystemAssigned());
        List<ResponseTime> responseTime = bladeServer.getResponseList();
        assertTrue(responseTime.isEmpty());
        List<ResponseTime> getResponseListWeb = bladeServer.getResponseListWeb();
        assertTrue(getResponseListWeb.isEmpty());
        assertEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertEquals("1_1_1", bladeServer.getID().toString());
        assertEquals(0.0, bladeServer.getSLAPercentage(), 1.0E-8);
        assertEquals(0.0, bladeServer.getTimeTreshold(), 1.0E-8);
        assertEquals(0.0, bladeServer.getTotalFinishedJob(), 1.0E-8);
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
        List<InteractiveJob> interactiveJobs = bladeServer.getInteractiveList();
        assertTrue(interactiveJobs.isEmpty());

    }

    @Test
    public void testGetPowerRunningBusy() {
        bladeServer.setCurrentCPU(50);
        bladeServer.setStatusAsRunningBusy();
        assertTrue(bladeServer.isRunningBusy());
        assertEquals(frequency, bladeServer.getMips(), 1.0E-8);
        assertEquals(75.0, bladeServer.getPower(), 1.0E-8);
        bladeServer.setCurrentCPU(100);
        assertEquals(100.0, bladeServer.getPower(), 1.0E-8);
        bladeServer.setCurrentCPU(10);
        assertEquals(55.0, bladeServer.getPower(), 1.0E-8);
    }

    @Test
    public void testRestart() {
        bladeServer.setRespTime(10);
        bladeServer.setCurrentCPU(10);

        List<BatchJob> activeBacthJobs = new ArrayList<BatchJob>();
        activeBacthJobs.add(null);
        bladeServer.setActiveBatchList(activeBacthJobs);

        List<BatchJob> blockedBacthJobs = new ArrayList<BatchJob>();
        blockedBacthJobs.add(null);
        bladeServer.setBlockedBatchList(blockedBacthJobs);

        List<EnterpriseJob> enterpriseJobs = new ArrayList<EnterpriseJob>();
        enterpriseJobs.add(null);
        bladeServer.setEnterprizList(enterpriseJobs);

        List<InteractiveJob> interactiveJobs = new ArrayList<InteractiveJob>();
        interactiveJobs.add(null);
        bladeServer.setWebBasedList(interactiveJobs);

        bladeServer.setQueueLength(1);
        bladeServer.setStatusAsRunningBusy();
        bladeServer.setTotalFinishedJob(10);
        bladeServer.setMips(1.8);
        bladeServer.setResTimeEpoch(1.0);
        bladeServer.setTotalJob(10);
        bladeServer.setTotalJobEpoch(30);

        assertNotEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertNotEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertFalse(bladeServer.activeBatchJobs().isEmpty());
        assertFalse(bladeServer.getBlockedBatchList().isEmpty());
        assertFalse(bladeServer.getEnterpriseList().isEmpty());
        assertFalse(bladeServer.getInteractiveList().isEmpty());
        assertNotEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertFalse(bladeServer.isIdle());
        assertNotEquals(0, bladeServer.getTotalFinishedJob());
        assertNotEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertNotEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertNotEquals(0, bladeServer.getTotalJob());
        assertNotEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);

        bladeServer.restart();

        assertEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertTrue(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertTrue(bladeServer.isIdle());
        assertEquals(0, bladeServer.getTotalFinishedJob());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
    }

    @Test
    public void testFeedBatchJobWork() {
        assertTrue(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertTrue(bladeServer.isNotSystemAssigned());
        assertEquals(0, bladeServer.getTotalJob());

        BatchJob mockBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockBatchJob);

        assertFalse(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertTrue(bladeServer.isRunningNormal());
        assertEquals(1, bladeServer.getTotalJob());
    }

    @Test
    public void testFeedInteractiveJobWork() {
        assertTrue(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertTrue(bladeServer.isNotSystemAssigned());
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0, bladeServer.getQueueLength());

        InteractiveJob interactiveJob = new InteractiveJob();

        final int numberOfJobsInInteractiveJob = 10;
        final int arrivalTime = 40;
        interactiveJob.setNumberOfJob(numberOfJobsInInteractiveJob);
        interactiveJob.setArrivalTimeOfJob(arrivalTime);

        bladeServer.feedWork(interactiveJob);

        List<InteractiveJob> interactiveJobs = bladeServer.getInteractiveList();
        assertTrue(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertFalse(interactiveJobs.isEmpty());

        assertTrue(bladeServer.isNotSystemAssigned());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getTotalJob());
        InteractiveJob job = interactiveJobs.get(0);
        assertEquals(arrivalTime, job.getArrivalTimeOfJob());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getQueueLength());
    }

    @Test
    public void testFeedEnterpriseJobWork() {
        assertTrue(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());

        List<EnterpriseJob> enterpriseJobs = bladeServer.getEnterpriseList();

        assertTrue(enterpriseJobs.isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertTrue(bladeServer.isNotSystemAssigned());
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0, bladeServer.getQueueLength());

        EnterpriseJob enterpriseJob = new EnterpriseJob();

        final int numberOfJobsInInteractiveJob = 10;
        final int arrivalTime = 40;
        enterpriseJob.setNumberOfJob(numberOfJobsInInteractiveJob);
        enterpriseJob.setArrivalTimeOfJob(arrivalTime);

        bladeServer.feedWork(enterpriseJob);

        assertTrue(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertFalse(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertTrue(bladeServer.isNotSystemAssigned());
        assertEquals(10, bladeServer.getTotalJob());
        assertEquals(10, bladeServer.getQueueLength());
        EnterpriseJob job = enterpriseJobs.get(0);
        assertEquals(arrivalTime, job.getArrivalTimeOfJob());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getQueueLength());
    }

    @Test
    public void testGetCurrentFreqLevelWhenItDiffersFromMips() {
        bladeServerPOD.setFrequencyLevelAt(0, 1.8);
        bladeServer = new BladeServer(bladeServerPOD, environment);
        bladeServer.setMips(1.4);

        assertEquals(-1, bladeServer.getCurrentFreqLevel());
    }

    @Test
    public void testGetCurrentFreqLevelWhenItEqualsToMips() {
        final int expectedIndex = 0;
        final double mips = 1.4;
        bladeServerPOD.setFrequencyLevelAt(expectedIndex, mips);
        bladeServer = new BladeServer(bladeServerPOD, environment);
        bladeServer.setMips(mips);

        assertEquals(expectedIndex, bladeServer.getCurrentFreqLevel());
    }

    @Test
    public void testGetCurrentFreqLevelWhenItIs_4MIPSInTheLastPosition() {
        final int expectedIndex = 1;
        final double mips = 1.4;
        bladeServerPOD.setFrequencyLevel(new double[2]);
        bladeServerPOD.setFrequencyLevelAt(0, 1.8);
        bladeServerPOD.setFrequencyLevelAt(expectedIndex, mips);
        bladeServer = new BladeServer(bladeServerPOD, environment);
        bladeServer.setMips(mips);

        assertEquals(expectedIndex, bladeServer.getCurrentFreqLevel());
    }

    @Test
    public void testIncreaseFrequency() {
        bladeServerPOD.setFrequencyLevel(new double[3]);
        bladeServerPOD.setFrequencyLevelAt(0, 1.4);
        bladeServerPOD.setFrequencyLevelAt(1, 1.8);
        bladeServerPOD.setFrequencyLevelAt(2, 2.2);
        bladeServer = new BladeServer(bladeServerPOD, environment);
        bladeServer.setMips(1.4);

        assertEquals(0, bladeServer.getCurrentFreqLevel());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(0, environment.getNumberOfMessagesFromDataCenterToSystem());

        assertEquals(1, bladeServer.increaseFrequency());
        assertEquals(1.8, bladeServer.getMips(), 1.0E-8);
        assertEquals(1, bladeServer.getCurrentFreqLevel());
        assertEquals(1, environment.getNumberOfMessagesFromDataCenterToSystem());

        assertEquals(1, bladeServer.increaseFrequency());
        assertEquals(2.2, bladeServer.getMips(), 1.0E-8);
        assertEquals(2, bladeServer.getCurrentFreqLevel());
        assertEquals(2, environment.getNumberOfMessagesFromDataCenterToSystem());

        assertEquals(0, bladeServer.increaseFrequency());
        assertEquals(2, bladeServer.getCurrentFreqLevel());
        assertEquals(2.2, bladeServer.getMips(), 1.0E-8);
        assertEquals(2, environment.getNumberOfMessagesFromDataCenterToSystem());
    }

    @Test
    public void testDecreaseFrequency() {
        bladeServerPOD.setFrequencyLevel(new double[3]);
        bladeServerPOD.setFrequencyLevelAt(0, 1.4);
        bladeServerPOD.setFrequencyLevelAt(1, 1.8);
        bladeServerPOD.setFrequencyLevelAt(2, 2.2);
        bladeServer = new BladeServer(bladeServerPOD, environment);
        bladeServer.setMips(2.2);

        assertEquals(2, bladeServer.getCurrentFreqLevel());
        assertEquals(2.2, bladeServer.getMips(), 1.0E-8);
        assertEquals(0, environment.getNumberOfMessagesFromDataCenterToSystem());

        assertEquals(1, bladeServer.decreaseFrequency());
        assertEquals(1.8, bladeServer.getMips(), 1.0E-8);
        assertEquals(1, bladeServer.getCurrentFreqLevel());
        assertEquals(1, environment.getNumberOfMessagesFromDataCenterToSystem());

        assertEquals(1, bladeServer.decreaseFrequency());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(0, bladeServer.getCurrentFreqLevel());
        assertEquals(2, environment.getNumberOfMessagesFromDataCenterToSystem());

        assertEquals(0, bladeServer.decreaseFrequency());
        assertEquals(0, bladeServer.getCurrentFreqLevel());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(2, environment.getNumberOfMessagesFromDataCenterToSystem());
    }

    @Test
    public void testRunBatchJobNotBelongingToActiveJobs() {
        assertTrue(bladeServer.isNotSystemAssigned());
        bladeServer.setCurrentCPU(1.0);
        assertEquals(1.0, bladeServer.getCurrentCPU(), 1.0E-8);

        assertFalse(bladeServer.run());

        assertTrue(bladeServer.isRunningNormal());
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
    }

    @Test
    public void testRunBatchJobBelongingToActiveJobs() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertTrue(bladeServer.isRunningNormal());
        bladeServer.setCurrentCPU(1.0);
        assertEquals(1.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);

        when(mockedBatchJob.getRemainAt(bladeServer.getID())).thenReturn(5.0, 3.6);
        when(mockedBatchJob.getUtilization()).thenReturn(1.0);
        when(mockedBatchJob.isModified()).thenReturn(false, true);

        assertTrue(bladeServer.run());

        assertTrue(bladeServer.isRunningBusy());
        assertEquals(71.42857, bladeServer.getCurrentCPU(), 1.0E-5);

        verify(mockedBatchJob, times(5)).getUtilization();
        verify(mockedBatchJob, times(3)).isModified();
        verify(mockedBatchJob).setAsModified();
        verify(mockedBatchJob).setAsNotModified();
        verify(mockedBatchJob).setRemainAt(bladeServer.getID(), 3.6);
        verify(mockedBatchJob, times(2)).getRemainAt(bladeServer.getID());

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testRunBatchJobArithmeticExceptionThrown() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertTrue(bladeServer.isRunningNormal());
        bladeServer.setCurrentCPU(1.0);
        assertEquals(1.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);

        when(mockedBatchJob.getRemainAt(bladeServer.getID())).thenReturn(5.0, 3.6);
        when(mockedBatchJob.getUtilization()).thenReturn(0.0);
        when(mockedBatchJob.isModified()).thenReturn(true, true, false);

        expectedException.expect(ArithmeticException.class);
        assertTrue(bladeServer.run());

        verify(mockedBatchJob, times(5)).getUtilization();
        verify(mockedBatchJob, times(3)).isModified();
        verify(mockedBatchJob).setAsModified();
        verify(mockedBatchJob).setAsNotModified();
        verify(mockedBatchJob).setRemainAt(bladeServer.getID(), 3.6);
        verify(mockedBatchJob, times(2)).getRemainAt(bladeServer.getID());

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testRunBatchJobWithJobsStillActive() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertTrue(bladeServer.isRunningNormal());
        bladeServer.setCurrentCPU(1.0);
        assertEquals(1.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);

        when(mockedBatchJob.getRemainAt(bladeServer.getID())).thenReturn(5.0);
        when(mockedBatchJob.getUtilization()).thenReturn(0.1);
        when(mockedBatchJob.isModified()).thenReturn(true, true, false);

        assertTrue(bladeServer.run());

        assertTrue(bladeServer.isRunningNormal());
        assertEquals(100, bladeServer.getCurrentCPU(), 1.0E-5);

        verify(mockedBatchJob, times(4)).getUtilization();
        verify(mockedBatchJob, times(3)).isModified();
        verify(mockedBatchJob).setAsModified();
        verify(mockedBatchJob).setAsNotModified();
        final ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(mockedBatchJob).setRemainAt(Matchers.eq(bladeServer.getID()), captor.capture());
        assertEquals(-8.99, captor.getValue(), 0.01);

        verify(mockedBatchJob, times(2)).getRemainAt(bladeServer.getID());

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testNotDoneWhenThereAreBatchJobAsActiveJobAndShareIsEqualsToZero() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertFalse(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertEquals(0, bladeServer.getTotalFinishedJob());

        final double share = 0.0;
        assertTrue(bladeServer.done(mockedBatchJob, share));

        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.activeBatchJobs().isEmpty());
        assertEquals(0, bladeServer.getTotalFinishedJob());

        verify(mockedBatchJob).getUtilization();

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testDoneWhenBatchJobRemainGreaterThanZero() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertFalse(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());

        when(mockedBatchJob.getNumOfNode()).thenReturn(1);
        when(mockedBatchJob.getRemainAt(bladeServer.getID())).thenReturn(2.0, 1.0);

        final double share = 1.0;
        assertFalse(bladeServer.done(mockedBatchJob, share));

        verify(mockedBatchJob).getUtilization();
        verify(mockedBatchJob).setRemainAt(bladeServer.getID(), 1.0);
        verify(mockedBatchJob, times(2)).getRemainAt(bladeServer.getID());

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testDoneWhenBatchJobRemainLessOrEqualZeroAndJobNotAllDone() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertFalse(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());

        when(mockedBatchJob.getNumOfNode()).thenReturn(1);
        when(mockedBatchJob.getRemainAt(bladeServer.getID())).thenReturn(1.0, 0.0);
        when(mockedBatchJob.allDone()).thenReturn(false);

        final double share = 1.0;
        assertFalse(bladeServer.done(mockedBatchJob, share));

        verify(mockedBatchJob).getUtilization();
        verify(mockedBatchJob).setRemainAt(bladeServer.getID(), 0.0);
        verify(mockedBatchJob).setAsNotModified();
        verify(mockedBatchJob, times(2)).getRemainAt(bladeServer.getID());
        verify(mockedBatchJob).allDone();

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testDoneWhenBatchJobRemainLessOrEqualZeroAndJobAllDone() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertFalse(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());

        assertEquals(0, bladeServer.getTotalFinishedJob());

        when(mockedBatchJob.getNumOfNode()).thenReturn(1);
        when(mockedBatchJob.getRemainAt(bladeServer.getID())).thenReturn(1.0, 0.0);
        when(mockedBatchJob.allDone()).thenReturn(true);

        final double share = 1.0;
        assertTrue(bladeServer.done(mockedBatchJob, share));

        verify(mockedBatchJob).getUtilization();
        verify(mockedBatchJob).setRemainAt(bladeServer.getID(), 0.0);
        verify(mockedBatchJob).setAsNotModified();
        verify(mockedBatchJob, times(2)).getRemainAt(bladeServer.getID());
        verify(mockedBatchJob).allDone();
        verify(mockedBatchJob).Finish(anyInt());

        assertEquals(1, bladeServer.getTotalFinishedJob());

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testSetReadyAsRunningNormalWhenThereAreNoActiveJobs() {
        bladeServer.setReady();
        assertTrue(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.isRunningNormal());
    }

    @Test
    public void testSetReadyAsRunningNormalWhenThereAreActiveJobsAndUtilizationLessThanThreshold() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);
        final double lessThanThreshold = 0.99999;
        when(mockedBatchJob.getUtilization()).thenReturn(lessThanThreshold);

        bladeServer.setReady();

        assertFalse(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.isRunningNormal());

        verify(mockedBatchJob, times(2)).getUtilization();

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testSetReadyAsRunningBusyWhenThereAreActiveJobsAndUtilizationGreaterThanThreshold() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);
        final double greaterThanThreshold = 1.000001;
        when(mockedBatchJob.getUtilization()).thenReturn(greaterThanThreshold);

        bladeServer.setReady();

        assertFalse(bladeServer.activeBatchJobs().isEmpty());
        assertTrue(bladeServer.isRunningBusy());

        verify(mockedBatchJob, times(2)).getUtilization();

        verifyNoMoreInteractions(mockedBatchJob);
    }
}
