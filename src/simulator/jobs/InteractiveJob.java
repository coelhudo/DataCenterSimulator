package simulator.jobs;

public class InteractiveJob extends Job {

    private int numberOfJob;
    private int arrivalTimeOfJob;

    public InteractiveJob() {
        setArrivalTimeOfJob(0);
        setNumberOfJob(0);
    }
    
    public InteractiveJob(InteractiveJob other) {
        setArrivalTimeOfJob(other.arrivalTimeOfJob);
        setNumberOfJob(other.numberOfJob);
    }

    public int getNumberOfJob() {
        return numberOfJob;
    }

    public void setNumberOfJob(int numberOfJob) {
        this.numberOfJob = numberOfJob;
    }

    public int getArrivalTimeOfJob() {
        return arrivalTimeOfJob;
    }

    public void setArrivalTimeOfJob(int arrivalTimeOfJob) {
        this.arrivalTimeOfJob = arrivalTimeOfJob;
    }
}
