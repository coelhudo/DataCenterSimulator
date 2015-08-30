package simulator.physical;

public class BladeServerPOD {

    private String bladeType;
    private double[] frequencyLevel;
    private double[] powerBusy;
    private double[] powerIdle;
    private double idleConsumption;
    private int bladeServerID;
    private int rackID;
    private int chassisID;

    public BladeServerPOD() {

    }

    public BladeServerPOD(BladeServerPOD bladeServerPOD) {
        bladeType = bladeServerPOD.bladeType;
        frequencyLevel = new double[bladeServerPOD.frequencyLevel.length];
        System.arraycopy(bladeServerPOD.frequencyLevel, 0, frequencyLevel, 0, bladeServerPOD.frequencyLevel.length);
        powerBusy = new double[bladeServerPOD.powerBusy.length];
        System.arraycopy(bladeServerPOD.powerBusy, 0, powerBusy, 0, bladeServerPOD.powerBusy.length);
        powerIdle = new double[bladeServerPOD.powerIdle.length];
        System.arraycopy(bladeServerPOD.powerIdle, 0, powerIdle, 0, bladeServerPOD.powerIdle.length);
        idleConsumption = bladeServerPOD.idleConsumption;
        rackID = bladeServerPOD.rackID;
        chassisID = bladeServerPOD.chassisID;
        bladeServerID = bladeServerPOD.bladeServerID;
    }

    public void setServerID(int bladeServerID) {
        this.bladeServerID = bladeServerID;
    }

    public int getServerID() {
        return this.bladeServerID;
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

    public void setRackID(int rackID) {
        this.rackID = rackID;
    }

    public int getRackID() {
        return rackID;
    }

    public void setChassisID(int chassisID) {
        this.chassisID = chassisID;
    }

    public int getChassisID() {
        return chassisID;
    }

}
