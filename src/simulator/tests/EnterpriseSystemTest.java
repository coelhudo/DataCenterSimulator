package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.am.EnterpriseSystemAM;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;

public class EnterpriseSystemTest {

    public static final String FAIL_ERROR_MESSAGE = "This was not supposed to happen";
    public EnterpriseSystemPOD enterpriseSystemPOD;
    public Scheduler mockedScheduler;
    public ResourceAllocation mockedResourceAllocation;
    public EnterpriseSystemAM mockedEnterpriseSystemAM;
    public List<EnterpriseApp> applications;

    @Before
    public void setUp() {
        enterpriseSystemPOD = new EnterpriseSystemPOD();
        mockedScheduler = mock(Scheduler.class);
        mockedResourceAllocation = mock(ResourceAllocation.class);
        mockedEnterpriseSystemAM = mock(EnterpriseSystemAM.class);
        applications = new ArrayList<EnterpriseApp>();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockedResourceAllocation, mockedScheduler, mockedEnterpriseSystemAM);
    }

    @Test
    public void testEnterpriseSystemCreation() {
        EnterpriseSystem enterpriseSystem = EnterpriseSystem.Create(enterpriseSystemPOD, mockedScheduler,
                mockedResourceAllocation, mockedEnterpriseSystemAM, applications);

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

        verify(mockedResourceAllocation).initialResourceAlocator(enterpriseSystem);
        verify(mockedEnterpriseSystemAM).setManagedSystem(enterpriseSystem);
    }

    @Test
    public void testRunACycle_WithoutApplication() {
        EnterpriseSystem enterpriseSystem = EnterpriseSystem.Create(enterpriseSystemPOD, mockedScheduler,
                mockedResourceAllocation, mockedEnterpriseSystemAM, applications);

        try {
            assertTrue(enterpriseSystem.runAcycle());
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        assertTrue(enterpriseSystem.isDone());
        
        verify(mockedResourceAllocation).initialResourceAlocator(enterpriseSystem);
        verify(mockedEnterpriseSystemAM).setManagedSystem(enterpriseSystem);
    }

    @Test
    public void testRunACycle_WithApplication_MarkAsNotDone() {
        EnterpriseApp mockedEnterpriseApp = mock(EnterpriseApp.class);
        when(mockedEnterpriseApp.runAcycle()).thenReturn(true);
        applications.add(mockedEnterpriseApp);

        EnterpriseSystem enterpriseSystem = EnterpriseSystem.Create(enterpriseSystemPOD, mockedScheduler,
                mockedResourceAllocation, mockedEnterpriseSystemAM, applications);

        try {
            assertFalse(enterpriseSystem.runAcycle());
        } catch (IOException e1) {
            fail(FAIL_ERROR_MESSAGE);
        }
        assertFalse(enterpriseSystem.isDone());

        verify(mockedResourceAllocation).initialResourceAlocator(enterpriseSystem);
        verify(mockedEnterpriseSystemAM).setManagedSystem(enterpriseSystem);
        verify(mockedEnterpriseApp).runAcycle();
        verifyNoMoreInteractions(mockedEnterpriseApp);
    }
}
