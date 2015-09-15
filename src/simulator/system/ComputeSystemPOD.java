package simulator.system;

import simulator.jobs.JobProducer;

public class ComputeSystemPOD extends SystemPOD {
    
    private JobProducer jobProducer;
    
    public void setJobProducer(JobProducer jobProducer) {
        this.jobProducer = jobProducer;
    }
    
    public JobProducer getJobProducer() {
        return jobProducer;
    }
}
