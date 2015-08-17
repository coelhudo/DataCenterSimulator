package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import simulator.Environment;
import simulator.SLAViolationLogger;
import simulator.physical.DataCenter;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;
import simulator.system.SystemPOD;

public class EnterpriseSystemTest {
    
    public static final String FAIL_ERROR_MESSAGE = "This was not supposed to happen";

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
    
    @Test
    public void testRunACycle_WithoutApplication () {
        SystemPOD systemPOD = new EnterpriseSystemPOD();
        Environment mockedEnvironment = mock(Environment.class);
        DataCenter mockedDataCenter = mock(DataCenter.class);
        SLAViolationLogger mockedSLAViolationLogger = mock(SLAViolationLogger.class);
        EnterpriseSystem enterpriseSystem = EnterpriseSystem.Create(systemPOD, mockedEnvironment, mockedDataCenter, mockedSLAViolationLogger);
        
        try {
            Method runACycle = EnterpriseSystem.class.getDeclaredMethod("runAcycle");
            runACycle.setAccessible(true);
            boolean result = (boolean) runACycle.invoke(enterpriseSystem);
            assertTrue(result);
        } catch (NoSuchMethodException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (SecurityException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (IllegalAccessException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            fail(FAIL_ERROR_MESSAGE);
        } catch (InvocationTargetException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
    }
}
