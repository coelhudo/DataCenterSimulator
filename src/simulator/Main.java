package simulator;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        FileHandler logFile = new FileHandler("log.txt");
        LOGGER.addHandler(logFile);

        SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml");
        SimulatorPOD simulatorPOD = dataCenterBuilder.build();

        Environment environment = new Environment();
        Simulator simulator = new Simulator(simulatorPOD, environment);
        Thread simulatorThread = new Thread(simulator);
        simulatorThread.start();
        simulatorThread.join();
        SimulationResults results = new SimulationResults(simulator);
        LOGGER.info("Total energy Consumption= " + results.getTotalPowerConsumption());
        LOGGER.info("LocalTime= " + results.getLocalTime());
        LOGGER.info("Mean Power Consumption= " + results.getMeanPowerConsumption());
        LOGGER.info("Over RED\t " + results.getOverRedTemperatureNumber() + "\t# of Messages DC to sys= "
                + results.getNumberOfMessagesFromDataCenterToSystem() + "\t# of Messages sys to nodes= "
                + results.getNumberOfMessagesFromSystemToNodes());
    }

}
