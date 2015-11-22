package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
    EnterpriseSystem enterpriseSystem;

    @Before
    public void setUp() {
        enterpriseSystemPOD = new EnterpriseSystemPOD();
        mockedScheduler = mock(Scheduler.class);
        mockedResourceAllocation = mock(ResourceAllocation.class);
        mockedEnterpriseSystemAM = mock(EnterpriseSystemAM.class);
        applications = new ArrayList<EnterpriseApp>();
        enterpriseSystem = new EnterpriseSystem(enterpriseSystemPOD, applications, mockedScheduler,
                mockedResourceAllocation, mockedEnterpriseSystemAM);
        enterpriseSystem.getResourceAllocation().initialResourceAlocator(enterpriseSystem);
        enterpriseSystem.setupAM();
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockedResourceAllocation, mockedScheduler, mockedEnterpriseSystemAM);
    }

    @Test
    public void testEnterpriseSystemCreation() {
        assertFalse(enterpriseSystem.isDone());
        assertFalse(enterpriseSystem.isThereFreeNodeforApp());
        assertFalse(enterpriseSystem.checkForViolation());
        assertEquals(0, enterpriseSystem.getAccumolatedViolation());
        assertNotNull(enterpriseSystem.getAM());
        assertTrue(enterpriseSystem.getApplications().isEmpty());
        assertNull(enterpriseSystem.getBis());
        assertTrue(enterpriseSystem.getComputeNodeList().isEmpty());
        assertNull(enterpriseSystem.getName());
        assertEquals(0, enterpriseSystem.getNumberOfActiveServ());
        assertEquals(0, enterpriseSystem.getNumberOfIdleNode());
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
        assertTrue(enterpriseSystem.runAcycle());
        assertTrue(enterpriseSystem.isDone());

        verify(mockedResourceAllocation).initialResourceAlocator(enterpriseSystem);
        verify(mockedEnterpriseSystemAM).setManagedSystem(enterpriseSystem);
    }

    @Test
    public void testRunACycle_WithApplication_MarkAsNotDone() {
        EnterpriseApp mockedEnterpriseApp = mock(EnterpriseApp.class);
        when(mockedEnterpriseApp.runAcycle()).thenReturn(true);
        applications.add(mockedEnterpriseApp);

        assertFalse(enterpriseSystem.runAcycle());
        assertFalse(enterpriseSystem.isDone());
        assertFalse(applications.isEmpty());

        verify(mockedResourceAllocation).initialResourceAlocator(enterpriseSystem);
        verify(mockedEnterpriseSystemAM).setManagedSystem(enterpriseSystem);
        verify(mockedEnterpriseApp).runAcycle();
        verifyNoMoreInteractions(mockedEnterpriseApp);
    }

    @Test
    public void testRunACycle_WithApplication_MarkAsDone() {
        EnterpriseApp mockedEnterpriseApp = mock(EnterpriseApp.class);
        when(mockedEnterpriseApp.runAcycle()).thenReturn(false);
        applications.add(mockedEnterpriseApp);

        assertTrue(enterpriseSystem.runAcycle());
        assertTrue(enterpriseSystem.isDone());
        assertTrue(applications.isEmpty());

        verify(mockedEnterpriseSystemAM).setManagedSystem(enterpriseSystem);
        verify(mockedResourceAllocation).initialResourceAlocator(enterpriseSystem);
        verify(mockedEnterpriseApp).runAcycle();
        verify(mockedEnterpriseApp).getComputeNodeList();
        verify(mockedEnterpriseApp).getID();
        verify(mockedEnterpriseApp).getNumofViolation();

        verify(mockedEnterpriseApp).destroyApplication();
        verifyNoMoreInteractions(mockedEnterpriseApp);
    }
}
