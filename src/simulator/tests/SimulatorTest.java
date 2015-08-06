package simulator.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import simulator.SimulationResults;
import simulator.Simulator;

public class SimulatorTest {

    @Test
    public void testIDidntBreakAnythingFromTheOriginalCode() {
        try {
            Simulator simulator = new Simulator();
            SimulationResults results = simulator.execute();
            final double expectedTotalPowerConsumption = 7.555028576875995E9;
            assertEquals(expectedTotalPowerConsumption, results.getTotalPowerConsumption(), 1.0E-5);
            final double expectedLocalTime = 686293.0;
            assertEquals(expectedLocalTime, results.getLocalTime(), 0.01);
            final double meanPowerConsumption = 11008.459326;
            assertEquals(meanPowerConsumption, results.getMeanPowerConsumption(), 1.0E5);
            final int expectedOverRedTemperatureNumber = 0;
            assertEquals(expectedOverRedTemperatureNumber, results.getOverRedTemperatureNumber());
            final int expectedNumberMessagesFromDataCenterToSystem = 11508;
            assertEquals(expectedNumberMessagesFromDataCenterToSystem,
                    results.getNumberOfMessagesFromDataCenterToSystem());
            final int expectedNumberMessagesFromSystemToNodes = 198253;
            assertEquals(expectedNumberMessagesFromSystemToNodes, results.getNumberOfMessagesFromSystemToNodes());
        } catch (IOException e) {
            Logger.getLogger(SimulatorTest.class.getName()).log(Level.SEVERE, "Something went wrong during the test", e);
        }
    }

}
