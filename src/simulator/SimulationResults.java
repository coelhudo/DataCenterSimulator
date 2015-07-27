package simulator;

public class SimulationResults {

	private double totalPowerConsumption;
	private Simulator.LocalTime localTime;
	private double meanPowerConsumption;
	private int overRedTemperatureNumber;
	private int numberOfMessagesFromDataCenterToSystem;
	private int numberOfMessagesFromSytemToNodes;

	public SimulationResults(Simulator simulator) {
		totalPowerConsumption = simulator.getTotalPowerConsumption();
		localTime = simulator.getLocalTime();
		meanPowerConsumption = totalPowerConsumption / localTime.getCurrentLocalTime();
		overRedTemperatureNumber = simulator.getOverRedTempNumber();
		numberOfMessagesFromDataCenterToSystem = simulator.numberOfMessagesFromDataCenterToSystem;
		numberOfMessagesFromSytemToNodes = simulator.numberOfMessagesFromSytemToNodes;
	}

	public double getTotalPowerConsumption() {
		return totalPowerConsumption;
	}

	public double getLocalTime() {
		return localTime.getCurrentLocalTime();
	}

	public int getOverRedTemperatureNumber() {
		return overRedTemperatureNumber;
	}

	public int getNumberOfMessagesFromDataCenterToSystem() {
		return numberOfMessagesFromDataCenterToSystem;
	}

	public int getNumberOfMessagesFromSystemToNodes() {
		return numberOfMessagesFromSytemToNodes;
	}

	public double getMeanPowerConsumption() {
		return meanPowerConsumption;
	}
}