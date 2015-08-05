package simulator.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import simulator.jobs.BatchJob;
import simulator.jobs.EnterpriseJob;
import simulator.jobs.InteractiveJob;
import simulator.jobs.Job;
import simulator.schedulers.LeastRemainFirst;

public class LeastRemainFirstTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testNextJobWhenTheQueueContainsOnlyOneJob() {
        LeastRemainFirst leastRemainFirst = new LeastRemainFirst();
        List<BatchJob> queue = new ArrayList<BatchJob>();
        BatchJob expectedJob = new BatchJob(null, null);
        queue.add(expectedJob);
        Job job = leastRemainFirst.nextJob(queue);
        assertEquals(expectedJob, job);
    }
    
    @Test
    public void testTwoBatchJobsInTheQueue() {
        LeastRemainFirst leastRemainFirst = new LeastRemainFirst();
        List<BatchJob> queue = new ArrayList<BatchJob>();
        BatchJob expectedJob = new BatchJob(null, null);
        queue.add(expectedJob);
        BatchJob notExpectedJob = new BatchJob(null, null);
        queue.add(notExpectedJob);
        assertNotEquals(expectedJob, notExpectedJob);
        Job job = leastRemainFirst.nextJob(queue);
        assertEquals(expectedJob, job);
        assertNotEquals(notExpectedJob, job);
        job = leastRemainFirst.nextJob(queue);
        assertEquals(expectedJob, job);
        assertNotEquals(notExpectedJob, job);
    }
    
    @Test
    public void testChangingReqTimeFromBatchJob() {
        LeastRemainFirst leastRemainFirst = new LeastRemainFirst();
        List<BatchJob> queue = new ArrayList<BatchJob>();
        BatchJob expectedJob = new BatchJob(null, null);
        expectedJob.setReqTime(0.1);
        queue.add(expectedJob);
        BatchJob otherExpectedJob = new BatchJob(null, null);
        otherExpectedJob.setReqTime(0.2);
        queue.add(otherExpectedJob);
        assertNotEquals(expectedJob, otherExpectedJob);
        
        Job job = leastRemainFirst.nextJob(queue);
        assertEquals(expectedJob, job);
        assertNotEquals(otherExpectedJob, job);
        
        expectedJob.setReqTime(0.2);
        otherExpectedJob.setReqTime(0.1);
        job = leastRemainFirst.nextJob(queue);
        assertNotEquals(expectedJob, job);
        assertEquals(otherExpectedJob, job);
    }

    @Test
    public void testWithEmptyJobQueue() {
        LeastRemainFirst leastRemainFirst = new LeastRemainFirst();
        List<BatchJob> queue = new ArrayList<BatchJob>();
        expected.expect(IndexOutOfBoundsException.class);
        leastRemainFirst.nextJob(queue);
    }

    @Test
    public void testWithEnterpriseJob() {
        LeastRemainFirst leastRemainFirst = new LeastRemainFirst();
        List<EnterpriseJob> queue = new ArrayList<EnterpriseJob>();
        EnterpriseJob expectedJob = new EnterpriseJob();
        queue.add(expectedJob);
        expected.expect(ClassCastException.class);
        leastRemainFirst.nextJob(queue);
    }
    
    @Test
    public void testWithInteractiveJob() {
        LeastRemainFirst leastRemainFirst = new LeastRemainFirst();
        List<InteractiveJob> queue = new ArrayList<InteractiveJob>();
        InteractiveJob expectedJob = new InteractiveJob();
        queue.add(expectedJob);
        expected.expect(ClassCastException.class);
        leastRemainFirst.nextJob(queue);
    }
}
