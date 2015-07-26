package simulator;

public class SimulationResults {

	private double totalPowerConsumption;
	private double localTime;
	private double meanPowerConsumption;
	private int overRedTemperatureNumber;
	private int mesg;
	private int mesg2;

	public SimulationResults(Simulator simulator) {
		totalPowerConsumption = simulator.getTotalPowerConsumption();
		localTime = simulator.getLocalTime();
		meanPowerConsumption = totalPowerConsumption / localTime;
		overRedTemperatureNumber = simulator.getOverRedTempNumber();
		mesg = simulator.numberOfMessagesFromDataCenterToSystem;
		mesg2 = simulator.numberOfMessagesFromSytemToNodes;
	}

	public double getTotalPowerConsumption() {
		return totalPowerConsumption;
	}

	public double getLocalTime() {
		return localTime;
	}

	public int getOverRedTemperatureNumber() {
		return overRedTemperatureNumber;
	}

	public int getNumberOfMessagesFromDataCenterToSystem() {
		return mesg;
	}

	public int getNumberOfMessagesFromSystemToNodes() {
		return mesg2;
	}

	public double getMeanPowerConsumption() {
		return meanPowerConsumption;
	}
}