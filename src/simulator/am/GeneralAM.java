package simulator.am;

import simulator.Simulator;

public abstract class GeneralAM {

    private double[] compPwrApps = new double[256];
    protected double[] SlaApps = new double[256];
    private int[] recForCoop;
    private int SLAViolationGen;
    Simulator.StrategyEnum strategy;

    public abstract void monitor();

    public abstract void analysis(Object vilation);

    public abstract void planning();

    public abstract void execution();

    public int[] getRecForCoop() {
        return recForCoop;
    }

    public void setRecForCoop(int[] recForCoop) {
        this.recForCoop = recForCoop;
    }

    public double getCompPowerAppsAt(int index) {
        return compPwrApps[index];
    }
    
    public void setCompPowerAppsAt(int index, double value) {
        this.compPwrApps[index] = value;
    }

    protected int getSLAViolationGen() {
        return SLAViolationGen;
    }

    protected void setSLAViolationGen(int sLAViolationGen) {
        SLAViolationGen = sLAViolationGen;
    }
}
