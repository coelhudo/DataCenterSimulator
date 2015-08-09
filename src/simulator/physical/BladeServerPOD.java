package simulator.physical;

public class BladeServerPOD {

    private String bladeType;
    private double[] frequencyLevel;
    private double[] powerBusy;
    private double[] powerIdle;
    private double idleConsumption;
    private int ID;
    
    public void setServerID(int ID) {
        this.ID = ID;
    }
    
    public int getServerID() {
        return this.ID;
    }
    
    public String getBladeType() {
        return bladeType;
    }

    public void setBladeType(String bladeType) {
        this.bladeType = bladeType;
    }

    public double[] getFrequencyLevel() {
        return frequencyLevel;
    }

    public void setFrequencyLevelAt(int index, double frequenceLevel) {
        assert(this.frequencyLevel.length != 0);
        this.frequencyLevel[index] = frequenceLevel;
    }

    public void setFrequencyLevel(double[] frequencyLevel) {
        this.frequencyLevel = frequencyLevel;
    }

    public double[] getPowerBusy() {
        return powerBusy;
    }

    public void setPowerBusyAt(int index, double powerBusy) {
        assert(this.powerBusy.length != 0);
        this.powerBusy[index] = powerBusy;
    }

    public void setPowerBusy(double[] powerBusy) {
        this.powerBusy = powerBusy;
    }

    public double[] getPowerIdle() {
        return powerIdle;
    }

    public void setPowerIdleAt(int index, double powerIdle) {
        assert(this.powerIdle.length != 0);
        this.powerIdle[index] = powerIdle;
    }

    public void setPowerIdle(double[] powerIdle) {
        this.powerIdle = powerIdle;
    }

    public double getIdleConsumption() {
        return idleConsumption;
    }

    public void setIdleConsumption(double idleConsumption) {
        this.idleConsumption = idleConsumption;
    }
}
