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
import simulator.schedulers.FIFOScheduler;

public class FIFOSchedulerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmptyQueue() {
        List<Job> queue = new ArrayList<Job>();
        FIFOScheduler fifoScheduler = new FIFOScheduler();
        expectedException.expect(IndexOutOfBoundsException.class);
        fifoScheduler.nextJob(queue);
    }
    
    @Test
    public void testWithOnlyOneJob() {
        testWithJob(new BatchJob(null));
        testWithJob(new EnterpriseJob());
        testWithJob(new InteractiveJob());
    }
    
    public <T extends Job> void testWithJob(T expectedJob) {
        List<T> queue = new ArrayList<T>();
        queue.add(expectedJob);
        
        FIFOScheduler fifoScheduler = new FIFOScheduler();
        Job job = fifoScheduler.nextJob(queue);
        assertEquals(expectedJob, job);
    }
    
    @Test
    public void testWithMoreThanOneJob() {
        List<BatchJob> queue = new ArrayList<BatchJob>();
        BatchJob job = new BatchJob(null);
        queue.add(job);
        BatchJob otherJob = new BatchJob(null);
        queue.add(otherJob);
        
        FIFOScheduler fifoScheduler = new FIFOScheduler();
        Job nextJob = fifoScheduler.nextJob(queue);
        assertEquals(job, nextJob);
        queue.remove(job);
        nextJob = fifoScheduler.nextJob(queue);
        assertEquals(otherJob, nextJob);
    }

}
