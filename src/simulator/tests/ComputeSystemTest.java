package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.Violation;
import simulator.jobs.Job;
import simulator.jobs.BatchJob;
import simulator.jobs.JobProducer;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.physical.DataCenterEntityID;
import simulator.schedulers.Scheduler;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemPOD;

public class ComputeSystemTest {

    public static final String FAIL_ERROR_MESSAGE = "This was not supposed to happen";

    public Environment mockedEnvironment;
    public Scheduler mockedScheduler;
    public ResourceAllocation mockedResourceAllocation;
    public ComputeSystemPOD systemPOD;
    public SLAViolationLogger mockedSLAViolationLogger;

    @Before
    public void setUp() {
        systemPOD = new ComputeSystemPOD();
        mockedEnvironment = mock(Environment.class);
        mockedResourceAllocation = mock(ResourceAllocation.class);
        mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        mockedScheduler = mock(Scheduler.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockedEnvironment, mockedScheduler,
                                 mockedResourceAllocation, mockedSLAViolationLogger);
    }

    @Test
    public void testComputeSytemCreation() {
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment,
                                                           mockedScheduler, mockedResourceAllocation,
                                                           mockedSLAViolationLogger);

        assertEquals(0, computeSystem.getAccumolatedViolation());
        assertNotNull(computeSystem.getAM());
        assertNull(computeSystem.getBis());
        assertTrue(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertTrue(computeSystem.getRackIDs().isEmpty());
        assertNotNull(computeSystem.getResourceAllocation());
        assertNotNull(computeSystem.getScheduler());
        assertEquals(0, computeSystem.getSLAviolation());
        assertFalse(computeSystem.isBlocked());
        assertFalse(computeSystem.isDone());

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);
    }

    @Test
    public void testRunACycleWithoutAnyJob() {
        JobProducer mockedJobProducer = mock(JobProducer.class);

        when(mockedJobProducer.hasNext()).thenReturn(false);

        systemPOD.setJobProducer(mockedJobProducer);

        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment,
                                                           mockedScheduler, mockedResourceAllocation,
                                                           mockedSLAViolationLogger);
        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());
        assertTrue(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(0, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(0, computeSystem.getAccumolatedViolation());

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();

        verify(mockedJobProducer).hasNext();

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);

        verifyNoMoreInteractions(mockedJobProducer);
    }

    @Test
    public void testRunACycleWithOneJob_SystemNotDone_ViolationComputeShortage() {
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(2);
        batchJob.setRemainParam(1, 1, 4, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);

        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(3);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment,
                                                           mockedScheduler, mockedResourceAllocation,
                                                           mockedSLAViolationLogger);

        when(mockedResourceAllocation.allocateSystemLevelServer(computeSystem.getComputeNodeList(),
                                                                batchJob.getNumOfNode())).thenReturn(null);

        assertFalse(computeSystem.runAcycle());
        assertFalse(computeSystem.isDone());
        assertTrue(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(1, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(1, computeSystem.getAccumolatedViolation());

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment, times(2)).getCurrentLocalTime();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();

        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();

        verify(mockedSLAViolationLogger).logHPCViolation(anyString(), eq(Violation.COMPUTE_NODE_SHORTAGE));

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);
        verify(mockedResourceAllocation).allocateSystemLevelServer(anyListOf(BladeServer.class), anyInt());

        verify(mockedScheduler).nextJob(anyListOf(Job.class));

        verifyNoMoreInteractions(mockedJobProducer);
    }

    @Test
    public void testRunACycleWithOneJob_SystemNotDone_ViolationDeadlinePassed() {
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(2);
        batchJob.setRemainParam(1, 1, 1, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);

        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(3, 4);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment,
                                                           mockedScheduler, mockedResourceAllocation,
                                                           mockedSLAViolationLogger);

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));

        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        when(mockedResourceAllocation.allocateSystemLevelServer(computeSystem.getComputeNodeList(),
                                                          batchJob.getNumOfNode())).thenReturn(Arrays.asList(mockedBladeServer));

        assertFalse(computeSystem.runAcycle());
        assertFalse(computeSystem.isDone());

        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(1, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(1, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer, times(3)).isIdle();
        verify(mockedBladeServer).isRunningBusy();
        verify(mockedBladeServer).getID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedBladeServer).run();
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();

        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();

        verify(mockedSLAViolationLogger).logHPCViolation(anyString(), eq(Violation.DEADLINE_PASSED));

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);
        verify(mockedResourceAllocation).allocateSystemLevelServer(anyListOf(BladeServer.class), anyInt());

        verify(mockedScheduler).nextJob(anyListOf(Job.class));

        verifyNoMoreInteractions(mockedJobProducer, mockedBladeServer);
    }

    @Test
    public void testRunACycleWithOneJob() {
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(2);
        batchJob.setRemainParam(1, 1, 1, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);

        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(3);

        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment,
                                                           mockedScheduler, mockedResourceAllocation,
                                                           mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.isRunning()).thenReturn(true);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));

        when(mockedBladeServer.getTotalFinishedJob()).thenReturn(1);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);
        when(mockedResourceAllocation.allocateSystemLevelServer(computeSystem.getComputeNodeList(),
                                                                batchJob.getNumOfNode())).thenReturn(Arrays.asList(mockedBladeServer));

        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());

        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(0, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(0, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer).isIdle();
        verify(mockedBladeServer, times(2)).isRunning();
        verify(mockedBladeServer).getID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedBladeServer).run();
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();

        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment).updateNumberOfMessagesFromSystemToNodes();

        verify(mockedBladeServer).decreaseFrequency();
        verify(mockedBladeServer).getActiveBatchList();
        verify(mockedBladeServer).getBlockedBatchList();
        verify(mockedBladeServer).setStatusAsIdle();

        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);
        verify(mockedResourceAllocation).allocateSystemLevelServer(anyListOf(BladeServer.class), anyInt());

        verify(mockedScheduler).nextJob(anyListOf(Job.class));;

        verifyNoMoreInteractions(mockedJobProducer, mockedBladeServer);
    }

    @Test
    public void testRunACycleWithOneJob_MissingDeadline() {
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(2);
        batchJob.setRemainParam(1, 1, 1, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);
        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(3, 4);

        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment,
                                                           mockedScheduler, mockedResourceAllocation,
                                                           mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getTotalFinishedJob()).thenReturn(1);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createChassisID(1, 1));

        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        when(mockedResourceAllocation.allocateSystemLevelServer(computeSystem.getComputeNodeList(),
                                                          batchJob.getNumOfNode())).thenReturn(Arrays.asList(mockedBladeServer));

        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());

        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(1, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(1, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer, times(3)).isIdle();
        verify(mockedBladeServer).isRunningBusy();
        verify(mockedBladeServer).getID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedBladeServer).run();
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();

        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();

        verify(mockedSLAViolationLogger).logHPCViolation(anyString(), eq(Violation.DEADLINE_PASSED));

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);
        verify(mockedResourceAllocation).allocateSystemLevelServer(anyListOf(BladeServer.class), anyInt());

        verify(mockedScheduler).nextJob(anyListOf(Job.class));

        verifyNoMoreInteractions(mockedJobProducer, mockedBladeServer);
    }

    @Test
    public void testMoveWaitingJobsToBladeServer() {
        DataCenterEntityID rackID = DataCenterEntityID.createRackID(1);
        systemPOD.appendRackID(rackID);
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(1);
        batchJob.setRemainParam(1, 1, 1, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);
        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);

        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment,
                                                           mockedScheduler, mockedResourceAllocation,
                                                           mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createChassisID(1, 15));

        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        when(mockedResourceAllocation.allocateSystemLevelServer(computeSystem.getComputeNodeList(),
                                                          batchJob.getNumOfNode())).thenReturn(Arrays.asList(mockedBladeServer));

        try {
            Method loadJobsIntoWaitingQueueMethod = ComputeSystem.class.getDeclaredMethod("loadJobsIntoWaitingQueue");
            loadJobsIntoWaitingQueueMethod.setAccessible(true);

            loadJobsIntoWaitingQueueMethod.invoke(computeSystem);

            Method moveWaitingJobsToBladeServerMethod = ComputeSystem.class
                    .getDeclaredMethod("moveWaitingJobsToBladeServer");
            moveWaitingJobsToBladeServerMethod.setAccessible(true);

            moveWaitingJobsToBladeServerMethod.invoke(computeSystem);
        } catch (IllegalAccessException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (InvocationTargetException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (NoSuchMethodException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (SecurityException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedJobProducer, times(2)).hasNext();
        verify(mockedJobProducer).next();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));

        verify(mockedBladeServer).getID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);
        verify(mockedResourceAllocation).allocateSystemLevelServer(anyListOf(BladeServer.class), anyInt());

        verify(mockedScheduler).nextJob(anyListOf(Job.class));

        verifyNoMoreInteractions(mockedJobProducer, mockedBladeServer);
    }

    @Test
    public void testNumberOfIdleNodeIsOne() {

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedScheduler,
                                                           mockedResourceAllocation, mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(true);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);
        assertEquals(1, computeSystem.numberOfIdleNode());

        verify(mockedBladeServer).isIdle();

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);

        verifyNoMoreInteractions(mockedBladeServer);
    }

    @Test
    public void testNumberOfIdleNodeIsNone() {

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedScheduler,
                                                           mockedResourceAllocation, mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(false);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);
        assertEquals(0, computeSystem.numberOfIdleNode());

        verify(mockedBladeServer).isIdle();

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);

        verifyNoMoreInteractions(mockedBladeServer);
    }

    @Test
    public void testFinalized() {
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment,
                                                           mockedScheduler, mockedResourceAllocation,
                                                           mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getResponseTime()).thenReturn(15.0);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        try {
            Method finalizeMethod = ComputeSystem.class.getDeclaredMethod("finalized");
            finalizeMethod.setAccessible(true);

            Double result = (Double) finalizeMethod.invoke(computeSystem);
            assertEquals(15.0, result, 1.0E-8);
        } catch (IllegalAccessException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (InvocationTargetException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (NoSuchMethodException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (SecurityException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedBladeServer).getResponseTime();

        verify(mockedResourceAllocation).initialResourceAloc(computeSystem);

        verifyNoMoreInteractions(mockedBladeServer);
    }

}
