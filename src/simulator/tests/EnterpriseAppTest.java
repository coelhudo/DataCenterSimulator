package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import simulator.Environment;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;

public class EnterpriseAppTest {

    @Test
    public void testEnterpriseAppCreation() {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        Environment mockedEnvironment = mock(Environment.class);
        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        assertNotNull(enterpriseApplication.getAM());
        assertEquals(0.0, enterpriseApplication.getAverageCPUutil(), 1.0E-8);
        assertNotEquals(0, enterpriseApplication.getAveragePwrParam().length);
        assertTrue(enterpriseApplication.getComputeNodeList().isEmpty());
        assertEquals(0, enterpriseApplication.getID());
        assertEquals(0, enterpriseApplication.getMaxExpectedResTime());
        assertEquals(0, enterpriseApplication.getMaxNumberOfRequest());
        assertEquals(0, enterpriseApplication.getMaxProc());
        assertEquals(0, enterpriseApplication.getMinProc());
        assertEquals(0, enterpriseApplication.getNumberofBasicNode());
        assertEquals(0, enterpriseApplication.getNumofViolation());
        assertEquals(0, enterpriseApplication.getNumofViolation());
        assertTrue(enterpriseApplication.getQueueApp().isEmpty());
        assertTrue(enterpriseApplication.getResponseList().isEmpty());
        assertEquals(0, enterpriseApplication.getSLAPercentage());
        assertEquals(0, enterpriseApplication.getSLAviolation());
        assertEquals(0, enterpriseApplication.getTimeTreshold());
    }

}
