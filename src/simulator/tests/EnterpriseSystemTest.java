package simulator.tests;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.physical.DataCenter;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.SystemPOD;

public class EnterpriseSystemTest {

    @Test
    public void testEnterpriseSystemCreation() { 
        SystemPOD systemPOD = new EnterpriseSystemPOD();
        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        EnterpriseSystem enterpriseSystem = EnterpriseSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger);
        
        assertFalse(enterpriseSystem.isDone());
        assertFalse(enterpriseSystem.isThereFreeNodeforApp());
        assertFalse(enterpriseSystem.checkForViolation());
        assertEquals(0, enterpriseSystem.getAccumolatedViolation());
        assertNotNull(enterpriseSystem.getAM());
        assertTrue(enterpriseSystem.getApplications().isEmpty());
        assertNull(enterpriseSystem.getBis());
        assertTrue(enterpriseSystem.getComputeNodeIndex().isEmpty());
        assertTrue(enterpriseSystem.getComputeNodeList().isEmpty());
        assertNull(enterpriseSystem.getName());
        assertEquals(0, enterpriseSystem.getNumberOfActiveServ());
        assertEquals(0, enterpriseSystem.getNumberofIdleNode());
        assertEquals(0, enterpriseSystem.getNumberOfNode());
        assertEquals(0, enterpriseSystem.getPower(), 1.0E-8);
        assertTrue(enterpriseSystem.getRackIDs().isEmpty());
        assertNotNull(enterpriseSystem.getResourceAllocation());
        assertNotNull(enterpriseSystem.getScheduler());
        assertEquals(0, enterpriseSystem.getSLAviolation());
        
        verifyNoMoreInteractions(mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger);    
    }
}
