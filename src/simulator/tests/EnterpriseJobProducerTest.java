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
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import simulator.jobs.EnterpriseJob;
import simulator.jobs.EnterpriseJobProducer;

public class EnterpriseJobProducerTest {

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
        EnterpriseJobProducer enterpriseJobProducer = new EnterpriseJobProducer(mockedBufferedReader);

        assertFalse(enterpriseJobProducer.hasNext());

        try {
            String stopValue = null;
            when(mockedBufferedReader.readLine()).thenReturn("1\t1", stopValue);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        enterpriseJobProducer.loadJobs();

        assertTrue(enterpriseJobProducer.hasNext());

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
        EnterpriseJobProducer enterpriseJobProducer = new EnterpriseJobProducer(mockedBufferedReader);

        final int arrivalTimeOfJob = 8;
        final int numberOfJob = 4;
        final String jobAttributes = arrivalTimeOfJob + "\t" + numberOfJob;
        
        try {
            String stopValue = null;
            when(mockedBufferedReader.readLine()).thenReturn(jobAttributes, stopValue);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        assertFalse(enterpriseJobProducer.hasNext());
        enterpriseJobProducer.loadJobs();

        assertTrue(enterpriseJobProducer.hasNext());

        EnterpriseJob enterpriseJob = (EnterpriseJob) enterpriseJobProducer.next();
        assertEquals(numberOfJob, enterpriseJob.getNumberOfJob(), 1.0E-8);
        assertEquals(arrivalTimeOfJob, enterpriseJob.getArrivalTimeOfJob());
        
        assertFalse(enterpriseJobProducer.hasNext());

        try {
            verify(mockedBufferedReader, times(2)).readLine();
            verify(mockedBufferedReader).close();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        verifyNoMoreInteractions(mockedBufferedReader);
    }

    @Test
    public void testLoadJobsMalformedEntry() {
        EnterpriseJobProducer enterpriseJobProducer = new EnterpriseJobProducer(mockedBufferedReader);

        try {
            String stopValue = null;
            when(mockedBufferedReader.readLine()).thenReturn("1\t1\t1", "1", stopValue);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        assertFalse(enterpriseJobProducer.hasNext());
        enterpriseJobProducer.loadJobs();
        assertFalse(enterpriseJobProducer.hasNext());

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
        EnterpriseJobProducer enterpriseJobProducer = new EnterpriseJobProducer(mockedBufferedReader);

        try {
            when(mockedBufferedReader.readLine()).thenThrow(new IOException("Ouch!"));
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        assertFalse(enterpriseJobProducer.hasNext());
        enterpriseJobProducer.loadJobs();
        assertFalse(enterpriseJobProducer.hasNext());

        try {
            verify(mockedBufferedReader).readLine();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        verifyNoMoreInteractions(mockedBufferedReader);
    }

    @Test
    public void testNextFail() {
        EnterpriseJobProducer enterpriseJobProducer = new EnterpriseJobProducer(mockedBufferedReader);

        assertFalse(enterpriseJobProducer.hasNext());
        expected.expect(NoSuchElementException.class);
        assertNotNull(enterpriseJobProducer.next());

        verifyNoMoreInteractions(mockedBufferedReader);
    }

    @Test
    public void testNextMoreThanOneJob() {
        EnterpriseJobProducer enterpriseJobProducer = new EnterpriseJobProducer(mockedBufferedReader);

        assertFalse(enterpriseJobProducer.hasNext());

        try {
            String stopValue = null;
            when(mockedBufferedReader.readLine()).thenReturn("1\t10", "2\t20", "3\t30", stopValue);
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        enterpriseJobProducer.loadJobs();

        assertTrue(enterpriseJobProducer.hasNext());
        EnterpriseJob enterpriseJob = (EnterpriseJob) enterpriseJobProducer.next();
        assertEquals(1, enterpriseJob.getArrivalTimeOfJob());
        assertEquals(10, enterpriseJob.getNumberOfJob());
        assertTrue(enterpriseJobProducer.hasNext());
        enterpriseJob = (EnterpriseJob) enterpriseJobProducer.next();
        assertEquals(2, enterpriseJob.getArrivalTimeOfJob());
        assertEquals(20, enterpriseJob.getNumberOfJob());
        assertTrue(enterpriseJobProducer.hasNext());
        enterpriseJob = (EnterpriseJob) enterpriseJobProducer.next();
        assertEquals(3, enterpriseJob.getArrivalTimeOfJob());
        assertEquals(30, enterpriseJob.getNumberOfJob());
        assertFalse(enterpriseJobProducer.hasNext());

        try {
            verify(mockedBufferedReader, times(4)).readLine();
            verify(mockedBufferedReader).close();
        } catch (IOException e) {
            fail(FAIL_ERROR_MESSAGE + e.getMessage());
        }

        verifyNoMoreInteractions(mockedBufferedReader);
    }
}
