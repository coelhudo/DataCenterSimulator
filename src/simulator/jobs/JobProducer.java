package simulator.jobs;

public interface JobProducer {
    
    public void loadJobs();
    
    public boolean hasNext();

    public Job next();
}
