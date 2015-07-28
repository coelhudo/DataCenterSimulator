/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.schedulers;

import simulator.schedulers.Scheduler;
import java.util.List;
import simulator.jobs.Job;

/**
 *
 * @author fnorouz
 */
public class FifoScheduler implements Scheduler {

    @Override
    public Job nextJob(List<? extends Job> queue) {
        return queue.get(0);
    }
}
