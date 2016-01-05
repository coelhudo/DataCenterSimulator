package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;

import java.util.Observable;
import java.util.Observer;
import java.util.logging.LogManager;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import simulator.Environment;
import simulator.SimulationResults;
import simulator.Simulator;
import simulator.Simulator.StrategyEnum;
import simulator.am.DataCenterAM;
import simulator.physical.DataCenter;
import simulator.system.Systems;

public class SimulatorIT {

    @Test
    public void testIDidntBreakAnythingFromTheOriginalCode() {
    	LogManager.getLogManager().reset();
    	
        Injector injector = Guice.createInjector(new ITModule());

        Systems systems = injector.getInstance(Systems.class);

        final DataCenterAM dataCenterAM = new DataCenterAM(injector.getInstance(Environment.class), systems);
        dataCenterAM.setStrategy(StrategyEnum.Green);

        class DataCenterAMXunxo implements Observer {
            public void update(Observable o, Object arg) {
                dataCenterAM.resetBlockTimer();
            }
        }

        injector.getInstance(DataCenter.class).setAM(dataCenterAM);

        systems.addObserver(new DataCenterAMXunxo());
        systems.setup();

        injector.injectMembers(systems);

        Simulator simulator = injector.getInstance(Simulator.class);

        simulator.run();
        SimulationResults results = new SimulationResults(simulator);
        final double expectedTotalPowerConsumption = 7.555028576875995E9;
        assertEquals(expectedTotalPowerConsumption, results.getTotalPowerConsumption(), 1.0E-5);
        final double expectedLocalTime = 686293.0;
        assertEquals(expectedLocalTime, results.getLocalTime(), 0.01);
        final double meanPowerConsumption = 11008.459326;
        assertEquals(meanPowerConsumption, results.getMeanPowerConsumption(), 1.0E5);
        final int expectedOverRedTemperatureNumber = 0;
        assertEquals(expectedOverRedTemperatureNumber, results.getOverRedTemperatureNumber());
        final int expectedNumberMessagesFromDataCenterToSystem = 11508;
        assertEquals(expectedNumberMessagesFromDataCenterToSystem, results.getNumberOfMessagesFromDataCenterToSystem());
        final int expectedNumberMessagesFromSystemToNodes = 198253;
        assertEquals(expectedNumberMessagesFromSystemToNodes, results.getNumberOfMessagesFromSystemToNodes());
    }

}
