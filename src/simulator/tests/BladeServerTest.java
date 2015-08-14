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

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.jobs.BatchJob;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.InteractiveJob;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerPOD;

public class BladeServerTest {

    public Environment environment;
    final int chassisID = 0;
    BladeServerPOD bladeServerPOD;
    final double frequency = 1.4;
    BladeServer bladeServer;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        environment = new Environment();
        bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setFrequencyLevel(new double[1]);
        final double frequency = 1.4;
        bladeServerPOD.setFrequencyLevelAt(0, frequency);
        bladeServerPOD.setPowerBusy(new double[1]);
        bladeServerPOD.setPowerBusyAt(0, 100.0);
        bladeServerPOD.setPowerIdle(new double[1]);
        bladeServerPOD.setPowerIdleAt(0, 50.0);
        bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
    }

    @Test
    public void testBladeServerCreation() {
        List<BatchJob> activeBatchJobs = bladeServer.getActiveBatchList();
        assertTrue(activeBatchJobs.isEmpty());
        List<BatchJob> blockedJobs = bladeServer.getBlockedBatchList();
        assertTrue(blockedJobs.isEmpty());
        assertEquals(chassisID, bladeServer.getChassisID());
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertEquals(0, bladeServer.getCurrentFreqLevel());
        assertEquals(0, bladeServer.getDependency());
        List<EnterpriseJob> enterpriseJobs = bladeServer.getEnterpriseList();
        assertTrue(enterpriseJobs.isEmpty());
        assertEquals(1, bladeServer.getNumberOfFrequencyLevel());
        assertEquals(0.0, bladeServer.getIdleConsumption(), 1.0E-8);
        assertEquals(0, bladeServer.getMaxExpectedRes());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(1, bladeServer.getNumberOfPowerBusy());
        assertEquals(1, bladeServer.getNumberOfPowerIdle());
        assertEquals(3, bladeServer.getPwrParam().length);
        assertEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertEquals(0.0, bladeServer.getRackId(), 1.0E-8);
        assertEquals(-3.0, bladeServer.getReady(), 1.0E-8);
        List<ResponseTime> responseTime = bladeServer.getResponseList();
        assertTrue(responseTime.isEmpty());
        List<ResponseTime> getResponseListWeb = bladeServer.getResponseListWeb();
        assertTrue(getResponseListWeb.isEmpty());
        assertEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertEquals(0.0, bladeServer.getServerID(), 1.0E-8);
        assertEquals(0.0, bladeServer.getSLAPercentage(), 1.0E-8);
        assertEquals(0.0, bladeServer.getTimeTreshold(), 1.0E-8);
        assertEquals(0.0, bladeServer.getTotalFinishedJob(), 1.0E-8);
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
        List<InteractiveJob> interactiveJobs = bladeServer.getInteractiveList();
        assertTrue(interactiveJobs.isEmpty());
        assertFalse(bladeServer.isSLAviolation());

    }

    @Test
    public void testGetPowerRunningBusy() {
        bladeServer.setCurrentCPU(50);
        bladeServer.setStatusAsRunningBusy();
        assertEquals(0, bladeServer.getReady());
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
        bladeServer.setSLAviolation(true);

        assertNotEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertNotEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertFalse(bladeServer.getBlockedBatchList().isEmpty());
        assertFalse(bladeServer.getEnterpriseList().isEmpty());
        assertFalse(bladeServer.getInteractiveList().isEmpty());
        assertNotEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertNotEquals(-1, bladeServer.getReady());
        assertNotEquals(0, bladeServer.getTotalFinishedJob());
        assertNotEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertNotEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertNotEquals(0, bladeServer.getTotalJob());
        assertNotEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
        assertTrue(bladeServer.isSLAviolation());

        bladeServer.restart();

        assertEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertEquals(-1, bladeServer.getReady());
        assertEquals(0, bladeServer.getTotalFinishedJob());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
        assertFalse(bladeServer.isSLAviolation());
    }

    @Test
    public void testFeedBatchJobWork() {
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(0, bladeServer.getTotalJob());

        BatchJob mockBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockBatchJob);

        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(1, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(1, bladeServer.getTotalJob());
    }

    @Test
    public void testFeedInteractiveJobWork() {
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0, bladeServer.getQueueLength());

        InteractiveJob interactiveJob = new InteractiveJob();

        final int numberOfJobsInInteractiveJob = 10;
        final int arrivalTime = 40;
        interactiveJob.setNumberOfJob(numberOfJobsInInteractiveJob);
        interactiveJob.setArrivalTimeOfJob(arrivalTime);

        bladeServer.feedWork(interactiveJob);

        List<InteractiveJob> interactiveJobs = bladeServer.getInteractiveList();
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertFalse(interactiveJobs.isEmpty());

        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getTotalJob());
        InteractiveJob job = interactiveJobs.get(0);
        assertEquals(arrivalTime, job.getArrivalTimeOfJob());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getQueueLength());
    }

    @Test
    public void testFeedEnterpriseJobWork() {
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());

        List<EnterpriseJob> enterpriseJobs = bladeServer.getEnterpriseList();

        assertTrue(enterpriseJobs.isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0, bladeServer.getQueueLength());

        EnterpriseJob enterpriseJob = new EnterpriseJob();

        final int numberOfJobsInInteractiveJob = 10;
        final int arrivalTime = 40;
        enterpriseJob.setNumberOfJob(numberOfJobsInInteractiveJob);
        enterpriseJob.setArrivalTimeOfJob(arrivalTime);

        bladeServer.feedWork(enterpriseJob);

        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertFalse(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(10, bladeServer.getTotalJob());
        assertEquals(10, bladeServer.getQueueLength());
        EnterpriseJob job = enterpriseJobs.get(0);
        assertEquals(arrivalTime, job.getArrivalTimeOfJob());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getQueueLength());
    }

    @Test
    public void testGetCurrentFreqLevelWhenItDiffersFromMips() {
        bladeServerPOD.setFrequencyLevelAt(0, 1.8);
        bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
        bladeServer.setMips(1.4);

        assertEquals(-1, bladeServer.getCurrentFreqLevel());
    }

    @Test
    public void testGetCurrentFreqLevelWhenItEqualsToMips() {
        final int expectedIndex = 0;
        final double mips = 1.4;
        bladeServerPOD.setFrequencyLevelAt(expectedIndex, mips);
        bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
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
        bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
        bladeServer.setMips(mips);

        assertEquals(expectedIndex, bladeServer.getCurrentFreqLevel());
    }

    @Test
    public void testIncreaseFrequency() {
        bladeServerPOD.setFrequencyLevel(new double[3]);
        bladeServerPOD.setFrequencyLevelAt(0, 1.4);
        bladeServerPOD.setFrequencyLevelAt(1, 1.8);
        bladeServerPOD.setFrequencyLevelAt(2, 2.2);
        bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
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
        bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
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
        BatchJob mockedBatchJob = mock(BatchJob.class);

        assertEquals(-3, bladeServer.getReady());
        bladeServer.setCurrentCPU(1.0);
        assertEquals(1.0, bladeServer.getCurrentCPU(), 1.0E-8);
        bladeServer.setDependency(1);
        assertEquals(1, bladeServer.getDependency());

        assertEquals(0, bladeServer.run(mockedBatchJob));

        assertEquals(1, bladeServer.getReady());
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertEquals(0, bladeServer.getDependency());

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testRunBatchJobBelongingToActiveJobs() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertEquals(1, bladeServer.getReady());
        bladeServer.setCurrentCPU(1.0);
        assertEquals(1.0, bladeServer.getCurrentCPU(), 1.0E-8);
        bladeServer.setDependency(1);
        assertEquals(1, bladeServer.getDependency());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);

        when(mockedBatchJob.getRemainAt(0)).thenReturn(5.0, 3.6);
        when(mockedBatchJob.getUtilization()).thenReturn(1.0);
        when(mockedBatchJob.getIsChangedThisTime()).thenReturn(0, 1);

        assertEquals(1, bladeServer.run(mockedBatchJob));

        assertEquals(0, bladeServer.getReady());
        assertEquals(71.42857, bladeServer.getCurrentCPU(), 1.0E-5);

        verify(mockedBatchJob, times(5)).getUtilization();
        verify(mockedBatchJob, times(3)).getIsChangedThisTime();
        verify(mockedBatchJob).setIsChangedThisTime(1);
        verify(mockedBatchJob).setIsChangedThisTime(0);
        verify(mockedBatchJob).getThisNodeIndex(0);
        verify(mockedBatchJob).setRemainAt(0, 3.6);
        verify(mockedBatchJob, times(2)).getRemainAt(0);

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testRunBatchJobArithmeticExceptionThrown() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertEquals(1, bladeServer.getReady());
        bladeServer.setCurrentCPU(1.0);
        assertEquals(1.0, bladeServer.getCurrentCPU(), 1.0E-8);
        bladeServer.setDependency(1);
        assertEquals(1, bladeServer.getDependency());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);

        when(mockedBatchJob.getRemainAt(0)).thenReturn(5.0, 3.6);
        when(mockedBatchJob.getUtilization()).thenReturn(0.0);
        when(mockedBatchJob.getIsChangedThisTime()).thenReturn(1, 1, 0);

        expectedException.expect(ArithmeticException.class);
        assertEquals(1, bladeServer.run(mockedBatchJob));

        verify(mockedBatchJob, times(5)).getUtilization();
        verify(mockedBatchJob, times(3)).getIsChangedThisTime();
        verify(mockedBatchJob).setIsChangedThisTime(1);
        verify(mockedBatchJob).setIsChangedThisTime(0);
        verify(mockedBatchJob).getThisNodeIndex(0);
        verify(mockedBatchJob).setRemainAt(0, 3.6);
        verify(mockedBatchJob, times(2)).getRemainAt(0);

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testRunBatchJobWithJobsStillActive() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertEquals(1, bladeServer.getReady());
        bladeServer.setCurrentCPU(1.0);
        assertEquals(1.0, bladeServer.getCurrentCPU(), 1.0E-8);
        bladeServer.setDependency(1);
        assertEquals(1, bladeServer.getDependency());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);

        when(mockedBatchJob.getRemainAt(0)).thenReturn(5.0);
        when(mockedBatchJob.getUtilization()).thenReturn(0.1);
        when(mockedBatchJob.getIsChangedThisTime()).thenReturn(1, 1, 0);

        assertEquals(1, bladeServer.run(mockedBatchJob));

        assertEquals(1, bladeServer.getReady());
        assertEquals(100, bladeServer.getCurrentCPU(), 1.0E-5);

        verify(mockedBatchJob, times(4)).getUtilization();
        verify(mockedBatchJob, times(3)).getIsChangedThisTime();
        verify(mockedBatchJob).setIsChangedThisTime(1);
        verify(mockedBatchJob).setIsChangedThisTime(0);
        verify(mockedBatchJob).getThisNodeIndex(0);
        final ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(mockedBatchJob).setRemainAt(Matchers.eq(0), captor.capture());
        assertEquals(-8.99, captor.getValue(), 0.01);

        verify(mockedBatchJob, times(2)).getRemainAt(0);

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testDoneWhenThereAreNoBatchJobsAsActiveJobs() {
        expectedException.expect(IndexOutOfBoundsException.class);
        bladeServer.done(0, 0.0);
    }

    @Test
    public void testNotDoneWhenThereAreBatchJobAsActiveJobAndShareIsEqualsToZero() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertEquals(0, bladeServer.getTotalFinishedJob());

        final double share = 0.0;
        assertEquals(1, bladeServer.done(0, share));

        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertEquals(0, bladeServer.getTotalFinishedJob());

        verify(mockedBatchJob).getThisNodeIndex(0);
        verify(mockedBatchJob).setExitTime(1.0);
        verify(mockedBatchJob).getUtilization();

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testDoneWhenBatchJobRemainGreaterThanZero() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());

        when(mockedBatchJob.getRemainLength()).thenReturn(1);
        when(mockedBatchJob.getThisNodeIndex(0)).thenReturn(0);
        when(mockedBatchJob.getRemainAt(0)).thenReturn(2.0, 1.0);

        final double share = 1.0;
        assertEquals(0, bladeServer.done(0, share));

        verify(mockedBatchJob).getUtilization();
        verify(mockedBatchJob).getThisNodeIndex(0);
        verify(mockedBatchJob).setRemainAt(0, 1.0);
        verify(mockedBatchJob, times(2)).getRemainAt(0);

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testDoneWhenBatchJobRemainLessOrEqualZeroAndJobNotAllDone() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());

        when(mockedBatchJob.getRemainLength()).thenReturn(1);
        when(mockedBatchJob.getThisNodeIndex(0)).thenReturn(0);
        when(mockedBatchJob.getRemainAt(0)).thenReturn(1.0, 0.0);
        when(mockedBatchJob.allDone()).thenReturn(false);

        final double share = 1.0;
        assertEquals(0, bladeServer.done(0, share));

        verify(mockedBatchJob).getUtilization();
        verify(mockedBatchJob).getThisNodeIndex(0);
        verify(mockedBatchJob).setRemainAt(0, 0.0);
        verify(mockedBatchJob).setIsChangedThisTime(0);
        verify(mockedBatchJob, times(2)).getRemainAt(0);
        verify(mockedBatchJob).allDone();

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testDoneWhenBatchJobRemainLessOrEqualZeroAndJobAllDone() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);

        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());

        assertEquals(0, bladeServer.getTotalFinishedJob());

        when(mockedBatchJob.getRemainLength()).thenReturn(1);
        when(mockedBatchJob.getThisNodeIndex(0)).thenReturn(0);
        when(mockedBatchJob.getRemainAt(0)).thenReturn(1.0, 0.0);
        when(mockedBatchJob.allDone()).thenReturn(true);

        final double share = 1.0;
        assertEquals(1, bladeServer.done(0, share));

        verify(mockedBatchJob).getUtilization();
        verify(mockedBatchJob).getThisNodeIndex(0);
        verify(mockedBatchJob).setRemainAt(0, 0.0);
        verify(mockedBatchJob).setIsChangedThisTime(0);
        verify(mockedBatchJob, times(2)).getRemainAt(0);
        verify(mockedBatchJob).allDone();
        verify(mockedBatchJob).jobFinished();

        assertEquals(1, bladeServer.getTotalFinishedJob());

        verifyNoMoreInteractions(mockedBatchJob);
    }

    @Test
    public void testSetReadyAsRunningNormalWhenThereAreNoActiveJobs() {
        bladeServer.setReady();
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertEquals(1, bladeServer.getReady());
    }
    
    @Test
    public void testSetReadyAsRunningNormalWhenThereAreActiveJobsAndUtilizationLessThanThreshold() {
        BatchJob mockedBatchJob = mock(BatchJob.class);
        bladeServer.feedWork(mockedBatchJob);
        final double lessThanThreshold = 0.99999;
        when(mockedBatchJob.getUtilization()).thenReturn(lessThanThreshold);
        
        bladeServer.setReady();
        
        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertEquals(1, bladeServer.getReady());
        
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
        
        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertEquals(0, bladeServer.getReady());
        
        verify(mockedBatchJob, times(2)).getUtilization();
        
        verifyNoMoreInteractions(mockedBatchJob);
    }
    
    /*
     * @Test public void testReadFromNode() { fail(
     * "Green in coverage, this is wrong, need to be moved to another place!");
     * }
     */

}
