package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;

import org.junit.Test;

import simulator.jobs.BatchJob;
import simulator.physical.BladeServer;
import simulator.physical.DataCenterEntityID;

public class BatchJobTest {

    @Test
    public void testBatchJobInstantiation() {
        BatchJob batchJob = new BatchJob();
        assertEquals(0.0, batchJob.getStartTime(), 1.0E-8);
        assertEquals(0.0, batchJob.getReqTime(), 1.0E-8);
        assertEquals(0, batchJob.getNumOfNode());
        assertEquals(0.0, batchJob.getDeadline(), 1.0E-8);
    }

    @Test
    public void testSetRemainParameters() {
        BatchJob batchJob = new BatchJob();
        batchJob.setRemainParam(10.0, 50, 1, 5.0);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));
        batchJob.setListOfServer(Arrays.asList(mockedBladeServer));
        assertEquals(0.5, batchJob.getUtilization(), 1.0E-8);
        assertEquals(1, batchJob.getNumOfNode());
        assertEquals(10.0, batchJob.getRemainAt(mockedBladeServer.getID()), 1.0E-8);
        assertEquals(5.0, batchJob.getDeadline(), 1.0E-8);

        verify(mockedBladeServer, times(2)).getID();
    }

    @Test
    public void testAllDoneWhenThereAreNoNodes() {
        BatchJob batchJob = new BatchJob();
        assertEquals(0, batchJob.getNumOfNode());
        assertTrue(batchJob.allDone());
    }

    @Test
    public void testAllDoneWhenThereAreNoneRemaining() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));

        BatchJob batchJob = new BatchJob();
        final int remainingTime = 0;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        batchJob.setListOfServer(Arrays.asList(mockedBladeServer));
        batchJob.setRemainAt(mockedBladeServer.getID(), 0);
        assertTrue(batchJob.allDone());

        verify(mockedBladeServer, times(2)).getID();

        verifyNoMoreInteractions(mockedBladeServer);
    }

    @Test
    public void testAllDoneWhenThereAreRemainingTime() {
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));

        BatchJob batchJob = new BatchJob();
        final int remainingTime = 1;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        batchJob.setListOfServer(Arrays.asList(mockedBladeServer));
        assertFalse(batchJob.allDone());

        verify(mockedBladeServer).getID();

        verifyNoMoreInteractions(mockedBladeServer);
    }

    @Test
    public void testFinish() {
        BatchJob batchJob = new BatchJob();
        final int remainingTime = 1;
        batchJob.setRemainParam(remainingTime, 0, 1, 5.0);
        BladeServer mockedBladeServer = mock(BladeServer.class);
        batchJob.setListOfServer(Arrays.asList(mockedBladeServer));

        assertEquals(2.0, batchJob.Finish(1.0), 1.0E-8);

        verify(mockedBladeServer).getBlockedBatchList();
        verify(mockedBladeServer).getID();
        verifyNoMoreInteractions(mockedBladeServer);
    }
}
