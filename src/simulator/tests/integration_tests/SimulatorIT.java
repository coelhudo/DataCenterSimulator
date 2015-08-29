package simulator.tests.integration_tests;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import simulator.Environment;
import simulator.SimulationResults;
import simulator.Simulator;
import simulator.SimulatorBuilder;
import simulator.SimulatorPOD;

public class SimulatorIT {

    @Test
    public void testIDidntBreakAnythingFromTheOriginalCode() {
        try {
            SimulatorBuilder dataCenterBuilder = new SimulatorBuilder("configs/DC_Logic.xml");
            SimulatorPOD simulatorPOD = dataCenterBuilder.buildLogicalDataCenter();

            Environment environment = new Environment();
            Simulator simulator = new Simulator(simulatorPOD, environment);

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
            Logger.getLogger(SimulatorIT.class.getName()).log(Level.SEVERE, "Something went wrong during the test", e);
        }
    }

}
