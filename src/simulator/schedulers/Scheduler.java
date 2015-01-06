package simulator.schedulers;

import java.util.ArrayList;
import simulator.jobs.Job;

public interface Scheduler {

    public Job nextJob(ArrayList<? extends Job> s);
}
