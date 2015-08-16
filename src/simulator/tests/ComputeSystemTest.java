package simulator.tests;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;

import simulator.Environment;
import simulator.SLAViolationLogger;
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
        ComputeSystem computeSystem = ComputeSystem.Create("dummy", systemPOD, mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger);
        
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
    
    /*runAcycle*/
    /*getFromWaitinglist*/
    /*Number of idle node*/
    /*finalized*/
}
