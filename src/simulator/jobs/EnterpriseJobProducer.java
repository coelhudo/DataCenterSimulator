package simulator.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

public class EnterpriseJobProducer implements JobProducer {

    private static final Logger LOGGER = Logger.getLogger(EnterpriseJobProducer.class.getName());
    
    private BufferedReader bufferedReader;
    private List<EnterpriseJob> availableJobs = new ArrayList<EnterpriseJob>();
    private static final int NUMBER_OF_PARAMETERS = 2;
    
    public EnterpriseJobProducer(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
    }

    @Override
    public void loadJobs() {
        try {
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] numbers = line.split("\t");
                if (numbers.length != NUMBER_OF_PARAMETERS) {
                    LOGGER.severe("Malformed line, expecting "+ NUMBER_OF_PARAMETERS + " elements. Found " + numbers.length);
                    continue;
                }
                EnterpriseJob j = new EnterpriseJob();
                j.setArrivalTimeOfJob(Integer.parseInt(numbers[0]));
                j.setNumberOfJob(Integer.parseInt(numbers[1]));
                availableJobs.add(j);
            }
            bufferedReader.close();
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
        }
    }

    @Override
    public boolean hasNext() {
        return !availableJobs.isEmpty();
    }

    @Override
    public Job next() {
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        
        Job result = availableJobs.get(0);
        availableJobs.remove(0);
        return result;
    }

}
