package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

import simulator.Environment;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.Job;
import simulator.physical.BladeServer;
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

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedEnvironment, mockedBladeServer);
    }

    @Test
    public void testRunACycle() {
        EnterpriseApplicationPOD enterpriseApplicationPOD = new EnterpriseApplicationPOD();
        BufferedReader mockedBufferedReader = mock(BufferedReader.class);
        try {
            when(mockedBufferedReader.readLine()).thenReturn("1\t1");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        enterpriseApplicationPOD.setBIS(mockedBufferedReader);
        
        EnterpriseSystem mockedEnterpriseSystem = mock(EnterpriseSystem.class);
        
        Scheduler mockedScheduler = mock(Scheduler.class);
        EnterpriseJob mockedEnterpriseJob = mock(EnterpriseJob.class);
        when(mockedEnterpriseJob.getNumberOfJob()).thenReturn(3);
        when(mockedScheduler.nextJob(anyListOf(Job.class))).thenReturn(mockedEnterpriseJob);
        when(mockedEnterpriseSystem.getScheduler()).thenReturn(mockedScheduler);
        
        Environment mockedEnvironment = mock(Environment.class);
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(5);
        
        EnterpriseApp enterpriseApplication = new EnterpriseApp(enterpriseApplicationPOD, mockedEnterpriseSystem,
                mockedEnvironment);
        assertTrue(enterpriseApplication.getComputeNodeList().isEmpty());

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getReady()).thenReturn(1);
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

        verify(mockedBladeServer).restart();
        verify(mockedBladeServer, times(3)).getReady();
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

        verifyNoMoreInteractions(mockedEnterpriseSystem, mockedEnvironment, mockedBladeServer, mockedBufferedReader,
                mockedScheduler);
    }

    /*
     * addCompNodetoBundle readingLogFile readWebJob resetReadyFlagAndCPU
     * runAcycle addToresponseArray destroyApplication
     * 
     */

}
