package simulator.jobs;

public class InteractiveJob extends Job {

    public double numberOfJob;
    public int arrivalTimeOfJob;

    public InteractiveJob() {
        arrivalTimeOfJob = 0;
        numberOfJob = 0;
    }
}
