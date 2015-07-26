package simulator.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import simulator.SimulationResults;
import simulator.Simulator;

public class SimulatorTest {

	@Test
	public void testIDidntBreakAnythingFromTheOriginalCode() {
		try {
			SimulationResults results = Simulator.execute();
			final double expectedTotalPowerConsumption = 7.555E9;
			assertEquals(expectedTotalPowerConsumption, results.getTotalPowerConsumption(), 1.0E8);
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
