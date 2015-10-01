package simulator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PartialResultConsumer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(PartialResultConsumer.class.getName());
    private BlockingQueue<String> partialResults;
    private int counter = 0;
    public PartialResultConsumer(BlockingQueue<String> partialResults) {
        this.partialResults = partialResults;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String result = partialResults.poll(100, TimeUnit.MILLISECONDS);
                if(result != null) {
                    consume(result);
                    counter = 0;
                } else {
                    counter++;
                }
                
                if(counter > 50) {
                    break;
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.info(ex.getMessage());
        }

        LOGGER.info("Consumer done");
    }

    private synchronized void consume(String result) {
        
    }
}
