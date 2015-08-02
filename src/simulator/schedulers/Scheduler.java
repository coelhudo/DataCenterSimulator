package simulator.schedulers;

import java.util.List;

import simulator.jobs.Job;

public interface Scheduler {

    public Job nextJob(List<? extends Job> s);
}
