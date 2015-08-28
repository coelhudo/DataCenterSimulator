package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import simulator.jobs.BatchJob;
import simulator.physical.BladeServer;
import simulator.physical.DataCenter;

public class BatchJobTest {

    DataCenter mockedDataCenter;

    @Before
    public void setUP() {
        mockedDataCenter = mock(DataCenter.class);
    }

    @Test
    public void testBatchJobInstantiation() {
        BatchJob batchJob = new BatchJob(mockedDataCenter);
        assertEquals(0.0, batchJob.getStartTime(), 1.0E-8);
        assertEquals(0.0, batchJob.getExitTime(), 1.0E-8);
        assertEquals(0.0, batchJob.getReqTime(), 1.0E-8);
        assertEquals(0.0, batchJob.getExitTime(), 1.0E-8);
        assertEquals(0, batchJob.getNumOfNode());
        assertEquals(0.0, batchJob.getDeadline(), 1.0E-8);
    }

    @Test
    public void testSetRemainParameters() {
        BatchJob batchJob = new BatchJob(mockedDataCenter);
        batchJob.setRemainParam(10.0, 50, 100, 5.0);
        assertEquals(0.5, batchJob.getUtilization(), 1.0E-8);
        assertEquals(100, batchJob.getNumOfNode());
        assertEquals(10.0, batchJob.getRemainAt(99), 1.0E-8);
        assertEquals(5.0, batchJob.getDeadline(), 1.0E-8);
    }

    @Test
    public void testAllDoneWhenThereAreNoNodes() {
        BatchJob batchJob = new BatchJob(mockedDataCenter);
        assertEquals(0, batchJob.getNumOfNode());
        assertTrue(batchJob.allDone());
    }

    @Test
    public void testAllDoneWhenThereAreNoneRemaining() {
        BatchJob batchJob = new BatchJob(mockedDataCenter);
        final int remainingTime = 0;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        assertTrue(batchJob.allDone());
    }

    @Test
    public void testAllDoneWhenThereAreRemainingTime() {
        BatchJob batchJob = new BatchJob(mockedDataCenter);
        final int remainingTime = 1;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        assertFalse(batchJob.allDone());
    }

    @Test
    public void testJobFinished() {
        BatchJob batchJob = new BatchJob(mockedDataCenter);
        final int remainingTime = 1;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        batchJob.setListOfServer(new int[batchJob.getNumOfNode()]);

        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedDataCenter.getServer(0)).thenReturn(mockedBladeServer);

        assertEquals(2.0, batchJob.Finish(1.0), 1.0E-8);

        verify(mockedDataCenter).getServer(0);
        verify(mockedBladeServer).getBlockedBatchList();
    }

    @Test
    public void testGetThisNodeIndexFails() {
        BatchJob batchJob = new BatchJob(mockedDataCenter);
        batchJob.setRemainParam(1.0, 1, 1, 1);
        batchJob.setListOfServer(new int[batchJob.getNumOfNode()]);
        final int nonExistentIndex = 1;
        assertEquals(-1, batchJob.getThisNodeIndex(nonExistentIndex));
    }

    @Test
    public void testGetThisNodeIndexSucceed() {
        BatchJob batchJob = new BatchJob(mockedDataCenter);
        batchJob.setRemainParam(1.0, 1, 1, 1);
        batchJob.setListOfServer(new int[batchJob.getNumOfNode()]);
        final int existentIndex = 0;
        assertEquals(0, batchJob.getThisNodeIndex(existentIndex));
    }
}
