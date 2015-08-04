package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import simulator.Environment;
import simulator.ResponseTime;
import simulator.jobs.BatchJob;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.InteractiveJob;
import simulator.physical.BladeServer;

public class BladeServerTest {

    @Test
    public void testBladeServerCreation() {

        Environment environment = new Environment();
        final int chassisID = 0;
        BladeServer bladeServer = new BladeServer(chassisID, environment);
        List<BatchJob> activeBatchJobs = bladeServer.getActiveBatchList();
        assertTrue(activeBatchJobs.isEmpty());
        assertEquals(0, bladeServer.getBackUpReady());
        List<BatchJob> blockedJobs = bladeServer.getBlockedBatchList();
        assertTrue(blockedJobs.isEmpty());
        assertEquals(chassisID, bladeServer.getChassisID());
        assertEquals(0.0, bladeServer.getCurrentCPU(), 1.0E8);
        // FIXME: require xml assertEquals(1,
        // bladeServer.getCurrentFreqLevel());
        assertEquals(0, bladeServer.getDependency());
        List<EnterpriseJob> enterpriseJobs = bladeServer.getEnterprizList();
        assertTrue(enterpriseJobs.isEmpty());
        // FIXME: require xml assertEquals(0,
        // bladeServer.getFrequencyLevel().length);
        assertEquals(0.0, bladeServer.getIdleConsumption(), 1.0E8);
        assertEquals(0, bladeServer.getMaxExpectedRes());
        assertEquals(0.0, bladeServer.getMips(), 1.0E8);
        // FIXME: require xml assertEquals(0.0, bladeServer.getPower(), 1.0E8);
        // FIXME: require xml assertEquals(0,
        // bladeServer.getPowerBusy().length);
        // FIXME: require xml assertEquals(0,
        // bladeServer.getPowerIdle().length);
        // FIXME: require xml assertEquals(0, bladeServer.getPwrParam().length);
        assertEquals(0.0, bladeServer.getQueueLength(), 1.0E8);
        assertEquals(0.0, bladeServer.getRackId(), 1.0E8);
        assertEquals(0.0, bladeServer.getReady(), 1.0E8);
        List<ResponseTime> responseTime = bladeServer.getResponseList();
        assertTrue(responseTime.isEmpty());
        List<ResponseTime> getResponseListWeb = bladeServer.getResponseListWeb();
        assertTrue(getResponseListWeb.isEmpty());
        assertEquals(0.0, bladeServer.getResponseTime(), 1.0E8);
        assertEquals(0.0, bladeServer.getResTimeEpoch(), 1.0E8);
        assertEquals(0.0, bladeServer.getServerID(), 1.0E8);
        assertEquals(0.0, bladeServer.getSLAPercentage(), 1.0E8);
        assertEquals(0.0, bladeServer.getTimeTreshold(), 1.0E8);
        assertEquals(0.0, bladeServer.getTotalFinishedJob(), 1.0E8);
        assertEquals(0.0, bladeServer.getTotalJob(), 1.0E8);
        assertEquals(0.0, bladeServer.getTotalJobEpoch(), 1.0E8);
        List<InteractiveJob> interactiveJobs = bladeServer.getWebBasedList();
        assertTrue(interactiveJobs.isEmpty());
        assertFalse(bladeServer.isSLAviolation());

    }

}
