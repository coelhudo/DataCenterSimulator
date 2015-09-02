package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.am.EnterpriseSystemAM;
import simulator.jobs.EnterpriseJob;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;
import simulator.system.EnterpriseSystemPOD;

public class EnterpriseSystemTest {

    public static final String FAIL_ERROR_MESSAGE = "This was not supposed to happen";
    public EnterpriseSystemPOD enterpriseSystemPOD;
    public Environment mockedEnvironment;
    public Scheduler mockedScheduler;
    public ResourceAllocation mockedResourceAllocation;
    public EnterpriseSystemAM mockedEnterpriseSystemAM;

    @Before
    public void setUp() {
        enterpriseSystemPOD = new EnterpriseSystemPOD();
        mockedEnvironment = mock(Environment.class);
        mockedScheduler = mock(Scheduler.class);
        mockedResourceAllocation = mock(ResourceAllocation.class);
        mockedEnterpriseSystemAM = mock(EnterpriseSystemAM.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockedEnvironment, mockedResourceAllocation, mockedScheduler,
                mockedEnterpriseSystemAM);
    }

    @Test
    public void testEnterpriseSystemCreation() {
        EnterpriseSystem enterpriseSystem = EnterpriseSystem.Create(enterpriseSystemPOD, mockedEnvironment,
                mockedScheduler, mockedResourceAllocation, mockedEnterpriseSystemAM);

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
        EnterpriseSystem enterpriseSystem = EnterpriseSystem.Create(enterpriseSystemPOD, mockedEnvironment,
                mockedScheduler, mockedResourceAllocation, mockedEnterpriseSystemAM);

        try {
            Method runACycle = EnterpriseSystem.class.getDeclaredMethod("runAcycle");
            runACycle.setAccessible(true);
            Boolean result = (Boolean) runACycle.invoke(enterpriseSystem);
            assertTrue(result);
            assertTrue(enterpriseSystem.isDone());
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

        verify(mockedResourceAllocation).initialResourceAlocator(enterpriseSystem);
        verify(mockedEnterpriseSystemAM).setManagedSystem(enterpriseSystem);
    }

    @Test
    public void testRunACycle_WithApplication_MarkAsNotDone() {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t1");
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }
        enterpriseApplicationPOD.setBIS(mockedBufferedReader);
        enterpriseSystemPOD.appendEnterpriseApplicationPOD(enterpriseApplicationPOD);
        EnterpriseSystem enterpriseSystem = EnterpriseSystem.Create(enterpriseSystemPOD, mockedEnvironment,
                mockedScheduler, mockedResourceAllocation, mockedEnterpriseSystemAM);

        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        EnterpriseJob enterpriseJob = new EnterpriseJob();
        enterpriseJob.setArrivalTimeOfJob(1);
        enterpriseJob.setNumberOfJob(1);
        when(mockedScheduler.nextJob(anyListOf(EnterpriseJob.class))).thenReturn(enterpriseJob);

        try {
            Method runACycle = EnterpriseSystem.class.getDeclaredMethod("runAcycle");
            runACycle.setAccessible(true);
            Boolean result = (Boolean) runACycle.invoke(enterpriseSystem);
            assertFalse(result);
            assertFalse(enterpriseSystem.isDone());
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

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedScheduler).nextJob(anyListOf(EnterpriseJob.class));
        verify(mockedEnvironment).getCurrentLocalTime();
        verify(mockedResourceAllocation).initialResourceAlocator(enterpriseSystem);
        verify(mockedEnterpriseSystemAM).setManagedSystem(enterpriseSystem);
        verifyNoMoreInteractions(mockedBufferedReader);
    }
}
