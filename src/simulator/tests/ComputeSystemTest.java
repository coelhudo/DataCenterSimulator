package simulator.tests;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.Violation;
import simulator.jobs.BatchJob;
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
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);

        assertEquals(0, computeSystem.getAccumolatedViolation());
        assertNotNull(computeSystem.getAM());
        assertNotNull(computeSystem.getBis());
        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertTrue(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberofIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertTrue(computeSystem.getRackIDs().isEmpty());
        assertNotNull(computeSystem.getResourceAllocation());
        assertNotNull(computeSystem.getScheduler());
        assertEquals(0, computeSystem.getSLAviolation());
        assertFalse(computeSystem.isBlocked());
        assertFalse(computeSystem.isDone());

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
    }

    @Test
    public void testRunACycleWithoutAnyJob() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn(null);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());
        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertTrue(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(0, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberofIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(0, computeSystem.getAccumolatedViolation());

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
    }

    @Test
    public void testRunACycleWithOneJob_SystemNotDone_ViolationComputeShortage() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("2\t1\t1\t1\t1");
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1, 3);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        assertFalse(computeSystem.runAcycle());
        assertFalse(computeSystem.isDone());
        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertTrue(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(1, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberofIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(1, computeSystem.getAccumolatedViolation());

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment, times(2)).getCurrentLocalTime();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedSLAViolationLogger).logHPCViolation(anyString(), eq(Violation.COMPUTE_NODE_SHORTAGE));

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
    }

    @Test
    public void testRunACycleWithOneJob_SystemNotDone_ViolationDeadlinePassed() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("2\t1\t1\t1\t1");
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1, 3, 4);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getReady()).thenReturn(1);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        assertFalse(computeSystem.runAcycle());
        assertFalse(computeSystem.isDone());

        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(1, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberofIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(1, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer).isIdle();
        verify(mockedBladeServer, times(30)).getReady(); // XXX: 30???
        verify(mockedBladeServer, times(26)).getChassisID(); // XXX: 26????
        verify(mockedBladeServer).getServerID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedBladeServer).run();
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        verify(mockedSLAViolationLogger).logHPCViolation(anyString(), eq(Violation.DEADLINE_PASSED));

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader,
                mockedBladeServer);
    }

    @Test
    public void testRunACycleWithOneJob() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("2\t1\t1\t1\t1");
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1, 3);

        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getReady()).thenReturn(1);
        when(mockedBladeServer.getTotalFinishedJob()).thenReturn(1);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());

        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(0, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberofIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(0, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer, times(30)).getReady(); // XXX: 30???
        verify(mockedBladeServer, times(26)).getChassisID(); // XXX: 26????
        verify(mockedBladeServer).getServerID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedBladeServer).run();
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment).updateNumberOfMessagesFromSystemToNodes();

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedBladeServer).decreaseFrequency();
        verify(mockedBladeServer).getActiveBatchList();
        verify(mockedBladeServer).getBlockedBatchList();
        verify(mockedBladeServer).setStatusAsIdle();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader,
                mockedBladeServer);
    }

    @Test
    public void testRunACycleWithOneJob_MissingDeadline() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("2\t1\t1\t1\t1");
        } catch (IOException e) {
            fail("This was not supposed to happen");
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1, 3, 4);

        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getReady()).thenReturn(1);
        when(mockedBladeServer.getTotalFinishedJob()).thenReturn(1);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        assertTrue(computeSystem.runAcycle());
        assertTrue(computeSystem.isDone());

        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertFalse(computeSystem.getComputeNodeList().isEmpty());
        assertEquals(1, computeSystem.getSLAviolation());
        assertEquals(0, computeSystem.getNumberOfActiveServ());
        assertEquals(0, computeSystem.getNumberofIdleNode());
        assertEquals(0, computeSystem.getNumberOfNode());
        assertEquals(0.0, computeSystem.getPower(), 1.0E-8);
        assertEquals(1, computeSystem.getAccumolatedViolation());

        verify(mockedBladeServer).isIdle(); 
        verify(mockedBladeServer, times(30)).getReady(); // XXX: 30???
        verify(mockedBladeServer, times(26)).getChassisID(); // XXX: 26????
        verify(mockedBladeServer).getServerID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedBladeServer).run();
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedSLAViolationLogger).logHPCViolation(anyString(), eq(Violation.DEADLINE_PASSED));

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader,
                mockedBladeServer);
    }

    @Test
    public void testReadJob_NoJobsFail() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn(null);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);

        BatchJob mockedBatchJob = mock(BatchJob.class);

        try {
            Method method = ComputeSystem.class.getDeclaredMethod("readJob", BatchJob.class);
            method.setAccessible(true);

            Boolean result = (Boolean) method.invoke(computeSystem, mockedBatchJob);
            assertFalse(result);
        } catch (IllegalAccessException  e) {
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

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
    }

    @Test
    public void testReadJob_WrongNumberOfParameters() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t1");
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);

        BatchJob mockedBatchJob = mock(BatchJob.class);

        try {
            Method method = ComputeSystem.class.getDeclaredMethod("readJob", BatchJob.class);
            method.setAccessible(true);

            Boolean result = (Boolean) method.invoke(computeSystem, mockedBatchJob);
            assertFalse(result);
        } catch (IllegalAccessException  e) {
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

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
    }

    @Test
    public void testReadJobSuccessfully() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t2\t3\t4\t5");
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);

        BatchJob mockedBatchJob = mock(BatchJob.class);

        try {
            Method readJobMethod = ComputeSystem.class.getDeclaredMethod("readJob", BatchJob.class);
            readJobMethod.setAccessible(true);

            Boolean result = (Boolean) readJobMethod.invoke(computeSystem, mockedBatchJob);
            assertTrue(result);
        } catch (IllegalAccessException  e) {
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

        verify(mockedBatchJob).setStartTime(1);
        verify(mockedBatchJob).setRemainParam(2, 3, 4, 5);
        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
    }

    @Test
    public void testMoveWaitingJobsToBladeServer() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t2\t3\t4\t5");
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getReady()).thenReturn(1);
        when(mockedBladeServer.getChassisID()).thenReturn(15);

        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);

        BatchJob mockedBatchJob = mock(BatchJob.class);
        when(mockedBatchJob.getNumOfNode()).thenReturn(1);
        when(mockedBatchJob.getDeadline()).thenReturn(10.0);
        when(mockedBatchJob.getStartTime()).thenReturn(1.0);
        try {
            Method readJobMethod = ComputeSystem.class.getDeclaredMethod("readJob", BatchJob.class);
            readJobMethod.setAccessible(true);

            Boolean result = (Boolean) readJobMethod.invoke(computeSystem, mockedBatchJob);
            assertTrue(result);

            Method moveWaitingJobsToBladeServerMethod = ComputeSystem.class
                    .getDeclaredMethod("moveWaitingJobsToBladeServer");
            moveWaitingJobsToBladeServerMethod.setAccessible(true);

            moveWaitingJobsToBladeServerMethod.invoke(computeSystem);
        } catch (IllegalAccessException  e) {
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

        verify(mockedEnvironment, times(2)).getCurrentLocalTime();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedBatchJob).setStartTime(1);
        verify(mockedBatchJob).setRemainParam(2, 3, 4, 5);

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedBladeServer, times(51)).getReady(); // XXX: 51???
        verify(mockedBladeServer, times(50)).getChassisID(); // XXX: 26????
        verify(mockedBladeServer).getServerID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedEnvironment, times(2)).getCurrentLocalTime();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader,
                mockedBladeServer);
    }

    @Test
    public void testNumberOfIdleNodeIsOne() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(true);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);
        assertEquals(1, computeSystem.numberOfIdleNode());

        verify(mockedBladeServer).isIdle();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader,
                mockedBladeServer);
    }

    @Test
    public void testNumberOfIdleNodeIsNone() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t2\t3\t4\t5");
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(false);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);
        assertEquals(0, computeSystem.numberOfIdleNode());

        verify(mockedBladeServer).isIdle();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader,
                mockedBladeServer);
    }
    
    @Test
    public void testFinalized() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getResponseTime()).thenReturn(15.0);
        computeSystem.appendBladeServerIntoComputeNodeList(mockedBladeServer);
        
        try {
            Method finalizeMethod = ComputeSystem.class.getDeclaredMethod("finalized");
            finalizeMethod.setAccessible(true);

            Double result = (Double) finalizeMethod.invoke(computeSystem);
            assertEquals(15.0, result, 1.0E-8);
        } catch (IllegalAccessException  e) {
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

        try {
            verify(mockedBufferedReader).close();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        
        verify(mockedBladeServer).getResponseTime();

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader,
                mockedBladeServer);
    }

}
