package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.Violation;
import simulator.jobs.BatchJob;
import simulator.jobs.JobProducer;
import simulator.physical.BladeServer;
import simulator.physical.DataCenter;
import simulator.system.ComputeSystem;
import simulator.system.ComputeSystemPOD;
import simulator.system.SystemPOD;

public class ComputeSystemTest {

    public static final String FAIL_ERROR_MESSAGE = "This was not supposed to happen";

    @Test
    public void testComputeSytemCreation() {
        SystemPOD systemPOD = new ComputeSystemPOD();

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);

        assertEquals(0, computeSystem.getAccumolatedViolation());
        assertNotNull(computeSystem.getAM());
        assertNull(computeSystem.getBis());
        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
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

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger);
    }

    @Test
    public void testRunACycleWithoutAnyJob() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);

        when(mockedJobProducer.hasNext()).thenReturn(false);

        systemPOD.setJobProducer(mockedJobProducer);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());
        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
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

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedJobProducer);
    }

    @Test
    public void testRunACycleWithOneJob_SystemNotDone_ViolationComputeShortage() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(2);
        batchJob.setRemainParam(1, 1, 4, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(3);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        assertFalse(computeSystem.runAcycle());
        assertFalse(computeSystem.isDone());
        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
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

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedJobProducer);
    }

    @Test
    public void testRunACycleWithOneJob_SystemNotDone_ViolationDeadlinePassed() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(2);
        batchJob.setRemainParam(1, 1, 1, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(3, 4);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        assertFalse(computeSystem.runAcycle());
        assertFalse(computeSystem.isDone());

        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(1, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(1, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer, times(3)).isIdle();
        verify(mockedBladeServer).isRunningBusy();
        verify(mockedBladeServer, times(27)).isRunningNormal();
        verify(mockedBladeServer, times(26)).getChassisID();
        verify(mockedBladeServer).getServerID();
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

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedJobProducer,
                mockedBladeServer);
    }

    @Test
    public void testRunACycleWithOneJob() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(2);
        batchJob.setRemainParam(1, 1, 1, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(3);

        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.isRunning()).thenReturn(true);
        when(mockedBladeServer.getTotalFinishedJob()).thenReturn(1);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());

        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(0, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(0, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer).isIdle();
        verify(mockedBladeServer, times(2)).isRunning();
        verify(mockedBladeServer, times(27)).isRunningNormal();
        verify(mockedBladeServer, times(26)).getChassisID();
        verify(mockedBladeServer).getServerID();
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

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedJobProducer,
                mockedBladeServer);
    }

    @Test
    public void testRunACycleWithOneJob_MissingDeadline() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(2);
        batchJob.setRemainParam(1, 1, 1, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(3, 4);

        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getTotalFinishedJob()).thenReturn(1);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());

        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(1, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberOfIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(1, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer, times(3)).isIdle();
        verify(mockedBladeServer, times(27)).isRunningNormal();
        verify(mockedBladeServer).isRunningBusy();
        verify(mockedBladeServer, times(26)).getChassisID();
        verify(mockedBladeServer).getServerID();
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

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedJobProducer,
                mockedBladeServer);
    }

    @Test
    public void testMoveWaitingJobsToBladeServer() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        JobProducer mockedJobProducer = mock(JobProducer.class);

        DataCenter mockedDataCenter = mock(DataCenter.class);

        BatchJob batchJob = new BatchJob();
        batchJob.setStartTime(1);
        batchJob.setRemainParam(1, 1, 1, 1);

        when(mockedJobProducer.hasNext()).thenReturn(true, false);
        when(mockedJobProducer.next()).thenReturn(batchJob);

        systemPOD.setJobProducer(mockedJobProducer);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);

        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getChassisID()).thenReturn(15);

        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

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
        verify(mockedBladeServer, times(51)).isRunningNormal();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));

        verify(mockedBladeServer, times(50)).getChassisID();
        verify(mockedBladeServer).getServerID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedJobProducer,
                mockedBladeServer);
    }

    @Test
    public void testNumberOfIdleNodeIsOne() {
        SystemPOD systemPOD = new ComputeSystemPOD();

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(true);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);
        assertEquals(1, computeSystem.numberOfIdleNode());

        verify(mockedBladeServer).isIdle();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBladeServer);
    }

    @Test
    public void testNumberOfIdleNodeIsNone() {
        SystemPOD systemPOD = new ComputeSystemPOD();

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(false);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);
        assertEquals(0, computeSystem.numberOfIdleNode());

        verify(mockedBladeServer).isIdle();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBladeServer);
    }

    @Test
    public void testFinalized() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.create(systemPOD, mockedEnvironment, mockedDataCenter,
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

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBladeServer);
    }

}
