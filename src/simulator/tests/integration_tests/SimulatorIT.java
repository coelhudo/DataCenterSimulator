package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import simulator.SimulationResults;
import simulator.Simulator;

public class SimulatorIT {

    @Test
    public void testIDidntBreakAnythingFromTheOriginalCode() {

        Injector injector = Guice.createInjector(new ITModule());
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
