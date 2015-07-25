/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package simulator.schedulers;

import simulator.jobs.BatchJob;
import simulator.jobs.Job;
import simulator.schedulers.Scheduler;
import java.util.List;

/**
 *
 * @author fnorouz
 */
public class LeastRemainFirst implements Scheduler {

    @Override
    public Job nextJob(List<? extends Job> queue) {
        double rem = ((BatchJob) queue.get(0)).getReqTime();
        int index = 0;
        int minIndex = 0;
        for (; index < queue.size(); index++) {
            if (((BatchJob) queue.get(index)).getReqTime() < rem) {
                rem = ((BatchJob) queue.get(index)).getReqTime();
                minIndex = index;
            }
        }

        return queue.get(minIndex);
    }
}
