package simulator.tests;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;

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

    @Test
    public void testComputeSytemCreation() {
        SystemPOD systemPOD = new ComputeSystemPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create("dummy", systemPOD, mockedEnvironment, mockedDataCenter,
                mockedSLAViolationLogger);

        assertEquals(0, computeSystem.getAccumolatedViolation());
        assertNotNull(computeSystem.getAM());
        assertNotNull(computeSystem.getBis());
        assertTrue(computeSystem.getComputeNodeIndex().isEmpty());
        assertTrue(computeSystem.getComputeNodeList().isEmpty());
        assertEquals("dummy", computeSystem.getName());
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
            fail("This was not supposed to happen");
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create("dummy", systemPOD, mockedEnvironment, mockedDataCenter,
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
            fail("This was not supposed to happen");
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
            fail("This was not supposed to happen");
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1, 3);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create("dummy", systemPOD, mockedEnvironment, mockedDataCenter,
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
            fail("This was not supposed to happen");
        }

        verify(mockedSLAViolationLogger).logHPCViolation("dummy", Violation.COMPUTE_NODE_SHORTAGE);

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
    }

    @Test
    public void testRunACycleWithOneJob_SystemNotDone_ViolationDeadlinePassed() {
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
        ComputeSystem computeSystem = ComputeSystem.Create("dummy", systemPOD, mockedEnvironment, mockedDataCenter,
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

        verify(mockedBladeServer, times(31)).getReady(); // XXX: 31???
        verify(mockedBladeServer, times(26)).getChassisID(); //XXX: 26????
        verify(mockedBladeServer).getServerID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedBladeServer).setDependency(0);
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedBladeServer).run(any(BatchJob.class));
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();
        
        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail("This was not supposed to happen");
        }
        verify(mockedSLAViolationLogger).logHPCViolation("dummy", Violation.DEADLINE_PASSED);

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
            fail("This was not supposed to happen");
        }
        systemPOD.setBis(mockedBufferedReader);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1, 3);
        
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        ComputeSystem computeSystem = ComputeSystem.Create("dummy", systemPOD, mockedEnvironment, mockedDataCenter,
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
        verify(mockedBladeServer, times(26)).getChassisID(); //XXX: 26????
        verify(mockedBladeServer).getServerID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedBladeServer).setDependency(0);
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedBladeServer).run(any(BatchJob.class));
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        verify(mockedEnvironment).updateNumberOfMessagesFromSystemToNodes();
        
        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail("This was not supposed to happen");
        }

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
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
        ComputeSystem computeSystem = ComputeSystem.Create("dummy", systemPOD, mockedEnvironment, mockedDataCenter,
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
        
        verify(mockedBladeServer, times(31)).getReady(); // XXX: 31???
        verify(mockedBladeServer, times(26)).getChassisID(); //XXX: 26????
        verify(mockedBladeServer).getServerID();
        verify(mockedBladeServer).feedWork(any(BatchJob.class));
        verify(mockedBladeServer).setDependency(0);
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedBladeServer).run(any(BatchJob.class));
        verify(mockedBladeServer).getTotalFinishedJob();
        verify(mockedBladeServer).getCurrentFreqLevel();

        verify(mockedEnvironment).localTimeByEpoch();
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();
        verify(mockedEnvironment).updateNumberOfMessagesFromDataCenterToSystem();
        
        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail("This was not supposed to happen");
        }
        
        verify(mockedSLAViolationLogger).logHPCViolation("dummy", Violation.DEADLINE_PASSED);

        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger, mockedBufferedReader);
    }

    /* Methods that are in coverage report */
    /* getFromWaitinglist */
    /* Number of idle node */
    /* finalized */
}
