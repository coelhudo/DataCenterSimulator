package simulator.tests;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import simulator.Environment;
import simulator.jobs.BatchJob;
import simulator.physical.BladeServer;

public class BladeServerTest {

    @Test
    public void testBladeServerCreation() {
        Environment environment = new Environment();
        final int chassisID = 0;
        BladeServer bladeServer = new BladeServer(chassisID, environment);
        List<BatchJob> batchJob = bladeServer.getActiveBatchList();
        
        assertTrue(batchJob.isEmpty());
    }

}
