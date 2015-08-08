package simulator.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.jobs.BatchJob;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.InteractiveJob;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerPOD;

public class BladeServerTest {

    @Test
    public void testBladeServerCreation() {

        Environment environment = new Environment();
        final int chassisID = 0;
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        BladeServer bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
        List<BatchJob> activeBatchJobs = bladeServer.getActiveBatchList();
        assertTrue(activeBatchJobs.isEmpty());
        List<BatchJob> blockedJobs = bladeServer.getBlockedBatchList();
        assertTrue(blockedJobs.isEmpty());
        assertEquals(chassisID, bladeServer.getChassisID());
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        //assertEquals(1,bladeServer.getCurrentFreqLevel());
        assertEquals(0, bladeServer.getDependency());
        List<EnterpriseJob> enterpriseJobs = bladeServer.getEnterprizList();
        assertTrue(enterpriseJobs.isEmpty()); 
        //assertEquals(0, bladeServer.getFrequencyLevel().length);
        assertEquals(0.0, bladeServer.getIdleConsumption(), 1.0E-8);
        assertEquals(0, bladeServer.getMaxExpectedRes());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        //assertEquals(0, bladeServer.getPowerBusy().length);
        //assertEquals(0, bladeServer.getPowerIdle().length);
        //assertEquals(0, bladeServer.getPwrParam().length);
        assertEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertEquals(0.0, bladeServer.getRackId(), 1.0E-8);
        assertEquals(-3.0, bladeServer.getReady(), 1.0E-8);
        List<ResponseTime> responseTime = bladeServer.getResponseList();
        assertTrue(responseTime.isEmpty());
        List<ResponseTime> getResponseListWeb = bladeServer.getResponseListWeb();
        assertTrue(getResponseListWeb.isEmpty());
        assertEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertEquals(0.0, bladeServer.getServerID(), 1.0E-8);
        assertEquals(0.0, bladeServer.getSLAPercentage(), 1.0E-8);
        assertEquals(0.0, bladeServer.getTimeTreshold(), 1.0E-8);
        assertEquals(0.0, bladeServer.getTotalFinishedJob(), 1.0E-8);
        assertEquals(0.0, bladeServer.getTotalJob(), 1.0E-8);
        assertEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
        List<InteractiveJob> interactiveJobs = bladeServer.getWebBasedList();
        assertTrue(interactiveJobs.isEmpty());
        assertFalse(bladeServer.isSLAviolation());

    }
    
    @Test
    public void testGetPowerAfterCreation() {
        Environment environment = new Environment();
        final int chassisID = 0;
        BladeServerPOD bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setFrequencyLevel(new double[1]);
        final double frequency = 1.4;
        bladeServerPOD.setFrequencyLevelAt(0, frequency);
        bladeServerPOD.setPowerBusy(new double[1]);
        bladeServerPOD.setPowerBusyAt(0, 100.0);
        bladeServerPOD.setPowerIdle(new double[1]);
        bladeServerPOD.setPowerIdleAt(0, 50);
        
        BladeServer bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
        bladeServer.setCurrentCPU(50);
        bladeServer.setStatusAsRunningBusy();
        assertEquals(0, bladeServer.getReady());
        assertEquals(frequency, bladeServer.getMips(), 1.0E-8);
        assertEquals(75.0, bladeServer.getPower(), 1.0E-8);
    }
    
    /*
    @Test
    public void testRestart() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testFeedWork() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testGetCurrentFreqLevel() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testIncreaseFrequency() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testDecreaseFrequency() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testRun() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testDone() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testSetDependency() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testSetRead() {
        fail("Green in coverage, implement!");
    }
    
    @Test
    public void testReadFromNode() {
        fail("Green in coverage, this is wrong, need to be moved to another place!");
    }*/
    
    
    

}
