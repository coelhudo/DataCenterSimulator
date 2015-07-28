package simulator.jobs;

public class InteractiveJob extends Job {

    private double numberOfJob;
    private int arrivalTimeOfJob;

    public InteractiveJob() {
        setArrivalTimeOfJob(0);
        setNumberOfJob(0);
    }

    public double getNumberOfJob() {
        return numberOfJob;
    }

    public void setNumberOfJob(double numberOfJob) {
        this.numberOfJob = numberOfJob;
    }

    public int getArrivalTimeOfJob() {
        return arrivalTimeOfJob;
    }

    public void setArrivalTimeOfJob(int arrivalTimeOfJob) {
        this.arrivalTimeOfJob = arrivalTimeOfJob;
    }
}
