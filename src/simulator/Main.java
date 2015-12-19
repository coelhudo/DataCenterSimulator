package simulator;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import simulator.Simulator.StrategyEnum;
import simulator.am.DataCenterAM;
import simulator.physical.DataCenter;
import simulator.system.Systems;

public class Main {

	private static final Logger LOGGER = Logger.getLogger(Simulator.class.getName());

	public static void main(String[] args) throws IOException, InterruptedException {
		FileHandler logFile = new FileHandler("log.txt");
		LOGGER.addHandler(logFile);

		Options options = new Options();

		options.addOption("h", "display help");
		options.addOption("a", false, "enable autonomic managers");	

		SimulatorOptions simulatorOptions = new SimulatorOptions();
		
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(options, args);
			if(cmd.hasOption('h')) {
				System.out.println("-h shows this helper");
				System.out.println("-a to enable autonomic managers [false] default");
				return;
			}
			
			if(cmd.hasOption('a')) {
				simulatorOptions.enableAutonomicManager();
			}
			
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			return;
		}
		
		
		Injector injector = Guice.createInjector(new MainModule(simulatorOptions));

		Systems systems = injector.getInstance(Systems.class);

		final DataCenterAM dataCenterAM = new DataCenterAM(injector.getInstance(Environment.class), systems);
		dataCenterAM.setStrategy(StrategyEnum.Green);

		class DataCenterAMXunxo implements Observer {
			public void update(Observable o, Object arg) {
				LOGGER.info("Update Called: executing xunxo that I made (and I'm not proud about it)");
				dataCenterAM.resetBlockTimer();
			}
		}

		injector.getInstance(DataCenter.class).setAM(dataCenterAM);

		systems.addObserver(new DataCenterAMXunxo());
		systems.setup();

		injector.injectMembers(systems);

		Simulator simulator = injector.getInstance(Simulator.class);

		PartialDataCenterStatsConsumer partialDataCenterStatsConsumer = injector
				.getInstance(PartialDataCenterStatsConsumer.class);

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
