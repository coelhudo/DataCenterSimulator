package simulator.jobs;

public class EnterpriseJob extends Job {

    private int numberOfJob;
    private int arrivalTimeOfJob;

    public EnterpriseJob() {
        setNumberOfJob(0);
        setArrivalTimeOfJob(0);
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
