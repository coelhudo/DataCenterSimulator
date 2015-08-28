package simulator.jobs;

public interface JobProducer {
    public boolean hasNext();

    public Job next();
}
