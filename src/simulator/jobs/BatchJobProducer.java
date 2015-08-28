package simulator.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import simulator.Environment;

public class BatchJobProducer {

    private static final Logger LOGGER = Logger.getLogger(BatchJobProducer.class.getName());

    private Environment environment;
    private BufferedReader bufferedReader;
    private List<BatchJob> availableJobs = new ArrayList<BatchJob>();
    private final int numberOfParameters = 5;

    public BatchJobProducer(Environment environment, BufferedReader bufferedReader) {
        this.environment = environment;
        this.bufferedReader = bufferedReader;
    }

    public void loadJobs() {
        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] numbers = line.split("\t");
                if (numbers.length < numberOfParameters) {
                    LOGGER.severe("Malformed line, expecting 5 elements found " + numbers.length);
                    continue;
                }

                BatchJob batchJob = new BatchJob();
                batchJob.setRemainParam(Double.parseDouble(numbers[1]), Double.parseDouble(numbers[2]),
                        Integer.parseInt(numbers[3]), Double.parseDouble(numbers[4]));
                batchJob.setStartTime(Double.parseDouble(numbers[0]));
                availableJobs.add(batchJob);
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "loadJobs", ex);
        }
    }

    public boolean hasNext() {
        return !availableJobs.isEmpty() && availableJobs.get(0).getStartTime() >= environment.getCurrentLocalTime();
    }

    public Job next() {
        if (!hasNext())
            throw new NoSuchElementException();

        return availableJobs.get(0);
    }

}
