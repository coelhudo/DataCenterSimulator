package simulator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.inject.Inject;

import simulator.physical.DataCenter.DataCenterStats;

public class PartialDataCenterStatsConsumer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(PartialDataCenterStatsConsumer.class.getName());
    private BlockingQueue<DataCenterStats> partialDataCenterStats;
    private int counter = 0;

    @Inject
    public PartialDataCenterStatsConsumer(BlockingQueue<DataCenterStats> partialResults) {
        this.partialDataCenterStats = partialResults;
    }

    @Override
    public void run() {
        try {
            while (true) {
                DataCenterStats result = partialDataCenterStats.poll(100, TimeUnit.MILLISECONDS);
                if (result != null) {
                    consume(result);
                    counter = 0;
                } else {
                    counter++;
                }

                if (counter > 50) {
                    break;
                }
            }
        } catch (InterruptedException ex) {
            LOGGER.info(ex.getMessage());
        }

        LOGGER.info("Consumer done");
    }

    private synchronized void consume(DataCenterStats result) {
        // XXX: the view of partial result is being done through webview. So,
        // this acting as just a dummy data consumer
    }
}
