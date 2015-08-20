package simulator.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;

import simulator.Environment;
import simulator.jobs.BatchJob;
import simulator.physical.BladeServer;
import simulator.physical.DataCenter;

public class BatchJobTest {
    
    Environment mockedEnvironment;
    DataCenter mockedDataCenter;
    
    @Before
    public void setUP() {
        mockedEnvironment = mock(Environment.class);
        mockedDataCenter = mock(DataCenter.class);
    }
    
    @Test
    public void testBatchJobInstantiation() {
        BatchJob batchJob = new BatchJob(mockedEnvironment, mockedDataCenter);
        assertEquals(0.0, batchJob.getStartTime(), 1.0E-8);
        assertEquals(0.0, batchJob.getExitTime(), 1.0E-8);
        assertEquals(0.0, batchJob.getReqTime(), 1.0E-8);
        assertEquals(0.0, batchJob.getExitTime(), 1.0E-8);
        assertEquals(0, batchJob.getNumOfNode());
        assertEquals(0.0, batchJob.getDeadline(), 1.0E-8);
    }
    
    @Test
    public void testSetRemainParameters() {
        BatchJob batchJob = new BatchJob(mockedEnvironment, mockedDataCenter);
        batchJob.setRemainParam(10.0, 50, 100, 5.0);
        assertEquals(0.5, batchJob.getUtilization(), 1.0E-8);
        assertEquals(100, batchJob.getNumOfNode());
        assertEquals(10.0, batchJob.getRemainAt(99), 1.0E-8);
        assertEquals(5.0, batchJob.getDeadline(), 1.0E-8);
    }

    @Test
    public void testAllDoneWhenThereAreNoNodes() {
        BatchJob batchJob = new BatchJob(mockedEnvironment, mockedDataCenter);
        assertEquals(0, batchJob.getNumOfNode());
        assertTrue(batchJob.allDone());
    }

    @Test
    public void testAllDoneWhenThereAreNoneRemaining() {
        BatchJob batchJob = new BatchJob(mockedEnvironment, mockedDataCenter);
        final int remainingTime = 0;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        assertTrue(batchJob.allDone());
    }
    
    @Test
    public void testAllDoneWhenThereAreRemainingTime() {
        BatchJob batchJob = new BatchJob(mockedEnvironment, mockedDataCenter);
        final int remainingTime = 1;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        assertFalse(batchJob.allDone());
    }

    @Test
    public void testJobFinished() {
        BatchJob batchJob = new BatchJob(mockedEnvironment, mockedDataCenter);
        final int remainingTime = 1;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        batchJob.setListOfServer(new int[batchJob.getNumOfNode()]);
        
        when(mockedEnvironment.getCurrentLocalTime()).thenReturn(1);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getResponseTime()).thenReturn(1.0);
        when(mockedDataCenter.getServer(0)).thenReturn(mockedBladeServer);

        batchJob.jobFinished();
        
        verify(mockedEnvironment, times(2)).getCurrentLocalTime();
        verify(mockedBladeServer).setRespTime(3.0);
        verify(mockedDataCenter, times(2)).getServer(0);
        verify(mockedBladeServer).getBlockedBatchList();
    }
    
    @Test
    public void testGetThisNodeIndexFails() {
        BatchJob batchJob = new BatchJob(mockedEnvironment, mockedDataCenter);
        batchJob.setRemainParam(1.0, 1, 1, 1);
        batchJob.setListOfServer(new int[batchJob.getNumOfNode()]);
        final int nonExistentIndex = 1;
        assertEquals(-1, batchJob.getThisNodeIndex(nonExistentIndex));
    }
    
    @Test
    public void testGetThisNodeIndexSucceed() {
        BatchJob batchJob = new BatchJob(mockedEnvironment, mockedDataCenter);
        batchJob.setRemainParam(1.0, 1, 1, 1);
        batchJob.setListOfServer(new int[batchJob.getNumOfNode()]);
        final int existentIndex = 0;
        assertEquals(0, batchJob.getThisNodeIndex(existentIndex));
    }
}
