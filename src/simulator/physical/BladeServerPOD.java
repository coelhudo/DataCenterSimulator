package simulator.physical;

public class BladeServerPOD {

    private String bladeType;
    private double[] frequencyLevel;
    private double[] powerBusy;
    private double[] powerIdle;
    private double standByConsumption;
    private DataCenterEntityID id;

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
        standByConsumption = bladeServerPOD.standByConsumption;
        id = bladeServerPOD.id;
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

    public double getStandByConsumption() {
        return standByConsumption;
    }

    public void setStandByConsumption(double standByConsumption) {
        this.standByConsumption = standByConsumption;
    }
    
    public void setID(DataCenterEntityID id) {
        this.id = id;
    }

    public DataCenterEntityID getID() {
        return id;
    }

}
