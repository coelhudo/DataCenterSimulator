package simulator;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        FileHandler logFile = new FileHandler("log.txt");
        LOGGER.addHandler(logFile);

        Injector injector = Guice.createInjector(new MainModule());
        Simulator simulator = injector.getInstance(Simulator.class);

        PartialDataCenterStatsConsumer partialDataCenterStatsConsumer = injector.getInstance(PartialDataCenterStatsConsumer.class);
        
        Thread simulatorThread = new Thread(simulator);
        Thread partialResultConsumerThread = new Thread(partialDataCenterStatsConsumer);
        simulatorThread.start();
        partialResultConsumerThread.start();
        simulatorThread.join();
        partialResultConsumerThread.join();

        SimulationResults results = new SimulationResults(simulator);
        LOGGER.info("Total energy Consumption= " + results.getTotalPowerConsumption());
        LOGGER.info("LocalTime= " + results.getLocalTime());
        LOGGER.info("Mean Power Consumption= " + results.getMeanPowerConsumption());
        LOGGER.info("Over RED\t " + results.getOverRedTemperatureNumber() + "\t# of Messages DC to sys= "
                + results.getNumberOfMessagesFromDataCenterToSystem() + "\t# of Messages sys to nodes= "
                + results.getNumberOfMessagesFromSystemToNodes());
    }

}
