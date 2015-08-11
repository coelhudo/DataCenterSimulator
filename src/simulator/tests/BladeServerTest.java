package simulator.tests;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.jobs.BatchJob;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.InteractiveJob;
import simulator.physical.BladeServer;
import simulator.physical.BladeServerPOD;

public class BladeServerTest {

    public Environment environment;
    final int chassisID = 0;
    BladeServerPOD bladeServerPOD;
    final double frequency = 1.4;

    @Before
    public void setUp() {
        environment = new Environment();
        bladeServerPOD = new BladeServerPOD();
        bladeServerPOD.setFrequencyLevel(new double[1]);
        final double frequency = 1.4;
        bladeServerPOD.setFrequencyLevelAt(0, frequency);
        bladeServerPOD.setPowerBusy(new double[1]);
        bladeServerPOD.setPowerBusyAt(0, 100.0);
        bladeServerPOD.setPowerIdle(new double[1]);
        bladeServerPOD.setPowerIdleAt(0, 50.0);
    }
    
    @Test
    public void testBladeServerCreation() {
        BladeServer bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
        List<BatchJob> activeBatchJobs = bladeServer.getActiveBatchList();
        assertTrue(activeBatchJobs.isEmpty());
        List<BatchJob> blockedJobs = bladeServer.getBlockedBatchList();
        assertTrue(blockedJobs.isEmpty());
        assertEquals(chassisID, bladeServer.getChassisID());
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertEquals(0,bladeServer.getCurrentFreqLevel());
        assertEquals(0, bladeServer.getDependency());
        List<EnterpriseJob> enterpriseJobs = bladeServer.getEnterpriseList();
        assertTrue(enterpriseJobs.isEmpty());
        assertEquals(1, bladeServer.getNumberOfFrequencyLevel());
        assertEquals(0.0, bladeServer.getIdleConsumption(), 1.0E-8);
        assertEquals(0, bladeServer.getMaxExpectedRes());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(1, bladeServer.getNumberOfPowerBusy());
        assertEquals(1, bladeServer.getNumberOfPowerIdle());
        assertEquals(3, bladeServer.getPwrParam().length);
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
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
        List<InteractiveJob> interactiveJobs = bladeServer.getInteractiveList();
        assertTrue(interactiveJobs.isEmpty());
        assertFalse(bladeServer.isSLAviolation());

    }

    @Test
    public void testGetPowerRunningBusy() {
        BladeServer bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
        bladeServer.setCurrentCPU(50);
        bladeServer.setStatusAsRunningBusy();
        assertEquals(0, bladeServer.getReady());
        assertEquals(frequency, bladeServer.getMips(), 1.0E-8);
        assertEquals(75.0, bladeServer.getPower(), 1.0E-8);
        bladeServer.setCurrentCPU(100);
        assertEquals(100.0, bladeServer.getPower(), 1.0E-8);
        bladeServer.setCurrentCPU(10);
        assertEquals(55.0, bladeServer.getPower(), 1.0E-8);
    }

    @Test
    public void testRestart() {
        BladeServer bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);

        bladeServer.setRespTime(10);
        bladeServer.setCurrentCPU(10);

        List<BatchJob> activeBacthJobs = new ArrayList<BatchJob>();
        activeBacthJobs.add(null);
        bladeServer.setActiveBatchList(activeBacthJobs);

        List<BatchJob> blockedBacthJobs = new ArrayList<BatchJob>();
        blockedBacthJobs.add(null);
        bladeServer.setBlockedBatchList(blockedBacthJobs);

        List<EnterpriseJob> enterpriseJobs = new ArrayList<EnterpriseJob>();
        enterpriseJobs.add(null);
        bladeServer.setEnterprizList(enterpriseJobs);

        List<InteractiveJob> interactiveJobs = new ArrayList<InteractiveJob>();
        interactiveJobs.add(null);
        bladeServer.setWebBasedList(interactiveJobs);

        bladeServer.setQueueLength(1);
        bladeServer.setStatusAsRunningBusy();
        bladeServer.setTotalFinishedJob(10);
        bladeServer.setMips(1.8);
        bladeServer.setResTimeEpoch(1.0);
        bladeServer.setTotalJob(10);
        bladeServer.setTotalJobEpoch(30);
        bladeServer.setSLAviolation(true);

        assertNotEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertNotEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertFalse(bladeServer.getBlockedBatchList().isEmpty());
        assertFalse(bladeServer.getEnterpriseList().isEmpty());
        assertFalse(bladeServer.getInteractiveList().isEmpty());
        assertNotEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertNotEquals(-1, bladeServer.getReady());
        assertNotEquals(0, bladeServer.getTotalFinishedJob());
        assertNotEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertNotEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertNotEquals(0, bladeServer.getTotalJob());
        assertNotEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
        assertTrue(bladeServer.isSLAviolation());

        bladeServer.restart();

        assertEquals(0.0, bladeServer.getResponseTime(), 1.0E-8);
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E-8);
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(0.0, bladeServer.getQueueLength(), 1.0E-8);
        assertEquals(-1, bladeServer.getReady());
        assertEquals(0, bladeServer.getTotalFinishedJob());
        assertEquals(1.4, bladeServer.getMips(), 1.0E-8);
        assertEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E-8);
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E-8);
        assertFalse(bladeServer.isSLAviolation());
    }

    @Test
    public void testFeedBatchJobWork() {
        BladeServer bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
 
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(0, bladeServer.getTotalJob());
        
        BatchJob batchJob = new BatchJob(environment, null);
        bladeServer.feedWork(batchJob);
        
        assertFalse(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(1, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(1, bladeServer.getTotalJob());
    }
    
    @Test
    public void testFeedInteractiveJobWork() {
        BladeServer bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
 
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0, bladeServer.getQueueLength());
        
        InteractiveJob interactiveJob = new InteractiveJob();
        
        final int numberOfJobsInInteractiveJob = 10;
        final int arrivalTime = 40;
        interactiveJob.setNumberOfJob(numberOfJobsInInteractiveJob);
        interactiveJob.setArrivalTimeOfJob(arrivalTime);
        
        bladeServer.feedWork(interactiveJob);
        
        List<InteractiveJob> interactiveJobs = bladeServer.getInteractiveList();
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertTrue(bladeServer.getEnterpriseList().isEmpty());
        assertFalse(interactiveJobs.isEmpty());
        
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getTotalJob());
        InteractiveJob job = interactiveJobs.get(0);
        assertEquals(arrivalTime, job.getArrivalTimeOfJob());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getQueueLength());
    }
    
    @Test
    public void testFeedEnterpriseJobWork() {
        BladeServer bladeServer = new BladeServer(bladeServerPOD, chassisID, environment);
 
        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        
        List<EnterpriseJob> enterpriseJobs = bladeServer.getEnterpriseList();
        
        assertTrue(enterpriseJobs.isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(0, bladeServer.getTotalJob());
        assertEquals(0, bladeServer.getQueueLength());
        
        EnterpriseJob enterpriseJob = new EnterpriseJob();
       
        final int numberOfJobsInInteractiveJob = 10;
        final int arrivalTime = 40;
        enterpriseJob.setNumberOfJob(numberOfJobsInInteractiveJob);
        enterpriseJob.setArrivalTimeOfJob(arrivalTime);
        
        bladeServer.feedWork(enterpriseJob);

        assertTrue(bladeServer.getActiveBatchList().isEmpty());
        assertTrue(bladeServer.getBlockedBatchList().isEmpty());
        assertFalse(bladeServer.getEnterpriseList().isEmpty());
        assertTrue(bladeServer.getInteractiveList().isEmpty());
        assertEquals(-3, bladeServer.getReady());
        assertEquals(0, bladeServer.getDependency());
        assertEquals(10, bladeServer.getTotalJob());
        assertEquals(10, bladeServer.getQueueLength());
        EnterpriseJob job = enterpriseJobs.get(0);
        assertEquals(arrivalTime, job.getArrivalTimeOfJob());
        assertEquals(numberOfJobsInInteractiveJob, bladeServer.getQueueLength());
    }
    
    /*
     * @Test public void testGetCurrentFreqLevel() { fail(
     * "Green in coverage, implement!"); }
     * 
     * @Test public void testIncreaseFrequency() { fail(
     * "Green in coverage, implement!"); }
     * 
     * @Test public void testDecreaseFrequency() { fail(
     * "Green in coverage, implement!"); }
     * 
     * @Test public void testRun() { fail("Green in coverage, implement!"); }
     * 
     * @Test public void testDone() { fail("Green in coverage, implement!"); }
     * 
     * @Test public void testSetDependency() { fail(
     * "Green in coverage, implement!"); }
     * 
     * @Test public void testSetRead() { fail("Green in coverage, implement!");
     * }
     * 
     * @Test public void testReadFromNode() { fail(
     * "Green in coverage, this is wrong, need to be moved to another place!");
     * }
     */

}
