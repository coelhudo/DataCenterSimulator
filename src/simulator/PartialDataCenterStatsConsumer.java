package simulator;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.inject.Inject;

import simulator.physical.BladeServer.BladeServerStats;
import simulator.physical.Chassis.ChassisStats;
import simulator.physical.DataCenter.DataCenterStats;
import simulator.physical.Rack.RackStats;

public class PartialDataCenterStatsConsumer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(PartialDataCenterStatsConsumer.class.getName());
    private BlockingQueue<DataCenterStats> partialDataCenterStats;
    private int counter = 0;
    private PrintWriter writer;

    @Inject
    public PartialDataCenterStatsConsumer(BlockingQueue<DataCenterStats> partialResults) {
        this.partialDataCenterStats = partialResults;
    }

    @Override
    public void run() {
        try {
            writer = new PrintWriter("partial_results.log", "UTF-8");
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
        } catch (FileNotFoundException ex) {
            LOGGER.info(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            LOGGER.info(ex.getMessage());
        } catch (InterruptedException ex) {
            LOGGER.info(ex.getMessage());
        }
        writer.close();
        LOGGER.info("Consumer done");
    }

    private synchronized void consume(DataCenterStats result) {
        writer.println("===== START =====");
        for(RackStats rackStats : result.getRacksStats()) {
            for(ChassisStats chassisStats : rackStats.getChassisStats()) {
                for(BladeServerStats bladeServerStats : chassisStats.getBladeServersStats()) {
                        writer.println(String.format("%s %.1f %.1f", bladeServerStats.getStatus(),
                                              bladeServerStats.getCurrentCPU(),
                                              bladeServerStats.getMIPS()));
                }
            }
        }
        writer.println("===== END =====");
    }
}
