package simulator.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import simulator.jobs.BatchJob;
import simulator.jobs.BatchJobProducer;
import simulator.physical.BladeServer;
import simulator.physical.DataCenterEntityID;

public class BatchJobProducerTest {

    private static final String FAIL_ERROR_MESSAGE = "That was supposed not to happen ";
    public BufferedReader mockedBufferedReader;

    @Before
    public void setUp() {
        mockedBufferedReader = mock(BufferedReader.class);
    }

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testHasNext() {
        BatchJobProducer batchJobProducer = new BatchJobProducer(mockedBufferedReader);

        assertFalse(batchJobProducer.hasNext());

        try {
            String stopValue = null;
            when(mockedBufferedReader.readLine()).thenReturn("1\t1\t1\t1\t1", stopValue);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        batchJobProducer.loadJobs();

        assertTrue(batchJobProducer.hasNext());

        try {
            verify(mockedBufferedReader, times(2)).readLine();
            verify(mockedBufferedReader).close();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        verifyNoMoreInteractions(mockedBufferedReader);
    }

    @Test
    public void testLoadJobs() {
        BatchJobProducer batchJobProducer = new BatchJobProducer(mockedBufferedReader);

        final int jobStartTime = 4;
        final double remainingTime = 8;
        final double utilization = 15;
        final int numberOfNodes = 1;
        final double deadline = 23;
        final String jobAttributes = jobStartTime + "\t" + remainingTime + "\t" + utilization + "\t" + numberOfNodes
                + "\t" + deadline;
        try {
            String stopValue = null;
            when(mockedBufferedReader.readLine()).thenReturn(jobAttributes, stopValue);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        assertFalse(batchJobProducer.hasNext());
        batchJobProducer.loadJobs();

        assertTrue(batchJobProducer.hasNext());
        
        BladeServer mockedBladeServer = mock(BladeServer.class);
        when(mockedBladeServer.getID()).thenReturn(DataCenterEntityID.createServerID(1, 1, 1));

        BatchJob batchJob = (BatchJob) batchJobProducer.next();
        batchJob.setListOfServer(Arrays.asList(mockedBladeServer));
        assertEquals(jobStartTime, batchJob.getStartTime(), 1.0E-8);
        assertEquals(remainingTime, batchJob.getRemainAt(mockedBladeServer.getID()), 1.0E-8);
        assertEquals(utilization / 100.0, batchJob.getUtilization(), 1.0E-8);
        assertEquals(numberOfNodes, batchJob.getNumOfNode());
        assertEquals(deadline, batchJob.getDeadline(), 1.0E-8);

        assertFalse(batchJobProducer.hasNext());

        try {
            verify(mockedBufferedReader, times(2)).readLine();
            verify(mockedBufferedReader).close();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }
        
        verify(mockedBladeServer, times(2)).getID();

        verifyNoMoreInteractions(mockedBufferedReader, mockedBladeServer);
    }

    @Test
    public void testLoadJobsMalformedEntry() {
        BatchJobProducer batchJobProducer = new BatchJobProducer(mockedBufferedReader);

        try {
            String stopValue = null;
            when(mockedBufferedReader.readLine()).thenReturn("1\t1", "1\t1\t1\t1\t1\t1", stopValue);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        assertFalse(batchJobProducer.hasNext());
        batchJobProducer.loadJobs();
        assertFalse(batchJobProducer.hasNext());

        try {
            verify(mockedBufferedReader, times(3)).readLine();
            verify(mockedBufferedReader).close();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        verifyNoMoreInteractions(mockedBufferedReader);
    }

    @Test
    public void testLoadJobsIOException() {
        BatchJobProducer batchJobProducer = new BatchJobProducer(mockedBufferedReader);

        try {
            when(mockedBufferedReader.readLine()).thenThrow(new IOException("Ouch!"));
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        assertFalse(batchJobProducer.hasNext());
        batchJobProducer.loadJobs();
        assertFalse(batchJobProducer.hasNext());

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        verifyNoMoreInteractions(mockedBufferedReader);
    }

    @Test
    public void testNextFail() {
        BatchJobProducer batchJobProducer = new BatchJobProducer(mockedBufferedReader);

        assertFalse(batchJobProducer.hasNext());
        expected.expect(NoSuchElementException.class);
        assertNotNull(batchJobProducer.next());

        verifyNoMoreInteractions(mockedBufferedReader);
    }

    @Test
    public void testNextMoreThanOneJob() {
        BatchJobProducer batchJobProducer = new BatchJobProducer(mockedBufferedReader);

        assertFalse(batchJobProducer.hasNext());

        try {
            String stopValue = null;
            when(mockedBufferedReader.readLine()).thenReturn("1\t1\t1\t1\t1", "2\t1\t1\t1\t1", "3\t1\t1\t1\t1",
                    stopValue);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        batchJobProducer.loadJobs();

        assertTrue(batchJobProducer.hasNext());
        BatchJob batchJob = (BatchJob) batchJobProducer.next();
        assertEquals(1.0, batchJob.getStartTime(), 1.0E-8);
        assertTrue(batchJobProducer.hasNext());
        batchJob = (BatchJob) batchJobProducer.next();
        assertEquals(2.0, batchJob.getStartTime(), 1.0E-8);
        assertTrue(batchJobProducer.hasNext());
        batchJob = (BatchJob) batchJobProducer.next();
        assertEquals(3.0, batchJob.getStartTime(), 1.0E-8);
        assertFalse(batchJobProducer.hasNext());

        try {
            verify(mockedBufferedReader, times(4)).readLine();
            verify(mockedBufferedReader).close();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        verifyNoMoreInteractions(mockedBufferedReader);
    }
}
