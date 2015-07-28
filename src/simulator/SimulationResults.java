package simulator;

public class SimulationResults {

    private double totalPowerConsumption;
    private Simulator.Environment environment;
    private double meanPowerConsumption;
    private int overRedTemperatureNumber;

    public SimulationResults(Simulator simulator) {
        totalPowerConsumption = simulator.getTotalPowerConsumption();
        environment = simulator.getEnvironment();
        meanPowerConsumption = totalPowerConsumption / environment.getCurrentLocalTime();
        overRedTemperatureNumber = simulator.getOverRedTempNumber();
    }

    public double getTotalPowerConsumption() {
        return totalPowerConsumption;
    }

    public double getLocalTime() {
        return environment.getCurrentLocalTime();
    }

    public int getOverRedTemperatureNumber() {
        return overRedTemperatureNumber;
    }

    public int getNumberOfMessagesFromDataCenterToSystem() {
        return environment.getNumberOfMessagesFromDataCenterToSystem();
    }

    public int getNumberOfMessagesFromSystemToNodes() {
        return environment.getNumberOfMessagesFromSystemToNodes();
    }

    public double getMeanPowerConsumption() {
        return meanPowerConsumption;
    }
}