package simulator.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BatchJobProducer implements JobProducer {

    private static final Logger LOGGER = Logger.getLogger(BatchJobProducer.class.getName());

    private BufferedReader bufferedReader;
    private List<BatchJob> availableJobs = new ArrayList<BatchJob>();
    private static final int NUMBER_OF_PARAMETERS = 5;
    public BatchJobProducer(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    public void loadJobs() {
        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] numbers = line.split("\t");
                if (numbers.length < NUMBER_OF_PARAMETERS) {
                    LOGGER.severe("Malformed line, expecting "+ NUMBER_OF_PARAMETERS + " elements. Found " + numbers.length);
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
        return !availableJobs.isEmpty();
    }

    public Job next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        
        Job result = availableJobs.get(0);
        availableJobs.remove(0);
        return result;
    }

}
