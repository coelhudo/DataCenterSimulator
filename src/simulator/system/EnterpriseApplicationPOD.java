package simulator.system;

import simulator.jobs.JobProducer;

public final class EnterpriseApplicationPOD {
    
    private int id = 0;
    private int maxProc = 0;
    private int minProc = 0;
    private int timeTreshold = 0;
    private int slaPercentage;
    private int maxNumberOfRequest = 0;
    private int numberofBasicNode = 0;
    private int maxExpectedResTime = 0;
    private JobProducer jobProducer;

    public int getMaxProc() {
        return maxProc;
    }

    public void setMaxProc(int maxProc) {
        this.maxProc = maxProc;
    }

    public int getMinProc() {
        return minProc;
    }

    public void setMinProc(int minProc) {
        this.minProc = minProc;
    }

    public int getTimeTreshold() {
        return timeTreshold;
    }

    public void setTimeTreshold(int timeTreshold) {
        this.timeTreshold = timeTreshold;
    }

    public int getSLAPercentage() {
        return slaPercentage;
    }

    public void setSLAPercentage(int sLAPercentage) {
        slaPercentage = sLAPercentage;
    }

    public int getMaxNumberOfRequest() {
        return maxNumberOfRequest;
    }

    public void setMaxNumberOfRequest(int maxNumberOfRequest) {
        this.maxNumberOfRequest = maxNumberOfRequest;
    }

    public int getID() {
        return id;
    }
    
    public void setID(int id) {
        this.id = id;
    }

    public int getNumberofBasicNode() {
        return numberofBasicNode;
    }

    public void setNumberofBasicNode(int numberofBasicNode) {
        this.numberofBasicNode = numberofBasicNode;
    }

    public int getMaxExpectedResTime() {
        return maxExpectedResTime;
    }

    public void setMaxExpectedResTime(int maxExpectedResTime) {
        this.maxExpectedResTime = maxExpectedResTime;
    }

    public void setJobProducer(JobProducer jobProducer) {
        this.jobProducer= jobProducer;
    }
    
    public JobProducer getJobProducer() {
        return jobProducer;
    }
}
