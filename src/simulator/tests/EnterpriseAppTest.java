package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.Job;
import simulator.physical.BladeServer;
import simulator.ra.ResourceAllocation;
import simulator.schedulers.Scheduler;
import simulator.system.EnterpriseApp;
import simulator.system.EnterpriseApplicationPOD;
import simulator.system.EnterpriseSystem;

public class EnterpriseAppTest {

    public static final String FAIL_ERROR_MESSAGE = "This was not supposed to happen";

    @Test
    public void testEnterpriseAppCreation() {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        Environment mockedEnvironment = mock(Environment.class);
        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        assertNull(enterpriseApplication.getAM());
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

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedEnterpriseSystem).getResourceAllocation();

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedEnvironment);
    }

    @Test
    public void testAddCompNodetoBundle() {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        Environment mockedEnvironment = mock(Environment.class);
        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        assertTrue(enterpriseApplication.getComputeNodeList().isEmpty());

        BladeServer mockedBladeServer = mock(BladeServer.class);
        enterpriseApplication.addCompNodetoBundle(mockedBladeServer);

        assertFalse(enterpriseApplication.getComputeNodeList().isEmpty());

        verify(mockedBladeServer).restart();
        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedEnterpriseSystem).getResourceAllocation();

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedEnvironment, mockedBladeServer);
    }

    @Test
    public void testRunACycle_MoreJobsThanCapacity() {
        final int valueToMakeMoreJobsThanCapacity = 3;
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t1");
        } catch (IOException e1) {
            fail("Ouch");
        }

        enterpriseApplicationPOD.setBIS(mockedBufferedReader);

        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);

        Scheduler mockedScheduler = mock(Scheduler.class);
        EnterpriseJob mockedEnterpriseJob = mock(EnterpriseJob.class);
        when(mockedEnterpriseJob.getNumberOfJob()).thenReturn(valueToMakeMoreJobsThanCapacity);
        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(mockedEnterpriseJob);
        when(mockedEnterpriseSystem.getScheduler()).thenReturn(mockedScheduler);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(5);

        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        assertTrue(enterpriseApplication.getComputeNodeList().isEmpty());

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(false);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getCurrentCPU()).thenReturn(10.0);
        when(mockedBladeServer.getMips()).thenReturn(1.4);
        enterpriseApplication.addCompNodetoBundle(mockedBladeServer);
        enterpriseApplication.setNumberofBasicNode(1);
        enterpriseApplication.setMaxNumberOfRequest(1);

        try {
            Method runAcycleMethod = EnterpriseApp.class.getDeclaredMethod("runAcycle");
            runAcycleMethod.setAccessible(true);

            Boolean result = (Boolean) runAcycleMethod.invoke(enterpriseApplication);

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

        assertFalse(enterpriseApplication.getComputeNodeList().isEmpty());
        assertFalse(enterpriseApplication.getQueueApp().isEmpty());

        verify(mockedBladeServer).isIdle();
        verify(mockedBladeServer, times(2)).isRunningNormal();
        verify(mockedBladeServer).restart();
        verify(mockedBladeServer, times(2)).setCurrentCPU(anyInt());
        verify(mockedBladeServer).setStatusAsRunningNormal();
        verify(mockedBladeServer).setStatusAsRunningBusy();
        verify(mockedBladeServer).getCurrentCPU();
        verify(mockedBladeServer).getMips();

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedScheduler).nextJob(anyListOf(Job.class));
        verify(mockedEnvironment, times(2)).getCurrentLocalTime();

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedEnterpriseSystem).getResourceAllocation();

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedEnvironment, mockedBladeServer, mockedBufferedReader,
                mockedScheduler);
    }

    @Test
    public void testRunACycle_CapacityEqualsZero() {
        final int valueToMakeCapacityEqualsZero = 2;

        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t1");
        } catch (IOException e1) {
            fail("Ouch");
        }

        enterpriseApplicationPOD.setBIS(mockedBufferedReader);

        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);

        Scheduler mockedScheduler = mock(Scheduler.class);
        EnterpriseJob mockedEnterpriseJob = mock(EnterpriseJob.class);
        when(mockedEnterpriseJob.getNumberOfJob()).thenReturn(valueToMakeCapacityEqualsZero);
        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(mockedEnterpriseJob);
        when(mockedEnterpriseSystem.getScheduler()).thenReturn(mockedScheduler);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(5);

        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        assertTrue(enterpriseApplication.getComputeNodeList().isEmpty());

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.isIdle()).thenReturn(false);
        when(mockedBladeServer.getCurrentCPU()).thenReturn(10.0);
        when(mockedBladeServer.getMips()).thenReturn(1.4);
        enterpriseApplication.addCompNodetoBundle(mockedBladeServer);
        enterpriseApplication.setNumberofBasicNode(1);
        enterpriseApplication.setMaxNumberOfRequest(1);

        try {
            Method runAcycleMethod = EnterpriseApp.class.getDeclaredMethod("runAcycle");
            runAcycleMethod.setAccessible(true);

            Boolean result = (Boolean) runAcycleMethod.invoke(enterpriseApplication);

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

        assertFalse(enterpriseApplication.getComputeNodeList().isEmpty());
        assertFalse(enterpriseApplication.getQueueApp().isEmpty());

        verify(mockedBladeServer).isIdle();
        verify(mockedBladeServer, times(2)).isRunningNormal();
        verify(mockedBladeServer).restart();
        verify(mockedBladeServer, times(2)).setCurrentCPU(anyInt());
        verify(mockedBladeServer).setStatusAsRunningNormal();
        verify(mockedBladeServer).setStatusAsRunningBusy();
        verify(mockedBladeServer).getCurrentCPU();
        verify(mockedBladeServer).getMips();

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedScheduler).nextJob(anyListOf(Job.class));
        verify(mockedEnvironment, times(2)).getCurrentLocalTime();

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedEnterpriseSystem).getResourceAllocation();

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedEnvironment, mockedBladeServer, mockedBufferedReader,
                mockedScheduler);
    }

    @Test
    public void testRunACycle_CapacityGreaterThanNumberOfJobs() {
        final int valueToMakeCapacityGreaterThanNumberOfJobs = 1;

        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t1");
        } catch (IOException e1) {
            fail(FAIL_ERROR_MESSAGE);
        }

        enterpriseApplicationPOD.setBIS(mockedBufferedReader);

        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);

        Scheduler mockedScheduler = mock(Scheduler.class);
        EnterpriseJob mockedEnterpriseJob = mock(EnterpriseJob.class);
        when(mockedEnterpriseJob.getNumberOfJob()).thenReturn(valueToMakeCapacityGreaterThanNumberOfJobs);
        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(mockedEnterpriseJob);
        when(mockedEnterpriseSystem.getScheduler()).thenReturn(mockedScheduler);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(5);

        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        assertTrue(enterpriseApplication.getComputeNodeList().isEmpty());

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isIdle()).thenReturn(false);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getCurrentCPU()).thenReturn(10.0);
        when(mockedBladeServer.getMips()).thenReturn(1.4);
        enterpriseApplication.addCompNodetoBundle(mockedBladeServer);
        enterpriseApplication.setNumberofBasicNode(1);
        enterpriseApplication.setMaxNumberOfRequest(1);

        try {
            Method runAcycleMethod = EnterpriseApp.class.getDeclaredMethod("runAcycle");
            runAcycleMethod.setAccessible(true);

            Boolean result = (Boolean) runAcycleMethod.invoke(enterpriseApplication);

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

        assertFalse(enterpriseApplication.getComputeNodeList().isEmpty());

        verify(mockedBladeServer, times(1)).isIdle();
        verify(mockedBladeServer).setStatusAsRunningNormal();
        verify(mockedBladeServer, times(2)).isRunningNormal();
        verify(mockedBladeServer).restart();
        verify(mockedBladeServer, times(2)).setCurrentCPU(anyInt());
        verify(mockedBladeServer).setStatusAsRunningBusy();
        verify(mockedBladeServer).getCurrentCPU();
        verify(mockedBladeServer).getMips();

        verify(mockedEnterpriseJob, times(2)).getArrivalTimeOfJob();
        verify(mockedEnterpriseJob, times(6)).getNumberOfJob();

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedScheduler, times(2)).nextJob(anyListOf(Job.class));
        verify(mockedEnvironment, times(3)).getCurrentLocalTime();

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedEnterpriseSystem).getResourceAllocation();

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedEnvironment, mockedBladeServer, mockedBufferedReader,
                mockedScheduler);
    }

    @Test
    public void testAppendEntepriseJobIntoQueueApplication() {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("2\t1");
        } catch (IOException e1) {
            fail(FAIL_ERROR_MESSAGE);
        }

        enterpriseApplicationPOD.setBIS(mockedBufferedReader);

        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);

        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        assertTrue(enterpriseApplication.getComputeNodeList().isEmpty());

        try {
            Method runAcycleMethod = EnterpriseApp.class.getDeclaredMethod("runAcycle");
            runAcycleMethod.setAccessible(true);

            // Because job arrivalTime is less than
            // environment.getCurrentLocalTime()
            // then nothing is going to be executed. The only thing that is
            // supposed to happen is the queue become filled
            for (int i = 1; i <= 3; i++) {
                assertTrue((Boolean) runAcycleMethod.invoke(enterpriseApplication));
                assertEquals(i, enterpriseApplication.getQueueApp().size());
            }

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

        verify(mockedEnvironment, times(3)).getCurrentLocalTime();

        try {
            verify(mockedBufferedReader, times(3)).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedEnterpriseSystem).getResourceAllocation();

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedEnvironment, mockedBufferedReader);
    }

    @Test
    public void testRunACycleMostBranchesAsPossible() {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("2\t1", "3\t1");
        } catch (IOException e1) {
            fail(FAIL_ERROR_MESSAGE);
        }

        enterpriseApplicationPOD.setBIS(mockedBufferedReader);

        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);

        Scheduler mockedScheduler = mock(Scheduler.class);
        EnterpriseJob mockedEnterpriseJob = mock(EnterpriseJob.class);
        when(mockedEnterpriseJob.getNumberOfJob()).thenReturn(1);
        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(mockedEnterpriseJob);
        when(mockedEnterpriseSystem.getScheduler()).thenReturn(mockedScheduler);

        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1, 2);

        ResourceAllocation mockedResourceAllocation = mock(ResourceAllocation.class);
        when(mockedResourceAllocation.nextServer(anyListOf(BladeServer.class))).thenReturn(0);
        when(mockedEnterpriseSystem.getResourceAllocation()).thenReturn(mockedResourceAllocation);

        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        enterpriseApplication.setMaxNumberOfRequest(100);
        enterpriseApplication.setNumberofBasicNode(1);

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getCurrentCPU()).thenReturn(10.0);
        when(mockedBladeServer.getMips()).thenReturn(1.4);

        enterpriseApplication.addCompNodetoBundle(mockedBladeServer);

        try {
            Method runAcycleMethod = EnterpriseApp.class.getDeclaredMethod("runAcycle");
            runAcycleMethod.setAccessible(true);

            // Will insert job in the queue, but because of the arrival time
            // will not execute
            assertTrue((Boolean) runAcycleMethod.invoke(enterpriseApplication));
            assertFalse(enterpriseApplication.getQueueApp().isEmpty());

            // Now it will execute the previous job and the current one. Hence
            // the queue will be consumed
            assertTrue((Boolean) runAcycleMethod.invoke(enterpriseApplication));
            assertTrue(enterpriseApplication.getQueueApp().isEmpty());

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

        List<ResponseTime> responses = enterpriseApplication.getResponseList();
        assertFalse(responses.isEmpty());
        assertEquals(3, responses.size());

        assertEquals(1, responses.get(0).getNumberOfJob(), 1.0E-8);
        assertEquals(1, responses.get(1).getNumberOfJob(), 1.0E-8);
        assertEquals(1, responses.get(2).getNumberOfJob(), 1.0E-8);

        assertEquals(3.0, responses.get(0).getResponseTime(), 1.0E-8);
        assertEquals(3.0, responses.get(1).getResponseTime(), 1.0E-8);
        assertEquals(3.0, responses.get(2).getResponseTime(), 1.0E-8);

        verify(mockedEnvironment, times(5)).getCurrentLocalTime();

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedEnterpriseSystem).getResourceAllocation();

        verify(mockedScheduler, times(3)).nextJob(anyListOf(Job.class));
        verify(mockedResourceAllocation).nextServer(anyListOf(BladeServer.class));

        verify(mockedEnterpriseJob, times(9)).getNumberOfJob();
        verify(mockedEnterpriseJob, times(3)).getArrivalTimeOfJob();

        verify(mockedBladeServer, times(2)).isIdle();
        verify(mockedBladeServer).isRunningNormal();
        verify(mockedBladeServer).restart();
        verify(mockedBladeServer, times(4)).setCurrentCPU(anyInt());
        verify(mockedBladeServer).setStatusAsRunningBusy();
        verify(mockedBladeServer, times(3)).setStatusAsRunningNormal();
        verify(mockedBladeServer, times(2)).getMips();
        verify(mockedBladeServer, times(2)).getCurrentCPU();

        try {
            verify(mockedBufferedReader, times(2)).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE);
        }

        verifyNoMoreInteractions(mockedScheduler, mockedResourceAllocation, mockedEnterpriseJob, mockedEnterpriseSystem,
                mockedEnvironment, mockedBufferedReader, mockedBladeServer);
    }

    @Test
    public void testDestroyApplication() {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();

        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        enterpriseApplicationPOD.setBIS(mockedBufferedReader);

        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        Environment mockedEnvironment = mock(Environment.class);
        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.isRunningNormal()).thenReturn(true);
        when(mockedBladeServer.getCurrentCPU()).thenReturn(10.0);
        when(mockedBladeServer.getMips()).thenReturn(1.4);

        enterpriseApplication.addCompNodetoBundle(mockedBladeServer);

        try {
            Method destroyApplication = EnterpriseApp.class.getDeclaredMethod("destroyApplication");
            destroyApplication.setAccessible(true);

            destroyApplication.invoke(enterpriseApplication);
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

        verify(mockedBladeServer, times(2)).restart();
        verify(mockedBladeServer).setStatusAsNotAssignedToAnyApplication();

        try {
            verify(mockedBufferedReader).close();
        } catch (IOException e) {
            fail("Ouch");
        }

        verify(mockedEnterpriseSystem).getScheduler();
        verify(mockedEnterpriseSystem).getResourceAllocation();

        verifyNoMoreInteractions(mockedBladeServer, mockedBufferedReader, mockedEnterpriseSystem, mockedEnvironment);

    }
}
